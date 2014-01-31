package net.sourceforge.cmus.droid;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

/**
 * Created by josh on 31/01/14.
 */
public class ActivityRemote extends Activity implements ICallback {
    private Host _host = null;
    private boolean _bMuted = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_remote);
    }

    @Override
    protected void onResume() {
        super.onResume();
        _host = Storage.getHost(this);
        if (_host == null) {
            setTitle("CMUS Remote Not Connected");
            disconnect();
        } else {
            setTitle("CMUS Remote " + _host.host);
            connect();
        }
    }

    private void disconnect() {
        _host = null;
    }

    private void connect() {

    }

    public void onClick(View view) {
        if (_host == null) return;
        switch(view.getId()) {
            case R.id.btnsettings :
                ActivitySettings.Show(this);
                break;
            case R.id.btnmute :
                sendCommand(CmusCommand.VOLUME_MUTE);
                break;
            case R.id.btnvoldown :
                sendCommand(CmusCommand.VOLUME_DOWN);
                break;
            case R.id.btnvolup :
                sendCommand(CmusCommand.VOLUME_UP);
                break;
            case R.id.btnshuffle :
                sendCommand(CmusCommand.SHUFFLE);
                break;
            case R.id.btnrepeat :
                sendCommand(CmusCommand.REPEAT);
                break;
//            case R.id.btnrepeatall :
//                sendCommand(CmusCommand.REPEAT_ALL);
//                break;
            case R.id.btnback :
                sendCommand(CmusCommand.PREV);
                break;
            case R.id.btnstop :
                sendCommand(CmusCommand.STOP);
                break;
            case R.id.btnplay :
                if (isPlaying()) {
                    sendCommand(CmusCommand.PAUSE);
                } else {
                    sendCommand(CmusCommand.PLAY);
                }
                break;
            case R.id.btnforward :
                sendCommand(CmusCommand.NEXT);
                break;
//            case R.id.btnstatus :
//                sendCommand(CmusCommand.STATUS);
//                break;
        }
    }

    private boolean isPlaying() {
        return false; //TODO: implement
    }

    private void sendCommand(final CmusCommand command) {
        new CommandThread(_host, command, this).start();
    }

    @Override
    public void onAnswer(CmusCommand command, String answer) {
        if (!command.equals(CmusCommand.STATUS)) {
            if (command.equals(CmusCommand.VOLUME_MUTE)) {
                _bMuted = !_bMuted;
            }
            else if (command.equals(CmusCommand.VOLUME_UP)) {
                _bMuted = false;
            }

        }
        CmusStatus cmusStatus = new CmusStatus();

        String[] strs = answer.split("\n");

        for (String str : strs) {
            if (str.startsWith("set") || str.startsWith("tag")) {
                addTagOrSetting(cmusStatus, str);
            } else {
                int firstSpace = str.indexOf(' ');
                String type = str.substring(0, firstSpace);
                String value = str.substring(firstSpace + 1);
                if (type.equals("status")) {
                    cmusStatus.setStatus(value);
                } else if (type.equals("file")) {
                    cmusStatus.setFile(value);
                } else if (type.equals("duration")) {
                    cmusStatus.setDuration(value);
                } else if (type.equals("position")) {
                    cmusStatus.setPosition(value);
                }
            }
        }
    }


    private void addTagOrSetting(CmusStatus cmusStatus, String line) {
        int firstSpace = line.indexOf(' ');
        int secondSpace = line.indexOf(' ', firstSpace + 1);
        String type = line.substring(0, firstSpace);
        String key = line.substring(firstSpace + 1, secondSpace);
        String value = line.substring(secondSpace + 1);
        if (type.equals("set")) {
            cmusStatus.setSetting(key, value);
        } else if (type.equals("tag")) {
            cmusStatus.setTag(key, value);
        } else {
            Log.e(getClass().getSimpleName(), "Unknown type in status: " + line);
        }
    }

    @Override
    public void onError(Exception e) {

    }
}
