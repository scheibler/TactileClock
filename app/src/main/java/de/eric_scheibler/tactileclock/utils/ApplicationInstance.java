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

import android.support.v4.content.ContextCompat;

import de.eric_scheibler.tactileclock.R;


public class ApplicationInstance extends Application {

    private AlarmManager alarmManager;

    @Override public void onCreate() {
        super.onCreate();
        alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        // set notification channel for android 8
        createNotificationChannel();
        // settings manager instance
        SettingsManager settingsManagerInstance = SettingsManager.getInstance(this);
        // update notification
        if (settingsManagerInstance.getPowerButtonServiceEnabled()
                || settingsManagerInstance.isWatchEnabled()) {
            updateServiceNotification();
        }
        // restore alarms from previous run
        boolean wasWatchEnabled = settingsManagerInstance.isWatchEnabled();
        settingsManagerInstance.disableWatch();
        if (wasWatchEnabled) {
            settingsManagerInstance.enableWatch();
        }
    }


    /**
     * notifications
     */

    @TargetApi(Build.VERSION_CODES.O)
    private void createNotificationChannel() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            NotificationChannel notificationChannel = new NotificationChannel(
                    Constants.ID.NOTIFICATION_CHANNEL_ID,
                    getResources().getString(R.string.app_name),
                    NotificationManager.IMPORTANCE_LOW);
            notificationChannel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);
            notificationChannel.setShowBadge(true);
            ((NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE))
                .createNotificationChannel(notificationChannel);
        }
    }

    public void updateServiceNotification() {
        Intent updateServiceNotificationIntent = new Intent(this, TactileClockService.class);
        updateServiceNotificationIntent.setAction(Constants.CustomAction.UPDATE_SERVICE_NOTIFICATION);
        ContextCompat.startForegroundService(this, updateServiceNotificationIntent);
    }


    /**
     * alarm manager
     */

    @TargetApi(Build.VERSION_CODES.KITKAT)
    public void setAlarm(long millisSinceDeviceStartup, PendingIntent pendingIntent) {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT) {
            alarmManager.setExact(
                    AlarmManager.ELAPSED_REALTIME_WAKEUP, millisSinceDeviceStartup, pendingIntent);
        } else {
            alarmManager.set(
                    AlarmManager.ELAPSED_REALTIME_WAKEUP, millisSinceDeviceStartup, pendingIntent);
        }
    }

    public void cancelAlarm(PendingIntent pendingIntent) {
        alarmManager.cancel(pendingIntent);
        pendingIntent.cancel();
    }

}
