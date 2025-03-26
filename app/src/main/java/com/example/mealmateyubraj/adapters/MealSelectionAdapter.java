package com.example.mealmateyubraj.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mealmateyubraj.R;
import com.example.mealmateyubraj.models.Meal;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class MealSelectionAdapter extends RecyclerView.Adapter<MealSelectionAdapter.MealSelectionViewHolder> {
    private final Context context;
    private final List<Meal> meals = new ArrayList<>();
    private final Set<Long> selectedMealIds = new HashSet<>();
    
    public MealSelectionAdapter(Context context) {
        this.context = context;
    }
    
    public void setMeals(List<Meal> meals) {
        this.meals.clear();
        if (meals != null) {
            this.meals.addAll(meals);
        }
        notifyDataSetChanged();
    }
    
    public void toggleMealSelection(int position) {
        if (position >= 0 && position < meals.size()) {
            Meal meal = meals.get(position);
            if (selectedMealIds.contains(meal.getId())) {
                selectedMealIds.remove(meal.getId());
            } else {
                selectedMealIds.add(meal.getId());
            }
            notifyItemChanged(position);
        }
    }
    
    public List<Meal> getSelectedMeals() {
        List<Meal> selectedMeals = new ArrayList<>();
        for (Meal meal : meals) {
            if (selectedMealIds.contains(meal.getId())) {
                selectedMeals.add(meal);
            }
        }
        return selectedMeals;
    }
    
    @NonNull
    @Override
    public MealSelectionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_meal_selection, parent, false);
        return new MealSelectionViewHolder(view);
    }
    
    @Override
    public void onBindViewHolder(@NonNull MealSelectionViewHolder holder, int position) {
        Meal meal = meals.get(position);
        holder.checkboxMeal.setText(meal.getName());
        holder.tvCategory.setText(meal.getCategory());
        holder.checkboxMeal.setChecked(selectedMealIds.contains(meal.getId()));
        
        holder.itemView.setOnClickListener(v -> {
            toggleMealSelection(position);
        });
        
        holder.checkboxMeal.setOnClickListener(v -> {
            toggleMealSelection(position);
        });
    }
    
    @Override
    public int getItemCount() {
        return meals.size();
    }
    
    static class MealSelectionViewHolder extends RecyclerView.ViewHolder {
        CheckBox checkboxMeal;
        TextView tvCategory;
        
        MealSelectionViewHolder(View itemView) {
            super(itemView);
            checkboxMeal = itemView.findViewById(R.id.checkbox_meal);
            tvCategory = itemView.findViewById(R.id.tv_meal_category);
        }
    }
} 