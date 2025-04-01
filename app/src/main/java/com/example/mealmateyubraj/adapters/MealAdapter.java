package com.example.mealmateyubraj.adapters;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mealmateyubraj.R;
import com.example.mealmateyubraj.models.Meal;

import java.util.ArrayList;
import java.util.List;

public class MealAdapter extends RecyclerView.Adapter<MealAdapter.MealViewHolder> {
    private static final String TAG = "MealAdapter";
    private final Context context;
    private List<Meal> meals;
    private final OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(Meal meal);
        void onEditClick(Meal meal);
        void onDeleteClick(Meal meal);
        void onAddToCartClick(Meal meal);
    }

    public MealAdapter(Context context, OnItemClickListener listener) {
        this.context = context;
        this.listener = listener;
        this.meals = new ArrayList<>();
    }

    @NonNull
    @Override
    public MealViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_meal, parent, false);
        return new MealViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MealViewHolder holder, int position) {
        try {
            Meal meal = meals.get(position);
            
            // Set meal name
            holder.tvMealName.setText(meal.getName());
            
            // Set meal category if available
            if (meal.getCategory() != null && !meal.getCategory().isEmpty()) {
                holder.tvCategory.setText(meal.getCategory());
                holder.tvCategory.setVisibility(View.VISIBLE);
            } else {
                holder.tvCategory.setVisibility(View.GONE);
            }
            
            // Set click listeners
            holder.itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onItemClick(meal);
                }
            });
            
            holder.btnEdit.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onEditClick(meal);
                }
            });
            
            holder.btnDelete.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onDeleteClick(meal);
                }
            });
            
            holder.btnAddToCart.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onAddToCartClick(meal);
                }
            });
            
        } catch (Exception e) {
            Log.e(TAG, "Error binding meal at position " + position + ": " + e.getMessage(), e);
        }
    }

    @Override
    public int getItemCount() {
        return meals != null ? meals.size() : 0;
    }

    public void setMeals(List<Meal> newMeals) {
        Log.d(TAG, "Setting new meals list with " + (newMeals != null ? newMeals.size() : 0) + " items");
        this.meals = newMeals != null ? newMeals : new ArrayList<>();
        notifyDataSetChanged();
    }

    static class MealViewHolder extends RecyclerView.ViewHolder {
        TextView tvMealName;
        TextView tvCategory;
        ImageButton btnEdit;
        ImageButton btnDelete;
        ImageButton btnAddToCart;

        MealViewHolder(@NonNull View itemView) {
            super(itemView);
            tvMealName = itemView.findViewById(R.id.tv_meal_name);
            tvCategory = itemView.findViewById(R.id.tv_category);
            btnEdit = itemView.findViewById(R.id.btn_edit);
            btnDelete = itemView.findViewById(R.id.btn_delete);
            btnAddToCart = itemView.findViewById(R.id.btn_add_to_cart);
        }
    }
}