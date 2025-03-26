package com.example.mealmateyubraj.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.example.mealmateyubraj.models.User;

public class UserDao {
    private static final String TAG = "UserDao";
    private SQLiteDatabase database;
    private DatabaseHelper dbHelper;

    public UserDao(Context context) {
        dbHelper = new DatabaseHelper(context);
    }

    public void open() {
        database = dbHelper.getWritableDatabase();
    }

    public void close() {
        if (database != null && database.isOpen()) {
            database.close();
        }
    }

    public SQLiteDatabase getWritableDatabase() {
        return database;
    }

    public long insertUser(User user) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.COLUMN_USERNAME, user.getUsername());
        values.put(DatabaseHelper.COLUMN_EMAIL, user.getEmail());
        values.put(DatabaseHelper.COLUMN_PASSWORD, user.getPassword());
        values.put(DatabaseHelper.COLUMN_REMEMBER_ME, user.isRememberMe() ? 1 : 0);
        return db.insert(DatabaseHelper.TABLE_USERS, null, values);
    }

    public User authenticateUser(String email, String password) {
        User user = null;
        Cursor cursor = null;

        try {
            cursor = database.query(
                    DatabaseHelper.TABLE_USERS,
                    null,
                    "LOWER(" + DatabaseHelper.COLUMN_EMAIL + ")=? AND " + DatabaseHelper.COLUMN_PASSWORD + "=?",
                    new String[]{email.toLowerCase(), password},
                    null, null, null);

            if (cursor != null && cursor.moveToFirst()) {
                user = cursorToUser(cursor);
                Log.d(TAG, "Login successful for user: " + user.getUsername() + ", email: " + user.getEmail());
            }
        } catch (Exception e) {
            Log.e(TAG, "Error authenticating user: " + e.getMessage());
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return user;
    }

    public boolean isEmailRegistered(String email) {
        Cursor cursor = null;
        try {
            cursor = database.query(
                    DatabaseHelper.TABLE_USERS,
                    new String[]{DatabaseHelper.COLUMN_ID},
                    "LOWER(" + DatabaseHelper.COLUMN_EMAIL + ")=?",
                    new String[]{email.toLowerCase()},
                    null, null, null);

            return cursor != null && cursor.getCount() > 0;
        } catch (Exception e) {
            Log.e(TAG, "Error checking email registration: " + e.getMessage());
            return false;
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    public User getUserByEmail(String email) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        User user = null;
        Cursor cursor = db.query(DatabaseHelper.TABLE_USERS,
                null,
                DatabaseHelper.COLUMN_EMAIL + "=?",
                new String[]{email},
                null, null, null);

        if (cursor != null && cursor.moveToFirst()) {
            user = new User();
            user.setId(cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_ID)));
            user.setUsername(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_USERNAME)));
            user.setEmail(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_EMAIL)));
            user.setPassword(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_PASSWORD)));
            user.setRememberMe(cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_REMEMBER_ME)) == 1);
            cursor.close();
        }
        return user;
    }
    
    public boolean updateUserRememberMe(long userId, boolean rememberMe) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.COLUMN_REMEMBER_ME, rememberMe ? 1 : 0);
        return db.update(DatabaseHelper.TABLE_USERS, values,
                DatabaseHelper.COLUMN_ID + "=?",
                new String[]{String.valueOf(userId)}) > 0;
    }
    
    public User getRememberedUser() {
        User user = null;
        
        try {
            Cursor cursor = database.query(
                    DatabaseHelper.TABLE_USERS,
                    null,
                    DatabaseHelper.COLUMN_REMEMBER_ME + "=?",
                    new String[]{"1"},
                    null, null, null);

            if (cursor != null && cursor.moveToFirst()) {
                user = cursorToUser(cursor);
                cursor.close();
            }
        } catch (Exception e) {
            Log.e(TAG, "Error getting remembered user: " + e.getMessage());
        }
        
        return user;
    }
    
    private User cursorToUser(Cursor cursor) {
        long id = cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_ID));
        String username = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_USERNAME));
        String email = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_EMAIL));
        String password = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_PASSWORD));
        boolean rememberMe = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_REMEMBER_ME)) == 1;
        
        User user = new User(id, username, email, password);
        user.setRememberMe(rememberMe);
        return user;
    }

    /**
     * Log all users in the database (for debugging)
     */
    public void logAllUsers() {
        Cursor cursor = null;
        try {
            cursor = database.query(
                    DatabaseHelper.TABLE_USERS,
                    null, null, null, null, null, null);
                    
            Log.d(TAG, "Total users in database: " + (cursor != null ? cursor.getCount() : 0));
            
            if (cursor != null && cursor.moveToFirst()) {
                do {
                    User user = cursorToUser(cursor);
                    Log.d(TAG, "User: id=" + user.getId() + 
                          ", username=" + user.getUsername() + 
                          ", email=" + user.getEmail());
                } while (cursor.moveToNext());
            } else {
                Log.d(TAG, "No users found in database");
            }
        } catch (Exception e) {
            Log.e(TAG, "Error logging users: " + e.getMessage());
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }
    
    /**
     * @deprecated Use authenticateUser instead
     */
    @Deprecated
    public User loginUser(String email, String password) {
        return authenticateUser(email, password);
    }

    public long registerUser(User user) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.COLUMN_USERNAME, user.getUsername());
        values.put(DatabaseHelper.COLUMN_EMAIL, user.getEmail().toLowerCase());
        values.put(DatabaseHelper.COLUMN_PASSWORD, user.getPassword());
        values.put(DatabaseHelper.COLUMN_REMEMBER_ME, user.isRememberMe() ? 1 : 0);

        long id = db.insert(DatabaseHelper.TABLE_USERS, null, values);
        Log.d(TAG, "Registered user: " + user.getUsername() + " with ID: " + id + ", email: " + user.getEmail());
        return id;
    }

    public boolean updateRememberMe(long userId, boolean rememberMe) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.COLUMN_REMEMBER_ME, rememberMe ? 1 : 0);
        
        int rows = db.update(
                DatabaseHelper.TABLE_USERS,
                values,
                DatabaseHelper.COLUMN_ID + "=?",
                new String[]{String.valueOf(userId)});
                
        return rows > 0;
    }

    public void updateUser(User existingUser) {
    }
} 