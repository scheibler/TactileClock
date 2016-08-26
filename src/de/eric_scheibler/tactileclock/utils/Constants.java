package de.eric_scheibler.tactileclock.utils;

import java.util.HashMap;
import java.util.Map;

public class Constants {

    public interface SETTINGS_KEY {
        public static final String ENABLE_SERVICE = "enableService";
        public static final String HOUR_FORMAT = "hourFormat";
        public static final String TIME_COMPONENT_ORDER = "timeComponentOrder";
        public static final String ERROR_VIBRATION = "errorVibration";
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
