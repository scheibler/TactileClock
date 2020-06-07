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



public class ApplicationInstance extends Application {

    private AlarmManager alarmManager;

    @Override public void onCreate() {
        super.onCreate();
        alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        // app context
        this.applicationInstance = this;
        // debug message initialization
        if (BuildConfig.DEBUG) {
            Timber.plant(new Timber.DebugTree());
        }
        // set notification channel for android 8
        createNotificationChannel();
        // restore alarms from previous run
        SettingsManager settingsManagerInstance = new SettingsManager();
        boolean wasWatchEnabled = settingsManagerInstance.isWatchEnabled();
        settingsManagerInstance.disableWatch();
        if (wasWatchEnabled) {
            settingsManagerInstance.enableWatch();
        }
        // update notivication
        Intent updateNotificationIntent = new Intent(ApplicationInstance.this, TactileClockService.class);
        updateNotificationIntent.setAction(TactileClockService.ACTION_UPDATE_NOTIFICATION);
        ContextCompat.startForegroundService(ApplicationInstance.this, updateNotificationIntent);
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
            notificationChannel.setShowBadge(true);
            ((NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE))
                .createNotificationChannel(notificationChannel);
        }
    }


    /**
     * alarm manager
     */
    private static final int PENDING_INTENT_VIBRATE_TIME_ID = 39128;

    @TargetApi(Build.VERSION_CODES.KITKAT)
    public void setAlarm(long millisSinceDeviceStartup) {
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
                PendingIntent.FLAG_CANCEL_CURRENT);
    }

}
