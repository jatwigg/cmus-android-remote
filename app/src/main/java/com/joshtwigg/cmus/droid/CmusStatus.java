package com.joshtwigg.cmus.droid;

import android.util.Log;

import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Map;

/**
 *
 status playing
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

    public final static String FILE = "file";
    public final static String STATUS = "status";
    public final static String DURATION = "duration";
    public final static String POSITION = "position";

    public final static class TAGS {
        public static final String DATE = "date";
        public static final String ALBUM = "album";
        public static final String TITLE = "title";
        public static final String GENRE = "genre";
        public static final String ARTIST = "artist";
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
        public static final String SHUFFLE= "shuffle";
        public static final String SOFTVOL = "s oftvol";
        public static final String VOL_LEFT = "vol_left";
        public static final String VOL_RIGHT = "vol_right";

    }
    private Map<String, String> _map = new HashMap<String, String>();

    public CmusStatus(final String answer) {
        String[] responseLines = answer.split("\n");
        for (String line : responseLines) {
            int firstSpace = line.indexOf(' ');
            String type = line.substring(0, firstSpace);
            String value = line.substring(firstSpace + 1);

            if (type.equals("set") || type.equals("tag")) {
                int secondSpace = line.indexOf(' ', firstSpace + 1);
                String key = line.substring(firstSpace + 1, secondSpace);
                value = line.substring(secondSpace + 1);
                _map.put(key, value);
            } else {
                // presuming its one of the first three
                _map.put(type, value);
            }
        }
    }

    public String get(String tagOrSettingConst) {
        if (_map.containsKey(tagOrSettingConst)) return _map.get(tagOrSettingConst);
        return "Unknown";
    }

    public int getPositionInt() {
        try {
            return Integer.parseInt(_map.get(POSITION));
        }
        catch (Exception e){
            Log.e(getClass().getSimpleName(), "Error parsing position as int.", e);
        }
        return -1;
    }

    public int getDurationInt() {
        try {
            return Integer.parseInt(_map.get(DURATION));
        }
        catch (Exception e){
            Log.e(getClass().getSimpleName(), "Error parsing duration as int.", e);
        }
        return -1;
    }

    public String getUnifiedVolume() {
        try {
            String volRight = _map.get(SETTINGS.VOL_RIGHT);
            String volLeft = _map.get(SETTINGS.VOL_LEFT);
            if (volLeft == null && volRight != null) {
                return volRight + "%";
            } else if (volLeft != null && volRight == null) {
                return volLeft + "%";
            }
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
        try {
            String volRight = _map.get(SETTINGS.VOL_RIGHT);
            String volLeft = _map.get(SETTINGS.VOL_LEFT);
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
        strBuilder.append("Title: ").append(_map.get(TAGS.TITLE)).append("\n");
        strBuilder.append("Album: ").append(_map.get(TAGS.ALBUM)).append("\n");
        strBuilder.append("Artist: ").append(_map.get(TAGS.ARTIST)).append("\n");
        strBuilder.append("Volume: ").append(getUnifiedVolume()).append("\n");
        return strBuilder.toString();
    }
}
