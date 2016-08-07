package de.eric_scheibler.tactileclock.utils;

import java.util.HashMap;
import java.util.Map;

public class Constants {

    public interface SETTINGS {
        public static final String ENABLE_SERVICE = "enableService";
        public static final String TIME_FORMAT = "timeFormat";
    }

    public enum TimeFormat {
        TWELVE_HOURS("12hours"),
        TWENTYFOUR_HOURS("24hours");

        private final String code;
        private static final Map<String,TimeFormat> valuesByCode;

        static {
            valuesByCode = new HashMap<String,TimeFormat>();
            for(TimeFormat timeFormat : TimeFormat.values()) {
                valuesByCode.put(timeFormat.code, timeFormat);
            }
        }

        public static TimeFormat lookupByCode(String code) {
            if (code == null || valuesByCode.get(code) == null)
                return TWENTYFOUR_HOURS;      // default value
            return valuesByCode.get(code);
        }

        private TimeFormat(String code) {
            this.code = code;
        }

        public String getCode() {
            return code;
        }
    }

}
