package com.example.mealmateyubraj.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mealmateyubraj.R;
import com.example.mealmateyubraj.models.Ingredient;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Adapter for displaying ingredients in a RecyclerView
 */
public class IngredientAdapter extends RecyclerView.Adapter<IngredientAdapter.IngredientViewHolder> {

    // Interface for click callbacks
    public interface OnIngredientClickListener {
        void onDeleteIngredient(int position);
    }

    private final Context context;
    private List<Ingredient> ingredients;
    private OnIngredientClickListener listener;

    public IngredientAdapter(Context context, List<Ingredient> ingredients) {
        this.context = context;
        this.ingredients = ingredients != null ? ingredients : new ArrayList<>();
    }
    
    public IngredientAdapter(Context context, OnIngredientClickListener listener) {
        this.context = context;
        this.ingredients = new ArrayList<>();
        this.listener = listener;
    }
    
    public void setOnIngredientClickListener(OnIngredientClickListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public IngredientViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_ingredient, parent, false);
        return new IngredientViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull IngredientViewHolder holder, int position) {
        Ingredient ingredient = ingredients.get(position);
        
        // Format quantity and unit for display
        String quantityFormatted;
        double quantity = ingredient.getQuantity();
        if (quantity == Math.floor(quantity)) {
            // Display as integer if it's a whole number
            quantityFormatted = String.format(Locale.getDefault(), "%.0f", quantity);
        } else {
            // Display with decimal places
            quantityFormatted = String.format(Locale.getDefault(), "%.2f", quantity);
            // Remove trailing zeros
            if (quantityFormatted.endsWith(".00")) {
                quantityFormatted = quantityFormatted.substring(0, quantityFormatted.length() - 3);
            } else if (quantityFormatted.endsWith("0")) {
                quantityFormatted = quantityFormatted.substring(0, quantityFormatted.length() - 1);
            }
        }
        
        // Set ingredient name and quantity with unit
        holder.tvIngredientName.setText(ingredient.getName());
        
        // Handle special units like "to taste" or "pinch"
        String unit = ingredient.getUnit();
        if (unit != null && (unit.equals("to taste") || unit.equals("pinch"))) {
            holder.tvIngredientQuantity.setText(unit);
        } else if (quantity == 0) {
            // If quantity is 0, just show the unit
            holder.tvIngredientQuantity.setText(unit != null ? unit : "");
        } else {
            holder.tvIngredientQuantity.setText(String.format("%s %s", 
                    quantityFormatted, unit != null ? unit : ""));
        }
        
        // Set up delete button
        holder.btnDeleteIngredient.setOnClickListener(v -> {
            if (listener != null) {
                listener.onDeleteIngredient(holder.getAdapterPosition());
                notifyDataSetChanged(); // Refresh the list
            } else {
                // If no listener is set, just remove from the list directly
                if (position >= 0 && position < ingredients.size()) {
                    ingredients.remove(position);
                    notifyItemRemoved(position);
                    notifyItemRangeChanged(position, getItemCount());
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return ingredients.size();
    }

    /**
     * Updates the ingredient list with a new list
     *
     * @param ingredients The new list of ingredients
     */
    public void setIngredients(List<Ingredient> ingredients) {
        this.ingredients = ingredients;
        notifyDataSetChanged();
    }

    /**
     * Add a new ingredient to the list
     */
    public void addIngredient(Ingredient ingredient) {
        this.ingredients.add(ingredient);
        notifyItemInserted(ingredients.size() - 1);
    }

    /**
     * Remove an ingredient from the list
     */
    public void removeIngredient(int position) {
        if (position >= 0 && position < ingredients.size()) {
            ingredients.remove(position);
            notifyItemRemoved(position);
            notifyItemRangeChanged(position, getItemCount());
        }
    }

    /**
     * Get the current list of ingredients
     */
    public List<Ingredient> getIngredients() {
        return ingredients;
    }

    /**
     * ViewHolder for ingredient items
     */
    static class IngredientViewHolder extends RecyclerView.ViewHolder {
        TextView tvIngredientName;
        TextView tvIngredientQuantity;
        ImageButton btnDeleteIngredient;

        public IngredientViewHolder(@NonNull View itemView) {
            super(itemView);
            tvIngredientName = itemView.findViewById(R.id.tv_ingredient_name);
            tvIngredientQuantity = itemView.findViewById(R.id.tv_ingredient_quantity);
            btnDeleteIngredient = itemView.findViewById(R.id.btn_delete_ingredient);
        }
    }
} 