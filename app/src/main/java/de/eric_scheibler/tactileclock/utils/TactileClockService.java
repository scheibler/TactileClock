package de.eric_scheibler.tactileclock.utils;

            import android.os.Handler;
            import android.os.Looper;
import java.util.Calendar;

import android.annotation.TargetApi;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

import android.media.AudioManager;

import android.os.Build;
import android.os.IBinder;
import android.os.Vibrator;

import androidx.core.app.NotificationCompat;

import de.eric_scheibler.tactileclock.R;
import de.eric_scheibler.tactileclock.data.HourFormat;
import de.eric_scheibler.tactileclock.data.TimeComponentOrder;
import de.eric_scheibler.tactileclock.ui.activity.MainActivity;
import de.eric_scheibler.tactileclock.utils.SettingsManager;
import timber.log.Timber;
import android.annotation.SuppressLint;
import android.content.pm.ServiceInfo;
import androidx.core.app.ServiceCompat;
import android.app.ForegroundServiceStartNotAllowedException;
import android.os.VibrationEffect;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;


public class TactileClockService extends Service {

    // actions
    public static final String ACTION_UPDATE_NOTIFICATION = "de.eric_scheibler.tactileclock.action.update_notification";
    public static final String ACTION_VIBRATE_TIME = "de.eric_scheibler.tactileclock.action.vibrate_time";
    public static final String ACTION_VIBRATE_TIME_AND_SET_NEXT_ALARM = "de.eric_scheibler.tactileclock.action.vibrate_time_and_set_next_alarm";

    // vibrations
    public static final long SHORT_VIBRATION = 100;
    public static final long LONG_VIBRATION = 500;
    public static final long ERROR_VIBRATION = 1000;

    // amplitudes
    public static final int AMPLITUDE_DEFAULT = 150;
    public static final int AMPLITUDE_MAX = 250;

    // gaps
    public static final long SHORT_GAP = 250;
    public static final long MEDIUM_GAP = 750;
    public static final long LONG_GAP = 1250;

    // broadcast responses
    public static final String VIBRATION_FINISHED = "de.eric_scheibler.tactileclock.response.vibration_finished";

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
        lastActivation = 0l;
        Timber.d("onCreate");
        applicationInstance = (ApplicationInstance) ApplicationInstance.getContext();
        audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        settingsManagerInstance = new SettingsManager();
        vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);

        // register receiver that handles screen on and screen off logic
        // can't be done in manifest
        mScreenReceiver = new ScreenReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_SCREEN_ON);
        filter.addAction(Intent.ACTION_SCREEN_OFF);
        registerReceiver(mScreenReceiver, filter);

        if (settingsManagerInstance.isWatchEnabled()
                && ! ApplicationInstance.canScheduleExactAlarms()) {
            settingsManagerInstance.disableWatch();
        }
    }

    @Override public IBinder onBind(Intent intent) {
        return null;
    }

    @Override public void onStart(Intent intent, int startId) {
        if (intent != null) {
            Timber.d("action: %1$s", intent.getAction());

            if (ACTION_UPDATE_NOTIFICATION.equals(intent.getAction())) {
                startForegroundService();
                if (shouldDestroyService()) {
                    destroyService();
                }

            } else if (Intent.ACTION_SCREEN_ON.equals(intent.getAction())) {
                long activationTimeDifference = System.currentTimeMillis() - lastActivation;
                Timber.d("diff: %1$d = %2$d - %3$d", activationTimeDifference, System.currentTimeMillis(), lastActivation);
                if (settingsManagerInstance.getPowerButtonServiceEnabled()
                        && settingsManagerInstance.getPowerButtonErrorVibration()
                        && activationTimeDifference > settingsManagerInstance.getPowerButtonLowerErrorBoundary()
                        && activationTimeDifference < settingsManagerInstance.getPowerButtonUpperErrorBoundary()) {
                    // double click detected
                    // but screen was turned off and on instead of on and off
                    // vibrate error message
                    vibrateOnce(ERROR_VIBRATION);
                }
                lastActivation = System.currentTimeMillis();

            } else if (Intent.ACTION_SCREEN_OFF.equals(intent.getAction())) {
                long activationTimeDifference = System.currentTimeMillis() - lastActivation;
                Timber.d("diff: %1$d = %2$d - %3$d", activationTimeDifference, System.currentTimeMillis(), lastActivation);
                if (settingsManagerInstance.getPowerButtonServiceEnabled()
                        && activationTimeDifference > settingsManagerInstance.getPowerButtonLowerSuccessBoundary()
                        && activationTimeDifference < settingsManagerInstance.getPowerButtonUpperSuccessBoundary()) {
                    // double click detected
                    // screen was turned on and off correctly
                    // vibrate time
                    vibrateTime(false, false);
                }
                lastActivation = System.currentTimeMillis();

            } else if (ACTION_VIBRATE_TIME.equals(intent.getAction())) {
                vibrateTime(false, false);

            } else if (ACTION_VIBRATE_TIME_AND_SET_NEXT_ALARM.equals(intent.getAction())) {
                // vibrate current time
                if (this.isVibrationAllowed()) {
                    vibrateTime(
                            settingsManagerInstance.getWatchAnnouncementVibration(),
                            settingsManagerInstance.getWatchOnlyVibrateMinutes());
                }

                // set next alarm
                applicationInstance.setAlarmAtFullMinute(
                        settingsManagerInstance.getWatchVibrationIntervalInMinutes());
            }
        }
    }

    private void startForegroundService() {
        int type = Build.VERSION.SDK_INT >= Build.VERSION_CODES.R
            ? ServiceInfo.FOREGROUND_SERVICE_TYPE_MANIFEST : 0;
        try {
            ServiceCompat.startForeground(
                    this, NOTIFICATION_ID, getServiceNotification(), type);
        } catch (Exception e) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
                    && e instanceof ForegroundServiceStartNotAllowedException) {
                // App not in a valid state to start foreground service
                Timber.e("ForegroundServiceStartNotAllowedException");
                destroyService();
            }
        }
    }

    @Override public void onDestroy() {
        super.onDestroy();
        Timber.d("onDestroy");
        try {
            if (mScreenReceiver != null) {
                unregisterReceiver(mScreenReceiver);
            }
        } catch (IllegalArgumentException e) {}
        destroyService();
    }

    private boolean shouldDestroyService() {
        return ! settingsManagerInstance.getPowerButtonServiceEnabled()
            && ! settingsManagerInstance.isWatchEnabled();
    }

    private void destroyService() {
        notificationManager.cancel(NOTIFICATION_ID);
        stopForeground(true);
        stopSelf();
    }

    @TargetApi(Build.VERSION_CODES.O)
    public void vibrateOnce(long duration) {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            vibrator.vibrate(
                    VibrationEffect.createOneShot(duration, getAmplitude()));
        } else {
            vibrator.vibrate(duration);
        }
    }

    private int getAmplitude() {
        return settingsManagerInstance.getMaxStrengthVibrationsEnabled()
            ? AMPLITUDE_MAX : AMPLITUDE_DEFAULT;
    }


    /**
     * vibration pattern functions
     */

    @TargetApi(Build.VERSION_CODES.O)
    private void vibrateTime(boolean announcementVibration, boolean minutesOnly) {
        // get current time
        int hours, minutes;
        Calendar c = Calendar.getInstance();
        if (settingsManagerInstance.getHourFormat() == HourFormat.TWELVE_HOURS) {
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
        // announcement vibration
        if (announcementVibration) {
            pattern = concat(pattern, new long[]{ERROR_VIBRATION, LONG_GAP});
        }
        // hours and minutes
        if (settingsManagerInstance.getTimeComponentOrder() == TimeComponentOrder.MINUTES_HOURS) {
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

        // total duration
        long totalDuration = 0l;
        for (long duration : pattern) {
            totalDuration += duration;
        }

        // start vibration
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            int[] amplitudes = new int[pattern.length];
            for (int i=0; i<amplitudes.length; i++) {
                amplitudes[i] = i % 2 == 0 ? 0 : getAmplitude();
            }
            vibrator.vibrate(
                    VibrationEffect.createWaveform(pattern, amplitudes, -1));
        } else {
            vibrator.vibrate(pattern, -1);
        }

        // send vibration finished broadcast
        new Handler(Looper.getMainLooper()).postDelayed(
                new Runnable() {
                    @Override public void run() {
                        LocalBroadcastManager.getInstance(TactileClockService.this)
                            .sendBroadcast(new Intent(VIBRATION_FINISHED));
                    }
                }, totalDuration + 500l);
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
    private static final int NOTIFICATION_ID = 91223;

    private Notification getServiceNotification() {
        // launch MainActivity intent
        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                this, 0, notificationIntent, getPendingIntentFlags());
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
            .setChannelId(ApplicationInstance.NOTIFICATION_CHANNEL_ID)
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

    @SuppressLint("Deprecation, UnspecifiedImmutableFlag")
    private static int getPendingIntentFlags() {
        return android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M
            ? PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
            : PendingIntent.FLAG_UPDATE_CURRENT;
    }


    /**
     * do not desturb and active call
     */

    @TargetApi(Build.VERSION_CODES.M)
    private boolean isVibrationAllowed() {
        // do not desturb
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            if (notificationManager.getCurrentInterruptionFilter() != NotificationManager.INTERRUPTION_FILTER_ALL) {
                return false;
            }
        }
        // active call
        if (audioManager.getMode() != AudioManager.MODE_NORMAL) {
            return false;
        }
        // else allow
        return true;
    }

}
