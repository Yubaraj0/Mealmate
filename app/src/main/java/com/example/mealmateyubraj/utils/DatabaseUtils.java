package com.example.mealmateyubraj.utils;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.example.mealmateyubraj.database.DatabaseHelper;

/**
 * Utility class for database operations
 */
public class DatabaseUtils {
    private static final String TAG = "DatabaseUtils";

    /**
     * Ensures the database is created and all tables exist
     */
    public static void ensureDatabaseExists(Context context) {
        Log.d(TAG, "Ensuring database exists");
        DatabaseHelper dbHelper = null;
        SQLiteDatabase db = null;
        
        try {
            dbHelper = new DatabaseHelper(context);
            db = dbHelper.getWritableDatabase();
            Log.d(TAG, "Database initialized successfully");
        } catch (Exception e) {
            Log.e(TAG, "Error initializing database: " + e.getMessage(), e);
        } finally {
            if (db != null && db.isOpen()) {
                db.close();
            }
            if (dbHelper != null) {
                dbHelper.close();
            }
        }
    }
    
    /**
     * Clears the database (for testing purposes)
     */
    public static void clearDatabase(Context context) {
        Log.d(TAG, "Clearing database");
        DatabaseHelper dbHelper = null;
        SQLiteDatabase db = null;
        
        try {
            dbHelper = new DatabaseHelper(context);
            db = dbHelper.getWritableDatabase();
            
            // Delete all data from tables
            db.execSQL("DELETE FROM " + DatabaseHelper.TABLE_MEAL_INGREDIENTS);
            db.execSQL("DELETE FROM " + DatabaseHelper.TABLE_INGREDIENTS);
            db.execSQL("DELETE FROM " + DatabaseHelper.TABLE_MEALS);
            db.execSQL("DELETE FROM " + DatabaseHelper.TABLE_USERS);
            
            Log.d(TAG, "Database cleared successfully");
        } catch (Exception e) {
            Log.e(TAG, "Error clearing database: " + e.getMessage(), e);
        } finally {
            if (db != null && db.isOpen()) {
                db.close();
            }
            if (dbHelper != null) {
                dbHelper.close();
            }
        }
    }
    
    /**
     * Logs database table information for debugging
     */
    public static void logDatabaseInfo(Context context) {
        Log.d(TAG, "Logging database information");
        DatabaseHelper dbHelper = null;
        SQLiteDatabase db = null;
        
        try {
            dbHelper = new DatabaseHelper(context);
            db = dbHelper.getReadableDatabase();
            
            // Query for all tables
            android.database.Cursor cursor = db.rawQuery(
                    "SELECT name FROM sqlite_master WHERE type='table'", null);
            
            Log.d(TAG, "Tables in database:");
            if (cursor != null) {
                while (cursor.moveToNext()) {
                    String tableName = cursor.getString(0);
                    Log.d(TAG, "- " + tableName);
                    
                    // Get count of rows in this table
                    try {
                        android.database.Cursor countCursor = db.rawQuery(
                                "SELECT COUNT(*) FROM " + tableName, null);
                        if (countCursor != null && countCursor.moveToFirst()) {
                            int count = countCursor.getInt(0);
                            Log.d(TAG, "  Rows: " + count);
                            countCursor.close();
                        }
                    } catch (Exception e) {
                        // Some system tables may not be queryable
                        Log.w(TAG, "  Could not query row count: " + e.getMessage());
                    }
                }
                cursor.close();
            } else {
                Log.d(TAG, "No tables found in database");
            }
        } catch (Exception e) {
            Log.e(TAG, "Error logging database info: " + e.getMessage(), e);
        } finally {
            if (db != null && db.isOpen()) {
                db.close();
            }
            if (dbHelper != null) {
                dbHelper.close();
            }
        }
    }
} 