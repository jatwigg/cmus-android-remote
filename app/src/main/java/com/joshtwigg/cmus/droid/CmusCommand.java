package com.joshtwigg.cmus.droid;

/**
* Created by josh on 31/01/14.
*/
public class CmusCommand {
    // these are commands that don't require a parameter
    public static final CmusCommand REPEAT = new CmusCommand("toggle repeat");
    public static final CmusCommand SHUFFLE = new CmusCommand("toggle shuffle");
    public static final CmusCommand STOP = new CmusCommand("player-stop");
    public static final CmusCommand NEXT = new CmusCommand("player-next");
    public static final CmusCommand PREV = new CmusCommand("player-prev");
    public static final CmusCommand PLAY = new CmusCommand("player-play");
    public static final CmusCommand PAUSE = new CmusCommand("player-pause");
    public static final CmusCommand VOLUME_MUTE = new CmusCommand("vol -100%");
    public static final CmusCommand VOLUME_UP = new CmusCommand("vol +10%");
    public static final CmusCommand VOLUME_DOWN = new CmusCommand("vol -10%");
    public static final CmusCommand STATUS = new CmusCommand("status");

    // these functions create a new instance of commands that require a parameter
    public static CmusCommand SEEK(int position) {
        return new CmusCommand(String.format("seek %s", position));
    }

    public static CmusCommand VOLUME(int amount) {
        return new CmusCommand(String.format("vol %s%%", amount));
    }

    public static CmusCommand FILE(String file) {
        return new CmusCommand(String.format("player-play %s", file));
    }

    // instance
    private final String _command;

    private CmusCommand(String command) {
        this._command = command;
    }

    public String getCommand() {
        return _command;
    }
}
