package com.example.mealmateyubraj;

import android.app.Application;
import android.util.Log;

import com.example.mealmateyubraj.utils.DatabaseUtils;

public class MealmateApplication extends Application {
    private static final String TAG = "MealmateApplication";

    @Override
    public void onCreate() {
        super.onCreate();
        
        Log.d(TAG, "Application starting up");
        
        // Initialize database on app startup
        try {
            DatabaseUtils.ensureDatabaseExists(this);
            DatabaseUtils.logDatabaseInfo(this);
            Log.d(TAG, "Database initialization completed");
        } catch (Exception e) {
            Log.e(TAG, "Error initializing database in Application.onCreate: " + e.getMessage(), e);
        }
    }
} 