package com.joshtwigg.cmus.droid;

import android.os.Handler;

/**
 * Created by josh on 05/02/14.
 */
public class ShowPopupMessage {
    private boolean _repeat = false;
    private boolean _repeatAll = false;
    private boolean _shuffle = false;
    private final int _mills = 1000; //TODO: config this

    private final Runnable _RunRepeat= new Runnable(){
        @Override
        public void run() {
            _repeat = true;
        }
    };

    private final Runnable _RunRepeatAll= new Runnable(){
        @Override
        public void run() {
            _repeatAll = true;
        }
    };

    private final Runnable _RunShuffle= new Runnable(){
        @Override
        public void run() {
            _shuffle = true;
        }
    };

    // After a period of time (allowing the change to be sent and to process any current status)
    // set a value to true to indicate that a toast is to be displayed.

    public void getRepeat(final Handler handler) {
        handler.postDelayed(_RunRepeat, _mills);
    }

    public void getRepeatAll(final Handler handler) {
        handler.postDelayed(_RunRepeatAll, _mills);
    }

    public void getShuffle(final Handler handler) {
        handler.postDelayed(_RunShuffle, _mills);
    }

    // these functions return false if the value is false, else true. The value will be reset to
    // false when read.
    public boolean readShuffle() {
        if (_shuffle) return !(_shuffle = false);
        return false;
    }
    public boolean readRepeat() {
        if (_repeat) return !(_repeat = false);
        return false;
    }
    public boolean readRepeatAll() {
        if (_repeatAll) return !(_repeatAll = false);
        return false;
    }
}
