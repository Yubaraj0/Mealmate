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
import com.example.mealmateyubraj.activities.AddEditMealActivity;
import com.example.mealmateyubraj.activities.GroceryListActivity;
import com.example.mealmateyubraj.database.MealDao;
import com.example.mealmateyubraj.database.GroceryDao;
import com.example.mealmateyubraj.utils.SessionManager;
import com.google.android.material.button.MaterialButton;

public class HomeFragment extends Fragment {
    private static final String TAG = "HomeFragment";
    private SessionManager sessionManager;
    private TextView tvWelcome, textMealCount, textGroceryCount;
    private MaterialButton buttonAddMeal, buttonGroceryList;
    private MealDao mealDao;
    private GroceryDao groceryDao;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        // Initialize session manager and DAOs
        sessionManager = new SessionManager(requireContext());
        mealDao = new MealDao(requireContext());
        groceryDao = new GroceryDao(requireContext());

        // Initialize views
        tvWelcome = view.findViewById(R.id.welcomeText);
        textMealCount = view.findViewById(R.id.textMealCount);
        textGroceryCount = view.findViewById(R.id.textGroceryCount);
        buttonAddMeal = view.findViewById(R.id.buttonAddMeal);
        buttonGroceryList = view.findViewById(R.id.buttonGroceryList);

        // Set welcome message
        String userName = sessionManager.getUserName();
        if (userName != null && !userName.isEmpty()) {
            tvWelcome.setText("Welcome, " + userName + "!");
        } else {
            tvWelcome.setText("Welcome to MealMate!");
        }

        // Load statistics
        loadStatistics();

        // Set up click listeners
        buttonAddMeal.setOnClickListener(v -> {
            try {
                long userId = sessionManager.getUserId();
                Intent intent = AddEditMealActivity.createIntent(requireContext(), "add", userId);
                startActivity(intent);
            } catch (Exception e) {
                Log.e(TAG, "Error launching AddEditMealActivity: " + e.getMessage(), e);
                Toast.makeText(requireContext(), "Error launching Add Meal screen: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        buttonGroceryList.setOnClickListener(v -> {
            try {
                Intent intent = new Intent(requireContext(), GroceryListActivity.class);
                startActivity(intent);
            } catch (Exception e) {
                Log.e(TAG, "Error launching GroceryListActivity: " + e.getMessage(), e);
                Toast.makeText(requireContext(), "Error launching Grocery List screen: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        return view;
    }

    private void loadStatistics() {
        try {
            long userId = sessionManager.getUserId();
            
            // Get meal count
            int mealCount = mealDao.getAllMealsByUserId(userId).size();
            textMealCount.setText(String.valueOf(mealCount));
            
            // Get grocery count
            int groceryCount = groceryDao.getGroceryListsCountByUserId(userId);
            textGroceryCount.setText(String.valueOf(groceryCount));
            
        } catch (Exception e) {
            Log.e(TAG, "Error loading statistics: " + e.getMessage(), e);
            Toast.makeText(requireContext(), "Error loading statistics: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        loadStatistics();
    }
}