package com.joshtwigg.cmus.droid;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;
import android.view.View;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;

import java.net.ConnectException;

/**
 * Created by josh on 31/01/14.
 */
public class ActivityRemote extends Activity implements ICallback {
    private Host _host = null;
    private boolean _bMuted = false;
    private boolean _bPlaying = false;
    private TextView _status;
    private ImageButton _playButton;
    private SeekBar _seekBar;
    private int pollFreq;
    private static final Handler _pollHandler = new Handler();
    private Runnable _pollRunnable = new Runnable() {
        @Override
        public void run() {
            sendCommand(CmusCommand.STATUS);
            _pollHandler.postDelayed(this, pollFreq);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_remote);
        pollFreq = getResources().getInteger(R.integer.default_poll_mills);
        _status = (TextView) findViewById(R.id.status);
        _playButton = (ImageButton)findViewById(R.id.btnplay);
        _seekBar = (SeekBar)findViewById(R.id.seekBar);
        _seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) sendCommand(CmusCommand.SEEK(progress));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        ActivityWelcome.showIfFirstTime(this);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        return false;
    }

    @Override
    protected void onPause() {
        super.onPause();
        _pollHandler.removeCallbacksAndMessages(null);
    }

    @Override
    protected void onResume() {
        super.onResume();
        _host = Storage.getHost(this);
        if (_host == null) {
            setTitle("CMUS Remote Not Connected");
            disconnect();
        } else {
            setTitle("CMUS Remote Connecting");
            connect();
        }
    }

    private void disconnect() {
        _host = null;
        _pollHandler.removeCallbacksAndMessages(null);
    }

    private void connect() {
        sendCommand(CmusCommand.STATUS);
        _pollHandler.postDelayed(_pollRunnable, pollFreq);
    }

    public void onClick(View view) {
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
                if (_bPlaying) {
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

    private void sendCommand(final CmusCommand command) {
        if (_host == null) return;
        new CommandThread(_host, command, this).start();
        // to refresh the details
        if (!command.equals(CmusCommand.STATUS)) sendCommand(CmusCommand.STATUS);
    }

    @Override
    public void onAnswer(CmusCommand command, String answer) {
        if (!command.equals(CmusCommand.STATUS)) {
            return;
        }

        final CmusStatus cmusStatus = new CmusStatus(answer);
        // set host and track.
        setTitle("" + _host.host + " " + cmusStatus.getTag(CmusStatus.Tags.ARTIST) + " " + cmusStatus.getTag(CmusStatus.Tags.TITLE));
        if (cmusStatus.getStatus().equals("stopped") || cmusStatus.getStatus().equals("paused")){
            _bPlaying = false;
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    _playButton.setImageResource(R.drawable.play);
                }
            });
        }
        else {
            _bPlaying = true;
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    _playButton.setImageResource(R.drawable.pause);
                }
            });
        }
        if (cmusStatus.getUnifiedVolume().equals("0%")){
            _bMuted = true;
        }
        else {
            _bMuted = false;
        }
        final int position = cmusStatus.getPositionInt();
        final int duration = cmusStatus.getDurationInt();

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                _status.setText(cmusStatus.toString());
                _status.postInvalidate();
                if (duration > 0) {
                    _seekBar.setMax(duration);
                    _seekBar.setProgress(position);
                    _seekBar.postInvalidate();
                }
            }
        });
    }

    @Override
    public void onError(Exception e) {
        if (e.getMessage().startsWith("Could not login")) {
            setTitle("CMUS Remote [Could not login, check password]");
        }
        else if (e instanceof ConnectException) {
            setTitle("CMUS Remote [Connection error, check host]");
        }
    }

    @Override
    public void setTitle(final CharSequence title) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                ActivityRemote.super.setTitle(title);
            }
        });
    }
}
