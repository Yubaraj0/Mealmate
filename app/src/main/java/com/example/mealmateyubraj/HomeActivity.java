package com.example.mealmateyubraj;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.example.mealmateyubraj.auth.LoginActivity;
import com.example.mealmateyubraj.activities.GroceryListActivity;
import com.example.mealmateyubraj.fragments.ProfileFragment;
import com.example.mealmateyubraj.utils.SessionManager;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationBarView;

public class HomeActivity extends AppCompatActivity {
    private static final String TAG = "HomeActivity";

    private TextView welcomeText;
    private MaterialToolbar topAppBar;
    private BottomNavigationView bottomNavigation;
    private MaterialCardView cardAddMeal, cardGroceryList, cardManageItems;
    private FloatingActionButton fabAddRecipe;
    private MaterialButton buttonLogout;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        try {
            // Initialize views
            welcomeText = findViewById(R.id.welcomeText);
            topAppBar = findViewById(R.id.topAppBar);
            bottomNavigation = findViewById(R.id.bottomNavigation);
            cardAddMeal = findViewById(R.id.cardAddMeal);
            cardGroceryList = findViewById(R.id.cardGroceryList);
            cardManageItems = findViewById(R.id.cardManageItems);
            fabAddRecipe = findViewById(R.id.fabAddRecipe);
            buttonLogout = findViewById(R.id.buttonLogout);

            // Initialize session manager
            sessionManager = new SessionManager(this);

            // Check if user is logged in
            if (!sessionManager.isLoggedIn()) {
                navigateToLogin();
                return;
            }

            // Set up top app bar
            setSupportActionBar(topAppBar);
            
            // Display welcome message with username
            String userName = sessionManager.getUserName();
            if (userName != null && !userName.isEmpty()) {
//                welcomeText.setText("Welcome, " + userName + "!");
            } else {
            //    welcomeText.setText("Welcome to MealMate!");
            }

            // Set up bottom navigation
            bottomNavigation.setSelectedItemId(R.id.navigation_home);
            bottomNavigation.setOnItemSelectedListener(item -> {
                int itemId = item.getItemId();
                
                if (itemId == R.id.navigation_home) {

                    FragmentManager fragmentManager = getSupportFragmentManager();
                    FragmentTransaction transaction = fragmentManager.beginTransaction();
                    transaction.replace(R.id.fragmentContainer, new HomeFragment());
                    transaction.commit();


                    return true;
                } else if (itemId == R.id.navigation_meals) {
                    Log.d(TAG, "Meals tab selected");
                    try {
                        // Get user ID from session manager
                        long userId = sessionManager.getUserId();
                        Log.d(TAG, "User ID from session: " + userId);
                        
                        // Launch AddEditMealActivity in "add" mode
                        Intent intent = com.example.mealmateyubraj.activities.AddEditMealActivity.createIntent(HomeActivity.this, "add", userId);
                        startActivity(intent);
                        Log.d(TAG, "AddEditMealActivity started successfully from bottom navigation");
                        return true;
                    } catch (Exception e) {
                        Log.e(TAG, "Error launching AddEditMealActivity from bottom navigation: " + e.getMessage(), e);
                        Toast.makeText(HomeActivity.this, "Error opening Meals screen: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        return false;
                    }
                } else if (itemId == R.id.navigation_groceries) {
                    // Handle groceries navigation

                    Intent intent = new Intent(HomeActivity.this,GroceryListActivity.class);
                    startActivity(intent);
                    return true;
                } else if (itemId == R.id.navigation_profile) {

                    FragmentManager fragmentManager = getSupportFragmentManager();
                    FragmentTransaction transaction = fragmentManager.beginTransaction();
                    transaction.replace(R.id.fragmentContainer, new ProfileFragment());
                    transaction.commit();

                    // Handle profile navigation
                    return true;
                }
                return false;
            });

            // Set up top app bar menu
            topAppBar.setOnMenuItemClickListener(new MaterialToolbar.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem item) {
                    int itemId = item.getItemId();
                    
                    if (itemId == R.id.menu_profile) {
                        // Will be implemented to navigate to profile screen
                        Toast.makeText(HomeActivity.this, "Profile section coming soon!", Toast.LENGTH_SHORT).show();
                        return true;
                    } else if (itemId == R.id.menu_settings) {
                        // Will be implemented to navigate to settings screen
                        Toast.makeText(HomeActivity.this, "Settings section coming soon!", Toast.LENGTH_SHORT).show();
                        return true;
                    } else if (itemId == R.id.menu_logout) {
                        // Log out the user
                        handleLogout();
                        return true;
                    }
                    
                    return false;
                }
            });

            // Set up card click listeners
//            cardAddMeal.setOnClickListener(v -> {
//                Log.d(TAG, "Card Add Meal clicked");
//                try {
//                    // Get user ID from session manager
//                    long userId = sessionManager.getUserId();
//                    Log.d(TAG, "User ID from session: " + userId);
//
//                    // Launch AddEditMealActivity in "add" mode
//                    Intent intent = com.example.mealmateyubraj.activities.AddEditMealActivity.createIntent(HomeActivity.this, "add", userId);
//                    startActivity(intent);
//                    Log.d(TAG, "AddEditMealActivity started successfully");
//                } catch (Exception e) {
//                    Log.e(TAG, "Error launching AddEditMealActivity: " + e.getMessage(), e);
//                    Toast.makeText(HomeActivity.this, "Error launching Add Meal screen: " + e.getMessage(), Toast.LENGTH_SHORT).show();
//                }
//            });
//
//
//
//            // Set up FAB click listener
//            fabAddRecipe.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View v) {
//                    // Will be implemented to navigate to add recipe screen
//                    Toast.makeText(HomeActivity.this, "Add Recipe feature coming soon!", Toast.LENGTH_SHORT).show();
//                }
//            });
//
//            // Set up logout button click listener
//            buttonLogout.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View v) {
//                    handleLogout();
//                }
//            });
        } catch (Exception e) {
            Log.e(TAG, "Error initializing HomeActivity: " + e.getMessage(), e);
            Toast.makeText(this, "An error occurred while starting the home screen. Please try again.", Toast.LENGTH_LONG).show();
        }
    }
    
    /**
     * Logs out the user and navigates to login screen
     */
    private void handleLogout() {
        sessionManager.logout();
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void navigateToLogin() {
        Intent intent = new Intent(HomeActivity.this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
} 