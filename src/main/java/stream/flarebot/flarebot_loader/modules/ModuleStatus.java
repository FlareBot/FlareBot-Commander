package stream.flarebot.flarebot_loader.modules;

public enum ModuleStatus {

    STARTING,
    STARTED,
    RESTARTING,
    UPDATING,
    UPDATED,
    STOPPING,
    STOPPED,

    RUNNING // Always will be set back to this unless it failed to start.
}
