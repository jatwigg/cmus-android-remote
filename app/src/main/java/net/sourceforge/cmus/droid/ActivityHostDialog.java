package net.sourceforge.cmus.droid;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

import java.util.ArrayList;

/**
 * Created by josh on 31/01/14.
 */
public class ActivityHostDialog extends Activity {

    public static final String PREF_FILE = "com.joshtwigg.cmus.hosts";
    private static final String INTENT_EXTRA_HOST = "HOST_ADDRESS";
    public static final String CURRENT_HOST = "CURRENT_HOST";
    private static final String AVAILABLE_HOSTS = "AVAILABLE_HOSTS";

    private String _hostAddress = "";
    private SharedPreferences _sharedPrefs;
    private EditText _host;
    private EditText _port;
    private EditText _password;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_host_dialog);
        _host = (EditText) findViewById(R.id.host);
        _port = (EditText) findViewById(R.id.port);
        _password = (EditText) findViewById(R.id.password);
        // get shared prefs for loading and storing saved adapters
        _sharedPrefs = getSharedPreferences(PREF_FILE, MODE_PRIVATE);
        // check if we have a host
        _hostAddress = getIntent().getStringExtra(INTENT_EXTRA_HOST);
        if (_hostAddress == null || _hostAddress.equals("")) {
            // type new address
            Log.d(getClass().getSimpleName(), "host is empty.");
        }
        else {
            // load saved or default values
            _host.setText(_hostAddress);
            int port = _sharedPrefs.getInt(prefPort(), getResources().getInteger(R.integer.default_port));
            String password = _sharedPrefs.getString(prefPassword(), getResources().getString(R.string.default_password));
            _port.setText(String.valueOf(port));
            _password.setText(password);
            Log.d(getClass().getSimpleName(), String.format("host{%s}port{%s}passwordlen{%s}", _hostAddress, port, password.length()));
        }
    }

    private String prefPassword() {
        return _hostAddress + ":PASSWORD";
    }

    private String prefPort() {
        return _hostAddress + ":PORT";
    }

    public void onClickOkay(View view) {
        //save values
        SharedPreferences.Editor editor = _sharedPrefs.edit();
        // get available hosts (except this host)
        String[] hosts = getSavedHosts(_sharedPrefs);
        ArrayList<String> newhosts = new ArrayList<String>();
        for (String s : hosts) {
            if (s.equals(_hostAddress)) continue;
            newhosts.add(s);
        }
        // delete old values in case host changed
        editor.remove(prefPort());
        editor.remove(prefPassword());
        // add new values
        _hostAddress = _host.getText().toString();
        editor.putString(CURRENT_HOST, _hostAddress); // does this matter?
        editor.putInt(prefPort(), Integer.parseInt(_port.getText().toString()));
        editor.putString(prefPassword(), _password.getText().toString());

        String newHostString = "";
        boolean found = false;
        for (String s : newhosts) {
            if (s.equals(_hostAddress)) found = true;
            newHostString += "=" + s;
        }
        if (!found) newHostString += "=" + _hostAddress;
        editor.putString(AVAILABLE_HOSTS, newHostString.substring(1));
        Log.d(getClass().getSimpleName(), "new host list {" + newHostString.substring(1) + "}");

        // save and exit
        editor.commit();
        setResult(RESULT_OK);
        finish();
    }

    public static String[] getSavedHosts(SharedPreferences prefs) {
        String[] hosts = prefs.getString(AVAILABLE_HOSTS, "").split("=");
        if (hosts.length == 1 && hosts[0].equals("")) return new String[]{};
        return hosts;
    }

    public void onClickCancel(View view) {
        setResult(RESULT_CANCELED);
        finish();
    }

    public static Intent getStartIntent(Context context, String hostAddress) {
        Intent intent = new Intent(context, ActivityHostDialog.class);
        intent.putExtra(INTENT_EXTRA_HOST, hostAddress);
        return intent;
    }
}
