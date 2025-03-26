package com.example.mealmateyubraj.utils;

import android.text.TextUtils;
import android.util.Patterns;

public class ValidationUtils {
    
    public static boolean isValidEmail(CharSequence email) {
        return (!TextUtils.isEmpty(email) && Patterns.EMAIL_ADDRESS.matcher(email).matches());
    }
    
    public static boolean isValidPassword(String password) {
        // Password must be at least 6 characters long
        return password != null && password.length() >= 6;
    }
    
    public static boolean isValidUsername(String username) {
        // Username must be at least 3 characters long
        return username != null && username.length() >= 3;
    }
    
    public static boolean isEmpty(String text) {
        return TextUtils.isEmpty(text);
    }
} 