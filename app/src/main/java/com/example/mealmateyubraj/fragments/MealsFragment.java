package com.example.mealmateyubraj.fragments;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mealmateyubraj.R;
import com.example.mealmateyubraj.activities.AddEditMealActivity;
import com.example.mealmateyubraj.activities.GroceryListActivity;
import com.example.mealmateyubraj.adapters.MealAdapter;
import com.example.mealmateyubraj.database.MealDao;
import com.example.mealmateyubraj.models.Meal;
import com.example.mealmateyubraj.utils.SessionManager;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

public class MealsFragment extends Fragment implements MealAdapter.OnItemClickListener {
    private static final String TAG = "MealsFragment";
    
    private RecyclerView recyclerView;
    private TextView textViewNoMeals;
    private MealAdapter mealAdapter;
    private MealDao mealDao;
    private List<Meal> mealList;
    private FloatingActionButton fabAddMeal;
    private SessionManager sessionManager;
    
    private ActivityResultLauncher<Intent> addMealLauncher;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Initialize the activity result launcher
        addMealLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == android.app.Activity.RESULT_OK) {
                    Log.d(TAG, "Meal added successfully, refreshing list");
                    loadMeals(); // Refresh the list when a meal is added
                }
            });
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_meals, container, false);
        
        try {
            // Initialize views
            recyclerView = view.findViewById(R.id.recyclerViewMeals);
            textViewNoMeals = view.findViewById(R.id.textViewNoMeals);
            fabAddMeal = view.findViewById(R.id.fab_add_meal);
            
            // Initialize session manager
            sessionManager = new SessionManager(requireContext());
            
            // Initialize database
            mealDao = new MealDao(requireContext());
            mealList = new ArrayList<>();
            
            // Setup RecyclerView
            recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
            mealAdapter = new MealAdapter(requireContext(), this);
            recyclerView.setAdapter(mealAdapter);
            
            // Setup FAB click listener
            fabAddMeal.setOnClickListener(v -> launchAddMealActivity());
            
            // Load meals initially
            loadMeals();
            
            return view;
        } catch (Exception e) {
            Log.e(TAG, "Error in onCreateView: " + e.getMessage(), e);
            Toast.makeText(requireContext(), "Error initializing view: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            return view;
        }
    }

    private void launchAddMealActivity() {
        try {
            // Get current user ID from SessionManager
            long userId = sessionManager.getUserId();
            
            if (userId == -1) {
                Toast.makeText(requireContext(), "Please log in again", Toast.LENGTH_SHORT).show();
                return;
            }
            
            // Launch AddEditMealActivity using createIntent method
            Intent intent = AddEditMealActivity.createIntent(requireContext(), "add", userId);
            addMealLauncher.launch(intent);
            
            Log.d(TAG, "Launching AddEditMealActivity with userId: " + userId);
        } catch (Exception e) {
            Log.e(TAG, "Error launching AddEditMealActivity: " + e.getMessage(), e);
            Toast.makeText(requireContext(), "Error opening Add Meal screen: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void loadMeals() {
        try {
            Log.d(TAG, "Loading meals...");
            
            // Get current user ID from SessionManager
            long userId = sessionManager.getUserId();
            
            if (userId == -1) {
                Log.e(TAG, "Invalid user ID");
                showNoMealsView();
                return;
            }
            
            // Initialize MealDao and get meals
            mealDao.open();
            List<Meal> meals = mealDao.getAllMealsByUserId(userId);
            mealDao.close();
            
            Log.d(TAG, "Found " + meals.size() + " meals for user " + userId);
            
            if (meals.isEmpty()) {
                showNoMealsView();
            } else {
                showMealsView(meals);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error loading meals: " + e.getMessage(), e);
            showErrorView(e.getMessage());
        }
    }

    private void showNoMealsView() {
        if (textViewNoMeals != null && recyclerView != null) {
            textViewNoMeals.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
        }
    }

    private void showMealsView(List<Meal> meals) {
        if (textViewNoMeals != null && recyclerView != null && mealAdapter != null) {
            Log.d(TAG, "Showing " + meals.size() + " meals");
            textViewNoMeals.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
            mealAdapter.setMeals(meals);
            mealAdapter.notifyDataSetChanged(); // Force refresh the adapter
        }
    }

    private void showErrorView(String errorMessage) {
        if (textViewNoMeals != null && recyclerView != null) {
            textViewNoMeals.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
            Toast.makeText(requireContext(), "Error loading meals: " + errorMessage, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        loadMeals(); // Reload meals when returning to this fragment
    }

    @Override
    public void onItemClick(Meal meal) {
        try {
            // Launch AddEditMealActivity in view mode
            Intent intent = AddEditMealActivity.createIntent(requireContext(), "view", meal.getUserId());
            intent.putExtra("meal_id", meal.getId());
            startActivity(intent);
        } catch (Exception e) {
            Log.e(TAG, "Error viewing meal: " + e.getMessage(), e);
            Toast.makeText(requireContext(), "Error viewing meal: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onEditClick(Meal meal) {
        try {
            // Launch AddEditMealActivity in edit mode
            Intent intent = AddEditMealActivity.createIntent(requireContext(), "edit", meal.getUserId());
            intent.putExtra("meal_id", meal.getId());
            addMealLauncher.launch(intent);
        } catch (Exception e) {
            Log.e(TAG, "Error editing meal: " + e.getMessage(), e);
            Toast.makeText(requireContext(), "Error editing meal: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onDeleteClick(Meal meal) {
        try {
            mealDao.open();
            mealDao.deleteMeal(meal.getId());
            mealDao.close();
            loadMeals();
            Toast.makeText(getContext(), "Meal deleted", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Log.e(TAG, "Error deleting meal: " + e.getMessage(), e);
            Toast.makeText(requireContext(), "Error deleting meal: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onAddToCartClick(Meal meal) {
        try {
            Intent intent = new Intent(requireActivity(), GroceryListActivity.class);
            intent.putExtra("meal_id", meal.getId());
            startActivity(intent);
            Toast.makeText(getContext(), "Added to grocery list", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Log.e(TAG, "Error adding to grocery list: " + e.getMessage(), e);
            Toast.makeText(requireContext(), "Error adding to grocery list: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
}