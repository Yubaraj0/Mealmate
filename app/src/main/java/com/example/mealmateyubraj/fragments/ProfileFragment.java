package com.example.mealmateyubraj.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.mealmateyubraj.R;
import com.example.mealmateyubraj.auth.LoginActivity;
import com.example.mealmateyubraj.database.DatabaseHelper;
import com.example.mealmateyubraj.utils.SessionManager;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.imageview.ShapeableImageView;

public class ProfileFragment extends Fragment {
    private static final String TAG = "ProfileFragment";
    private ShapeableImageView profileImage;
    private TextView userNameText;
    private TextView userEmailText;
    private TextView mealsCountText;
    private TextView groceryCountText;
    private MaterialButton editProfileButton;
    private MaterialButton notificationsButton;
    private MaterialButton privacyButton;
    private MaterialButton logoutButton;
    private SessionManager sessionManager;
    private DatabaseHelper dbHelper;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);
        
        try {
            // Initialize views
            profileImage = view.findViewById(R.id.profileImage);
            userNameText = view.findViewById(R.id.userNameText);
            userEmailText = view.findViewById(R.id.userEmailText);
            mealsCountText = view.findViewById(R.id.mealsCountText);
            groceryCountText = view.findViewById(R.id.groceryCountText);
            editProfileButton = view.findViewById(R.id.editProfileButton);
            notificationsButton = view.findViewById(R.id.notificationsButton);
            privacyButton = view.findViewById(R.id.privacyButton);
            logoutButton = view.findViewById(R.id.logoutButton);

            // Initialize managers
            sessionManager = new SessionManager(requireContext());
            dbHelper = new DatabaseHelper(requireContext());

            // Load user data
            loadUserData();

            // Setup click listeners
            setupClickListeners();
        } catch (Exception e) {
            Log.e(TAG, "Error in onCreateView: " + e.getMessage(), e);
            Toast.makeText(requireContext(), "Error initializing profile", Toast.LENGTH_SHORT).show();
        }

        return view;
    }

    private void loadUserData() {
        try {
            // Load user info from session
            String userName = sessionManager.getUserName();
            String userEmail = sessionManager.getUserEmail();
            long userId = sessionManager.getUserId();

            userNameText.setText(userName != null ? userName : "");
            userEmailText.setText(userEmail != null ? userEmail : "");

            // Load stats
            int mealsCount = dbHelper.getMealsByUserId(userId).size();
            int groceryCount = dbHelper.getGroceryItemsByUserId(userId).size();

            mealsCountText.setText(String.valueOf(mealsCount));
            groceryCountText.setText(String.valueOf(groceryCount));
        } catch (Exception e) {
            Log.e(TAG, "Error loading user data: " + e.getMessage(), e);
            Toast.makeText(requireContext(), "Error loading profile data", Toast.LENGTH_SHORT).show();
        }
    }

    private void setupClickListeners() {
        editProfileButton.setOnClickListener(v -> {
            // TODO: Implement edit profile functionality
            Toast.makeText(requireContext(), "Edit profile coming soon!", Toast.LENGTH_SHORT).show();
        });

        notificationsButton.setOnClickListener(v -> {
            // TODO: Implement notifications settings
            Toast.makeText(requireContext(), "Notifications settings coming soon!", Toast.LENGTH_SHORT).show();
        });

        privacyButton.setOnClickListener(v -> {
            // TODO: Implement privacy settings
            Toast.makeText(requireContext(), "Privacy settings coming soon!", Toast.LENGTH_SHORT).show();
        });

        logoutButton.setOnClickListener(v -> {
            // Logout user
            sessionManager.logout();
            Intent intent = new Intent(requireActivity(), LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            requireActivity().finish();
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        loadUserData(); // Reload data when fragment resumes
    }
}