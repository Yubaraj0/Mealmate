package com.example.mealmateyubraj.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.example.mealmateyubraj.database.UserDao;

public class SessionManager {
    private static final String TAG = "SessionManager";
    private static final String PREF_NAME = "MealMateLogin";
    private static final String KEY_IS_LOGGED_IN = "isLoggedIn";
    private static final String KEY_USER_ID = "userId";
    private static final String KEY_USER_EMAIL = "userEmail";
    private static final String KEY_USER_NAME = "userName";
    
    private final SharedPreferences pref;
    private final SharedPreferences.Editor editor;
    private final Context context;
    
    private static final int PRIVATE_MODE = Context.MODE_PRIVATE;
    
    public SessionManager(Context context) {
        this.context = context;
        pref = context.getSharedPreferences(PREF_NAME, PRIVATE_MODE);
        editor = pref.edit();
    }
    
    /**
     * Create login session
     */
    public void createLoginSession(long userId, String userName, String email) {
        editor.putBoolean(KEY_IS_LOGGED_IN, true);
        editor.putLong(KEY_USER_ID, userId);
        editor.putString(KEY_USER_NAME, userName);
        editor.putString(KEY_USER_EMAIL, email);
        editor.apply();
        
        Log.d(TAG, "User login session created for ID: " + userId + ", email: " + email);
    }
    
    /**
     * Check login method wil check user login status
     * If false it will redirect user to login page
     */
    public boolean isLoggedIn() {
        return pref.getBoolean(KEY_IS_LOGGED_IN, false);
    }
    
    /**
     * Clear session details
     */
    public void clearSession() {
        editor.clear();
        editor.apply();
        
        Log.d(TAG, "User session cleared");
    }
    
    /**
     * Get stored session data
     */
    public long getUserId() {
        return pref.getLong(KEY_USER_ID, -1);
    }
    
    public String getUserEmail() {
        return pref.getString(KEY_USER_EMAIL, "");
    }
    
    public String getUserName() {
        return pref.getString(KEY_USER_NAME, "");
    }
    
    /**
     * Clear session details and log out user
     */
    public void logout() {
        // Clear all session data
        editor.clear();
        editor.commit();
        
        Log.d(TAG, "User logged out");
    }
} 