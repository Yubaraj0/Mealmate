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
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mealmateyubraj.R;
import com.example.mealmateyubraj.activities.AddEditMealActivity;
import com.example.mealmateyubraj.activities.GroceryListActivity;
import com.example.mealmateyubraj.adapters.MealAdapter;
import com.example.mealmateyubraj.database.MealDao;
import com.example.mealmateyubraj.models.Meal;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

public class MealsFragment extends Fragment implements MealAdapter.OnItemClickListener {
    private RecyclerView recyclerView;
    private MealAdapter mealAdapter;
    private MealDao mealDao;
    private List<Meal> mealList;
    private FloatingActionButton fabAddMeal;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_meals, container, false);
        
        // Initialize views
        recyclerView = view.findViewById(R.id.rv_meals);
        fabAddMeal = view.findViewById(R.id.fab_add_meal);
        
        // Initialize database
        mealDao = new MealDao(requireContext());
        mealList = new ArrayList<>();
        
        // Setup RecyclerView
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        mealAdapter = new MealAdapter(requireContext(), this);
        recyclerView.setAdapter(mealAdapter);
        
        // Load meals
        loadMeals();
        
        // Setup FAB click listener
        fabAddMeal.setOnClickListener(v -> {
            Intent intent = AddEditMealActivity.createIntent(requireActivity(), "add", 0);
            startActivity(intent);
        });
        
        return view;
    }

    private void loadMeals() {
        mealDao.open();
        List<Meal> meals = mealDao.getAllMeals();
        mealDao.close();
        mealAdapter.setMeals(meals);
    }

    @Override
    public void onItemClick(Meal meal) {
        // View meal details
        Intent intent = AddEditMealActivity.createIntent(requireActivity(), "view", meal.getUserId());
        intent.putExtra("meal_id", meal.getId());
        startActivity(intent);
    }

    @Override
    public void onEditClick(Meal meal) {
        // Edit meal
        Intent intent = AddEditMealActivity.createIntent(requireActivity(), "edit", meal.getUserId());
        intent.putExtra("meal_id", meal.getId());
        startActivity(intent);
    }

    @Override
    public void onDeleteClick(Meal meal) {
        // Delete meal
        mealDao.open();
        mealDao.deleteMeal(meal.getId());
        mealDao.close();
        loadMeals();
        Toast.makeText(getContext(), "Meal deleted", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onAddToCartClick(Meal meal) {
        // Add meal ingredients to grocery list
        Intent intent = new Intent(requireActivity(), GroceryListActivity.class);
        intent.putExtra("meal_id", meal.getId());
        startActivity(intent);
        Toast.makeText(getContext(), "Added to grocery list", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onResume() {
        super.onResume();
        loadMeals();
    }
} 