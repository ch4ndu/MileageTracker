package com.udnahc.locationapp.util;

import android.content.SharedPreferences;
import android.location.Location;
import android.preference.PreferenceManager;

import androidx.annotation.Nullable;

import com.udnahc.locationapp.App;


public class Preferences {
    private final static SharedPreferences defaultPreferences = PreferenceManager.getDefaultSharedPreferences(App.get().getApplicationContext());

    public static void saveUserName(String userName) {
        defaultPreferences.edit().putString("username", userName).apply();
    }

    public static String getUserName() {
        return defaultPreferences.getString("username", "");
    }

    public static void savePassword(String password) {
        defaultPreferences.edit().putString("password", password).apply();
    }

    public static String getPassword() {
        return defaultPreferences.getString("password", "");
    }

    public static String getInviteText(String defaultText) {
        return defaultPreferences.getString("inviteText", defaultText);
    }

    public static String getInviteTextTwo(String defaultText) {
        return defaultPreferences.getString("inviteTextTwo", defaultText);
    }

    public static String getInviteTextThree(String defaultText) {
        return defaultPreferences.getString("inviteTextThree", defaultText);
    }

    public static void saveInviteText(String inviteText) {
        defaultPreferences.edit().putString("inviteText", inviteText).apply();
    }

    public static void saveInviteTextTwo(String inviteText) {
        defaultPreferences.edit().putString("inviteTextTwo", inviteText).apply();
    }

    public static void saveInviteTextThree(String inviteText) {
        defaultPreferences.edit().putString("inviteTextThree", inviteText).apply();
    }

    public static void saveAutoTrackPreference(boolean enabled) {
        defaultPreferences.edit().putBoolean("autoTrack", enabled).apply();
    }

    public static void saveDebugLogPreference(boolean enabled) {
        defaultPreferences.edit().putBoolean("debugLogs", enabled).apply();
    }

    public static boolean shouldSaveLogs() {
//        return false;
        return defaultPreferences.getBoolean("debugLogs", false);
    }

    public static boolean isAutoTrackEnabled() {
        return defaultPreferences.getBoolean("autoTrack", false);
    }

    public static void saveAutoTrackSchedule(boolean enabled) {
        defaultPreferences.edit().putBoolean("autoTrackSchedule", enabled).apply();
    }

    public static boolean isAutoTrackScheduleEnabled() {
        return defaultPreferences.getBoolean("autoTrackSchedule", false);
    }

    public static void saveStartScheduleHour(int hour) {
        defaultPreferences.edit().putInt("scheduleStartHour", hour).apply();
    }

    public static int getScheduleStartHour() {
        return defaultPreferences.getInt("scheduleStartHour", 0);
    }

    public static void saveStartScheduleMinute(int minute) {
        defaultPreferences.edit().putInt("scheduleStartMinute", minute).apply();
    }

    public static int getScheduleStartMinute() {
        return defaultPreferences.getInt("scheduleStartMinute", 0);
    }

    public static void saveStopScheduleHour(int hour) {
        defaultPreferences.edit().putInt("scheduleStopHour", hour).apply();
    }

    public static int getScheduleStopHour() {
        return defaultPreferences.getInt("scheduleStopHour", 0);
    }

    public static void saveStopScheduleMinute(int minute) {
        defaultPreferences.edit().putInt("scheduleStopMinute", minute).apply();
    }

    public static int getScheduleStopMinute() {
        return defaultPreferences.getInt("scheduleStopMinute", 0);
    }

    public static void saveStartScheduleTwoHour(int hour) {
        defaultPreferences.edit().putInt("scheduleTwoStartHour", hour).apply();
    }

    public static int getScheduleTwoStartHour() {
        return defaultPreferences.getInt("scheduleTwoStartHour", 0);
    }

    public static void saveStartScheduleTwoMinute(int minute) {
        defaultPreferences.edit().putInt("scheduleTwoStartMinute", minute).apply();
    }

    public static int getScheduleTwoStartMinute() {
        return defaultPreferences.getInt("scheduleTwoStartMinute", 0);
    }

    public static void saveStopScheduleTwoHour(int hour) {
        defaultPreferences.edit().putInt("scheduleTwoStopHour", hour).apply();
    }

    public static int getScheduleTwoStopHour() {
        return defaultPreferences.getInt("scheduleTwoStopHour", 0);
    }

    public static void saveStopScheduleTwoMinute(int minute) {
        defaultPreferences.edit().putInt("scheduleTwoStopMinute", minute).apply();
    }

    public static int getScheduleTwoStopMinute() {
        return defaultPreferences.getInt("scheduleTwoStopMinute", 0);
    }

    public static void saveDisableWeekend(boolean enabled) {
        defaultPreferences.edit().putBoolean("disableWeekend", enabled).apply();
    }

    public static boolean isWeekendDisabled() {
        return defaultPreferences.getBoolean("disableWeekend", false);
    }

    public static void saveLocation(Location location) {
        defaultPreferences.edit().putString("currentFence", "" + location.getLatitude() + "~~" + location.getLongitude()).apply();
    }

    @Nullable
    public static Location getLastFence() {
        String latLng = defaultPreferences.getString("currentFence", "");
        String[] split = latLng.split("~~");
        try {
            if (split.length == 2) {
                Location location = new Location("temp");
                location.setLatitude(Double.parseDouble(split[0]));
                location.setLongitude(Double.parseDouble(split[1]));
                return location;
            }
        } catch (Exception e) {
            Plog.e("prefs", e, "getLastFence");
        }
        return null;
    }

    public static void putBoolean(String key, boolean value) {
        defaultPreferences.edit().putBoolean(key, value).apply();
    }

    public static boolean getBoolean(String key) {
        return defaultPreferences.getBoolean(key, false);
    }

    public static void putInt(String key, int value) {
        defaultPreferences.edit().putInt(key, value).apply();
    }

    public static int getInt(String key) {
        return defaultPreferences.getInt(key, 0);
    }
}
