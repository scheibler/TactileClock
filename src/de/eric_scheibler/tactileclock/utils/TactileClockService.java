package de.eric_scheibler.tactileclock.utils;

import java.util.Calendar;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.os.Vibrator;
import android.preference.PreferenceManager;

public class TactileClockService extends Service {

    // vibrations
    public static final long SHORT_VIBRATION = 100;
    public static final long LONG_VIBRATION = 500;
    public static final long ERROR_VIBRATION = 900;

    // gaps
    public static final long SHORT_GAP = 250;
    public static final long MEDIUM_GAP = 750;
    public static final long LONG_GAP = 1250;

    // double click parameters
    public static final long LOWER_SUCCESS_BOUNDARY = 50;
    public static final long UPPER_SUCCESS_BOUNDARY = 1250;
    public static final long LOWER_ERROR_BOUNDARY = 50;
    public static final long UPPER_ERROR_BOUNDARY = 500;

    // service vars
    private long lastActivation;
    private SharedPreferences settings;
    private Vibrator vibrator;

    @Override public void onCreate() {
        super.onCreate();
        lastActivation = System.currentTimeMillis();
        settings = PreferenceManager.getDefaultSharedPreferences(this);
        vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);

        // register receiver that handles screen on and screen off logic
        // can't be done in manifest
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_SCREEN_ON);
        filter.addAction(Intent.ACTION_SCREEN_OFF);
        BroadcastReceiver mReceiver = new ScreenReceiver();
        registerReceiver(mReceiver, filter);
    }

    @Override public IBinder onBind(Intent intent) {
        return null;
    }

    @Override public void onStart(Intent intent, int startId) {
        if (intent != null
                && settings.getBoolean(Constants.SETTINGS_KEY.ENABLE_SERVICE, true)) {
            long timeDifference = System.currentTimeMillis() - lastActivation;

            if (settings.getBoolean(Constants.SETTINGS_KEY.ERROR_VIBRATION, true)
                    && Intent.ACTION_SCREEN_ON.equals(intent.getAction())
                    && timeDifference > LOWER_ERROR_BOUNDARY
                    && timeDifference < UPPER_ERROR_BOUNDARY) {
                // double click detected
                // but screen was turned off and on instead of on and off
                // vibrate error message
                vibrator.vibrate(ERROR_VIBRATION);

            } else if (Intent.ACTION_SCREEN_OFF.equals(intent.getAction())
                    && timeDifference > LOWER_SUCCESS_BOUNDARY
                    && timeDifference < UPPER_SUCCESS_BOUNDARY) {
                // double click detected
                // screen was turned on and off correctly
                // vibrate time
                //
                // get current time
                int hours, minutes;
                Calendar c = Calendar.getInstance();
                Constants.HourFormat hourFormat = Constants.HourFormat.lookupByCode(
                        settings.getString(Constants.SETTINGS_KEY.HOUR_FORMAT, null));
                if (Constants.HourFormat.TWELVE_HOURS == hourFormat) {
                    // 12 hour format
                    hours = c.get(Calendar.HOUR);
                    if (hours == 0)
                        hours = 12;
                } else {
                    // 24 hour format
                    hours = c.get(Calendar.HOUR_OF_DAY);
                }
                minutes = c.get(Calendar.MINUTE);

                // create vibration pattern
                // start with initial gap
                long[] pattern = new long[]{LONG_GAP};
                Constants.TimeComponentOrder timeComponentOrder = Constants.TimeComponentOrder.lookupByCode(
                        settings.getString(Constants.SETTINGS_KEY.TIME_COMPONENT_ORDER, null));
                if (Constants.TimeComponentOrder.MINUTES_HOURS == timeComponentOrder) {
                    // minutes first
                    pattern = concat(pattern, getVibrationPatternForMinutes(minutes));
                    // long gap between hours and minutes
                    pattern = concat(pattern, new long[]{LONG_GAP});
                    // then hours
                    pattern = concat(pattern, getVibrationPatternForHours(hours));
                } else {
                    // hours first
                    pattern = concat(pattern, getVibrationPatternForHours(hours));
                    // long gap between hours and minutes
                    pattern = concat(pattern, new long[]{LONG_GAP});
                    // then minutes
                    pattern = concat(pattern, getVibrationPatternForMinutes(minutes));
                }

                // start vibration
                vibrator.vibrate(pattern, -1);
            }
            lastActivation = System.currentTimeMillis();
        }
    }

    private long[] getVibrationPatternForHours(int hours) {
        long[] pattern = new long[]{};
        // only add first digit of hour if it is not a zero
        if (hours / 10 > 0) {
            // first number of hour
            pattern = concat(pattern, getVibrationPatternForDigit(hours/10));
            // medium gap between first and second number of hours
            pattern = concat(pattern, new long[]{MEDIUM_GAP});
        }
        // second number of hour
        pattern = concat(pattern, getVibrationPatternForDigit(hours%10));
        return pattern;
    }

    private long[] getVibrationPatternForMinutes(int minutes) {
        long[] pattern = new long[]{};
        // first number of minute
        pattern = concat(pattern, getVibrationPatternForDigit(minutes/10));
        // medium gap between first and second number of minutes
        pattern = concat(pattern, new long[]{MEDIUM_GAP});
        // second number of minute
        pattern = concat(pattern, getVibrationPatternForDigit(minutes%10));
        return pattern;
    }

    private long[] getVibrationPatternForDigit(int digit) {
        switch (digit) {
            case 0:
                return new long[]{LONG_VIBRATION, SHORT_GAP, LONG_VIBRATION};
            case 1:
                return new long[]{SHORT_VIBRATION};
            case 2:
                return new long[]{SHORT_VIBRATION, SHORT_GAP, SHORT_VIBRATION};
            case 3:
                return new long[]{SHORT_VIBRATION, SHORT_GAP, SHORT_VIBRATION,
                        SHORT_GAP, SHORT_VIBRATION};
            case 4:
                return new long[]{SHORT_VIBRATION, SHORT_GAP, SHORT_VIBRATION,
                        SHORT_GAP, SHORT_VIBRATION, SHORT_GAP, SHORT_VIBRATION};
            case 5:
                return new long[]{LONG_VIBRATION};
            case 6:
                return new long[]{LONG_VIBRATION, SHORT_GAP, SHORT_VIBRATION};
            case 7:
                return new long[]{LONG_VIBRATION, SHORT_GAP, SHORT_VIBRATION,
                        SHORT_GAP, SHORT_VIBRATION};
            case 8:
                return new long[]{LONG_VIBRATION, SHORT_GAP, SHORT_VIBRATION,
                        SHORT_GAP, SHORT_VIBRATION, SHORT_GAP, SHORT_VIBRATION};
            case 9:
                return new long[]{LONG_VIBRATION, SHORT_GAP, SHORT_VIBRATION,
                        SHORT_GAP, SHORT_VIBRATION, SHORT_GAP, SHORT_VIBRATION,
                        SHORT_GAP, SHORT_VIBRATION};
            default:
                return new long[]{};
        }
    }

    private long[] concat(long[] array1, long[] array2) {
        int array1Len = array1.length;
        int array2Len = array2.length;
        long[] arrayResult = new long[array1Len+array2Len];
        System.arraycopy(array1, 0, arrayResult, 0, array1Len);
        System.arraycopy(array2, 0, arrayResult, array1Len, array2Len);
        return arrayResult;
    }

}
