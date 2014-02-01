package com.joshtwigg.cmus.droid;

import android.app.Service;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.util.Log;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Enumeration;

public final class Util {
    private Util() {
    }

    public static boolean validateString(String str) {
        return str != null && str.trim().length() != 0;
    }

    public static boolean validateInteger(String str) {
        if (str != null && str.trim().length() != 0) {
            try {
                Integer.parseInt(str);
                return true;
            } catch (Exception e) {
                return false;
            }
        }
        return false;
    }

    public static boolean isUsingWifi(Context context) {
        if ("sdk".equals(Build.PRODUCT)) {
            Log.v(Util.class.getSimpleName(), "Executing on emulator");
            return true;
        }
        ConnectivityManager connManager = (ConnectivityManager) context.getSystemService(Service.CONNECTIVITY_SERVICE);
        NetworkInfo mWifi = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);

        return mWifi.isConnected();
    }

    public static void runSearchHosts(final Context context, final IReceiveHost receiveHosts) {
        if (!isUsingWifi(context)) return;

        new Thread(new Runnable() {
            public void run() {
                try {
                    InetAddress localhost = null;

                    for (final Enumeration<NetworkInterface> interfaces = NetworkInterface .getNetworkInterfaces(); interfaces.hasMoreElements() && localhost == null;) {
                        final NetworkInterface cur = interfaces.nextElement();

                        if (cur.getName().equals("lo")) {
                            continue;
                        }
                        Log.v(Util.class.getSimpleName(), "interface " + cur.getName());

                        for (final Enumeration<InetAddress> inetAddresses = cur
                                .getInetAddresses(); inetAddresses
                                     .hasMoreElements() && localhost == null;) {
                            final InetAddress inet_addr = inetAddresses
                                    .nextElement();

                            if (!(inet_addr instanceof Inet4Address)) {
                                continue;
                            }

                            Log.v(Util.class.getSimpleName(), "Found local addr: " + inet_addr);
                            localhost = inet_addr;
                        }
                    }

                    // this code assumes IPv4 is used

                    if (localhost != null) {

                        byte[] ip = localhost.getAddress();

                        for (int i = 1; i <= 254; i++) {

                            ip[3] = (byte) i;
                            final InetAddress address = InetAddress.getByAddress(ip);

                            if (address.isReachable(200)) {
                                Log.v(Util.class.getSimpleName(), "Found an addr on LAN: "
                                        + address.getHostAddress());
                                receiveHosts.receiveHost(address.getHostAddress());
                                // machine is turned on and can be pinged
                            } else if (!address.getHostAddress().equals(
                                    address.getHostName())) {
                                // machine is known in a DNS lookup
                            } else {
                                // the host address and host name are equal,
                                // meaning
                                // the
                                // host
                                // name could not be resolved
                            }
                        }

                    }
                } catch (Exception e) {
                    Log.e(Util.class.getSimpleName(), "Error: " + e.getMessage(), e);
                }
            }
        }).start();
    }

}
