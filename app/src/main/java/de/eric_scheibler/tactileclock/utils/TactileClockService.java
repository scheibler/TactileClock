package de.eric_scheibler.tactileclock.utils;

import android.annotation.TargetApi;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

import android.media.AudioManager;

import android.os.Build;
import android.os.IBinder;
import android.os.SystemClock;
import android.os.Vibrator;

import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;

import de.eric_scheibler.tactileclock.R;
import de.eric_scheibler.tactileclock.ui.activity.MainActivity;
import de.eric_scheibler.tactileclock.utils.SettingsManager;

import java.util.Calendar;


public class TactileClockService extends Service {

    // vibrations
    public static final long SHORT_VIBRATION = 100;
    public static final long LONG_VIBRATION = 500;
    public static final long ERROR_VIBRATION = 900;

    // gaps
    public static final long SHORT_GAP = 250;
    public static final long MEDIUM_GAP = 750;
    public static final long LONG_GAP = 1250;

    // service vars
    private long lastActivation;
    private ApplicationInstance applicationInstance;
    private AudioManager audioManager;
    private NotificationManager notificationManager;
    private ScreenReceiver mScreenReceiver;
    private SettingsManager settingsManagerInstance;
    private Vibrator vibrator;

    @Override public void onCreate() {
        super.onCreate();
        lastActivation = System.currentTimeMillis();
        applicationInstance = (ApplicationInstance) getApplicationContext();
        audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        settingsManagerInstance = SettingsManager.getInstance(this);
        vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        // register receiver that handles screen on and screen off logic
        // can't be done in manifest
        mScreenReceiver = new ScreenReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_SCREEN_ON);
        filter.addAction(Intent.ACTION_SCREEN_OFF);
        registerReceiver(mScreenReceiver, filter);
    }

    @Override public IBinder onBind(Intent intent) {
        return null;
    }

    @Override public void onStart(Intent intent, int startId) {
        if (intent != null) {

            if (Constants.CustomAction.UPDATE_SERVICE_NOTIFICATION.equals(intent.getAction())) {
                startForeground(Constants.ID.NOTIFICATION_ID, getServiceNotification());
                if (! settingsManagerInstance.getPowerButtonServiceEnabled()
                        && ! settingsManagerInstance.isWatchEnabled()) {
                    stopSelf();
                }

            } else if (Intent.ACTION_SCREEN_ON.equals(intent.getAction())) {
                long activationTimeDifference = System.currentTimeMillis() - lastActivation;
                if (settingsManagerInstance.getPowerButtonServiceEnabled()
                        && settingsManagerInstance.getPowerButtonErrorVibration()
                        && activationTimeDifference > settingsManagerInstance.getPowerButtonLowerErrorBoundary()
                        && activationTimeDifference < settingsManagerInstance.getPowerButtonUpperErrorBoundary()) {
                    // double click detected
                    // but screen was turned off and on instead of on and off
                    // vibrate error message
                    vibrator.vibrate(ERROR_VIBRATION);
                }
                lastActivation = System.currentTimeMillis();

            } else if (Intent.ACTION_SCREEN_OFF.equals(intent.getAction())) {
                long activationTimeDifference = System.currentTimeMillis() - lastActivation;
                if (settingsManagerInstance.getPowerButtonServiceEnabled()
                        && activationTimeDifference > settingsManagerInstance.getPowerButtonLowerSuccessBoundary()
                        && activationTimeDifference < settingsManagerInstance.getPowerButtonUpperSuccessBoundary()) {
                    // double click detected
                    // screen was turned on and off correctly
                    // vibrate time
                    vibrateTime(false);
                }
                lastActivation = System.currentTimeMillis();

            } else if (Constants.CustomAction.WATCH_VIBRATE.equals(intent.getAction())
                    && settingsManagerInstance.isWatchEnabled()) {
                // vibrate current time
                if (this.isVibrationAllowed()) {
                    vibrateTime(settingsManagerInstance.getWatchOnlyVibrateMinutes());
                }
                // new vibrate time pending intent
                Intent intentStartTimeVibration = new Intent(this, TactileClockService.class);
                intentStartTimeVibration.setAction(Constants.CustomAction.WATCH_VIBRATE);
                PendingIntent pendingIntentStartTimeVibration = PendingIntent.getService(
                        this, Constants.ID.PENDING_INTENT_VIBRATE_TIME_ID, intentStartTimeVibration, PendingIntent.FLAG_CANCEL_CURRENT);
                // set alarm at next watch interval
                applicationInstance.setAlarm(
                        SystemClock.elapsedRealtime() 
                            + settingsManagerInstance.getWatchVibrationIntervalInMinutes()*60*1000l,
                        pendingIntentStartTimeVibration);

            } else if (Constants.CustomAction.WATCH_DISABLE.equals(intent.getAction())
                    && settingsManagerInstance.isWatchEnabled()) {
                // vibrate stop message
                if (this.isVibrationAllowed()) {
                    vibrator.vibrate(ERROR_VIBRATION);
                }
                // disable watch
                settingsManagerInstance.disableWatch();
                settingsManagerInstance.setWatchAutoSwitchOffEnabled(false);
                // reload ui
                Intent reloadUIIntent = new Intent(Constants.CustomAction.RELOAD_UI);
                LocalBroadcastManager.getInstance(this).sendBroadcast(reloadUIIntent);
            }
        }
    }

    @Override public void onDestroy() {
        super.onDestroy();
        try {
            if (mScreenReceiver != null) {
                unregisterReceiver(mScreenReceiver);
            }
        } catch (IllegalArgumentException e) {}
        notificationManager.cancel(Constants.ID.NOTIFICATION_ID);
        stopForeground(true);
    }


    /**
     * vibration pattern functions
     */

    private void vibrateTime(boolean minutesOnly) {
        // get current time
        int hours, minutes;
        Calendar c = Calendar.getInstance();
        if (settingsManagerInstance.getHourFormat() == Constants.HourFormat.TWELVE_HOURS) {
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
        // start with short initial gap
        long[] pattern = new long[]{SHORT_GAP};
        // hours and minutes
        if (settingsManagerInstance.getTimeComponentOrder() == Constants.TimeComponentOrder.MINUTES_HOURS) {
            // minutes first
            pattern = concat(pattern, getVibrationPatternForMinutes(minutes));
            // hours
            if (! minutesOnly) {
                // long gap between hours and minutes
                pattern = concat(pattern, new long[]{LONG_GAP});
                // then hours
                pattern = concat(pattern, getVibrationPatternForHours(hours));
            }
        } else {
            // hours
            if (! minutesOnly) {
                // hours first
                pattern = concat(pattern, getVibrationPatternForHours(hours));
                // long gap between hours and minutes
                pattern = concat(pattern, new long[]{LONG_GAP});
            }
            // then minutes
            pattern = concat(pattern, getVibrationPatternForMinutes(minutes));
        }

        // start vibration
        vibrator.vibrate(pattern, -1);
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


    /**
     * notification
     */

    private Notification getServiceNotification() {
        // launch MainActivity intent
        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                this, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        // notification message text
        String notificationMessage;
        if (settingsManagerInstance.isWatchEnabled()) {
            notificationMessage = String.format(
                    getResources().getString(R.string.serviceNotificationWatchEnabled),
                    enabledOrDisabled(settingsManagerInstance.getPowerButtonServiceEnabled()),
                    enabledOrDisabled(settingsManagerInstance.isWatchEnabled()),
                    getResources().getQuantityString(
                        R.plurals.minutes,
                        settingsManagerInstance.getWatchVibrationIntervalInMinutes(),
                        settingsManagerInstance.getWatchVibrationIntervalInMinutes()));
        } else {
            notificationMessage = String.format(
                    getResources().getString(R.string.serviceNotification),
                    enabledOrDisabled(settingsManagerInstance.getPowerButtonServiceEnabled()),
                    enabledOrDisabled(settingsManagerInstance.isWatchEnabled()));
        }
        // return notification
        return new NotificationCompat.Builder(this)
            .setChannelId(Constants.ID.NOTIFICATION_CHANNEL_ID)
            .setVisibility(NotificationCompat.VISIBILITY_SECRET)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setShowWhen(false)
            .setUsesChronometer(false)
            .setOngoing(true)
            .setContentIntent(pendingIntent)
            .setSmallIcon(R.drawable.ic_launcher)
            .setContentText(notificationMessage)
            .build();
    }

    private String enabledOrDisabled(boolean enabled) {
        if (enabled) {
            return getResources().getString(R.string.dialogEnabled);
        }
        return getResources().getString(R.string.dialogDisabled);
    }


    /**
     * do not desturb and active call
     */

    @TargetApi(Build.VERSION_CODES.M)
    private boolean isVibrationAllowed() {
        // do not desturb
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            if (notificationManager.getCurrentInterruptionFilter() == NotificationManager.INTERRUPTION_FILTER_NONE
                    || notificationManager.getCurrentInterruptionFilter() == NotificationManager.INTERRUPTION_FILTER_ALARMS) {
                return false;
            }
        }
        // active call
        if(audioManager.getMode()!=AudioManager.MODE_NORMAL){
            return false;
        }
        return true;
    }

}
