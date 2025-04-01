package com.example.mealmateyubraj.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mealmateyubraj.R;
import com.example.mealmateyubraj.models.GroceryItem;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GroceryAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    public static final int TYPE_HEADER = 0;
    public static final int TYPE_ITEM = 1;
    
    private final Context context;
    private final List<Object> items = new ArrayList<>();
    private final Map<String, Integer> categoryPositions = new HashMap<>();
    private OnGroceryItemClickListener listener;
    
    public interface OnGroceryItemClickListener {
        void onCheckboxClicked(GroceryItem item, boolean isChecked);
        void onDeleteClicked(GroceryItem item);
        void onEditClicked(GroceryItem item);
    }
    
    public GroceryAdapter(Context context) {
        this.context = context;
    }
    
    public void setOnGroceryItemClickListener(OnGroceryItemClickListener listener) {
        this.listener = listener;
    }
    
    public void setItems(List<GroceryItem> groceryItems) {
        // Clear existing items
        items.clear();
        categoryPositions.clear();
        
        if (groceryItems == null || groceryItems.isEmpty()) {
            notifyDataSetChanged();
            return;
        }
        
        // Sort items by category and name
        Collections.sort(groceryItems, (item1, item2) -> {
            int categoryCompare = item1.getCategory().compareTo(item2.getCategory());
            if (categoryCompare != 0) {
                return categoryCompare;
            }
            return item1.getItemName().compareTo(item2.getItemName());
        });
        
        // Group items by category
        String currentCategory = null;
        
        for (GroceryItem item : groceryItems) {
            if (!item.getCategory().equals(currentCategory)) {
                currentCategory = item.getCategory();
                categoryPositions.put(currentCategory, items.size());
                items.add(currentCategory); // Add category header
            }
            items.add(item); // Add item
        }
        
        notifyDataSetChanged();
    }
    
    public void updateItem(GroceryItem item) {
        for (int i = 0; i < items.size(); i++) {
            if (items.get(i) instanceof GroceryItem) {
                GroceryItem existingItem = (GroceryItem) items.get(i);
                if (existingItem.getId() == item.getId()) {
                    items.set(i, item);
                    notifyItemChanged(i);
                    break;
                }
            }
        }
    }
    
    public void removeItem(GroceryItem item) {
        for (int i = 0; i < items.size(); i++) {
            if (items.get(i) instanceof GroceryItem) {
                GroceryItem existingItem = (GroceryItem) items.get(i);
                if (existingItem.getId() == item.getId()) {
                    // Remove the item
                    items.remove(i);
                    notifyItemRemoved(i);
                    
                    // Check if the category is now empty
                    checkCategoryEmpty(item.getCategory());
                    break;
                }
            }
        }
    }
    
    private void checkCategoryEmpty(String category) {
        if (!categoryPositions.containsKey(category)) {
            return;
        }
        
        int headerPosition = categoryPositions.get(category);
        boolean hasItems = false;
        
        // Check if there are any items with this category
        for (Object obj : items) {
            if (obj instanceof GroceryItem) {
                GroceryItem item = (GroceryItem) obj;
                if (item.getCategory().equals(category)) {
                    hasItems = true;
                    break;
                }
            }
        }
        
        // If no items in this category, remove the header
        if (!hasItems && headerPosition < items.size() && items.get(headerPosition) instanceof String) {
            items.remove(headerPosition);
            notifyItemRemoved(headerPosition);
            
            // Update category positions
            updateCategoryPositions();
        }
    }
    
    private void updateCategoryPositions() {
        categoryPositions.clear();
        for (int i = 0; i < items.size(); i++) {
            if (items.get(i) instanceof String) {
                categoryPositions.put((String) items.get(i), i);
            }
        }
    }
    
    @Override
    public int getItemViewType(int position) {
        return items.get(position) instanceof String ? TYPE_HEADER : TYPE_ITEM;
    }
    
    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == TYPE_HEADER) {
            View view = LayoutInflater.from(context).inflate(R.layout.item_grocery_category_header, parent, false);
            return new HeaderViewHolder(view);
        } else {
            View view = LayoutInflater.from(context).inflate(R.layout.item_grocery, parent, false);
            return new GroceryViewHolder(view);
        }
    }
    
    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof HeaderViewHolder) {
            HeaderViewHolder headerHolder = (HeaderViewHolder) holder;
            String category = (String) items.get(position);
            headerHolder.tvCategoryName.setText(category);
        } else if (holder instanceof GroceryViewHolder) {
            GroceryViewHolder groceryHolder = (GroceryViewHolder) holder;
            GroceryItem item = (GroceryItem) items.get(position);
            
            // Set item data
            groceryHolder.tvGroceryName.setText(item.getItemName());
            groceryHolder.tvGroceryQuantity.setText(String.format("%s %s", item.getQuantity(), item.getUnit()));
            groceryHolder.checkboxGrocery.setChecked(item.isPurchased());
            
            // Show/hide purchased text
            groceryHolder.tvPurchasedStatus.setVisibility(item.isPurchased() ? View.VISIBLE : View.GONE);
            
            // Apply strikethrough style if purchased
            if (item.isPurchased()) {
                groceryHolder.checkboxGrocery.setAlpha(0.5f);
                groceryHolder.tvGroceryName.setPaintFlags(groceryHolder.tvGroceryName.getPaintFlags() | android.graphics.Paint.STRIKE_THRU_TEXT_FLAG);
                groceryHolder.tvGroceryQuantity.setPaintFlags(groceryHolder.tvGroceryQuantity.getPaintFlags() | android.graphics.Paint.STRIKE_THRU_TEXT_FLAG);
            } else {
                groceryHolder.checkboxGrocery.setAlpha(1.0f);
                groceryHolder.tvGroceryName.setPaintFlags(groceryHolder.tvGroceryName.getPaintFlags() & ~android.graphics.Paint.STRIKE_THRU_TEXT_FLAG);
                groceryHolder.tvGroceryQuantity.setPaintFlags(groceryHolder.tvGroceryQuantity.getPaintFlags() & ~android.graphics.Paint.STRIKE_THRU_TEXT_FLAG);
            }
            
            // Set listeners
            groceryHolder.checkboxGrocery.setOnClickListener(v -> {
                boolean isChecked = ((CheckBox) v).isChecked();
                if (listener != null) {
                    listener.onCheckboxClicked(item, isChecked);
                }
            });
            
            groceryHolder.ivDeleteGrocery.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onDeleteClicked(item);
                }
            });
            
            groceryHolder.ivEditGrocery.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onEditClicked(item);
                }
            });
        }
    }
    
    @Override
    public int getItemCount() {
        return items.size();
    }
    
    public void addItem(GroceryItem newItem) {
        // Find the correct position to insert the item
        int insertPosition = findInsertPosition(newItem);
        
        // If this is a new category, add the header first
        if (!categoryPositions.containsKey(newItem.getCategory())) {
            items.add(insertPosition, newItem.getCategory());
            categoryPositions.put(newItem.getCategory(), insertPosition);
            insertPosition++;
        }
        
        // Add the item
        items.add(insertPosition, newItem);
        notifyItemInserted(insertPosition);
        
        // Update category positions for all categories after this one
        updateCategoryPositions();
    }
    
    private int findInsertPosition(GroceryItem newItem) {
        // If no items exist, return 0
        if (items.isEmpty()) {
            return 0;
        }
        
        // Find the position of the category header
        Integer categoryPosition = categoryPositions.get(newItem.getCategory());
        if (categoryPosition == null) {
            // If category doesn't exist, find where to insert it
            for (int i = 0; i < items.size(); i++) {
                if (items.get(i) instanceof String) {
                    String category = (String) items.get(i);
                    if (category.compareTo(newItem.getCategory()) > 0) {
                        return i;
                    }
                }
            }
            return items.size();
        }
        
        // Find the position within the category
        int startPos = categoryPosition + 1;
        for (int i = startPos; i < items.size(); i++) {
            if (items.get(i) instanceof String) {
                // We've reached the next category
                return i;
            }
            GroceryItem item = (GroceryItem) items.get(i);
            if (item.getItemName().compareTo(newItem.getItemName()) > 0) {
                return i;
            }
        }
        
        return items.size();
    }
    
    public Object getItemAt(int position) {
        if (position >= 0 && position < items.size()) {
            return items.get(position);
        }
        return null;
    }
    
    /**
     * ViewHolder for category headers
     */
    static class HeaderViewHolder extends RecyclerView.ViewHolder {
        TextView tvCategoryName;
        
        HeaderViewHolder(View itemView) {
            super(itemView);
            tvCategoryName = itemView.findViewById(R.id.tv_category_name);
        }
    }
    
    /**
     * ViewHolder for grocery items
     */
    public static class GroceryViewHolder extends RecyclerView.ViewHolder {
        public CheckBox checkboxGrocery;
        public TextView tvGroceryName;
        public TextView tvGroceryQuantity;
        public TextView tvPurchasedStatus;
        public ImageView ivDeleteGrocery;
        public ImageView ivEditGrocery;
        
        public GroceryViewHolder(View itemView) {
            super(itemView);
            checkboxGrocery = itemView.findViewById(R.id.checkbox_grocery);
            tvGroceryName = itemView.findViewById(R.id.tv_grocery_name);
            tvGroceryQuantity = itemView.findViewById(R.id.tv_grocery_quantity);
            tvPurchasedStatus = itemView.findViewById(R.id.tv_purchased_status);
            ivDeleteGrocery = itemView.findViewById(R.id.iv_delete_grocery);
            ivEditGrocery = itemView.findViewById(R.id.iv_edit_grocery);
        }
    }

    public List<GroceryItem> getItems() {
        List<GroceryItem> groceryItems = new ArrayList<>();
        for (Object obj : items) {
            if (obj instanceof GroceryItem) {
                groceryItems.add((GroceryItem) obj);
            }
        }
        return groceryItems;
    }
} 