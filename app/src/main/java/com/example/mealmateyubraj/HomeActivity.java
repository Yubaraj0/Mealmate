package com.example.mealmateyubraj;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.example.mealmateyubraj.activities.GroceryListActivity;
import com.example.mealmateyubraj.fragments.HomeFragment;
import com.example.mealmateyubraj.fragments.MealsFragment;
import com.example.mealmateyubraj.fragments.ProfileFragment;
import com.example.mealmateyubraj.utils.SessionManager;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class HomeActivity extends AppCompatActivity {
    private BottomNavigationView bottomNavigationView;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        // Initialize session manager
        sessionManager = new SessionManager(this);

        // Initialize views
        bottomNavigationView = findViewById(R.id.bottom_navigation);

        // Set up bottom navigation
        bottomNavigationView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_home) {
                loadFragment(new HomeFragment());
            } else if (itemId == R.id.nav_meals) {
                loadFragment(new MealsFragment());
            } else if (itemId == R.id.nav_groceries) {
                startActivity(new Intent(this, GroceryListActivity.class));
            } else if (itemId == R.id.nav_profile) {
                loadFragment(new ProfileFragment());
            }
            return true;
        });

        // Load default fragment
        loadFragment(new HomeFragment());
    }

    private void loadFragment(Fragment fragment) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.replace(R.id.fragment_container, fragment);
        transaction.commit();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Refresh the current fragment
        Fragment currentFragment = getSupportFragmentManager().findFragmentById(R.id.fragment_container);
        if (currentFragment != null) {
            loadFragment(currentFragment);
        }
    }
}