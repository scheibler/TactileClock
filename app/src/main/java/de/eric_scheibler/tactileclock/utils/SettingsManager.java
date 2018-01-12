package de.eric_scheibler.tactileclock.utils;

import android.app.AlarmManager;
import android.app.PendingIntent;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

import android.os.SystemClock;

import android.preference.PreferenceManager;


import android.text.format.DateFormat;

import java.lang.Math;

import java.util.Calendar;


public class SettingsManager {

	// static settings
    // general settings
    public static final boolean DEFAULT_FIRST_START = true;
    public static final int DEFAULT_RECENT_OPEN_TAB = 0;
    // power button
    public static final boolean DEFAULT_POWER_BUTTON_SERVICE_ENABLED = true;
    public static final boolean DEFAULT_POWER_BUTTON_ERROR_VIBRATION = true;
    // double click parameters
    public static final long DEFAULT_POWER_BUTTON_LOWER_ERROR_BOUNDARY = 50;
    public static final long DEFAULT_POWER_BUTTON_UPPER_ERROR_BOUNDARY = 500;
    public static final long DEFAULT_POWER_BUTTON_LOWER_SUCCESS_BOUNDARY = 50;
    public static final long DEFAULT_POWER_BUTTON_UPPER_SUCCESS_BOUNDARY = 1500;
    // watch
    public static final boolean DEFAULT_WATCH_ENABLED = false;
    public static final boolean DEFAULT_WATCH_AUTO_SWITCH_OFF_ENABLED = false;
    public static final int DEFAULT_WATCH_VIBRATION_INTERVAL = 1;
    public static final boolean DEFAULT_WATCH_ONLY_VIBRATE_MINUTES = false;
    public static final boolean DEFAULT_WATCH_START_AT_NEXT_FULL_HOUR = false;

	// class variables
	private static SettingsManager settingsManagerInstance;
	private Context context;
    private ApplicationInstance applicationInstance;
	private SharedPreferences settings;

    public static SettingsManager getInstance(Context context) {
        if (settingsManagerInstance == null) {
            settingsManagerInstance = new SettingsManager(
                    context.getApplicationContext());
        }
        return settingsManagerInstance;
    }

	private SettingsManager(Context context) {
		this.context = context;
        this.applicationInstance = (ApplicationInstance) context.getApplicationContext();
		this.settings = PreferenceManager.getDefaultSharedPreferences(context);
        // delete deprecated keys from preferences
        if (settings.contains("24HourFormat")) {
            Editor editor = settings.edit();
            editor.remove("24HourFormat");
            editor.apply();
        }
        if (settings.contains("timeFormat")) {
            Editor editor = settings.edit();
            editor.remove("timeFormat");
            editor.apply();
        }
	}

    public String getApplicationVersion() {
        try {
            return context.getPackageManager().getPackageInfo(
                    context.getPackageName(), 0).versionName;
        } catch (NameNotFoundException e) {
            return "";
        }
    }

    public boolean getFirstStart() {
        return settings.getBoolean(
                Constants.SETTINGS_KEY.FIRST_START,
                DEFAULT_FIRST_START);
    }

    public void setFirstStart(boolean firstStart) {
        Editor editor = settings.edit();
        editor.putBoolean(Constants.SETTINGS_KEY.FIRST_START, firstStart);
        editor.apply();
    }

    public int getRecentOpenTab() {
        return settings.getInt(
                Constants.SETTINGS_KEY.RECENT_OPEN_TAB,
                DEFAULT_RECENT_OPEN_TAB);
    }

    public void setRecentOpenTab(int tabNr) {
        Editor editor = settings.edit();
        editor.putInt(Constants.SETTINGS_KEY.RECENT_OPEN_TAB, tabNr);
        editor.apply();
    }

    public Constants.HourFormat getHourFormat() {
        return Constants.HourFormat.lookupByCode(
                settings.getString(
                    Constants.SETTINGS_KEY.HOUR_FORMAT,
                    DateFormat.is24HourFormat(context)
                        ? Constants.HourFormat.TWENTYFOUR_HOURS.getCode()
                        : Constants.HourFormat.TWELVE_HOURS.getCode())
                );
    }

    public void setHourFormat(Constants.HourFormat hourFormat) {
        Editor editor = settings.edit();
        editor.putString(
                Constants.SETTINGS_KEY.HOUR_FORMAT, hourFormat.getCode());
        editor.apply();
    }

    public Constants.TimeComponentOrder getTimeComponentOrder() {
        return Constants.TimeComponentOrder.lookupByCode(
                settings.getString(
                    Constants.SETTINGS_KEY.TIME_COMPONENT_ORDER,
                    Constants.TimeComponentOrder.HOURS_MINUTES.getCode())
                );
    }

    public void setTimeComponentOrder(Constants.TimeComponentOrder timeComponentOrder) {
        Editor editor = settings.edit();
        editor.putString(
                Constants.SETTINGS_KEY.TIME_COMPONENT_ORDER, timeComponentOrder.getCode());
        editor.apply();
    }


    /**
     * background service
     */

    public boolean getPowerButtonServiceEnabled() {
        return settings.getBoolean(
                Constants.SETTINGS_KEY.POWER_BUTTON_SERVICE_ENABLED,
                DEFAULT_POWER_BUTTON_SERVICE_ENABLED);
    }

    public void setPowerButtonServiceEnabled(boolean enabled) {
        Editor editor = settings.edit();
        editor.putBoolean(Constants.SETTINGS_KEY.POWER_BUTTON_SERVICE_ENABLED, enabled);
        editor.apply();
        applicationInstance.updateServiceNotification();
    }

    public boolean getPowerButtonErrorVibration() {
        return settings.getBoolean(
                Constants.SETTINGS_KEY.POWER_BUTTON_ERROR_VIBRATION,
                DEFAULT_POWER_BUTTON_ERROR_VIBRATION);
    }

    public void setPowerButtonErrorVibration(boolean enabled) {
        Editor editor = settings.edit();
        editor.putBoolean(Constants.SETTINGS_KEY.POWER_BUTTON_ERROR_VIBRATION, enabled);
        editor.apply();
    }

    public long getPowerButtonLowerErrorBoundary() {
        return DEFAULT_POWER_BUTTON_LOWER_ERROR_BOUNDARY;
    }

    public long getPowerButtonUpperErrorBoundary() {
        return DEFAULT_POWER_BUTTON_UPPER_ERROR_BOUNDARY;
    }

    public long getPowerButtonLowerSuccessBoundary() {
        return settings.getLong(
                Constants.SETTINGS_KEY.POWER_BUTTON_LOWER_SUCCESS_BOUNDARY,
                DEFAULT_POWER_BUTTON_LOWER_SUCCESS_BOUNDARY);
    }

    public void setPowerButtonLowerSuccessBoundary(long boundary) {
        Editor editor = settings.edit();
        editor.putLong(
                Constants.SETTINGS_KEY.POWER_BUTTON_LOWER_SUCCESS_BOUNDARY, boundary);
        editor.apply();
    }

    public long getPowerButtonUpperSuccessBoundary() {
        return settings.getLong(
                Constants.SETTINGS_KEY.POWER_BUTTON_UPPER_SUCCESS_BOUNDARY,
                DEFAULT_POWER_BUTTON_UPPER_SUCCESS_BOUNDARY);
    }

    public void setPowerButtonUpperSuccessBoundary(long boundary) {
        Editor editor = settings.edit();
        editor.putLong(
                Constants.SETTINGS_KEY.POWER_BUTTON_UPPER_SUCCESS_BOUNDARY, boundary);
        editor.apply();
    }


    /**
     * watch
     */

    public boolean isWatchEnabled() {
        return settings.getBoolean(
                Constants.SETTINGS_KEY.WATCH_ENABLED,
                DEFAULT_WATCH_ENABLED);
    }

    public void enableWatch() {
        // save setting
        Editor editor = settings.edit();
        editor.putBoolean(
                Constants.SETTINGS_KEY.WATCH_ENABLED, true);
        editor.apply();
        applicationInstance.updateServiceNotification();

        // handle vibrate time pending intent
        Intent intentStartTimeVibration = new Intent(context, TactileClockService.class);
        intentStartTimeVibration.setAction(Constants.CustomAction.WATCH_VIBRATE);
        PendingIntent pendingIntentStartTimeVibration = PendingIntent.getService(
                context, Constants.ID.PENDING_INTENT_VIBRATE_TIME_ID, intentStartTimeVibration, PendingIntent.FLAG_CANCEL_CURRENT);
        // set first exact watch vibration alarm
        Calendar calendar = Calendar.getInstance();
        if (this.getWatchStartAtNextFullHour()) {
            // at next full hour
            calendar.setTimeInMillis(System.currentTimeMillis() + 60*60*1000l);
            calendar.set(Calendar.MINUTE, 0);
            calendar.set(Calendar.SECOND, 0);
        } else {
            // at next full minute
            calendar.setTimeInMillis(System.currentTimeMillis() + 60*1000l);
            calendar.set(Calendar.SECOND, 0);
        }
        applicationInstance.setAlarm(
                SystemClock.elapsedRealtime()
                    + Math.abs(calendar.getTimeInMillis() - System.currentTimeMillis()),
                pendingIntentStartTimeVibration);

        // handle disable watch pending intent
        if (this.getWatchAutoSwitchOffEnabled()) {
            Intent intentDisableWatch = new Intent(context, TactileClockService.class);
            intentDisableWatch.setAction(Constants.CustomAction.WATCH_DISABLE);
            PendingIntent pendingIntentDisableWatch = PendingIntent.getService(
                    context, Constants.ID.PENDING_INTENT_DISABLE_WATCH_ID, intentDisableWatch, PendingIntent.FLAG_CANCEL_CURRENT);
            // set disable watch alarm at watchEndTime
            applicationInstance.setAlarm(
                    SystemClock.elapsedRealtime()
                        + Math.abs(this.getWatchAutoSwitchOffTime() - System.currentTimeMillis()),
                    pendingIntentDisableWatch);
        }
    }

    public void disableWatch() {
        // set in settings
        Editor editor = settings.edit();
        editor.putBoolean(
                Constants.SETTINGS_KEY.WATCH_ENABLED, false);
        editor.apply();
        applicationInstance.updateServiceNotification();
        // handle vibrate time pending intent
        Intent intentStartTimeVibration = new Intent(context, TactileClockService.class);
        intentStartTimeVibration.setAction(Constants.CustomAction.WATCH_VIBRATE);
        PendingIntent pendingIntentStartTimeVibration = PendingIntent.getService(
                context, Constants.ID.PENDING_INTENT_VIBRATE_TIME_ID, intentStartTimeVibration, PendingIntent.FLAG_CANCEL_CURRENT);
        applicationInstance.cancelAlarm(pendingIntentStartTimeVibration);
        // handle disable watch pending intent
        Intent intentDisableWatch = new Intent(context, TactileClockService.class);
        intentDisableWatch.setAction(Constants.CustomAction.WATCH_DISABLE);
        PendingIntent pendingIntentDisableWatch = PendingIntent.getService(
                context, Constants.ID.PENDING_INTENT_DISABLE_WATCH_ID, intentDisableWatch, PendingIntent.FLAG_CANCEL_CURRENT);
        applicationInstance.cancelAlarm(pendingIntentDisableWatch);
    }

    public boolean getWatchAutoSwitchOffEnabled() {
        return settings.getBoolean(
                Constants.SETTINGS_KEY.WATCH_AUTO_SWITCH_OFF_ENABLED,
                DEFAULT_WATCH_AUTO_SWITCH_OFF_ENABLED);
    }

    public void setWatchAutoSwitchOffEnabled(boolean newStatus) {
        Editor editor = settings.edit();
        editor.putBoolean(
                Constants.SETTINGS_KEY.WATCH_AUTO_SWITCH_OFF_ENABLED,
                newStatus);
        editor.apply();
    }

    public long getWatchAutoSwitchOffTime() {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        calendar.set(Calendar.SECOND, 0);
        long newDate = settings.getLong(
                Constants.SETTINGS_KEY.WATCH_AUTO_SWITCH_OFF_TIME,
                calendar.getTimeInMillis());
        if (newDate < System.currentTimeMillis()) {
            newDate += 24*60*60*1000l;
        }
        return newDate;
    }

    public void setWatchAutoSwitchOffTime(long newAutoSwitchOffTime) {
        Editor editor = settings.edit();
        editor.putLong(
                Constants.SETTINGS_KEY.WATCH_AUTO_SWITCH_OFF_TIME,
                newAutoSwitchOffTime);
        editor.apply();
    }

    public boolean getWatchOnlyVibrateMinutes() {
        return settings.getBoolean(
                Constants.SETTINGS_KEY.WATCH_ONLY_VIBRATE_MINUTES,
                DEFAULT_WATCH_ONLY_VIBRATE_MINUTES);
    }

    public void setWatchOnlyVibrateMinutes(boolean onlyVibrateMinutes) {
        Editor editor = settings.edit();
        editor.putBoolean(
                Constants.SETTINGS_KEY.WATCH_ONLY_VIBRATE_MINUTES,
                onlyVibrateMinutes);
        editor.apply();
    }

    public boolean getWatchStartAtNextFullHour() {
        return settings.getBoolean(
                Constants.SETTINGS_KEY.WATCH_START_AT_NEXT_FULL_HOUR,
                DEFAULT_WATCH_START_AT_NEXT_FULL_HOUR);
    }

    public void setWatchStartAtNextFullHour(boolean startAtNextFullHour) {
        Editor editor = settings.edit();
        editor.putBoolean(
                Constants.SETTINGS_KEY.WATCH_START_AT_NEXT_FULL_HOUR,
                startAtNextFullHour);
        editor.apply();
    }

    public int getWatchVibrationIntervalInMinutes() {
        return settings.getInt(
                Constants.SETTINGS_KEY.WATCH_VIBRATION_INTERVAL,
                DEFAULT_WATCH_VIBRATION_INTERVAL);
    }

    public void setWatchVibrationIntervalInMinutes(int watchVibrationInterval) {
        Editor editor = settings.edit();
        editor.putInt(
                Constants.SETTINGS_KEY.WATCH_VIBRATION_INTERVAL,
                watchVibrationInterval);
        editor.apply();
    }

}
