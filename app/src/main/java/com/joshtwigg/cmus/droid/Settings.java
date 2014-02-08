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

    private int original_POLL_DURATION_MILLS;
    private boolean original_FETCH_ARTWORK;

    public Settings(final Context context, final SharedPreferences prefs) {
        original_POLL_DURATION_MILLS = POLL_DURATION_MILLS = prefs.getInt(KEY_POLL_MILLS, context.getResources().getInteger(R.integer.default_poll_mills));
        original_FETCH_ARTWORK = FETCH_ARTWORK = prefs.getBoolean(KEY_FETCHARTWORK, context.getResources().getBoolean(R.bool.default_fetch_artwork));
    }

    public boolean saveChanges(final Context context) {
        SharedPreferences prefs = Storage.getPrefs(context);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putInt(KEY_POLL_MILLS, POLL_DURATION_MILLS);
        editor.putBoolean(KEY_FETCHARTWORK, FETCH_ARTWORK);
        return editor.commit();
    }

    public boolean hasChanged() {
        return original_FETCH_ARTWORK != FETCH_ARTWORK ||
                original_POLL_DURATION_MILLS != original_POLL_DURATION_MILLS;
    }
}
