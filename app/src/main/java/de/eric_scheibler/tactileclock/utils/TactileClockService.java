package de.eric_scheibler.tactileclock.utils;


import androidx.annotation.RequiresApi;
import android.os.Build;
import android.os.IBinder;
import android.os.Handler;
import android.os.Looper;

import android.annotation.SuppressLint;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

import android.media.AudioManager;
import android.media.MediaPlayer;

import de.eric_scheibler.tactileclock.R;
import de.eric_scheibler.tactileclock.data.HourFormat;
import de.eric_scheibler.tactileclock.data.TimeComponentOrder;
import de.eric_scheibler.tactileclock.ui.activity.MainActivity;

import java.util.Calendar;
import timber.log.Timber;
import android.content.pm.ServiceInfo;
import androidx.core.app.ServiceCompat;
import androidx.core.app.NotificationCompat;
import android.app.ForegroundServiceStartNotAllowedException;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.core.content.ContextCompat;
import android.Manifest;
import android.content.pm.PackageManager;
import android.media.AudioFocusRequest;
import android.media.AudioAttributes;


public class TactileClockService extends Service {

    public enum ScreenOffOn {
        DO_NOTHING, ERROR_VIBRATION, VIBRATE_TIME
    }

    // actions
    public static final String ACTION_UPDATE_NOTIFICATION = "de.eric_scheibler.tactileclock.action.update_notification";
    public static final String ACTION_DISABLE_WATCH_FROM_NOTIFICATION = "de.eric_scheibler.tactileclock.action.disableWatchFromNotification";
    public static final String ACTION_VIBRATE_TIME = "de.eric_scheibler.tactileclock.action.vibrate_time";
    public static final String ACTION_VIBRATE_TEST_TIME = "de.eric_scheibler.tactileclock.action.vibrate_test_time";
    public static final String ACTION_VIBRATE_TIME_AND_SET_NEXT_ALARM = "de.eric_scheibler.tactileclock.action.vibrate_time_and_set_next_alarm";
    public static final String ACTION_PLAY_GTS = "de.eric_scheibler.tactileclock.action.play_gts";

    // broadcast responses
    public static final String VIBRATION_FINISHED = "de.eric_scheibler.tactileclock.response.vibration_finished";
    public static final String UPDATE_WATCH_UI = "de.eric_scheibler.tactileclock.response.update_watch_ui";

    // testing
    public static int TEST_HOUR;
    public static int TEST_MINUTE;

    // service vars
    private long lastActivation;
    private ApplicationInstance applicationInstance;
    private AudioManager audioManager;
    private NotificationManager notificationManager;
    private ScreenReceiver mScreenReceiver;
    private SettingsManager settingsManagerInstance;
    private Handler handler;

    @Override public void onCreate() {
        super.onCreate();
        lastActivation = 0l;
        Timber.d("onCreate");
        applicationInstance = (ApplicationInstance) ApplicationInstance.getContext();
        audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        settingsManagerInstance = SettingsManager.getInstance();
        handler = new Handler(Looper.getMainLooper());

        // register receiver that handles screen on and screen off logic
        // can't be done in manifest
        mScreenReceiver = new ScreenReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_SCREEN_ON);
        filter.addAction(Intent.ACTION_SCREEN_OFF);
        filter.addAction(Intent.ACTION_BOOT_COMPLETED);
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

            } else if (ACTION_DISABLE_WATCH_FROM_NOTIFICATION.equals(intent.getAction())) {
                if (settingsManagerInstance.isWatchEnabled()) {
                    settingsManagerInstance.disableWatch();
                    LocalBroadcastManager.getInstance(TactileClockService.this)
                        .sendBroadcast(new Intent(UPDATE_WATCH_UI));
                }

            // next actions were triggered by the user and need an initialization delay (most important for screen reader talkback)

            } else if (Intent.ACTION_SCREEN_ON.equals(intent.getAction())) {
                // vibration will be canceled if screen is turned on while vibrating
                Helper.cancelVibration();

                long activationTimeDifference = System.currentTimeMillis() - lastActivation;
                Timber.d("diff: %1$d = %2$d - %3$d", activationTimeDifference, System.currentTimeMillis(), lastActivation);
                if (settingsManagerInstance.getPowerButtonServiceEnabled()
                        && activationTimeDifference > settingsManagerInstance.getPowerButtonLowerErrorBoundary()
                        && activationTimeDifference < settingsManagerInstance.getPowerButtonUpperErrorBoundary()) {
                    // double click detected
                    // but screen was turned off and on instead of on and off
                    //
                    // short delay due to display=on and Talkback (see SettingsActivity.java:buttonTest for details)
                    switch (settingsManagerInstance.getScreenOffOnAction()) {
                        case ERROR_VIBRATION:
                            handler.postDelayed(() -> {
                                Helper.vibrateOnce(settingsManagerInstance.getLongVibration() * 2);
                            }, getInitializationDelay());
                            break;
                        case VIBRATE_TIME:
                            handler.postDelayed(() -> {
                                vibrateTime(false, false, false);
                            }, getInitializationDelay());
                            break;
                    }
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
                    // important exception: vibrate time doesn't need a delay here, because display=off and therefore Talkback doesn't consume the vibration
                    vibrateTime(false, false, false);
                }
                lastActivation = System.currentTimeMillis();

            } else if (ACTION_VIBRATE_TIME.equals(intent.getAction())) {
                handler.postDelayed(() -> {
                    vibrateTime(false, false, false);
                }, getInitializationDelay());

            } else if (ACTION_VIBRATE_TEST_TIME.equals(intent.getAction())) {
                handler.postDelayed(() -> {
                    vibrateTime(false, false, true);
                }, getInitializationDelay());

            // next actions were triggered by an alarm and need no initialization delay

            } else if (ACTION_VIBRATE_TIME_AND_SET_NEXT_ALARM.equals(intent.getAction())) {
                // vibrate current time
                if (this.isVibrationAllowed()) {
                    vibrateTime(
                            settingsManagerInstance.getWatchAnnouncementVibration(),
                            settingsManagerInstance.getWatchOnlyVibrateMinutes(),
                            false);
                }

                // set next alarm
                applicationInstance.setAlarmAtFullMinute(
                        settingsManagerInstance.getWatchVibrationIntervalInMinutes());

            } else if (ACTION_PLAY_GTS.equals(intent.getAction())) {
                // Play sound
                if (audioManager.isMusicActive()
                        ? settingsManagerInstance.getPlayGTSWhileMusic()
                        : this.isSoundAllowed()) {
                    if (tryToGetAudioFocus()) {
                        MediaPlayer mediaPlayer = MediaPlayer.create(this, R.raw.pips);
                        mediaPlayer.setOnCompletionListener(mp -> {
                            giveUpAudioFocus();
                            mp.release();
                        });
                        mediaPlayer.start();
                    }
                }

                // set next alarm
                applicationInstance.setAlarmForGTS();
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

    private long getInitializationDelay() {
        // requires a short delay
        // otherwise Talkback consumes the time vibration if Talkbacks vibration feedback is enabled
        return Helper.isScreenReaderEnabled() ? 250 : 50;
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


    /**
     * vibration pattern functions
     */
    private void vibrateTime(boolean announcementVibration, boolean minutesOnly, boolean testTime) {
        int hours = testTime ? TEST_HOUR : Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
        int minutes = testTime ? TEST_MINUTE : Calendar.getInstance().get(Calendar.MINUTE);

        // Convert to 12 hour format
        if (settingsManagerInstance.getHourFormat() == HourFormat.TWELVE_HOURS && hours > 12)
            hours -= 12;

        // vibrate time
        vibrateTime(announcementVibration, minutesOnly, hours, minutes);
    }

    private void vibrateTime(boolean announcementVibration, boolean minutesOnly, int hours, int minutes) {
        long MEDIUM_GAP = settingsManagerInstance.getMediumGap();
        long LONG_GAP = settingsManagerInstance.getLongGap();

        // Round minutes to the nearest multiple of 5 using integer arithmetic.
        // (minutes + 2) shifts the value so integer division truncates correctly.
        if (settingsManagerInstance.getWatchRoundMinutes())
            minutes = (minutes + 2) / 5 * 5;

        // Increase hour if minutes rounded to 60
        if (minutes == 60){
            minutes = 0;
            hours++;
        }

        // create vibration pattern
        //
        // start with a medium gap
        long[] pattern = {MEDIUM_GAP};

        // announcement vibration
        if (announcementVibration) {
            pattern = concat(
                    pattern,
                    new long[]{settingsManagerInstance.getLongVibration() * 2, LONG_GAP});
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
        Helper.vibratePattern(pattern);

        // send vibration finished broadcast
        handler.postDelayed(() -> {
            LocalBroadcastManager.getInstance(TactileClockService.this)
                .sendBroadcast(new Intent(VIBRATION_FINISHED));
        }, totalDuration + 500l);
    }

    private long[] getVibrationPatternForHours(int hours) {
        long MEDIUM_GAP = settingsManagerInstance.getMediumGap();

        long[] pattern = new long[]{};
        // only add first digit of hour if it's bigger than 9 (Remove leading 0)
        if (hours > 9) {
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
        long MEDIUM_GAP = settingsManagerInstance.getMediumGap();

        long[] pattern = new long[]{};
        if (minutes > 9) {
            // First number of minute
            pattern = concat(pattern, getVibrationPatternForDigit(minutes / 10));
            // medium gap between first and second number of minutes
            pattern = concat(pattern, new long[]{MEDIUM_GAP});
        }
        // second number of minute
        pattern = concat(pattern, getVibrationPatternForDigit(minutes%10));
        return pattern;
    }

    private long[] getVibrationPatternForDigit(int digit) {
        long DOT = settingsManagerInstance.getShortVibration();
        long DASH = settingsManagerInstance.getLongVibration();
        long GAP = settingsManagerInstance.getShortGap();

        switch (digit) {
            case 0: return new long[]{DASH, GAP, DASH};
            case 1: return new long[]{DOT};
            case 2: return new long[]{DOT, GAP, DOT};
            case 3: return new long[]{DOT, GAP, DOT, GAP, DOT};
            case 4: return new long[]{DOT, GAP, DOT, GAP, DOT, GAP, DOT};
            case 5: return new long[]{DASH};
            case 6: return new long[]{DASH, GAP, DOT};
            case 7: return new long[]{DASH, GAP, DOT, GAP, DOT};
            case 8: return new long[]{DASH, GAP, DOT, GAP, DOT, GAP, DOT};
            case 9: return new long[]{DASH, GAP, DOT, GAP, DOT, GAP, DOT, GAP, DOT};
            default: return new long[]{};
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

    public static boolean hasPostNotificationsPermission() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU
            ? hasPostNotificationsPermissionForTiramisuAndNewer()
            : true;
    }

    @RequiresApi(api = Build.VERSION_CODES.TIRAMISU)
    private static boolean hasPostNotificationsPermissionForTiramisuAndNewer() {
        return ContextCompat.checkSelfPermission(ApplicationInstance.getContext(), Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED;
    }

    private Notification getServiceNotification() {
        // launch MainActivity intent
        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                this, 0, notificationIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

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

        // build notification
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this)
            .setChannelId(ApplicationInstance.NOTIFICATION_CHANNEL_ID)
            .setVisibility(NotificationCompat.VISIBILITY_SECRET)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setShowWhen(false)
            .setUsesChronometer(false)
            .setOngoing(true)
            .setContentIntent(pendingIntent)
            .setSmallIcon(R.drawable.ic_launcher)
            .setContentText(notificationMessage);

        // add disable watch action
        if (settingsManagerInstance.isWatchEnabled()) {
            Intent disableWatchFromNotificationIntent = new Intent(this, TactileClockService.class);
            disableWatchFromNotificationIntent.setAction(ACTION_DISABLE_WATCH_FROM_NOTIFICATION);

            PendingIntent pDeleteIntent = PendingIntent.getService(
                    this, 0, disableWatchFromNotificationIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

            PendingIntent pDisableWatchFromNotificationIntent = PendingIntent.getService(
                    this, 1, disableWatchFromNotificationIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

            notificationBuilder
                .setDeleteIntent(pDeleteIntent)
                .addAction(
                        new NotificationCompat.Action.Builder(
                            R.drawable.ic_stop,
                            getResources().getString(R.string.serviceNotificationActionStopWatch),
                            pDisableWatchFromNotificationIntent)
                        .build());
        }

        return notificationBuilder.build();
    }

    private String enabledOrDisabled(boolean enabled) {
        if (enabled) {
            return getResources().getString(R.string.dialogEnabled);
        }
        return getResources().getString(R.string.dialogDisabled);
    }


    /**
     * is vibration / sound allowed
     * check do not disturb, silent mode and active call settings
     */

    private boolean isVibrationAllowed() {
        // do not disturb
        if (isDoNotDisturbEnabled(notificationManager)) {
            return false;
        }

        // active call
        if (isActiveCall(audioManager)) {
            return false;
        }

        // ringer mode
        switch (audioManager.getRingerMode()) {
            case AudioManager.RINGER_MODE_SILENT:
                return false;
            // else allow (includes RINGER_MODE_VIBRATE and RINGER_MODE_NORMAL)
        }

        return true;
    }

    private boolean isSoundAllowed() {
        // do not disturb
        if (isDoNotDisturbEnabled(notificationManager)) {
            return false;
        }

        // active call
        if (isActiveCall(audioManager)) {
            return false;
        }

        // check ringer mode
        switch (audioManager.getRingerMode()) {
            case AudioManager.RINGER_MODE_SILENT:
            case AudioManager.RINGER_MODE_VIBRATE:
                return false;
            // else allow (only RINGER_MODE_NORMAL)
        }

        return true;
    }

    public static boolean isDoNotDisturbEnabled(NotificationManager notificationManager) {
        return notificationManager.getCurrentInterruptionFilter() != NotificationManager.INTERRUPTION_FILTER_ALL;
    }

    public static boolean isActiveCall(AudioManager audioManager) {
        return audioManager.getMode() != AudioManager.MODE_NORMAL;
    }


    /**
     * audio focus
     */

    private boolean tryToGetAudioFocus() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            return audioManager.requestAudioFocus(buildAudioFocusRequestForOAndNewer()) == AudioManager.AUDIOFOCUS_REQUEST_GRANTED;
        }
        return true;
    }

    private void giveUpAudioFocus() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            audioManager.abandonAudioFocusRequest(buildAudioFocusRequestForOAndNewer());
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private AudioFocusRequest buildAudioFocusRequestForOAndNewer() {
        return new AudioFocusRequest.Builder(
                AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK)
            .setAudioAttributes(buildAudioAttributes())
            .setAcceptsDelayedFocusGain(false)
            .build();
    }

    private AudioAttributes buildAudioAttributes() {
        return new AudioAttributes.Builder()
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .setUsage(AudioAttributes.USAGE_ALARM)
            .build();
    }

}
