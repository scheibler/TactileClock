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

    // gaps
    public static final long SHORT_GAP = 250;
    public static final long MEDIUM_GAP = 750;
    public static final long LONG_GAP = 1250;

    // service vars
    private long lastScreenActivation;
    private SharedPreferences settings;
    private Vibrator vibrator;

    @Override public void onCreate() {
        super.onCreate();
        lastScreenActivation = System.currentTimeMillis();
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
        if (intent != null && intent.hasExtra("screenOn")) {
            boolean screenOn = intent.getBooleanExtra("screenOn", false);
            if (screenOn) {
                lastScreenActivation = System.currentTimeMillis();
            } else {
                long timeDifference = System.currentTimeMillis() - lastScreenActivation;
                if (timeDifference > 500 && timeDifference < 1500) {
                    vibrateTime();
                }
            }
        }
    }

    private void vibrateTime() {
        // get time format setting
        Constants.TimeFormat timeFormat = Constants.TimeFormat.lookupByCode(
                settings.getString(Constants.SETTINGS.TIME_FORMAT, null));
        // get current time
        Calendar c = Calendar.getInstance();
        int hours;
        if (timeFormat == Constants.TimeFormat.TWENTYFOUR_HOURS) {
            // 24 hour format
            hours = c.get(Calendar.HOUR_OF_DAY);
        } else {
            // 12 hour format
            hours = c.get(Calendar.HOUR);
            if (hours == 0)
                hours = 12;
        }
        int minutes = c.get(Calendar.MINUTE);

        // create vibration pattern
        // start with initial gap
        long[] pattern = new long[]{LONG_GAP};
        // only add first digit of hour if it is not a zero
        if (hours / 10 > 0) {
            // first number of hour
            pattern = concat(pattern, getVibrationPatternForDigit(hours/10));
            // medium gap between first and second number of hours
            pattern = concat(pattern, new long[]{MEDIUM_GAP});
        }
        // second number of hour
        pattern = concat(pattern, getVibrationPatternForDigit(hours%10));
        // long gap between hours and minutes
        pattern = concat(pattern, new long[]{LONG_GAP});
        // first number of minute
        pattern = concat(pattern, getVibrationPatternForDigit(minutes/10));
        // medium gap between first and second number of minutes
        pattern = concat(pattern, new long[]{MEDIUM_GAP});
        // second number of minute
        pattern = concat(pattern, getVibrationPatternForDigit(minutes%10));

        // start vibration
        vibrator.vibrate(pattern, -1);
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
