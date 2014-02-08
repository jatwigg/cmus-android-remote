package com.joshtwigg.cmus.droid;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import java.util.ArrayList;

/**
 * Created by josh on 31/01/14.
 */
public class Storage {
    public static final String PREF_FILE = "com.joshtwigg.cmus.hosts";
    public static final String CURRENT_HOST = "CURRENT_HOST";
    private static final String AVAILABLE_HOSTS = "AVAILABLE_HOSTS";
    private static final String READ_WELCOME = "READ_WELCOME";

    public static Settings getSettings(Context context) {
        return new Settings(context, getPrefs(context));
    }

    public static SharedPreferences getPrefs(Context context) {
        return context.getSharedPreferences(PREF_FILE, Context.MODE_PRIVATE);
    }

    private static String prefPassword(String hostAddress) {
        return hostAddress + ":PASSWORD";
    }

    private static String prefPort(String hostAddress) {
        return hostAddress + ":PORT";
    }

    public static void save(final Context context, final String oldHost, final String newHost, final int port, final String password) {

        SharedPreferences sharedPrefs = getPrefs(context);
        //save values
        SharedPreferences.Editor editor = sharedPrefs.edit();
        // get available hosts (except this host)
        ArrayList<String> hosts = getSavedHosts(context);
        hosts.remove(oldHost);
        // delete old values in case host changed
        editor.remove(prefPort(oldHost));
        editor.remove(prefPassword(oldHost));
        // add new values
        editor.putString(CURRENT_HOST, newHost);
        editor.putInt(prefPort(newHost), port);
        editor.putString(prefPassword(newHost), password);
        // build new string of available hosts
        if (!hosts.contains(newHost)) hosts.add(newHost);
        String newHostString = "";
        for (String s : hosts) newHostString += "=" + s;
        editor.putString(AVAILABLE_HOSTS, newHostString.substring(1));
        Log.d(Storage.class.getSimpleName(), "new host list {" + newHostString.substring(1) + "}");
        // save and exit
        editor.commit();
    }

    public static ArrayList<String> getSavedHosts(Context context) {
        String[] hostStrings = getPrefs(context).getString(AVAILABLE_HOSTS, "").split("=");
        ArrayList<String> hosts = new ArrayList<String>();
        if (hostStrings.length == 1 && hostStrings[0].equals("")) return hosts;
        for (int i = 0; i < hostStrings.length; ++i) {
            hosts.add(hostStrings[i]);
        }
        return hosts;
    }

    public static int getPort(Context context, String hostAddress) {
        return getPrefs(context).getInt(prefPort(hostAddress), context.getResources().getInteger(R.integer.default_port));
    }

    public static String getPassword(Context context, String hostAddress) {
        return getPrefs(context).getString(prefPassword(hostAddress), context.getResources().getString(R.string.default_password));
    }

    public static Host getHost(Context context) {
        String host = getPrefs(context).getString(CURRENT_HOST, null);
        if (host == null) return null;
        return new Host(host, getPort(context, host), getPassword(context, host));
    }

    public static void markWelcomeRead(Context context) {
        SharedPreferences prefs = getPrefs(context);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean(READ_WELCOME, true);
        editor.commit();
    }

    public static boolean hasReadWelcome(Context context) {
        return getPrefs(context).getBoolean(READ_WELCOME, false);
    }
}
