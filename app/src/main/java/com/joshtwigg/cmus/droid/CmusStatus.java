package com.joshtwigg.cmus.droid;

import android.util.Log;

import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Map;

/**
 *
 file /home/josh/Music/Radiohead - The Best Of Radiohead 2008/1-12 Idioteque.mp3
 duration 277
 position 0
 tag artist Radiohead
 tag album The Best Of Radiohead 2008
 tag title Idioteque
 tag date 2008
 tag genre Rock
 tag discnumber 1
 tag tracknumber 12
 tag albumartist Radiohead
 tag replaygain_track_gain -7.2 dB
 set aaa_mode all
 set continue true
 set play_library true
 set play_sorted false
 set replaygain disabled
 set replaygain_limit true
 set replaygain_preamp 6.000000
 set repeat false
 set repeat_current false
 set shuffle false
 set softvol false
 set vol_left 100
 set vol_right 100
 */
public class CmusStatus {

    public final static class TAGS {
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

    public final static class SETTINGS {
        public static final String AAA_MODE = "aaa_mode";
        public static final String CONTINUE = "continue";
        public static final String PLAY_LIBRARY = "play_library";
        public static final String PLAY_SORTED = "play_sorted";
        public static final String REPLAYGAIN = "replaygain";
        public static final String REPLAYGAIN_LIMIT = "replaygain_limit";
        public static final String REPLAYGAIN_PREAMP = "replaygain_preamp";
        public static final String REPEAT_ALL = "repeat";
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
    private Map<String, String> tags = new HashMap<String, String>();
    private Map<String, String> settings = new HashMap<String, String>();

    public CmusStatus(final String answer) {

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
        this.tags.put(key, value);
    }

    public String getSettings(String key) {
        String value = settings.get(key);
        return value != null ? value : "Unknown";
    }

    public void setSetting(String key, String value) {
        this.settings.put(key, value);
    }

    public String getUnifiedVolume() {
        String volRight = settings.get(SETTINGS.VOL_RIGHT);
        String volLeft = settings.get(SETTINGS.VOL_LEFT);
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


    public int getUnifiedVolumeInt() {
        String volRight = settings.get(SETTINGS.VOL_RIGHT);
        String volLeft = settings.get(SETTINGS.VOL_LEFT);
        try {
            if (volLeft == null && volRight != null) {
                return Integer.parseInt(volRight);
            } else if (volLeft != null && volRight == null) {
                return Integer.parseInt(volLeft);
            }

            Float volRightF = Float.parseFloat(volRight);
            Float volLeftF = Float.parseFloat(volLeft);
            return (int)((volRightF + volLeftF) / 2.0f);
        } catch (Exception e) {
            Log.w(getClass().getSimpleName(), e);
            return -1;
        }
    }

    @Override
    public String toString() {
        StringBuilder strBuilder = new StringBuilder();
        strBuilder.append("Title: ").append(getTag(TAGS.TITLE)).append("\n");
        strBuilder.append("Album: ").append(getTag(TAGS.ALBUM)).append("\n");
        strBuilder.append("Artist: ").append(getTag(TAGS.ARTIST)).append("\n");
        strBuilder.append("Volume: ").append(getUnifiedVolume()).append("\n");
        return strBuilder.toString();
    }
}
