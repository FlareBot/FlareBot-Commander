package stream.flarebot.flarebot_loader.modules;

import org.slf4j.LoggerFactory;

import java.io.File;

public class ModuleHandler {

    private static final ModuleLoader loader = ModuleLoader.getInstance();
    private static final ModuleHandler instance;

    static {
        instance = new ModuleHandler();
    }

    /**
     * Gets a loaded module, if you want to try and load a module (with return) use {@link ModuleLoader#loadModule(File)}
     *
     * @param s Module id (eg "core", "commands")
     * @return The module if it can be found in the loaded ones with the passed id, null if it is not found
     */
    public Module getModule(String s) {
        for (Module module : loader.getModules()) {
            if (module.getDescription().id().equalsIgnoreCase(s))
                return module;
        }
        return null;
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

    public void stopModule(String s) {
        stopModule(getModule(s));
    }

    public void stopModule(Module module) {
        if (module == null) return;
        loader.stopModule(module);
    }

    public static ModuleHandler getInstance() {
        return instance;
    }
}
