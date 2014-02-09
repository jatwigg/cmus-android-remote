package com.joshtwigg.cmus.droid;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import java.net.ConnectException;

/**
 * Created by josh on 31/01/14.
 */
public class ActivityRemote extends Activity implements ICallback {
    private Host _host = null;
    private TrackInfo _currentInfo = new TrackInfo();
    private ShowPopupMessage _showPopup = new ShowPopupMessage();
    private TextView _trackDetails;
    private ImageButton _playButton;
    private SeekBar _seekBar;
    private ImageView _albumArt;
    private static final Handler _pollHandler = new Handler();
    private Settings _settings;
    private Runnable _pollRunnable = new Runnable() {
        @Override
        public void run() {
            sendCommand(CmusCommand.STATUS);
            _pollHandler.postDelayed(this, _settings.POLL_DURATION_MILLS);
        }
    };
    private BroadcastReceiver _intentReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            _settings = Storage.getSettings(ActivityRemote.this);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        IntentFilter intentFilter = new IntentFilter(getResources().getString(R.string.intent_settings_changed));
        registerReceiver(_intentReceiver, intentFilter);
        _settings = Storage.getSettings(this);
        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            setContentView(R.layout.activity_remote_horizontal);
        }
        else {
            setContentView(R.layout.activity_remote);
        }
        _trackDetails = (TextView) findViewById(R.id.track_details);
        _trackDetails.setBackgroundColor(Color.argb(150, 0, 0, 0));
        _playButton = (ImageButton)findViewById(R.id.btnplay);
        _albumArt = (ImageView)findViewById(R.id.album_art);
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
        if (_settings.FETCH_ARTWORK) {
            _albumArt.setVisibility(View.VISIBLE);
            _currentInfo.album = ""; //regetch if needed
        } else {
            _albumArt.setVisibility(View.INVISIBLE);
        }
    }

    private void disconnect() {
        _host = null;
        _pollHandler.removeCallbacksAndMessages(null);
    }

    private void connect() {
        sendCommand(CmusCommand.STATUS);
        _pollHandler.postDelayed(_pollRunnable, _settings.POLL_DURATION_MILLS);
    }

    public void onClick(View view) {
        switch(view.getId()) {
            case R.id.btnsettings :
                ActivityHostManager.Show(this);
                break;
            case R.id.btnmute :
                if (_currentInfo.isMuted && _currentInfo.lastRecordedVolume > 0) {
                    sendCommand(CmusCommand.VOLUME(_currentInfo.lastRecordedVolume));
                } else {
                    sendCommand(CmusCommand.VOLUME_MUTE);
                }
                break;
            case R.id.btnvoldown :
                sendCommand(CmusCommand.VOLUME_DOWN);
                break;
            case R.id.btnvolup :
                sendCommand(CmusCommand.VOLUME_UP);
                break;
            case R.id.btnshuffle :
                sendCommand(CmusCommand.SHUFFLE);
                _showPopup.getShuffle(_pollHandler);
                break;
            case R.id.btnrepeat :
                sendCommand(CmusCommand.REPEAT);
                _showPopup.getRepeat(_pollHandler);
                break;
            case R.id.btnrepeatall :
                sendCommand(CmusCommand.REPEAT_ALL);
                _showPopup.getRepeatAll(_pollHandler);
                break;
            case R.id.btnback :
                sendCommand(CmusCommand.PREV);
                break;
            case R.id.btnstop :
                sendCommand(CmusCommand.STOP);
                break;
            case R.id.btnplay :
                if (_currentInfo.isPlaying) {
                    sendCommand(CmusCommand.PAUSE);
                } else {
                    sendCommand(CmusCommand.PLAY);
                }
                break;
            case R.id.btnforward :
                sendCommand(CmusCommand.NEXT);
                break;
        }
    }

    private void sendCommand(final CmusCommand command) {
        if (_host == null) return;
        new CommandThread(_host, command, this).start();
        // to refresh the details
        if (!command.equals(CmusCommand.STATUS)) sendCommand(CmusCommand.STATUS);
    }

    @Override
    public void onAnswer(final CmusCommand command, final String answer) {
        if (!command.equals(CmusCommand.STATUS)) {
            return;
        }

        final CmusStatus cmusStatus = new CmusStatus(answer);
        // don't update display if stopped.
        if ("stopped".equals(cmusStatus.get(CmusStatus.STATUS))) return;
        // set host and track.
        setTitle(String.format("%s:%d",_host.host, _host.port));
        if (cmusStatus.get(CmusStatus.STATUS).equals("stopped") || cmusStatus.get(CmusStatus.STATUS).equals("paused")){
            _currentInfo.isPlaying = false;
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    _playButton.setImageResource(R.drawable.play);
                }
            });
        }
        else {
            _currentInfo.isPlaying = true;
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    _playButton.setImageResource(R.drawable.pause);
                }
            });
        }
        if (cmusStatus.volumeIsZero()){
            _currentInfo.isMuted = true;
        }
        else {
            _currentInfo.isMuted = false;
            _currentInfo.lastRecordedVolume = cmusStatus.getUnifiedVolumeInt();
        }
        // check image art is still correct
        if (_settings.FETCH_ARTWORK) {
            if (!_currentInfo.album.equals(cmusStatus.get(CmusStatus.TAGS.ALBUM)) ||
                    !_currentInfo.artist.equals(cmusStatus.get(CmusStatus.TAGS.ARTIST))) {
                _currentInfo.album = cmusStatus.get(CmusStatus.TAGS.ALBUM);
                _currentInfo.artist = cmusStatus.get(CmusStatus.TAGS.ARTIST);
                Log.d(getClass().getSimpleName(), "Detected different album, getting artwork.");
                // change art
                Runnable artFetch = new Runnable() {
                    @Override
                    public void run() {
                        final Bitmap artwork = ArtRetriever.getArt(ActivityRemote.this, _currentInfo.album, _currentInfo.artist);
                        Log.d(getClass().getSimpleName(), "artwork is" + (artwork==null?"":" not") + " null.");
                        if (artwork != null) {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    _albumArt.setImageBitmap(artwork);
                                    _albumArt.postInvalidate(); //TODO: do i need this or will setBitmap call this?
                                }
                            });
                        }
                    };

                };
                _pollHandler.post(artFetch);
            }
        }
        // check duration and position for seekbar
        final int position = cmusStatus.getInt(CmusStatus.POSITION);
        final int duration = cmusStatus.getInt(CmusStatus.DURATION);

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                _trackDetails.setText(cmusStatus.toString());
                _trackDetails.postInvalidate();
                if (duration != -1 && position != -1) {
                    _seekBar.setMax(duration);
                    _seekBar.setProgress(position);
                    _seekBar.postInvalidate();
                }
                if (_showPopup.readShuffle()) {
                    Toast.makeText(ActivityRemote.this, "Shuffle is " +
                            (cmusStatus.get(CmusStatus.SETTINGS.SHUFFLE).equals("true")?"on":"off"),
                            Toast.LENGTH_SHORT).show();
                }
                if (_showPopup.readRepeat()) {
                    Toast.makeText(ActivityRemote.this, "Repeat is " +
                            (cmusStatus.get(CmusStatus.SETTINGS.REPEAT_CURRENT).equals("true")?"on":"off"),
                            Toast.LENGTH_SHORT).show();
                }
                if (_showPopup.readRepeatAll()) {
                    Toast.makeText(ActivityRemote.this, "Repeat all is " +
                            (cmusStatus.get(CmusStatus.SETTINGS.REPEAT_ALL).equals("true")?"on":"off"),
                            Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @Override
    public void onError(Exception e) {
        if (e != null && e.getMessage() != null && e.getMessage().startsWith("Could not login")) {
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

    @Override
    protected void onDestroy() {
        unregisterReceiver(_intentReceiver);
        super.onDestroy();
    }
}
