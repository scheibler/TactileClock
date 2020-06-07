package de.eric_scheibler.tactileclock.data;

import java.util.HashMap;
import java.util.Map;


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
