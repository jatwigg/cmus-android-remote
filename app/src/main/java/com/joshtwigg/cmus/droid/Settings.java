package com.joshtwigg.cmus.droid;

import android.content.Context;
import android.content.SharedPreferences;

import java.io.Serializable;

/**
 * Created by josh on 08/02/14.
 */
public class Settings implements Serializable{
    private static final String KEY_POLL_MILLS = "KEY_POLL_MILLS";
    private static final String KEY_FETCHARTWORK = "KEY_FETCHARTWORK";

    public int POLL_DURATION_MILLS;
    public boolean FETCH_ARTWORK;

    public Settings(final Context context, final SharedPreferences prefs) {
        POLL_DURATION_MILLS = prefs.getInt(KEY_POLL_MILLS, context.getResources().getInteger(R.integer.default_poll_mills));
        FETCH_ARTWORK = prefs.getBoolean(KEY_FETCHARTWORK, context.getResources().getBoolean(R.bool.default_fetch_artwork));
    }
}
