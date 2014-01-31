package net.sourceforge.cmus.droid;

/**
* Created by josh on 31/01/14.
*/
public enum CmusCommand {
    REPEAT("Repeat", "toggle repeat"),
    SHUFFLE("Shuffle", "toggle shuffle"),
    STOP("Stop", "player-stop"),
    NEXT("Next", "player-next"),
    PREV("Previous", "player-prev"),
    PLAY("Play", "player-play"),
    PAUSE("Pause", "player-pause"),
    // FILE("player-play %s");
    // VOLUME("vol %s"),
    VOLUME_MUTE("Mute", "vol -100%"),
    VOLUME_UP("Volume +", "vol +10%"),
    VOLUME_DOWN("Volume -", "vol -10%"),
    // SEEK("seek %s"),
    STATUS("Status", "status");

    private final String label;
    private final String command;

    CmusCommand(String label, String command) {
        this.label = label;
        this.command = command;
    }

    public String getCommand() {
        return command;
    }

    public String getLabel() {
        return label;
    }

    @Override
    public String toString() {
        return getLabel();
    }
}
