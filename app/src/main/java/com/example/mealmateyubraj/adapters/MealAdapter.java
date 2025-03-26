package com.example.mealmateyubraj.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mealmateyubraj.R;
import com.example.mealmateyubraj.models.Meal;

import java.util.ArrayList;
import java.util.List;

public class MealAdapter extends RecyclerView.Adapter<MealAdapter.MealViewHolder> {
    private List<Meal> meals = new ArrayList<>();
    private OnItemClickListener listener;
    private Context context;

    public interface OnItemClickListener {
        void onItemClick(Meal meal);
        void onEditClick(Meal meal);
        void onDeleteClick(Meal meal);
        void onAddToCartClick(Meal meal);
    }

    public MealAdapter(Context context, OnItemClickListener listener) {
        this.context = context;
        this.listener = listener;
    }

    @NonNull
    @Override
    public MealViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_meal, parent, false);
        return new MealViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull MealViewHolder holder, int position) {
        Meal currentMeal = meals.get(position);
        holder.mealName.setText(currentMeal.getName());
        holder.mealDescription.setText(currentMeal.getDescription());
        holder.mealCalories.setText(String.format("%d calories", currentMeal.getCalories()));
        holder.mealTime.setText(currentMeal.getTime());
        
        // Set meal image if available
        if (currentMeal.getImageUrl() != null && !currentMeal.getImageUrl().isEmpty()) {
            // TODO: Load image using Glide or Picasso
            holder.mealImage.setImageResource(R.drawable.ic_restaurant);
        } else {
            holder.mealImage.setImageResource(R.drawable.ic_restaurant);
        }
    }

    @Override
    public int getItemCount() {
        return meals.size();
    }

    public void setMeals(List<Meal> meals) {
        this.meals = meals;
        notifyDataSetChanged();
    }

    public Meal getMealAt(int position) {
        return meals.get(position);
    }

    class MealViewHolder extends RecyclerView.ViewHolder {
        private TextView mealName;
        private TextView mealDescription;
        private TextView mealCalories;
        private TextView mealTime;
        private ImageView mealImage;
        private ImageButton moreOptionsButton;

        public MealViewHolder(@NonNull View itemView) {
            super(itemView);
            mealName = itemView.findViewById(R.id.mealName);
            mealDescription = itemView.findViewById(R.id.mealDescription);
            mealCalories = itemView.findViewById(R.id.mealCalories);
            mealTime = itemView.findViewById(R.id.mealTime);
            mealImage = itemView.findViewById(R.id.mealImage);
            moreOptionsButton = itemView.findViewById(R.id.moreOptionsButton);

            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (listener != null && position != RecyclerView.NO_POSITION) {
                    listener.onItemClick(meals.get(position));
                }
            });

            moreOptionsButton.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION) {
                    Meal meal = meals.get(position);
                    showOptionsMenu(v, meal);
                }
            });
        }

        private void showOptionsMenu(View view, Meal meal) {
            android.widget.PopupMenu popup = new android.widget.PopupMenu(context, view);
            popup.inflate(R.menu.meal_item_menu);

            popup.setOnMenuItemClickListener(item -> {
                int itemId = item.getItemId();
                if (itemId == R.id.action_edit) {
                    listener.onEditClick(meal);
                    return true;
                } else if (itemId == R.id.action_delete) {
                    listener.onDeleteClick(meal);
                    return true;
                } else if (itemId == R.id.action_add_to_cart) {
                    listener.onAddToCartClick(meal);
                    return true;
                }
                return false;
            });

            popup.show();
        }
    }
} 