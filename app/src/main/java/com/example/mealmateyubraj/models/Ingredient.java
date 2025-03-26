package com.example.mealmateyubraj.models;

import java.io.Serializable;

public class Ingredient implements Serializable {
    private long id;
    private String name;
    private double quantity;
    private String unit;
    private long mealId;

    public Ingredient() {
    }

    public Ingredient(String name, double quantity, String unit) {
        this.name = name;
        this.quantity = quantity;
        this.unit = unit;
    }

    public Ingredient(long id, String name, double quantity, String unit, long mealId) {
        this.id = id;
        this.name = name;
        this.quantity = quantity;
        this.unit = unit;
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

    public long getMealId() {
        return mealId;
    }

    public void setMealId(long mealId) {
        this.mealId = mealId;
    }

    @Override
    public String toString() {
        return quantity + " " + unit + " " + name;
    }
} 