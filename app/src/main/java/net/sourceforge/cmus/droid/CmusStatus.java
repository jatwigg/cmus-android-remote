package net.sourceforge.cmus.droid;

import android.util.Log;

import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * <pre>
 *  status playing
 *  file /home/me/Music/Queen . Greatest Hits I, II, III The Platinum Collection/Queen/Queen - Greatest Hits III (1999)(The Platinum Collection)/11 - Let me Live.mp3
 *  duration 285
 *  position 186
 * tag artist Queen
 * tag album Greatest Hits, Vol. 3
 * tag title Let Me Live
 * tag date 2000
 * tag genre Rock
 * tag tracknumber 11
 * tag albumartist Queen
 * set aaa_mode all
 * set continue true
 * set play_library true
 * set play_sorted false
 * set replaygain disabled
 * set replaygain_limit true
 * set replaygain_preamp 6.000000
 * set repeat true
 * set repeat_current false
 * set shuffle true
 * set softvol false
 * set vol_left 69
 * set vol_right 69
 * </pre>
 *
 * @author bboudreau
 *
 */
public class CmusStatus {

    private String status;
    private String file;
    private String duration;
    private String position;
    private Map<String, String> tags;
    private Map<String, String> settings;

    public CmusStatus() {
        this.tags = new HashMap<String, String>();
        this.settings = new HashMap<String, String>();
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getFile() {
        return file;
    }

    public void setFile(String file) {
        this.file = file;
    }

    public String getDuration() {
        return duration;
    }

    public void setDuration(String duration) {
        this.duration = duration;
    }

    public String getPosition() {
        return position;
    }

    public String getPositionPercent() {
        if (position == null || duration == null) {
            return "Unknown";
        }
        try {
            DecimalFormat twoDForm = new DecimalFormat("#.##%");
            Float positionF = Float.parseFloat(position);
            Float durationF = Float.parseFloat(duration);
            return twoDForm.format(positionF / durationF);
        } catch (Exception e) {
            Log.w(CmusDroidRemoteActivity.TAG, e);
            return "Unknown";
        }
    }

    public void setPosition(String position) {
        this.position = position;
    }

    public String getTag(String key) {
        String value = tags.get(key);
        return value != null ? value : "Unknown";
    }

    public void setTag(String key, String value) {
        if (this.tags == null) {
            this.tags = new HashMap<String, String>();
        }
        this.tags.put(key, value);
    }

    public String getSettings(String key) {
        String value = settings.get(key);
        return value != null ? value : "Unknown";
    }

    public void setSetting(String key, String value) {
        if (this.settings == null) {
            this.settings = new HashMap<String, String>();
        }
        this.settings.put(key, value);
    }

    public String getUnifiedVolume() {
        String volRight = settings.get("vol_right");
        String volLeft = settings.get("vol_left");
        if (volLeft == null && volRight != null) {
            return volRight + "%";
        } else if (volLeft != null && volRight == null) {
            return volLeft + "%";
        }
        try {
            Float volRightF = Float.parseFloat(volRight);
            Float volLeftF = Float.parseFloat(volLeft);

            DecimalFormat twoDForm = new DecimalFormat("#.##");
            return twoDForm.format((volRightF + volLeftF) / 2.0f) + "%";
        } catch (Exception e) {
            Log.w(CmusDroidRemoteActivity.TAG, e);
            return "Unknown";
        }
    }

    public String toSimpleString() {
        StringBuilder strBuilder = new StringBuilder();
        strBuilder.append("Artist: ").append(getTag("artist")).append("\n");
        strBuilder.append("Title: ").append(getTag("title")).append("\n");
        strBuilder.append("Position: ").append(getPositionPercent()).append("\n");
        strBuilder.append("Volume: ").append(getUnifiedVolume()).append("\n");
        return strBuilder.toString();
    }
}
