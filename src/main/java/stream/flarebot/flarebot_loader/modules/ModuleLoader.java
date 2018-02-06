package stream.flarebot.flarebot_loader.modules;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Path;
import java.util.*;
import java.util.function.Consumer;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.NotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ModuleLoader {


    private final Map<String, Module> loadedModules = new HashMap<>();

    private static final ClassPool CLASS_POOL = new ClassPool(true);

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private static ModuleLoader instance;

    public ModuleLoader() {
        instance = this;
    }

    public Module loadModule(File moduleFile) {
        long startTime = System.currentTimeMillis();
        Set<String> classNames = new HashSet<>();
        try (JarFile jarFile = new JarFile(moduleFile)) {
            CLASS_POOL.appendClassPath(moduleFile.getPath());

            ModuleDesc desc = null;
            String main = null;

            long a = System.currentTimeMillis();
            for (Enumeration<JarEntry> entries = jarFile.entries(); entries.hasMoreElements(); ) {
                JarEntry entry = entries.nextElement();

                if (entry.isDirectory() || !entry.getName().endsWith(".class")) {
                    continue;
                }

                String className = entry.getName().replace(".class", "").replace('/', '.');
                classNames.add(className);

                CtClass cls = CLASS_POOL.get(className);
                ModuleDesc moduleDesc = (ModuleDesc) cls.getAnnotation(ModuleDesc.class);
                if (moduleDesc != null && cls.getSuperclass().getName().equals(Module.class.getName())) {
                    if (desc != null) {
                        throw new RuntimeException("Module cannot have more than 2 main classes!");
                    }

                    main = className;
                    desc = moduleDesc;
                }
            }
            long b = System.currentTimeMillis();
            logger.debug("Took " + (b - a) + "ms to cache class names and find the main class");

            if (desc == null) {
                logger.error("Could not find main class in module '" + jarFile.getName() + "'. Make sure there's the " +
                        "ModuleDesc annotation and you extend Module!");
                return null;
            }

            if (loadedModules.containsKey(desc.id())) {
                logger.error("Module with ID '" + desc.id() + "' has already been loaded!");
                return null;
            }

            logger.info("Loading " + (desc.core() ? "core module " : "") + desc.name() + " v"
                    + desc.version() + "...");

            long c = System.currentTimeMillis();
            ModuleClassLoader loader = new ModuleClassLoader(moduleFile);
            Class<? extends Module> mainClass = null;
            for (String className : classNames) {
                Class<?> clazz;
                try {
                    clazz = loader.loadClass(className);
                } catch (NoClassDefFoundError | ClassNotFoundException e) {
                    logger.debug("Class not found: " + e.getMessage() + " - Ignoring (This is usually Gradle or other " +
                            "build tools)");
                    continue;
                }

                if (clazz.getName().equals(main)) {
                    mainClass = clazz.asSubclass(Module.class);
                }
            }
            if (mainClass == null) {
                throw new RuntimeException("Walshy fucked up! No main class in " + moduleFile.getName());
            }
            //loader.close();
            long d = System.currentTimeMillis();
            logger.debug("Took " + (d - c) + "ms to load all classes");

            long e = System.currentTimeMillis();
            logger.debug("Making new instance of " + mainClass.getName());
            Module module = mainClass.getConstructor().newInstance();
            module.init(desc, moduleFile, loader);

            loadedModules.put(desc.id(), module);
            long f = System.currentTimeMillis();
            logger.debug("Took " + (f - e) + "ms to init a new module");
            long endTime = System.currentTimeMillis();
            logger.info("Successfully loaded " + (desc.core() ? "core module " : "") + desc.id()
                    + " v" + desc.version() + " in " + (endTime - startTime) + "ms!");

            return module;
        } catch (IOException | NotFoundException | ClassNotFoundException | IllegalAccessException
                | NoSuchMethodException | InstantiationException | InvocationTargetException
                e) {
            logger.error("Failed to load module! Module File: '" + moduleFile.getName() + "'", e);
            return null;
        }
    }

    /**
     * This will load all the modules in our `modules` folder.
     * Note that the modules <b>must be a jar file and have a module.info file</b>
     */
    public void loadAllModules(Path path) {
        File modulesFile = path.toFile();
        List<File> tmp = new ArrayList<>();
        boolean foundCore = false;
        for (File moduleFile : Objects.requireNonNull(modulesFile.listFiles(), "Modules file cannot be null!")) {
            // Check if it is a jar file
            if (moduleFile.isFile() && moduleFile.getName().endsWith(".jar")) {
                if (moduleFile.getName().contains("core")) { // Assume we found the core module
                    foundCore = true;
                    loadModule(moduleFile);
                } else
                    tmp.add(moduleFile);
            }
        }
        if (!foundCore) {
            logger.error("Cannot find the core! Please make sure the file has the word 'core' in it!");
            return;
        }
        for (File f : tmp)
            loadModule(f);
    }

    public void startModule(Module module) {
        Objects.requireNonNull(module, "Cannot start a null module!");
        module.setStatus(ModuleStatus.STARTING);
        module.init();
        module.setStatus(ModuleStatus.STARTED);
        module.run();
        module.setStatus(ModuleStatus.RUNNING);
    }

    public void startAllModules() {
        Objects.requireNonNull(getCoreModule(), "Cannot start without a core module!");
        startModule(getCoreModule());
        for (Module module : loadedModules.values()) {
            if (module.getStatus() == ModuleStatus.STARTING || module.getStatus() == ModuleStatus.STARTED
                    || module.getStatus() == ModuleStatus.RUNNING) continue;
            startModule(module);
        }
    }

    public void unloadModule(Module module) {
        logger.info("Unloading " + module.getDescription().id());
        this.loadedModules.remove(module.getDescription().id());

        module.cleanup();
        module.release();
    }

    public void stopModule(Module module) {
        logger.info("Stopping " + module.getDescription().id());
        if (loadedModules.containsKey(module.getDescription().id()))
            unloadModule(module);
        module.setStatus(ModuleStatus.STOPPING);
        module.stop();
        module.setStatus(ModuleStatus.STOPPED);
    }

    public void stopAllModules() {
        for (Module module : loadedModules.values()) {
            stopModule(module);
        }
    }

    public void restartModule(Module module) {
        File f = module.getModuleFile();
        stopModule(module);
        Module newModule = loadModule(f);
        startModule(newModule);
    }

    public Collection<Module> getModules() {
        return Collections.unmodifiableCollection(loadedModules.values());
    }

    /**
     * Gets a loaded module, if you want to try and load a module (with return) use
     * {@link ModuleLoader#loadModule(File)}
     *
     * @param id Module id (eg "core", "commands")
     * @return The module if it can be found in the loaded ones with the passed id, null if it is not found
     */
    public Module getModule(String id) {
        return this.loadedModules.get(id);
    }

    private boolean isValidModule(String s) {
        return isValidModule(getModule(s));
    }

    private boolean isValidModule(Module module) {
        return module != null;
    }

    public boolean isModuleLoaded(String s) {
        return isValidModule(s);
    }

    public boolean isModuleLoaded(Module module) {
        return isValidModule(module);
    }

    public boolean isModuleRunning(String s) {
        return isModuleRunning(getModule(s));
    }

    public boolean isModuleRunning(Module module) {
        return isValidModule(module) && module.getStatus() == ModuleStatus.RUNNING;
    }

    public static ModuleLoader getInstance() {
        return instance;
    }

    public Module getCoreModule() {
        for (Module module : loadedModules.values())
            if (module.getDescription().core())
                return module;
        return null;
    }

    public static class ModuleAction {

        public static final Consumer<Module> STOP = (m) -> ModuleLoader.getInstance().stopModule(m);
        public static final Consumer<Module> START = (m) -> ModuleLoader.getInstance().startModule(m);
        public static final Consumer<Module> RESTART = (m) -> ModuleLoader.getInstance().restartModule(m);

    }

}
