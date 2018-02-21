package stream.flarebot.flarebot_loader.modules;

public enum ModuleStatus {

    INITIALISING,
    INITIALISED,
    RUNNING, // Always will be set back to this unless it failed to start.

    RESTARTING,

    UPDATING,
    UPDATED,

    STOPPING,
    STOPPED
}
