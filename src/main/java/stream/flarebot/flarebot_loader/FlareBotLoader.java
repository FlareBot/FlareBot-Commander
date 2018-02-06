package stream.flarebot.flarebot_loader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spark.Service;
import stream.flarebot.flarebot_loader.modules.ModuleLoader;
import stream.flarebot.flarebot_loader.rest.ModuleRest;

import java.nio.file.Paths;

public class FlareBotLoader {

    private static FlareBotLoader instance;
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private ModuleLoader loader;

    public static void main(String[] args) {
        (instance = new FlareBotLoader()).init();
    }

    private void init() {
        logger.info("########### FlareBot Commander ###########"
                + "\n#                                        #"
                + "\n# Put modules in the /modules folder and #"
                + "\n#        watch the magic happen!!        #"
                + "\n#                                        #"
                + "\n##########################################");

        Thread.setDefaultUncaughtExceptionHandler(((t, e) -> logger.error("Uncaught exception in thread " + t.getName(), e)));
        Thread.currentThread()
                .setUncaughtExceptionHandler(((t, e) -> logger.error("Uncaught exception in thread " + t.getName(), e)));

        logger.info("Initializing module loader!");
        loader = new ModuleLoader();
        logger.info("Initialised module loader!");

        // Init heartbeat thread
        logger.info("Initializing heartbeat");
        new Heartbeat().initHeartbeat();
        logger.info("Initialised heartbeat");

        // Init REST server
        logger.info("Starting REST server");
        Service service = Service.ignite().port(4567);
        new ModuleRest(service);

        logger.info("Running FlareBot Commander");
        run();
    }

    private void run() {
        logger.info("Loading all modules");
        loader.loadAllModules(Paths.get("modules"));
        logger.info("Loaded " + loader.getModules().size() + " modules!");
        if (loader.getModules().isEmpty()) {
            logger.error("Cannot do anything with no modules, shutting down.");
            System.exit(1);
        }
        if (loader.getCoreModule() == null) {
            logger.error("Cannot do anything with no core module, shutting down.");
            System.exit(1);
        }

        logger.info("Starting all modules");
        loader.startAllModules();
        logger.info("Started all modules");
    }

    public static FlareBotLoader getInstance() {
        return instance;
    }

    public ModuleLoader getModuleLoader() {
        return loader;
    }
}
