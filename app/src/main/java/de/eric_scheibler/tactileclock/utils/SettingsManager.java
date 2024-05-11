package de.eric_scheibler.tactileclock.utils;




import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences.Editor;
import android.content.SharedPreferences;
import android.content.pm.PackageManager.NameNotFoundException;


import android.preference.PreferenceManager;

import android.text.format.DateFormat;

import androidx.core.content.ContextCompat;

import de.eric_scheibler.tactileclock.data.HourFormat;
import de.eric_scheibler.tactileclock.data.TimeComponentOrder;


public class SettingsManager {

    // keys
    //
    // general
    private static final String KEY_FIRST_START = "firstStart";
    private static final String KEY_ASKED_FOR_NOTIFICATION_PERMISSION = "askedForNotificationPermission";
    private static final String KEY_RECENT_OPEN_TAB = "recentOpenTab";
    private static final String KEY_HOUR_FORMAT = "hourFormat";
    private static final String KEY_TIME_COMPONENT_ORDER = "timeComponentOrder";
    private static final String KEY_MAX_STRENGTH_VIBRATIONS_ENABLED = "maxStrengthVibrationsEnabled";
    // power button
    private static final String KEY_POWER_BUTTON_SERVICE_ENABLED = "enableService";
    private static final String KEY_POWER_BUTTON_ERROR_VIBRATION = "errorVibration";
    private static final String KEY_POWER_BUTTON_LOWER_SUCCESS_BOUNDARY = "lowerSuccessBoundary";
    private static final String KEY_POWER_BUTTON_UPPER_SUCCESS_BOUNDARY = "upperSuccessBoundary";
    // watch
    private static final String KEY_WATCH_ENABLED = "watchEnabled";
    private static final String KEY_WATCH_VIBRATION_INTERVAL = "watchVibrationInterval";
    private static final String KEY_WATCH_ONLY_VIBRATE_MINUTES = "onlyVibrateMinutes";
    private static final String KEY_WATCH_START_AT_NEXT_FULL_HOUR = "startAtNextFullHour";
    private static final String KEY_WATCH_ANNOUNCEMENT_VIBRATION = "announcement_vibration";

	// defaults
    //
    // general settings
    public static final boolean DEFAULT_FIRST_START = true;
    public static final boolean DEFAULT_ASKED_FOR_NOTIFICATION_PERMISSION = false;
    public static final boolean DEFAULT_MAX_STRENGTH_VIBRATIONS_ENABLED = true;
    // power button
    public static final boolean DEFAULT_POWER_BUTTON_SERVICE_ENABLED = true;
    public static final boolean DEFAULT_POWER_BUTTON_ERROR_VIBRATION = true;
    // double click parameters
    public static final long DEFAULT_POWER_BUTTON_LOWER_ERROR_BOUNDARY = 100;
    public static final long DEFAULT_POWER_BUTTON_UPPER_ERROR_BOUNDARY = 1000;
    public static final long DEFAULT_POWER_BUTTON_LOWER_SUCCESS_BOUNDARY = 250;
    public static final long DEFAULT_POWER_BUTTON_UPPER_SUCCESS_BOUNDARY = 1350;
    // watch
    public static final boolean DEFAULT_WATCH_ENABLED = false;
    public static final int DEFAULT_WATCH_VIBRATION_INTERVAL = 5;
    public static final boolean DEFAULT_WATCH_ONLY_VIBRATE_MINUTES = false;
    public static final boolean DEFAULT_WATCH_START_AT_NEXT_FULL_HOUR = false;
    public static final boolean DEFAULT_WATCH_ANNOUNCEMENT_VIBRATION = false;

	// class variables
	private Context context;
	private SharedPreferences settings;

	public SettingsManager() {
        this.context = ApplicationInstance.getContext();
		this.settings = PreferenceManager.getDefaultSharedPreferences(context);
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
                KEY_FIRST_START,
                DEFAULT_FIRST_START);
    }

    public void setFirstStart(boolean firstStart) {
        Editor editor = settings.edit();
        editor.putBoolean(KEY_FIRST_START, firstStart);
        editor.apply();
    }

    public boolean getAskedForNotificationPermission() {
        return settings.getBoolean(
                KEY_ASKED_FOR_NOTIFICATION_PERMISSION,
                DEFAULT_ASKED_FOR_NOTIFICATION_PERMISSION);
    }

    public void setAskedForNotificationPermission(boolean askedForNotificationPermission) {
        Editor editor = settings.edit();
        editor.putBoolean(KEY_ASKED_FOR_NOTIFICATION_PERMISSION, askedForNotificationPermission);
        editor.apply();
    }

    public HourFormat getHourFormat() {
        return HourFormat.lookupByCode(
                settings.getString(
                    KEY_HOUR_FORMAT,
                    DateFormat.is24HourFormat(context)
                        ? HourFormat.TWENTYFOUR_HOURS.getCode()
                        : HourFormat.TWELVE_HOURS.getCode())
                );
    }

    public void setHourFormat(HourFormat hourFormat) {
        Editor editor = settings.edit();
        editor.putString(
                KEY_HOUR_FORMAT, hourFormat.getCode());
        editor.apply();
    }

    public TimeComponentOrder getTimeComponentOrder() {
        return TimeComponentOrder.lookupByCode(
                settings.getString(
                    KEY_TIME_COMPONENT_ORDER,
                    TimeComponentOrder.HOURS_MINUTES.getCode())
                );
    }

    public void setTimeComponentOrder(TimeComponentOrder timeComponentOrder) {
        Editor editor = settings.edit();
        editor.putString(
                KEY_TIME_COMPONENT_ORDER, timeComponentOrder.getCode());
        editor.apply();
    }

    public boolean getMaxStrengthVibrationsEnabled() {
        return settings.getBoolean(
                KEY_MAX_STRENGTH_VIBRATIONS_ENABLED,
                DEFAULT_MAX_STRENGTH_VIBRATIONS_ENABLED);
    }

    public void setMaxStrengthVibrationsEnabled(boolean enabled) {
        Editor editor = settings.edit();
        editor.putBoolean(KEY_MAX_STRENGTH_VIBRATIONS_ENABLED, enabled);
        editor.apply();
    }


    /**
     * power button
     */

    public boolean getPowerButtonServiceEnabled() {
        return settings.getBoolean(
                KEY_POWER_BUTTON_SERVICE_ENABLED,
                DEFAULT_POWER_BUTTON_SERVICE_ENABLED);
    }

    public void setPowerButtonServiceEnabled(boolean enabled) {
        Editor editor = settings.edit();
        editor.putBoolean(KEY_POWER_BUTTON_SERVICE_ENABLED, enabled);
        editor.apply();
        // update notivication
        Intent updateNotificationIntent = new Intent(context, TactileClockService.class);
        updateNotificationIntent.setAction(TactileClockService.ACTION_UPDATE_NOTIFICATION);
        ContextCompat.startForegroundService(context, updateNotificationIntent);
    }

    public boolean getPowerButtonErrorVibration() {
        return settings.getBoolean(
                KEY_POWER_BUTTON_ERROR_VIBRATION,
                DEFAULT_POWER_BUTTON_ERROR_VIBRATION);
    }

    public void setPowerButtonErrorVibration(boolean enabled) {
        Editor editor = settings.edit();
        editor.putBoolean(KEY_POWER_BUTTON_ERROR_VIBRATION, enabled);
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
                KEY_POWER_BUTTON_LOWER_SUCCESS_BOUNDARY,
                DEFAULT_POWER_BUTTON_LOWER_SUCCESS_BOUNDARY);
    }

    public void setPowerButtonLowerSuccessBoundary(long boundary) {
        Editor editor = settings.edit();
        editor.putLong(
                KEY_POWER_BUTTON_LOWER_SUCCESS_BOUNDARY, boundary);
        editor.apply();
    }

    public long getPowerButtonUpperSuccessBoundary() {
        return settings.getLong(
                KEY_POWER_BUTTON_UPPER_SUCCESS_BOUNDARY,
                DEFAULT_POWER_BUTTON_UPPER_SUCCESS_BOUNDARY);
    }

    public void setPowerButtonUpperSuccessBoundary(long boundary) {
        Editor editor = settings.edit();
        editor.putLong(
                KEY_POWER_BUTTON_UPPER_SUCCESS_BOUNDARY, boundary);
        editor.apply();
    }


    /**
     * watch
     */

    public boolean isWatchEnabled() {
        return settings.getBoolean(
                KEY_WATCH_ENABLED,
                DEFAULT_WATCH_ENABLED);
    }

    public void enableWatch() {
        setWatchEnabled(true);
        // set first exact watch vibration alarm
        if (this.getWatchStartAtNextFullHour()) {
            // at next full hour
            ((ApplicationInstance) ApplicationInstance.getContext()).setAlarmAtFullHour(1);
        } else {
            // at next full minute
            ((ApplicationInstance) ApplicationInstance.getContext()).setAlarmAtFullMinute(1);
        }
    }

    public void disableWatch() {
        setWatchEnabled(false);
        ((ApplicationInstance) ApplicationInstance.getContext()).cancelAlarm();
    }

    private void setWatchEnabled(boolean enabled) {
        // save setting
        Editor editor = settings.edit();
        editor.putBoolean(
                KEY_WATCH_ENABLED, enabled);
        editor.apply();
        // update notivication
        Intent updateNotificationIntent = new Intent(context, TactileClockService.class);
        updateNotificationIntent.setAction(TactileClockService.ACTION_UPDATE_NOTIFICATION);
        ContextCompat.startForegroundService(context, updateNotificationIntent);
    }

    // settings

    public int getWatchVibrationIntervalInMinutes() {
        return settings.getInt(
                KEY_WATCH_VIBRATION_INTERVAL,
                DEFAULT_WATCH_VIBRATION_INTERVAL);
    }

    public void setWatchVibrationIntervalInMinutes(int watchVibrationInterval) {
        Editor editor = settings.edit();
        editor.putInt(
                KEY_WATCH_VIBRATION_INTERVAL,
                watchVibrationInterval);
        editor.apply();
    }

    public boolean getWatchOnlyVibrateMinutes() {
        return settings.getBoolean(
                KEY_WATCH_ONLY_VIBRATE_MINUTES,
                DEFAULT_WATCH_ONLY_VIBRATE_MINUTES);
    }

    public void setWatchOnlyVibrateMinutes(boolean onlyVibrateMinutes) {
        Editor editor = settings.edit();
        editor.putBoolean(
                KEY_WATCH_ONLY_VIBRATE_MINUTES,
                onlyVibrateMinutes);
        editor.apply();
    }

    public boolean getWatchStartAtNextFullHour() {
        return settings.getBoolean(
                KEY_WATCH_START_AT_NEXT_FULL_HOUR,
                DEFAULT_WATCH_START_AT_NEXT_FULL_HOUR);
    }

    public void setWatchStartAtNextFullHour(boolean startAtNextFullHour) {
        Editor editor = settings.edit();
        editor.putBoolean(
                KEY_WATCH_START_AT_NEXT_FULL_HOUR,
                startAtNextFullHour);
        editor.apply();
    }

    public boolean getWatchAnnouncementVibration() {
        return settings.getBoolean(
                KEY_WATCH_ANNOUNCEMENT_VIBRATION,
                DEFAULT_WATCH_ANNOUNCEMENT_VIBRATION);
    }

    public void setWatchAnnouncementVibration(boolean newAnnouncementVibration) {
        Editor editor = settings.edit();
        editor.putBoolean(
                KEY_WATCH_ANNOUNCEMENT_VIBRATION,
                newAnnouncementVibration);
        editor.apply();
    }

}
