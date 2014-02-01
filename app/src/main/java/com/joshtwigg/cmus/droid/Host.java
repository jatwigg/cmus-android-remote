package com.joshtwigg.cmus.droid;

/**
 * Created by josh on 31/01/14.
 */
public class Host {
    public final String host;
    public final int port;
    public final String password;

    public Host(String phost, int pport, String ppassword) {
        host = phost;
        port = pport;
        password = ppassword;
    }
}
