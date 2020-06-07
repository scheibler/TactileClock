package de.eric_scheibler.tactileclock.data;

import java.util.HashMap;
import java.util.Map;


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
