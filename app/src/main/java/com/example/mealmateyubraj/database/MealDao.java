package com.example.mealmateyubraj.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.example.mealmateyubraj.models.Meal;

import java.util.ArrayList;
import java.util.List;

public class MealDao {
    private final DatabaseHelper dbHelper;
    private SQLiteDatabase database;

    // Column names from DatabaseHelper
    private static final String COLUMN_MEAL_NAME = DatabaseHelper.COLUMN_NAME;
    private static final String COLUMN_MEAL_DESCRIPTION = DatabaseHelper.COLUMN_DESCRIPTION;
    private static final String COLUMN_MEAL_CATEGORY = DatabaseHelper.COLUMN_CATEGORY;
    private static final String COLUMN_MEAL_IMAGE_PATH = DatabaseHelper.COLUMN_IMAGE_PATH;
    private static final String COLUMN_MEAL_USER_ID = DatabaseHelper.COLUMN_USER_ID;
    private static final String COLUMN_MEAL_PREP_TIME = DatabaseHelper.COLUMN_PREP_TIME;
    private static final String COLUMN_MEAL_INSTRUCTIONS = DatabaseHelper.COLUMN_INSTRUCTIONS;

    public MealDao(Context context) {
        dbHelper = new DatabaseHelper(context);
    }

    public void open() {
        database = dbHelper.getWritableDatabase();
    }

    public void close() {
        dbHelper.close();
    }

    public long insertMeal(Meal meal) {
        ContentValues values = new ContentValues();
        values.put(COLUMN_MEAL_NAME, meal.getName());
        values.put(COLUMN_MEAL_DESCRIPTION, meal.getDescription());
        values.put(COLUMN_MEAL_CATEGORY, meal.getCategory());
        values.put(COLUMN_MEAL_IMAGE_PATH, meal.getImagePath());
        values.put(COLUMN_MEAL_USER_ID, meal.getUserId());
        values.put(COLUMN_MEAL_PREP_TIME, meal.getPrepTime());
        values.put(COLUMN_MEAL_INSTRUCTIONS, meal.getInstructions());

        return database.insert(DatabaseHelper.TABLE_MEALS, null, values);
    }

    public boolean updateMeal(Meal meal) {
        ContentValues values = new ContentValues();
        values.put(COLUMN_MEAL_NAME, meal.getName());
        values.put(COLUMN_MEAL_DESCRIPTION, meal.getDescription());
        values.put(COLUMN_MEAL_CATEGORY, meal.getCategory());
        values.put(COLUMN_MEAL_IMAGE_PATH, meal.getImagePath());
        values.put(COLUMN_MEAL_USER_ID, meal.getUserId());
        values.put(COLUMN_MEAL_PREP_TIME, meal.getPrepTime());
        values.put(COLUMN_MEAL_INSTRUCTIONS, meal.getInstructions());

        return database.update(DatabaseHelper.TABLE_MEALS, values, 
                DatabaseHelper.COLUMN_ID + " = ?", 
                new String[]{String.valueOf(meal.getId())}) > 0;
    }

    public boolean deleteMeal(long id) {
        return database.delete(DatabaseHelper.TABLE_MEALS, 
                DatabaseHelper.COLUMN_ID + " = ?", 
                new String[]{String.valueOf(id)}) > 0;
    }

    public Meal getMealById(long id) {
        Cursor cursor = database.query(DatabaseHelper.TABLE_MEALS,
                null,
                DatabaseHelper.COLUMN_ID + " = ?",
                new String[]{String.valueOf(id)},
                null, null, null);

        if (cursor != null && cursor.moveToFirst()) {
            Meal meal = cursorToMeal(cursor);
            cursor.close();
            return meal;
        }
        return null;
    }

    public List<Meal> getAllMeals() {
        List<Meal> meals = new ArrayList<>();
        Cursor cursor = database.query(DatabaseHelper.TABLE_MEALS,
                null, null, null, null, null, null);

        if (cursor != null && cursor.moveToFirst()) {
            do {
                Meal meal = cursorToMeal(cursor);
                meals.add(meal);
            } while (cursor.moveToNext());
            cursor.close();
        }
        return meals;
    }

    public List<Meal> getMealsByCategory(String category) {
        List<Meal> meals = new ArrayList<>();
        Cursor cursor = database.query(DatabaseHelper.TABLE_MEALS,
                null,
                COLUMN_MEAL_CATEGORY + " = ?",
                new String[]{category},
                null, null, null);

        if (cursor != null && cursor.moveToFirst()) {
            do {
                Meal meal = cursorToMeal(cursor);
                meals.add(meal);
            } while (cursor.moveToNext());
            cursor.close();
        }
        return meals;
    }

    private Meal cursorToMeal(Cursor cursor) {
        long id = cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_ID));
        String name = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_MEAL_NAME));
        String description = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_MEAL_DESCRIPTION));
        String category = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_MEAL_CATEGORY));
        String imagePath = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_MEAL_IMAGE_PATH));
        long userId = cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_MEAL_USER_ID));

        Meal meal = new Meal(name, description, category, userId);
        meal.setId(id);
        meal.setImagePath(imagePath);
        
        // Get prep time if column exists
        int prepTimeColumnIndex = cursor.getColumnIndex(COLUMN_MEAL_PREP_TIME);
        if (prepTimeColumnIndex != -1) {
            int prepTime = cursor.getInt(prepTimeColumnIndex);
            meal.setPrepTime(prepTime);
        }

        // Get instructions if column exists
        int instructionsColumnIndex = cursor.getColumnIndex(COLUMN_MEAL_INSTRUCTIONS);
        if (instructionsColumnIndex != -1) {
            String instructions = cursor.getString(instructionsColumnIndex);
            meal.setInstructions(instructions);
        }
        
        return meal;
    }
} 