package com.example.mealmateyubraj.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.example.mealmateyubraj.models.Meal;
import com.example.mealmateyubraj.models.GroceryItem;
import com.example.mealmateyubraj.models.Ingredient;

import java.util.ArrayList;
import java.util.List;

public class MealDao extends SQLiteOpenHelper {
    private static final String TAG = "MealDao";
    private static final String DATABASE_NAME = "mealmate.db";
    private static final int DATABASE_VERSION = 4;

    // Table names
    private static final String TABLE_MEALS = "meals";
    private static final String TABLE_GROCERY_ITEMS = "grocery_items";
    private static final String TABLE_MEAL_INGREDIENTS = "meal_ingredients";

    // Common column names
    private static final String KEY_ID = "id";
    private static final String KEY_USER_ID = "user_id";
    private static final String KEY_CREATED_AT = "created_at";

    // MEALS Table Columns
    private static final String KEY_NAME = "name";
    private static final String KEY_DESCRIPTION = "description";
    private static final String KEY_CATEGORY = "category";
    private static final String KEY_IMAGE_PATH = "image_path";
    private static final String KEY_PREP_TIME = "prep_time";
    private static final String KEY_INSTRUCTIONS = "instructions";
    private static final String KEY_CALORIES = "calories";
    private static final String KEY_TIME = "time";

    // GROCERY_ITEMS Table Columns
    private static final String KEY_ITEM_NAME = "item_name";
    private static final String KEY_QUANTITY = "quantity";
    private static final String KEY_UNIT = "unit";
    private static final String KEY_PURCHASED = "purchased";

    // MEAL_INGREDIENTS Table Columns
    private static final String KEY_MEAL_ID = "meal_id";

    // Create table queries
    private static final String CREATE_TABLE_MEALS = "CREATE TABLE " + TABLE_MEALS + "("
            + KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
            + KEY_USER_ID + " INTEGER,"
            + KEY_NAME + " TEXT,"
            + KEY_DESCRIPTION + " TEXT,"
            + KEY_CATEGORY + " TEXT,"
            + KEY_IMAGE_PATH + " TEXT,"
            + KEY_PREP_TIME + " INTEGER,"
            + KEY_INSTRUCTIONS + " TEXT,"
            + KEY_CALORIES + " INTEGER,"
            + KEY_TIME + " TEXT,"
            + KEY_CREATED_AT + " DATETIME DEFAULT CURRENT_TIMESTAMP"
            + ")";

    private static final String CREATE_TABLE_GROCERY_ITEMS = "CREATE TABLE " + TABLE_GROCERY_ITEMS + "("
            + KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
            + KEY_USER_ID + " INTEGER,"
            + KEY_ITEM_NAME + " TEXT,"
            + KEY_QUANTITY + " TEXT,"
            + KEY_UNIT + " TEXT,"
            + KEY_PURCHASED + " INTEGER DEFAULT 0,"
            + KEY_CREATED_AT + " DATETIME DEFAULT CURRENT_TIMESTAMP"
            + ")";

    private static final String CREATE_TABLE_MEAL_INGREDIENTS = "CREATE TABLE " + TABLE_MEAL_INGREDIENTS + "("
            + KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
            + KEY_MEAL_ID + " INTEGER,"
            + KEY_ITEM_NAME + " TEXT,"
            + KEY_QUANTITY + " REAL,"
            + KEY_UNIT + " TEXT"
            + ")";

    private final DatabaseHelper dbHelper;
    private SQLiteDatabase database;

    public MealDao(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        dbHelper = new DatabaseHelper(context);
        database = getWritableDatabase();
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE_MEALS);
        db.execSQL(CREATE_TABLE_GROCERY_ITEMS);
        db.execSQL(CREATE_TABLE_MEAL_INGREDIENTS);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_MEALS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_GROCERY_ITEMS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_MEAL_INGREDIENTS);
        onCreate(db);
    }

    public void open() {
        database = getWritableDatabase();
    }

    public void close() {
        dbHelper.close();
    }

    public long insertMeal(Meal meal) {
        try {
            Log.d(TAG, "Inserting meal: " + meal.getName() + " for user: " + meal.getUserId());
            
            ContentValues values = new ContentValues();
            values.put(KEY_USER_ID, meal.getUserId());
            values.put(KEY_NAME, meal.getName());
            values.put(KEY_DESCRIPTION, meal.getDescription());
            values.put(KEY_CATEGORY, meal.getCategory());
            values.put(KEY_IMAGE_PATH, meal.getImagePath());
            values.put(KEY_PREP_TIME, meal.getPrepTime());
            values.put(KEY_INSTRUCTIONS, meal.getInstructions());
            values.put(KEY_CALORIES, meal.getCalories());
            values.put(KEY_TIME, meal.getTime());
            
            long mealId = database.insert(TABLE_MEALS, null, values);
            Log.d(TAG, "Inserted meal with ID: " + mealId);
            
            if (mealId != -1 && meal.getIngredients() != null) {
                // Insert ingredients
                for (Ingredient ingredient : meal.getIngredients()) {
                    addMealIngredient(mealId, ingredient);
                }
                Log.d(TAG, "Inserted " + (meal.getIngredients().size()) + " ingredients");
            }
            
            return mealId;
            
        } catch (Exception e) {
            Log.e(TAG, "Error inserting meal: " + e.getMessage(), e);
            return -1;
        }
    }

    public long addMealIngredient(long mealId, Ingredient ingredient) {
        try {
            ContentValues values = new ContentValues();
            values.put(KEY_MEAL_ID, mealId);
            values.put(KEY_ITEM_NAME, ingredient.getName());
            values.put(KEY_QUANTITY, ingredient.getQuantity());
            values.put(KEY_UNIT, ingredient.getUnit());
            
            long id = database.insert(TABLE_MEAL_INGREDIENTS, null, values);
            Log.d(TAG, "Added ingredient " + ingredient.getName() + " to meal " + mealId);
            return id;
        } catch (Exception e) {
            Log.e(TAG, "Error adding ingredient: " + e.getMessage(), e);
            return -1;
        }
    }

    public Meal getMealById(long mealId) {
        try {
            String selectQuery = "SELECT * FROM " + TABLE_MEALS + 
                               " WHERE " + KEY_ID + " = ?";
            
            Cursor cursor = database.rawQuery(selectQuery, new String[]{String.valueOf(mealId)});
            
            if (cursor != null && cursor.moveToFirst()) {
                Meal meal = cursorToMeal(cursor);
                meal.setIngredients(getIngredientsForMeal(mealId));
                cursor.close();
                return meal;
            }
        } catch (Exception e) {
            Log.e(TAG, "Error getting meal: " + e.getMessage(), e);
        }
        return null;
    }

    public List<Meal> getAllMealsByUserId(long userId) {
        List<Meal> meals = new ArrayList<>();
        
        try {
            String selectQuery = "SELECT * FROM " + TABLE_MEALS + 
                               " WHERE " + KEY_USER_ID + " = ?" +
                               " ORDER BY " + KEY_CREATED_AT + " DESC";
            
            Log.d(TAG, "Getting meals for user: " + userId);
            
            Cursor cursor = database.rawQuery(selectQuery, new String[]{String.valueOf(userId)});
            
            if (cursor != null) {
                Log.d(TAG, "Found " + cursor.getCount() + " meals");
                
                if (cursor.moveToFirst()) {
                    do {
                        Meal meal = cursorToMeal(cursor);
                        meal.setIngredients(getIngredientsForMeal(meal.getId()));
                        meals.add(meal);
                    } while (cursor.moveToNext());
                }
                cursor.close();
            }
            
            Log.d(TAG, "Returning " + meals.size() + " meals");
            return meals;
            
        } catch (Exception e) {
            Log.e(TAG, "Error getting meals: " + e.getMessage(), e);
            return new ArrayList<>();
        }
    }

    public boolean updateMeal(Meal meal) {
        try {
            ContentValues values = new ContentValues();
            values.put(KEY_NAME, meal.getName());
            values.put(KEY_DESCRIPTION, meal.getDescription());
            values.put(KEY_CATEGORY, meal.getCategory());
            values.put(KEY_IMAGE_PATH, meal.getImagePath());
            values.put(KEY_USER_ID, meal.getUserId());
            values.put(KEY_PREP_TIME, meal.getPrepTime());
            values.put(KEY_INSTRUCTIONS, meal.getInstructions());
            values.put(KEY_CALORIES, meal.getCalories());
            values.put(KEY_TIME, meal.getTime());

            String whereClause = KEY_ID + " = ?";
            String[] whereArgs = {String.valueOf(meal.getId())};

            int rowsAffected = database.update(TABLE_MEALS, values, whereClause, whereArgs);
            Log.d(TAG, "Updated meal: " + rowsAffected + " rows affected");

            // Update ingredients
            if (meal.getIngredients() != null) {
                deleteMealIngredients(meal.getId());
                for (Ingredient ingredient : meal.getIngredients()) {
                    addMealIngredient(meal.getId(), ingredient);
                }
            }

            return rowsAffected > 0;
        } catch (Exception e) {
            Log.e(TAG, "Error updating meal: " + e.getMessage(), e);
            return false;
        }
    }

    public boolean deleteMeal(long mealId) {
        try {
            deleteMealIngredients(mealId);
            String whereClause = KEY_ID + " = ?";
            String[] whereArgs = {String.valueOf(mealId)};
            int rowsAffected = database.delete(TABLE_MEALS, whereClause, whereArgs);
            Log.d(TAG, "Deleted meal: " + rowsAffected + " rows affected");
            return rowsAffected > 0;
        } catch (Exception e) {
            Log.e(TAG, "Error deleting meal: " + e.getMessage(), e);
            return false;
        }
    }

    public List<Ingredient> getIngredientsForMeal(long mealId) {
        List<Ingredient> ingredients = new ArrayList<>();
        try {
            String selectQuery = "SELECT * FROM " + TABLE_MEAL_INGREDIENTS + 
                               " WHERE " + KEY_MEAL_ID + " = ?";
            
            Cursor cursor = database.rawQuery(selectQuery, new String[]{String.valueOf(mealId)});

            if (cursor != null && cursor.moveToFirst()) {
                do {
                    Ingredient ingredient = new Ingredient();
                    ingredient.setId(cursor.getLong(cursor.getColumnIndexOrThrow(KEY_ID)));
                    ingredient.setName(cursor.getString(cursor.getColumnIndexOrThrow(KEY_ITEM_NAME)));
                    ingredient.setQuantity(cursor.getDouble(cursor.getColumnIndexOrThrow(KEY_QUANTITY)));
                    ingredient.setUnit(cursor.getString(cursor.getColumnIndexOrThrow(KEY_UNIT)));
                    ingredient.setMealId(mealId);
                    ingredients.add(ingredient);
                } while (cursor.moveToNext());
                cursor.close();
            }
        } catch (Exception e) {
            Log.e(TAG, "Error getting ingredients: " + e.getMessage(), e);
        }
        return ingredients;
    }

    public void deleteMealIngredients(long mealId) {
        try {
            String whereClause = KEY_MEAL_ID + " = ?";
            String[] whereArgs = {String.valueOf(mealId)};
            database.delete(TABLE_MEAL_INGREDIENTS, whereClause, whereArgs);
        } catch (Exception e) {
            Log.e(TAG, "Error deleting ingredients: " + e.getMessage(), e);
        }
    }

    private Meal cursorToMeal(Cursor cursor) {
        Meal meal = new Meal();
        meal.setId(cursor.getLong(cursor.getColumnIndexOrThrow(KEY_ID)));
        meal.setName(cursor.getString(cursor.getColumnIndexOrThrow(KEY_NAME)));
        
        // Handle nullable fields
        int descIndex = cursor.getColumnIndex(KEY_DESCRIPTION);
        if (descIndex != -1 && !cursor.isNull(descIndex)) {
            meal.setDescription(cursor.getString(descIndex));
        }
        
        meal.setCategory(cursor.getString(cursor.getColumnIndexOrThrow(KEY_CATEGORY)));
        
        int imagePathIndex = cursor.getColumnIndex(KEY_IMAGE_PATH);
        if (imagePathIndex != -1 && !cursor.isNull(imagePathIndex)) {
            meal.setImagePath(cursor.getString(imagePathIndex));
        }
        
        meal.setUserId(cursor.getLong(cursor.getColumnIndexOrThrow(KEY_USER_ID)));
        
        int prepTimeIndex = cursor.getColumnIndex(KEY_PREP_TIME);
        if (prepTimeIndex != -1 && !cursor.isNull(prepTimeIndex)) {
            meal.setPrepTime(cursor.getInt(prepTimeIndex));
        }
        
        int instructionsIndex = cursor.getColumnIndex(KEY_INSTRUCTIONS);
        if (instructionsIndex != -1 && !cursor.isNull(instructionsIndex)) {
            meal.setInstructions(cursor.getString(instructionsIndex));
        }
        
        int caloriesIndex = cursor.getColumnIndex(KEY_CALORIES);
        if (caloriesIndex != -1 && !cursor.isNull(caloriesIndex)) {
            meal.setCalories(cursor.getInt(caloriesIndex));
        }
        
        int timeIndex = cursor.getColumnIndex(KEY_TIME);
        if (timeIndex != -1 && !cursor.isNull(timeIndex)) {
            meal.setTime(cursor.getString(timeIndex));
        }
        
        return meal;
    }

    // Get count of meals for a user
    public int getMealsCountByUserId(long userId) {
        Cursor cursor = database.rawQuery("SELECT COUNT(*) FROM " + TABLE_MEALS + " WHERE " + KEY_USER_ID + " = ?",
                new String[]{String.valueOf(userId)});
        
        int count = 0;
        if (cursor.moveToFirst()) {
            count = cursor.getInt(0);
        }
        cursor.close();
        return count;
    }

    // Get count of grocery lists for a user
    public int getGroceryListsCountByUserId(long userId) {
        Cursor cursor = database.rawQuery("SELECT COUNT(DISTINCT " + KEY_CREATED_AT + ") FROM " + TABLE_GROCERY_ITEMS + 
                " WHERE " + KEY_USER_ID + " = ?", new String[]{String.valueOf(userId)});
        
        int count = 0;
        if (cursor.moveToFirst()) {
            count = cursor.getInt(0);
        }
        cursor.close();
        return count;
    }

    // Add a grocery item
    public long addGroceryItem(GroceryItem item) {
        ContentValues values = new ContentValues();
        values.put(KEY_USER_ID, item.getUserId());
        values.put(KEY_ITEM_NAME, item.getItemName());
        values.put(KEY_QUANTITY, item.getQuantity());
        values.put(KEY_UNIT, item.getUnit());
        values.put(KEY_PURCHASED, item.isPurchased() ? 1 : 0);

        long id = database.insert(TABLE_GROCERY_ITEMS, null, values);
        return id;
    }

    // Get all grocery items for a user
    public List<GroceryItem> getAllGroceryItemsByUserId(long userId) {
        List<GroceryItem> items = new ArrayList<>();
        Cursor cursor = database.rawQuery("SELECT * FROM " + TABLE_GROCERY_ITEMS + " WHERE " + KEY_USER_ID + " = ? ORDER BY " + KEY_CREATED_AT + " DESC",
                new String[]{String.valueOf(userId)});

        if (cursor != null && cursor.moveToFirst()) {
            do {
                GroceryItem item = new GroceryItem();
                item.setId(cursor.getLong(cursor.getColumnIndex(KEY_ID)));
                item.setUserId(cursor.getLong(cursor.getColumnIndex(KEY_USER_ID)));
                item.setItemName(cursor.getString(cursor.getColumnIndex(KEY_ITEM_NAME)));
                item.setQuantity(cursor.getString(cursor.getColumnIndex(KEY_QUANTITY)));
                item.setUnit(cursor.getString(cursor.getColumnIndex(KEY_UNIT)));
                item.setPurchased(cursor.getInt(cursor.getColumnIndex(KEY_PURCHASED)) == 1);
                items.add(item);
            } while (cursor.moveToNext());
            cursor.close();
        }
        return items;
    }

    // Update grocery item purchased status
    public int updateGroceryItemPurchased(long itemId, boolean purchased) {
        ContentValues values = new ContentValues();
        values.put(KEY_PURCHASED, purchased ? 1 : 0);

        int result = database.update(TABLE_GROCERY_ITEMS, values, KEY_ID + " = ?",
                new String[]{String.valueOf(itemId)});
        return result;
    }

    // Delete a grocery item
    public boolean deleteGroceryItem(long itemId) {
        try {
            String whereClause = KEY_ID + " = ?";
            String[] whereArgs = {String.valueOf(itemId)};
            int rowsAffected = database.delete(TABLE_GROCERY_ITEMS, whereClause, whereArgs);
            return rowsAffected > 0;
        } catch (Exception e) {
            Log.e(TAG, "Error deleting grocery item: " + e.getMessage(), e);
            return false;
        }
    }
}