package com.example.mealmateyubraj.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.mealmateyubraj.R;
import com.example.mealmateyubraj.activities.LoginActivity;
import com.example.mealmateyubraj.database.DatabaseHelper;
import com.example.mealmateyubraj.models.User;
import com.example.mealmateyubraj.utils.SessionManager;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

public class ProfileFragment extends Fragment {
    private TextInputEditText etUsername;
    private TextInputEditText etEmail;
    private MaterialButton btnLogout;
    private DatabaseHelper dbHelper;
    private SessionManager sessionManager;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialize views
        etUsername = view.findViewById(R.id.et_username);
        etEmail = view.findViewById(R.id.et_email);
        btnLogout = view.findViewById(R.id.btn_logout);

        // Initialize database and session manager
        dbHelper = new DatabaseHelper(requireContext());
        sessionManager = new SessionManager(requireContext());

        // Load user data
        loadUserData();

        // Set up logout button
        btnLogout.setOnClickListener(v -> handleLogout());
    }

    private void loadUserData() {
        try {
            long userId = sessionManager.getUserId();
            User user = dbHelper.getUserById(userId);
            
            if (user != null) {
                etUsername.setText(user.getUsername());
                etEmail.setText(user.getEmail());
            } else {
                Toast.makeText(requireContext(), "Error loading user data", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Toast.makeText(requireContext(), "Error loading user data", Toast.LENGTH_SHORT).show();
        }
    }

    private void handleLogout() {
        try {
            // Clear session
            sessionManager.logout();
            
            // Navigate to login screen
            Intent intent = new Intent(requireContext(), LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        } catch (Exception e) {
            Toast.makeText(requireContext(), "Error during logout", Toast.LENGTH_SHORT).show();
        }
    }
} 