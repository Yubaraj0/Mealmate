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

import com.example.mealmateyubraj.HomeActivity;
import com.example.mealmateyubraj.R;
import com.example.mealmateyubraj.activities.GroceryListActivity;
import com.example.mealmateyubraj.utils.SessionManager;
import com.example.mealmateyubraj.utils.SharedPreferencesHelper;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class HomeFragment extends Fragment {
    private TextView welcomeText;
    private static final String TAG = "HomeFrag";

    private MaterialToolbar topAppBar;
    private BottomNavigationView bottomNavigation;
    private MaterialCardView cardAddMeal, cardGroceryList, cardManageItems;
    private FloatingActionButton fabAddRecipe;
    private MaterialButton buttonLogout;
    private SessionManager sessionManager;
    private TextView tvWelcome;
    private SharedPreferencesHelper prefsHelper;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialize views
        //tvWelcome = view.findViewById(R.id.tv_welcome);


        // Initialize SharedPreferencesHelper

        welcomeText = view.findViewById(R.id.welcomeText);
        topAppBar = view.findViewById(R.id.topAppBar);
        bottomNavigation = view.findViewById(R.id.bottomNavigation);
        cardAddMeal = view.findViewById(R.id.cardAddMeal);
        cardGroceryList = view.findViewById(R.id.cardGroceryList);
        cardManageItems = view.findViewById(R.id.cardManageItems);
        fabAddRecipe = view.findViewById(R.id.fabAddRecipe);
        buttonLogout = view.findViewById(R.id.buttonLogout);
        prefsHelper = new SharedPreferencesHelper(requireContext());

        cardGroceryList.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "Card Grocery List clicked");
                try {
                    // Launch GroceryListActivity
                    Intent intent = new Intent(getActivity(), GroceryListActivity.class);
                    startActivity(intent);
                    Log.d(TAG, "GroceryListActivity started successfully from card");
                } catch (Exception e) {
                    Log.e(TAG, "Error launching GroceryListActivity from card: " + e.getMessage(), e);
                    Toast.makeText(getActivity(), "Error opening Grocery List: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });

        cardManageItems.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Will be implemented to navigate to manage items screen
                Toast.makeText(getActivity(), "Manage Items feature coming soon!", Toast.LENGTH_SHORT).show();
            }
        });

        // Set welcome message
        String username = prefsHelper.getUsername();
        if (username != null && !username.isEmpty()) {
            tvWelcome.setText("Welcome, " + username + "!");
        }
    }
} 