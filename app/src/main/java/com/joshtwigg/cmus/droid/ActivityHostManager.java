package com.joshtwigg.cmus.droid;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ListView;
import android.widget.TextView;

import java.io.File;
import java.util.ArrayList;

/**
 * Created by josh on 31/01/14.
 */
public class ActivityHostManager extends Activity implements IReceiveHost {
    private static final int REQUEST_CODE = 100;
    private ListView _hostView;
    private ArrayAdapter<String> _hostAdapter;
    private Settings _settings;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_host_manager);
        _settings = Storage.getSettings(ActivityHostManager.this);
        setupClearCacheAlert();
        CheckBox checkBox = (CheckBox)findViewById(R.id.fetch_art);
        checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                _settings.FETCH_ARTWORK = isChecked;
            }
        });
        checkBox.setChecked(_settings.FETCH_ARTWORK);
        _hostAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1);
        _hostView = (ListView) findViewById(R.id.hostList);
        _hostView.setAdapter(_hostAdapter);
        _hostAdapter.add(getResources().getString(R.string.add_new));
        _hostView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapter, View item, int position, long id) {
                String address = adapter.getItemAtPosition(position).toString();
                if (address == getResources().getString(R.string.add_new)) address = "";
                ActivityHostManager.this.startActivityForResult(ActivityHostDialog.getStartIntent(ActivityHostManager.this, address), REQUEST_CODE);
            }
        });
        ArrayList<String> hosts = Storage.getSavedHosts(this);
        Log.d(getClass().getSimpleName(), "no of hosts received{" + hosts.size() + "}.");
        for (String s : hosts) _hostAdapter.add(s);
        _hostAdapter.notifyDataSetChanged();
        Util.runSearchHosts(this, this);
        setupVersionInfo();
    }

    private void setupClearCacheAlert() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Are you sure?");
        builder.setMessage("If you clear your cache it will delete all artwork stored on this device.");
        builder.setNegativeButton("Cancel", null);
        builder.setPositiveButton("Proceed", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                trimCache();
            }
        });
        Button button = (Button)findViewById(R.id.clear_cache);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        builder.show();
                    }
                });
            }
        });
    }

    private void setupVersionInfo() {
        try {
        PackageInfo pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
        TextView version = (TextView)findViewById(R.id.version);
        version.setText(pInfo.versionName);
        } catch (Exception e) {
            Log.e(getClass().getSimpleName(), "Error setting version number", e);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d(getClass().getSimpleName(), String.format("Activity returned result {%s} {%s}", requestCode, resultCode));
        if (requestCode == REQUEST_CODE && resultCode == RESULT_OK) finish();
        else super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected void onPause() {
        if (_settings.hasChanged()) {
            _settings.saveChanges(this);
            Intent intent = new Intent(getResources().getString(R.string.intent_settings_changed));
            intent.putExtra(getResources().getString(R.string.intent_settings_extra), _settings);
            sendBroadcast(intent);
        }
        super.onStop();
    }

    @Override
    public void receiveHost(final String hostAddress) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                for (int i = 0; i < _hostAdapter.getCount(); ++i) {
                    if (_hostAdapter.getItem(i).equals(hostAddress)) return;
                }
                _hostAdapter.add(hostAddress);
                _hostAdapter.notifyDataSetChanged();
            }
        });
    }

    public void trimCache() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    File dir = getCacheDir();
                    if (dir != null && dir.isDirectory()) {
                        for (File file : dir.listFiles()) {
                            deleteDir(file);
                        }
                    }
                } catch (Exception e) {
                    Log.e(getClass().getSimpleName(), "Error clearing cache.", e);
                }
            }
        }).start();
    }

    public static boolean deleteDir(File dir) {
        if (dir != null && dir.isDirectory()) {
            String[] children = dir.list();
            for (int i = 0; i < children.length; i++) {
                boolean success = deleteDir(new File(dir, children[i]));
                if (!success) {
                    return false;
                }
            }
        }
        // The directory is now empty so delete it
        return dir.delete();
    }

    public static void Show(Context context) {
        Intent intent = new Intent(context, ActivityHostManager.class);
        context.startActivity(intent);
    }
}
