package de.eric_scheibler.tactileclock.utils;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import android.support.v4.content.ContextCompat;


public class ScreenReceiver extends BroadcastReceiver {

    @Override public void onReceive(Context context, Intent intent) {
        SettingsManager settingsManagerInstance = SettingsManager.getInstance(context);
        if (intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED)
                && (
                           settingsManagerInstance.getPowerButtonServiceEnabled()
                        || settingsManagerInstance.isWatchEnabled())
                ) {
            Intent updateServiceNotificationIntent = new Intent(context, TactileClockService.class);
            updateServiceNotificationIntent.setAction(Constants.CustomAction.UPDATE_SERVICE_NOTIFICATION);
            ContextCompat.startForegroundService(context, updateServiceNotificationIntent);
        } else if (intent.getAction().equals(Intent.ACTION_SCREEN_ON)
                || intent.getAction().equals(Intent.ACTION_SCREEN_OFF)) {
            Intent screenOnOffIntent = new Intent(context, TactileClockService.class);
            screenOnOffIntent.setAction(intent.getAction());
            ContextCompat.startForegroundService(context, screenOnOffIntent);
        }
    }

}
