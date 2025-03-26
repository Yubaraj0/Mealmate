package com.example.mealmateyubraj.adapters;

import android.content.Context;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mealmateyubraj.R;
import com.example.mealmateyubraj.models.Ingredient;
import com.example.mealmateyubraj.models.Meal;

import java.io.File;
import java.util.List;

/**
 * Adapter for handling recent meals in a RecyclerView
 */
public class RecentMealAdapter extends RecyclerView.Adapter<RecentMealAdapter.ViewHolder> {

    // Interface for handling click events
    public interface OnRecentMealClickListener {
        void onEditMealClick(Meal meal);
        void onDeleteMealClick(Meal meal);
        void onAddToCartClick(Meal meal);
    }

    private final Context context;
    private List<Meal> recentMeals;
    private final OnRecentMealClickListener listener;

    public RecentMealAdapter(Context context, List<Meal> recentMeals, OnRecentMealClickListener listener) {
        this.context = context;
        this.recentMeals = recentMeals;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_recent_meal, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Meal meal = recentMeals.get(position);
        
        // Set meal name and category
        holder.tvMealName.setText(meal.getName());
        holder.tvCategory.setText(meal.getCategory());
        
        // Set preparation time
        holder.tvPrepTime.setText(String.format("Prep: %d mins", meal.getPrepTime()));
        
        // Set meal image if available
        if (meal.getImagePath() != null && !meal.getImagePath().isEmpty()) {
            File imgFile = new File(meal.getImagePath());
            if (imgFile.exists()) {
                holder.ivMealImage.setImageBitmap(BitmapFactory.decodeFile(imgFile.getAbsolutePath()));
            } else {
                holder.ivMealImage.setImageResource(R.drawable.ic_menu_gallery);
            }
        } else {
            holder.ivMealImage.setImageResource(R.drawable.ic_menu_gallery);
        }
        
        // Set ingredients
        StringBuilder ingredientsText = new StringBuilder();
        List<Ingredient> ingredients = meal.getIngredients();
        if (ingredients != null && !ingredients.isEmpty()) {
            for (int i = 0; i < ingredients.size(); i++) {
                Ingredient ingredient = ingredients.get(i);
                ingredientsText.append("• ").append(ingredient.getName());
                if (ingredient.getQuantity() > 0) {
                    ingredientsText.append(" - ").append(ingredient.getQuantity());
                    if (ingredient.getUnit() != null && !ingredient.getUnit().isEmpty()) {
                        ingredientsText.append(" ").append(ingredient.getUnit());
                    }
                }
                if (i < ingredients.size() - 1) {
                    ingredientsText.append("\n");
                }
            }
            holder.tvIngredients.setText(ingredientsText.toString());
            holder.tvIngredients.setVisibility(View.VISIBLE);
        } else {
            holder.tvIngredients.setText("No ingredients added");
            holder.tvIngredients.setVisibility(View.GONE);
        }
        
        // Set instructions
        String instructions = meal.getInstructions();
        if (instructions != null && !instructions.isEmpty()) {
            // If instructions are already formatted with bullet points, display as is
            if (instructions.contains("• ")) {
                holder.tvInstructions.setText(instructions);
            } else {
                // Format instructions with bullet points if not already formatted
                String[] instructionSteps = instructions.split("\n");
                StringBuilder formattedInstructions = new StringBuilder();
                for (int i = 0; i < instructionSteps.length; i++) {
                    String step = instructionSteps[i].trim();
                    if (!step.isEmpty()) {
                        formattedInstructions.append("• ").append(step);
                        if (i < instructionSteps.length - 1) {
                            formattedInstructions.append("\n");
                        }
                    }
                }
                holder.tvInstructions.setText(formattedInstructions.toString());
            }
            holder.tvInstructions.setVisibility(View.VISIBLE);
        } else {
            holder.tvInstructions.setText("No instructions added");
            holder.tvInstructions.setVisibility(View.GONE);
        }
        
        // Set click listeners for edit and delete buttons
        holder.btnEdit.setOnClickListener(v -> {
            if (listener != null) {
                listener.onEditMealClick(meal);
            }
        });
        
        holder.btnDelete.setOnClickListener(v -> {
            if (listener != null) {
                listener.onDeleteMealClick(meal);
            }
        });
        
        // Set click listener for the add to cart button
        holder.layoutAddToCart.setOnClickListener(v -> {
            if (listener != null) {
                listener.onAddToCartClick(meal);
            }
        });
    }

    @Override
    public int getItemCount() {
        return recentMeals != null ? recentMeals.size() : 0;
    }

    /**
     * Update the list of recent meals
     */
    public void setRecentMeals(List<Meal> recentMeals) {
        this.recentMeals = recentMeals;
        notifyDataSetChanged();
    }

    /**
     * ViewHolder for recent meal items
     */
    public static class ViewHolder extends RecyclerView.ViewHolder {
        final ImageView ivMealImage;
        final TextView tvMealName;
        final TextView tvCategory;
        final TextView tvPrepTime;
        final TextView tvIngredients;
        final TextView tvInstructions;
        final ImageButton btnEdit;
        final ImageButton btnDelete;
        final View layoutAddToCart;
        
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivMealImage = itemView.findViewById(R.id.iv_meal_image);
            tvMealName = itemView.findViewById(R.id.tv_meal_name);
            tvCategory = itemView.findViewById(R.id.tv_category);
            tvPrepTime = itemView.findViewById(R.id.tv_prep_time);
            tvIngredients = itemView.findViewById(R.id.tv_ingredients);
            tvInstructions = itemView.findViewById(R.id.tv_instructions);
            btnEdit = itemView.findViewById(R.id.btn_edit);
            btnDelete = itemView.findViewById(R.id.btn_delete);
            layoutAddToCart = itemView.findViewById(R.id.layout_add_to_cart);
        }
    }
} 