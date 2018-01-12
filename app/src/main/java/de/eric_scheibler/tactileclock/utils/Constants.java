package de.eric_scheibler.tactileclock.utils;

import java.util.HashMap;
import java.util.Map;

import de.eric_scheibler.tactileclock.BuildConfig;


public class Constants {

    public interface ID {
        public static final int NOTIFICATION_ID = 91223;
        public static final String NOTIFICATION_CHANNEL_ID = BuildConfig.APPLICATION_ID;
        public static final int PENDING_INTENT_VIBRATE_TIME_ID = 39128;
        public static final int PENDING_INTENT_DISABLE_WATCH_ID = 39129;
    }

    public interface SETTINGS_KEY {
        // general
        public static final String FIRST_START = "firstStart";
        public static final String RECENT_OPEN_TAB = "recentOpenTab";
        public static final String HOUR_FORMAT = "hourFormat";
        public static final String TIME_COMPONENT_ORDER = "timeComponentOrder";
        // power button
        public static final String POWER_BUTTON_SERVICE_ENABLED = "enableService";
        public static final String POWER_BUTTON_ERROR_VIBRATION = "errorVibration";
        public static final String POWER_BUTTON_LOWER_SUCCESS_BOUNDARY = "lowerSuccessBoundary";
        public static final String POWER_BUTTON_UPPER_SUCCESS_BOUNDARY = "upperSuccessBoundary";
        // watch
        public static final String WATCH_ENABLED = "watchEnabled";
        public static final String WATCH_AUTO_SWITCH_OFF_ENABLED = "watchAutoSwitchOffEnabled";
        public static final String WATCH_AUTO_SWITCH_OFF_TIME = "watchAutoSwitchOffTime";
        public static final String WATCH_ONLY_VIBRATE_MINUTES = "onlyVibrateMinutes";
        public static final String WATCH_START_AT_NEXT_FULL_HOUR = "startAtNextFullHour";
        public static final String WATCH_VIBRATION_INTERVAL = "watchVibrationInterval";
    }

    public interface CustomAction {
        public static final String RELOAD_UI = "de.eric_scheibler.tactileclock.customAction.reloadui";
        public static final String UPDATE_SERVICE_NOTIFICATION = "de.eric_scheibler.tactileclock.customAction.updateservicenotification";
        public static final String WATCH_DISABLE = "de.eric_scheibler.tactileclock.customAction.watchdisable";
        public static final String WATCH_VIBRATE = "de.eric_scheibler.tactileclock.customAction.watchvibrate";
    }

    public enum HourFormat {
        TWELVE_HOURS("12hours"),
        TWENTYFOUR_HOURS("24hours");

        private final String code;
        private static final Map<String,HourFormat> valuesByCode;

        static {
            valuesByCode = new HashMap<String,HourFormat>();
            for(HourFormat hourFormat : HourFormat.values()) {
                valuesByCode.put(hourFormat.code, hourFormat);
            }
        }

        public static HourFormat lookupByCode(String code) {
            if (code == null || valuesByCode.get(code) == null)
                return TWENTYFOUR_HOURS;      // default value
            return valuesByCode.get(code);
        }

        private HourFormat(String code) {
            this.code = code;
        }

        public String getCode() {
            return code;
        }
    }

    public enum TimeComponentOrder {
        HOURS_MINUTES("hoursMinutes"),
        MINUTES_HOURS("minutesHours");

        private final String code;
        private static final Map<String,TimeComponentOrder> valuesByCode;

        static {
            valuesByCode = new HashMap<String,TimeComponentOrder>();
            for(TimeComponentOrder timeComponentOrder : TimeComponentOrder.values()) {
                valuesByCode.put(timeComponentOrder.code, timeComponentOrder);
            }
        }

        public static TimeComponentOrder lookupByCode(String code) {
            if (code == null || valuesByCode.get(code) == null)
                return HOURS_MINUTES;      // default value
            return valuesByCode.get(code);
        }

        private TimeComponentOrder(String code) {
            this.code = code;
        }

        public String getCode() {
            return code;
        }
    }

}
