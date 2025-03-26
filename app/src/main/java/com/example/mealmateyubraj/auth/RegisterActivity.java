package com.example.mealmateyubraj.auth;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.mealmateyubraj.R;
import com.example.mealmateyubraj.activities.LoginActivity;
import com.example.mealmateyubraj.database.DatabaseHelper;
import com.example.mealmateyubraj.database.UserDao;
import com.example.mealmateyubraj.models.User;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

public class RegisterActivity extends AppCompatActivity {
    private static final String TAG = "RegisterActivity";

    private TextInputLayout textInputLayoutUsername;
    private TextInputLayout textInputLayoutEmail;
    private TextInputLayout textInputLayoutPassword;
    private TextInputLayout textInputLayoutConfirmPassword;
    private TextInputEditText editTextUsername;
    private TextInputEditText editTextEmail;
    private TextInputEditText editTextPassword;
    private TextInputEditText editTextConfirmPassword;
    private MaterialButton buttonRegister;
    
    private DatabaseHelper dbHelper;
    private UserDao userDao;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        initializeViews();
        setupClickListeners();
        
        dbHelper = new DatabaseHelper(this);
        userDao = new UserDao(this);
    }

    private void initializeViews() {
        textInputLayoutUsername = findViewById(R.id.textInputLayoutUsername);
        textInputLayoutEmail = findViewById(R.id.textInputLayoutEmail);
        textInputLayoutPassword = findViewById(R.id.textInputLayoutPassword);
        textInputLayoutConfirmPassword = findViewById(R.id.textInputLayoutConfirmPassword);
        editTextUsername = findViewById(R.id.editTextUsername);
        editTextEmail = findViewById(R.id.editTextEmail);
        editTextPassword = findViewById(R.id.editTextPassword);
        editTextConfirmPassword = findViewById(R.id.editTextConfirmPassword);
        buttonRegister = findViewById(R.id.buttonRegister);
    }

    private void setupClickListeners() {
        buttonRegister.setOnClickListener(v -> handleRegistration());
    }

    private void handleRegistration() {
        String username = editTextUsername.getText().toString().trim();
        String email = editTextEmail.getText().toString().trim();
        String password = editTextPassword.getText().toString().trim();
        String confirmPassword = editTextConfirmPassword.getText().toString().trim();

        if (!validateInputs(username, email, password, confirmPassword)) {
            return;
        }

        try {
            userDao.open();
            User existingUser = userDao.getUserByEmail(email);
            if (existingUser != null) {
                Toast.makeText(this, "Email already registered", Toast.LENGTH_SHORT).show();
                return;
            }

            User newUser = new User(username, email, password);
            long userId = userDao.registerUser(newUser);

            if (userId != -1) {
                newUser.setId(userId);
                Toast.makeText(this, "Registration successful", Toast.LENGTH_SHORT).show();
                
                // Verify registration
                try {
                    User savedUser = userDao.getUserByEmail(email);
                    if (savedUser != null) {
                        Log.d(TAG, "Verification: User retrieved after registration: " + savedUser.getUsername() + 
                                ", email: " + savedUser.getEmail());
                    }
                } catch (Exception e) {
                    Log.e(TAG, "Error verifying registration: " + e.getMessage());
                }
                
                finish();
            } else {
                Toast.makeText(this, "Registration failed", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Log.e(TAG, "Error during registration: " + e.getMessage());
            Toast.makeText(this, "Error during registration", Toast.LENGTH_SHORT).show();
        } finally {
            userDao.close();
        }
    }

    private boolean validateInputs(String username, String email, String password, String confirmPassword) {
        if (username.isEmpty() || email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
            Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (username.length() < 3) {
            Toast.makeText(this, "Username must be at least 3 characters", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(this, "Please enter a valid email address", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (password.length() < 6) {
            Toast.makeText(this, "Password must be at least 6 characters", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (!password.equals(confirmPassword)) {
            Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }
} 