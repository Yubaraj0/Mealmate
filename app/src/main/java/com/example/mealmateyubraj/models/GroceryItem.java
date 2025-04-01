package com.example.mealmateyubraj.models;

import java.io.Serializable;

public class GroceryItem implements Serializable {
    private long id;
    private long userId;
    private String itemName;
    private String quantity;
    private String unit;
    private boolean purchased;
    private String category;
    private long mealId;

    public GroceryItem() {
    }

    public GroceryItem(String itemName, String quantity, String unit) {
        this.itemName = itemName;
        this.quantity = quantity;
        this.unit = unit;
        this.purchased = false;
    }

    public GroceryItem(long id, String name, String category, float quantity, String unit, 
                      boolean isPurchased, long userId, long mealId) {
        this.id = id;
        this.itemName = name;
        this.category = category;
        this.quantity = String.valueOf(quantity);
        this.unit = unit;
        this.purchased = isPurchased;
        this.userId = userId;
        this.mealId = mealId;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getUserId() {
        return userId;
    }

    public void setUserId(long userId) {
        this.userId = userId;
    }

    public String getItemName() {
        return itemName;
    }

    public void setItemName(String itemName) {
        this.itemName = itemName;
    }

    public String getQuantity() {
        return quantity;
    }

    public void setQuantity(String quantity) {
        this.quantity = quantity;
    }

    public void setQuantity(float quantity) {
        this.quantity = String.valueOf(quantity);
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public boolean isPurchased() {
        return purchased;
    }

    public void setPurchased(boolean purchased) {
        this.purchased = purchased;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public long getMealId() {
        return mealId;
    }

    public void setMealId(long mealId) {
        this.mealId = mealId;
    }

    // Alias methods for compatibility
    public String getName() {
        return itemName;
    }

    public void setName(String name) {
        this.itemName = name;
    }

    // Compatibility method for quantity as float
    public float getQuantityAsFloat() {
        try {
            return Float.parseFloat(quantity);
        } catch (NumberFormatException e) {
            return 0.0f;
        }
    }

    @Override
    public String toString() {
        return String.format("%s %s %s%s", quantity, unit, itemName, purchased ? " (✓)" : "");
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        
        GroceryItem that = (GroceryItem) o;
        
        return id == that.id;
    }
    
    @Override
    public int hashCode() {
        return (int) (id ^ (id >>> 32));
    }
} 