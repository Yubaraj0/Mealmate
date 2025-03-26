package com.example.mealmateyubraj.models;

import java.io.Serializable;

public class GroceryItem implements Serializable {
    private long id;
    private String name;
    private String category;
    private double quantity;
    private String unit;
    private boolean purchased;
    private long userId;
    private long mealId;

    public GroceryItem() {
    }

    public GroceryItem(String name, double quantity, String unit, long userId) {
        this.name = name;
        this.quantity = quantity;
        this.unit = unit;
        this.userId = userId;
        this.purchased = false;
        this.category = "General";
        this.mealId = 0;
    }

    public GroceryItem(long id, String name, String category, float quantity, String unit, boolean purchased, long userId, long mealId) {
        this.id = id;
        this.name = name;
        this.category = category;
        this.quantity = quantity;
        this.unit = unit;
        this.purchased = purchased;
        this.userId = userId;
        this.mealId = mealId;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public double getQuantity() {
        return quantity;
    }

    public void setQuantity(double quantity) {
        this.quantity = quantity;
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

    public long getUserId() {
        return userId;
    }

    public void setUserId(long userId) {
        this.userId = userId;
    }

    public long getMealId() {
        return mealId;
    }

    public void setMealId(long mealId) {
        this.mealId = mealId;
    }

    @Override
    public String toString() {
        return String.format("%s %.1f %s%s", name, quantity, unit, purchased ? " (✓)" : "");
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