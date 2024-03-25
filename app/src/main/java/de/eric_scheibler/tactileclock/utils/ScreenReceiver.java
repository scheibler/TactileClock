package de.eric_scheibler.tactileclock.utils;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import androidx.core.content.ContextCompat;
import timber.log.Timber;


public class ScreenReceiver extends BroadcastReceiver {

    @Override public void onReceive(Context context, Intent intent) {
        Timber.d("action = %1$s", intent.getAction());
        if (intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED)) {
            Intent updateNotificationIntent = new Intent(context, TactileClockService.class);
            updateNotificationIntent.setAction(TactileClockService.ACTION_UPDATE_NOTIFICATION);
            ContextCompat.startForegroundService(context, updateNotificationIntent);
        } else if (intent.getAction().equals(Intent.ACTION_SCREEN_ON)
                || intent.getAction().equals(Intent.ACTION_SCREEN_OFF)) {
            Intent screenOnOffIntent = new Intent(context, TactileClockService.class);
            screenOnOffIntent.setAction(intent.getAction());
            ContextCompat.startForegroundService(context, screenOnOffIntent);
        }
    }

}
