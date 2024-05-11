package de.eric_scheibler.tactileclock.utils;

import android.annotation.TargetApi;

import android.app.AlarmManager;
import android.app.Application;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;

import android.content.Context;
import android.content.Intent;

import android.os.Build;

import androidx.core.content.ContextCompat;

import timber.log.Timber;

import de.eric_scheibler.tactileclock.BuildConfig;
import de.eric_scheibler.tactileclock.R;
import android.annotation.SuppressLint;
import android.os.Handler;
import java.lang.Runnable;
import java.util.Calendar;
import android.os.SystemClock;



public class ApplicationInstance extends Application {

    private AlarmManager alarmManager;

    @Override public void onCreate() {
        super.onCreate();
        alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        createNotificationChannel();
        // app context
        this.applicationInstance = this;
        // debug message initialization
        if (BuildConfig.DEBUG) {
            Timber.plant(new Timber.DebugTree());
        }

        // restore alarms from previous run
        // wait for a few seconds to prevent ForegroundServiceStartNotAllowedException after boot completed
        new Handler().postDelayed(new Runnable() {
            @Override public void run() {
                SettingsManager settingsManagerInstance = new SettingsManager();
                boolean wasWatchEnabled = settingsManagerInstance.isWatchEnabled();
                settingsManagerInstance.disableWatch();
                if (wasWatchEnabled
                        && ApplicationInstance.canScheduleExactAlarms()) {
                    settingsManagerInstance.enableWatch();
                }
                // update notivication
                Intent updateNotificationIntent = new Intent(ApplicationInstance.this, TactileClockService.class);
                updateNotificationIntent.setAction(TactileClockService.ACTION_UPDATE_NOTIFICATION);
                ContextCompat.startForegroundService(ApplicationInstance.this, updateNotificationIntent);
            }
        }, 5000);
    }


    /**
     * application context
     */
    private static ApplicationInstance applicationInstance;

    public static Context getContext() {
        return applicationInstance;
    }

    public static String getResourceString(int resourceId) {
        return getContext().getResources().getString(resourceId);
    }


    /**
     * notifications
     */
    public static final String NOTIFICATION_CHANNEL_ID = BuildConfig.APPLICATION_ID;

    @TargetApi(Build.VERSION_CODES.O)
    private void createNotificationChannel() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            NotificationChannel notificationChannel = new NotificationChannel(
                    NOTIFICATION_CHANNEL_ID,
                    getResources().getString(R.string.app_name),
                    NotificationManager.IMPORTANCE_LOW);
            notificationChannel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);
            notificationChannel.setShowBadge(false);
            ((NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE))
                .createNotificationChannel(notificationChannel);
        }
    }


    /**
     * alarm manager
     */
    private static final int PENDING_INTENT_VIBRATE_TIME_ID = 39128;

    /**
     * SCHEDULE_EXACT_ALARM permission
     * only required for android 12 (api 31 / S)
     * on Android 13 onwards the implicitly granted permission USE_EXACT_ALARM is used and canScheduleExactAlarms() is always true
     */
    @TargetApi(Build.VERSION_CODES.S)
    public static boolean canScheduleExactAlarms() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            return ((AlarmManager) ApplicationInstance.getContext().getSystemService(Context.ALARM_SERVICE)).canScheduleExactAlarms();
        }
        return true;
    }

    public boolean setAlarmAtFullHour(int hours) {
        Calendar calendar = Calendar.getInstance();
        // at full hour
        calendar.setTimeInMillis(
                System.currentTimeMillis() + hours*60*60*1000l);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        return setAlarm(calendar);
    }

    public boolean setAlarmAtFullMinute(int minutes) {
        Calendar calendar = Calendar.getInstance();
        // at full minute
        calendar.setTimeInMillis(
                System.currentTimeMillis() + minutes*60*1000l);
        calendar.set(Calendar.SECOND, 0);
        return setAlarm(calendar);
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    @SuppressLint("MissingPermission")
    private boolean setAlarm(Calendar calendar) {
        if (! canScheduleExactAlarms()) {
            return false;
        }

        long millisSinceDeviceStartup = SystemClock.elapsedRealtime()
            + Math.abs(calendar.getTimeInMillis() - System.currentTimeMillis());

        // create vibrate time pending intent
        PendingIntent pendingIntent = createActionVibrateTimeAndSetNextAlarmPendingIntent();

        // set alarm
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.ELAPSED_REALTIME_WAKEUP, millisSinceDeviceStartup, pendingIntent);
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            alarmManager.setExact(
                    AlarmManager.ELAPSED_REALTIME_WAKEUP, millisSinceDeviceStartup, pendingIntent);
        } else {
            alarmManager.set(
                    AlarmManager.ELAPSED_REALTIME_WAKEUP, millisSinceDeviceStartup, pendingIntent);
        }
        return true;
    }

    public void cancelAlarm() {
        // create vibrate time pending intent
        PendingIntent pendingIntent = createActionVibrateTimeAndSetNextAlarmPendingIntent();
        // cancel
        alarmManager.cancel(pendingIntent);
        pendingIntent.cancel();
    }

    private PendingIntent createActionVibrateTimeAndSetNextAlarmPendingIntent() {
        Intent intent = new Intent(this, TactileClockService.class);
        intent.setAction(TactileClockService.ACTION_VIBRATE_TIME_AND_SET_NEXT_ALARM);
        intent.addFlags(Intent.FLAG_RECEIVER_FOREGROUND);
        return PendingIntent.getService(
                this,
                PENDING_INTENT_VIBRATE_TIME_ID,
                intent,
                getPendingIntentFlags());
    }

    @SuppressLint("Deprecation, UnspecifiedImmutableFlag")
    private static int getPendingIntentFlags() {
        return android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M
            ? PendingIntent.FLAG_CANCEL_CURRENT | PendingIntent.FLAG_IMMUTABLE
            : PendingIntent.FLAG_CANCEL_CURRENT;
    }

}
