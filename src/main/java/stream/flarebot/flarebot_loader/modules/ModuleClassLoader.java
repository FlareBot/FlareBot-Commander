package stream.flarebot.flarebot_loader.modules;

import stream.flarebot.flarebot_loader.FlareBotLoader;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class ModuleClassLoader extends URLClassLoader {

    /**
     * Mapping of class names to classes that have already been loaded by other class loaders
     */
    private static final Map<String, Class<?>> CLASS_MAP = new HashMap<>();

    /**
     * A collection of classes loaded by this class loader
     */
    private final Set<Class<?>> classes = new HashSet<>();

    /**
     * Creates a new module class loader that uses the given module jar file as the lookup path.
     *
     * @param module The module jar file.
     * @throws MalformedURLException If something dumb happens... let's hope it doesn't.
     */
    public ModuleClassLoader(File module) throws MalformedURLException {
        super(new URL[]{module.toURI().toURL()}, FlareBotLoader.class.getClassLoader());
    }

    @Override
    public void addURL(URL url) {
        super.addURL(url);
    }

    @Override
    protected Class<?> findClass(String name) throws ClassNotFoundException {
        Class<?> cls = CLASS_MAP.get(name);
        //System.out.println(name + ", Cached: " + (cls != null));
        //if (cls != null) {
            //return cls;
        //}

        try {
            try {
                cls = super.findClass(name);
            } catch (NoClassDefFoundError | ClassNotFoundException e) {
                cls = Class.forName(name);
            }
        } catch (NoClassDefFoundError | ClassNotFoundException e) {
            if (cls != null)
                return cls;

            throw new ClassNotFoundException("Could not find class '" + name + "'");
        }
        //System.out.println(cls.getClassLoader().getClass().getName());
        this.classes.add(cls);
        //System.out.println("Overriding: " + CLASS_MAP.containsKey(name));
        CLASS_MAP.put(name, cls);

        return cls;
    }

    @Override
    public void close() throws IOException {
        super.close();

        for (Class<?> c : this.classes) {
            if (CLASS_MAP.remove(c.getName()) == null) {
                throw new RuntimeException("Failed to cleanup after class, memory leak may occur");
            }
        }

        this.classes.clear();
    }
}