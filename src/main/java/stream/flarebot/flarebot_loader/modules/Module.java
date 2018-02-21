package stream.flarebot.flarebot_loader.modules;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.spi.FilterReply;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;

public abstract class Module {

    public Logger logger;

    private ModuleClassLoader classLoader;

    private File moduleFile;
    private ModuleStatus status;
    private ModuleDesc description;

    public void setStatus(ModuleStatus status) {
        logger.debug("Changing status: " + this.status + " -> " + status);
        this.status = status;
    }

    public ModuleStatus getStatus() {
        return status;
    }

    public ModuleDesc getDescription() {
        return description;
    }

    public File getModuleFile() {
        return this.moduleFile;
    }

    public void load(ModuleDesc desc, File moduleFile, ModuleClassLoader loader) {
        this.description = desc;
        this.moduleFile = moduleFile;
        this.classLoader = loader;

        this.logger = LoggerFactory.getLogger("Module [" + desc.id() + "]");
    }

    /**
     * Init is called when the module is first loaded, this is a good time to set things up which you don't want to do
     * when everything is running.
     */
    public void init() {}

    /**
     * This is called when the module is loaded, this will usually be when everything is running and
     *
     * <b>The core module will always be ran first!</b>
     */
    public abstract void run();

    public void cleanup() {}

    public void stop() {}

    protected void release() {
        try {
            classLoader.close();
        } catch (IOException e) {
            logger.error("Failed to release module", e);
        }
    }

    public FilterReply filter(ILoggingEvent event) {
        return FilterReply.NEUTRAL;
    }
}
