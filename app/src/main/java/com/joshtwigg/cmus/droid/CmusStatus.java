package com.joshtwigg.cmus.droid;

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

    public final static class Tags {
        public static final String ARTIST = "artist";
        public static final String ALBUM = "album";
        public static final String TITLE = "title";
        public static final String DATE = "date";
        public static final String GENRE = "genre";
        public static final String DISCNUMBER = "discnumber";
        public static final String TRACKNUMBER = "tracknumber";
        public static final String ALBUMARTIST = "albumartist";
        public static final String REPLAY_GAIN = "replaygain_track_gain";
    }

    public final static class Sets {
        public static final String AAA_MODE = "aaa_mode";
        public static final String CONTINUE = "continue";
        public static final String PLAY_LIBRARY = "play_library";
        public static final String PLAY_SORTED = "play_sorted";
        public static final String REPLAYGAIN = "replaygain";
        public static final String REPLAYGAIN_LIMIT = "replaygain_limit";
        public static final String REPLAYGAIN_PREAMP = "replaygain_preamp";
        public static final String REPEAT = "repeat";
        public static final String REPEAT_CURRENT = "repeat_current";
        public static final String SHUFFLE = "shuffle";
        public static final String SOFTVOL = "softvol";
        public static final String VOL_LEFT = "vol_left";
        public static final String VOL_RIGHT = "vol_right";

    }
    private String status;
    private String file;
    private String duration;
    private String position;
    private Map<String, String> tags;
    private Map<String, String> settings;

    public CmusStatus(final String answer) {
        this.tags = new HashMap<String, String>();
        this.settings = new HashMap<String, String>();

        String[] strs = answer.split("\n");

        for (String str : strs) {
            if (str.startsWith("set") || str.startsWith("tag")) {
                addTagOrSetting(str);
            } else {
                int firstSpace = str.indexOf(' ');
                String type = str.substring(0, firstSpace);
                String value = str.substring(firstSpace + 1);
                if (type.equals("status")) {
                    setStatus(value);
                } else if (type.equals("file")) {
                    setFile(value);
                } else if (type.equals("duration")) {
                    setDuration(value);
                } else if (type.equals("position")) {
                    setPosition(value);
                }
            }
        }
    }

    private void addTagOrSetting(final String line) {
        int firstSpace = line.indexOf(' ');
        int secondSpace = line.indexOf(' ', firstSpace + 1);
        String type = line.substring(0, firstSpace);
        String key = line.substring(firstSpace + 1, secondSpace);
        String value = line.substring(secondSpace + 1);
        if (type.equals("set")) {
            setSetting(key, value);
        } else if (type.equals("tag")) {
            setTag(key, value);
        } else {
            Log.e(getClass().getSimpleName(), "Unknown type in status: " + line);
        }
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
            Log.w(getClass().getSimpleName(), e);
            return "Unknown";
        }
    }


    public float getPositionPercentFloat() {
        if (position == null || duration == null) {
            return -1;
        }
        try {
            Float positionF = Float.parseFloat(position);
            Float durationF = Float.parseFloat(duration);
            return (positionF / durationF) * 100;
        } catch (Exception e) {
            Log.w(getClass().getSimpleName(), e);
            return -1;
        }
    }


    public int getPositionInt() {
        try {
            return Integer.parseInt(position);
        }
        catch (Exception e){
            Log.e(getClass().getSimpleName(), "Error parsing position as int.", e);
        }
        return -1;
    }

    public int getDurationInt() {
        try {
            return Integer.parseInt(duration);
        }
        catch (Exception e){
            Log.e(getClass().getSimpleName(), "Error parsing duration as int.", e);
        }
        return -1;
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
            Log.w(getClass().getSimpleName(), e);
            return "Unknown";
        }
    }

    @Override
    public String toString() {
        StringBuilder strBuilder = new StringBuilder();
        strBuilder.append("Artist: ").append(getTag("artist")).append("\n");
        strBuilder.append("Title: ").append(getTag("title")).append("\n");
        strBuilder.append("Position: ").append(getPositionPercent()).append("\n");
        strBuilder.append("Volume: ").append(getUnifiedVolume()).append("\n");
        return strBuilder.toString();
    }
}
