package com.example.mealmateyubraj.auth;

import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.mealmateyubraj.HomeActivity;
import com.example.mealmateyubraj.R;
import com.example.mealmateyubraj.database.DatabaseHelper;
import com.example.mealmateyubraj.database.UserDao;
import com.example.mealmateyubraj.models.User;
import com.example.mealmateyubraj.utils.SessionManager;
import com.example.mealmateyubraj.utils.ValidationUtils;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

public class LoginActivity extends AppCompatActivity {
    private static final String TAG = "LoginActivity";

    private TextInputLayout textInputLayoutEmail, textInputLayoutPassword;
    private TextInputEditText editTextEmail, editTextPassword;
    private CheckBox checkBoxRememberMe;
    private MaterialButton buttonLogin;
    private TextView textViewRegister;
    
    private UserDao userDao;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        
        try {
            // Initialize views
            textInputLayoutEmail = findViewById(R.id.textInputLayoutEmail);
            textInputLayoutPassword = findViewById(R.id.textInputLayoutPassword);
            editTextEmail = findViewById(R.id.editTextEmail);
            editTextPassword = findViewById(R.id.editTextPassword);
            checkBoxRememberMe = findViewById(R.id.checkBoxRememberMe);
            buttonLogin = findViewById(R.id.buttonLogin);
            textViewRegister = findViewById(R.id.textViewRegister);
            
            // Initialize userDao and sessionManager
            userDao = new UserDao(this);
            sessionManager = new SessionManager(this);
            
            // Force database creation if it doesn't exist
            try {
                DatabaseHelper dbHelper = new DatabaseHelper(this);
                SQLiteDatabase db = dbHelper.getWritableDatabase();
                if (db != null) {
                    db.close();
                    Log.d(TAG, "Database initialized successfully");
                }
            } catch (Exception e) {
                Log.e(TAG, "Error initializing database: " + e.getMessage());
            }
            
            // Check if user is already logged in
            if (sessionManager.isLoggedIn()) {
                Log.d(TAG, "User already logged in, navigating to HomeActivity");
                navigateToHomeActivity();
                return;
            }
            
            try {
                // Check for remembered user
                userDao.open();
                User rememberedUser = userDao.getRememberedUser();
                if (rememberedUser != null) {
                    Log.d(TAG, "Found remembered user: " + rememberedUser.getEmail());
                    editTextEmail.setText(rememberedUser.getEmail());
                    checkBoxRememberMe.setChecked(true);
                } else {
                    Log.d(TAG, "No remembered user found");
                }
            } catch (Exception e) {
                Log.e(TAG, "Error checking for remembered user: " + e.getMessage());
            } finally {
                userDao.close();
            }
            
            // Set up click listeners
            buttonLogin.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    handleLogin();
                }
            });
            
            textViewRegister.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Log.d(TAG, "Navigate to register screen");
                    startActivity(new Intent(LoginActivity.this, RegisterActivity.class));
                }
            });
        } catch (Exception e) {
            Log.e(TAG, "Error initializing LoginActivity: " + e.getMessage(), e);
            Toast.makeText(this, "An error occurred while starting the app. Please try again.", Toast.LENGTH_LONG).show();
        }
    }
    
    private void handleLogin() {
        String email = editTextEmail.getText().toString().trim();
        String password = editTextPassword.getText().toString().trim();

        if (!validateInputs(email, password)) {
            return;
        }

        try {
            userDao.open();
            User user = userDao.authenticateUser(email, password);

            if (user != null) {
                Log.d(TAG, "Login successful for user: " + user.getEmail());
                
                // Update remember me status
                boolean rememberMe = checkBoxRememberMe.isChecked();
                user.setRememberMe(rememberMe);
                userDao.updateRememberMe(user.getId(), rememberMe);
                
                // Create login session
                sessionManager.createLoginSession(user.getId(), user.getUsername(), user.getEmail());
                
                startMainActivity();
                finish();
            } else {
                // Check if user exists to provide better error message
                User existingUser = userDao.getUserByEmail(email);
                if (existingUser != null) {
                    Toast.makeText(this, "Incorrect password", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "User not found", Toast.LENGTH_SHORT).show();
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error during login: " + e.getMessage());
            Toast.makeText(this, "Error during login", Toast.LENGTH_SHORT).show();
        } finally {
            userDao.close();
        }
    }
    
    private boolean validateInputs(String email, String password) {
        // Reset any previous errors
        textInputLayoutEmail.setError(null);
        textInputLayoutPassword.setError(null);
        
        if (ValidationUtils.isEmpty(email)) {
            textInputLayoutEmail.setError(getString(R.string.empty_fields));
            return false;
        }
        
        if (ValidationUtils.isEmpty(password)) {
            textInputLayoutPassword.setError(getString(R.string.empty_fields));
            return false;
        }
        
        if (!ValidationUtils.isValidEmail(email)) {
            textInputLayoutEmail.setError(getString(R.string.invalid_email));
            return false;
        }
        
        return true;
    }
    
    private void startMainActivity() {
        Intent intent = new Intent(LoginActivity.this, HomeActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
    
    private void navigateToHomeActivity() {
        Intent intent = new Intent(LoginActivity.this, HomeActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
} 