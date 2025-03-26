package com.example.mealmateyubraj.models;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Model class representing a meal/recipe
 */
public class Meal implements Serializable {
    private long id;
    private String name;
    private String description;
    private String category;
    private String instructions;
    private String imagePath;
    private long userId;
    private int prepTime;
    private List<Ingredient> ingredients;
    private int calories;
    private String time;
    private String imageUrl;

    /**
     * Default constructor
     */
    public Meal() {
        ingredients = new ArrayList<>();
    }

    /**
     * Constructor for creating a new meal
     *
     * @param name        The meal name
     * @param description The meal description
     * @param category    The meal category
     * @param userId      The ID of the user who created the meal
     */
    public Meal(String name, String description, String category, long userId) {
        this.name = name;
        this.description = description;
        this.category = category;
        this.userId = userId;
        this.ingredients = new ArrayList<>();
    }

    /**
     * Full constructor for creating a meal with all fields
     *
     * @param id          The meal ID
     * @param name        The meal name
     * @param description The meal description
     * @param category    The meal category
     * @param instructions The meal preparation instructions
     * @param imagePath   The path to the meal image
     * @param userId      The ID of the user who created the meal
     * @param prepTime    The preparation time in minutes
     */
    public Meal(long id, String name, String description, String category, 
                String instructions, String imagePath, long userId, int prepTime) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.category = category;
        this.instructions = instructions;
        this.imagePath = imagePath;
        this.userId = userId;
        this.prepTime = prepTime;
        this.ingredients = new ArrayList<>();
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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getInstructions() {
        return instructions;
    }

    public void setInstructions(String instructions) {
        this.instructions = instructions;
    }

    public String getImagePath() {
        return imagePath;
    }

    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
    }

    public long getUserId() {
        return userId;
    }

    public void setUserId(long userId) {
        this.userId = userId;
    }

    public int getPrepTime() {
        return prepTime;
    }

    public void setPrepTime(int prepTime) {
        this.prepTime = prepTime;
    }

    public List<Ingredient> getIngredients() {
        return ingredients;
    }

    public void setIngredients(List<Ingredient> ingredients) {
        this.ingredients = ingredients;
    }

    public void addIngredient(Ingredient ingredient) {
        if (ingredients == null) {
            ingredients = new ArrayList<>();
        }
        ingredients.add(ingredient);
    }

    public int getIngredientCount() {
        return ingredients != null ? ingredients.size() : 0;
    }

    public int getCalories() {
        return calories;
    }

    public void setCalories(int calories) {
        this.calories = calories;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    @Override
    public String toString() {
        return "Meal{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", category='" + category + '\'' +
                ", ingredients=" + (ingredients != null ? ingredients.size() : 0) +
                '}';
    }
} 