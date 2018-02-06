package stream.flarebot.flarebot_loader.error_handling;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.filter.Filter;
import ch.qos.logback.core.spi.FilterReply;
import stream.flarebot.flarebot_loader.modules.Module;
import stream.flarebot.flarebot_loader.modules.ModuleLoader;

public class ErrorCatcher extends Filter<ILoggingEvent> {

    @Override
    public FilterReply decide(ILoggingEvent event) {
        for (Module module : ModuleLoader.getInstance().getModules()) {
            FilterReply reply = module.filter(event);
            if(reply != FilterReply.NEUTRAL)
                return reply;
        }
        return FilterReply.NEUTRAL;
    }
}
