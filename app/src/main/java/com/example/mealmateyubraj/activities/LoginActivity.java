package com.example.mealmateyubraj.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.widget.CheckBox;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.mealmateyubraj.MainActivity;
import com.example.mealmateyubraj.R;
import com.example.mealmateyubraj.database.DatabaseHelper;
import com.example.mealmateyubraj.database.UserDao;
import com.example.mealmateyubraj.models.User;
import com.example.mealmateyubraj.utils.SessionManager;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

public class LoginActivity extends AppCompatActivity {
    private static final String TAG = "LoginActivity";

    private TextInputLayout textInputLayoutEmail;
    private TextInputLayout textInputLayoutPassword;
    private TextInputEditText editTextEmail;
    private TextInputEditText editTextPassword;
    private CheckBox checkBoxRememberMe;
    private MaterialButton buttonLogin;
    private MaterialButton buttonRegister;
    
    private DatabaseHelper dbHelper;
    private UserDao userDao;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        initializeViews();
        setupClickListeners();
        
        dbHelper = new DatabaseHelper(this);
        userDao = new UserDao(this);
        sessionManager = new SessionManager(this);
        
        // Check if user is already logged in
        if (sessionManager.isLoggedIn()) {
            startMainActivity();
            finish();
        }
    }

    private void initializeViews() {
        textInputLayoutEmail = findViewById(R.id.textInputLayoutEmail);
        textInputLayoutPassword = findViewById(R.id.textInputLayoutPassword);
        editTextEmail = findViewById(R.id.editTextEmail);
        editTextPassword = findViewById(R.id.editTextPassword);
        checkBoxRememberMe = findViewById(R.id.checkBoxRememberMe);
        buttonLogin = findViewById(R.id.buttonLogin);
        buttonRegister = findViewById(R.id.textViewRegister);
    }

    private void setupClickListeners() {
        buttonLogin.setOnClickListener(v -> handleLogin());
        buttonRegister.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
            startActivity(intent);
        });
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
        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(this, "Please enter a valid email address", Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }

    private void startMainActivity() {
        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }
} 