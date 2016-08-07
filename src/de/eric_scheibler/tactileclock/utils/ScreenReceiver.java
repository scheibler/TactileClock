package de.eric_scheibler.tactileclock.utils;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class ScreenReceiver extends BroadcastReceiver {

    @Override public void onReceive(Context context, Intent intent) {
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);
        if (settings.getBoolean(Constants.SETTINGS.ENABLE_SERVICE, true)
                && (
                       intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED)
                    || intent.getAction().equals(Intent.ACTION_SCREEN_ON)
                    || intent.getAction().equals(Intent.ACTION_SCREEN_OFF))
                ) {
            Intent i = new Intent(context, TactileClockService.class);
            if (intent.getAction().equals(Intent.ACTION_SCREEN_ON)) {
                i.putExtra("screenOn", true);
            } else {
                i.putExtra("screenOn", false);
            }
            context.startService(i);
        }
    }

}