package com.example.mealmateyubraj.utils;

import android.content.Context;
import android.content.SharedPreferences;

public class SharedPrefsUtil {
    private static final String PREFS_NAME = "MealMatePrefs";
    private static final String KEY_USER_ID = "user_id";
    private static final String KEY_REMEMBER_ME = "remember_me";
    private static final String KEY_USER_EMAIL = "user_email";

    public static void saveUserId(Context context, long userId) {
        SharedPreferences.Editor editor = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit();
        editor.putLong(KEY_USER_ID, userId);
        editor.apply();
    }

    public static long getUserId(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return prefs.getLong(KEY_USER_ID, -1);
    }

    public static void saveRememberMe(Context context, boolean rememberMe) {
        SharedPreferences.Editor editor = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit();
        editor.putBoolean(KEY_REMEMBER_ME, rememberMe);
        editor.apply();
    }

    public static boolean getRememberMe(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return prefs.getBoolean(KEY_REMEMBER_ME, false);
    }

    public static void saveUserEmail(Context context, String email) {
        SharedPreferences.Editor editor = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit();
        editor.putString(KEY_USER_EMAIL, email);
        editor.apply();
    }

    public static String getUserEmail(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return prefs.getString(KEY_USER_EMAIL, null);
    }

    public static void clearUserData(Context context) {
        SharedPreferences.Editor editor = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit();
        editor.remove(KEY_USER_ID);
        editor.remove(KEY_REMEMBER_ME);
        editor.remove(KEY_USER_EMAIL);
        editor.apply();
    }
} 