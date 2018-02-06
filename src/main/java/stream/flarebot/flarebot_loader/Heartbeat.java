package stream.flarebot.flarebot_loader;

import org.slf4j.LoggerFactory;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class Heartbeat {

    private final ScheduledExecutorService service = Executors.newScheduledThreadPool(1);

    public void initHeartbeat() {
        service.scheduleAtFixedRate(() -> LoggerFactory.getLogger(this.getClass()).debug("HEARTBEAT"), 30, 30, TimeUnit.SECONDS);
    }
}
