package com.example.mealmateyubraj.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.example.mealmateyubraj.models.GroceryItem;
import com.example.mealmateyubraj.models.Ingredient;
import com.example.mealmateyubraj.models.Meal;

import java.util.ArrayList;
import java.util.List;

public class GroceryDao {
    private static final String TAG = "GroceryDao";
    private final DatabaseHelper dbHelper;
    private SQLiteDatabase database;

    // Add static constant for table creation
    public static final String CREATE_TABLE_GROCERY = "CREATE TABLE " + DatabaseHelper.TABLE_GROCERY_ITEMS + "("
            + DatabaseHelper.COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
            + DatabaseHelper.COLUMN_GROCERY_USER_ID + " INTEGER,"
            + DatabaseHelper.COLUMN_GROCERY_NAME + " TEXT NOT NULL,"
            + DatabaseHelper.COLUMN_GROCERY_CATEGORY + " TEXT,"
            + DatabaseHelper.COLUMN_GROCERY_QUANTITY + " REAL,"
            + DatabaseHelper.COLUMN_GROCERY_UNIT + " TEXT,"
            + DatabaseHelper.COLUMN_GROCERY_IS_PURCHASED + " INTEGER DEFAULT 0,"
            + DatabaseHelper.COLUMN_GROCERY_MEAL_ID + " INTEGER,"
            + "FOREIGN KEY (" + DatabaseHelper.COLUMN_GROCERY_USER_ID + ") REFERENCES " 
            + DatabaseHelper.TABLE_USERS + "(" + DatabaseHelper.COLUMN_ID + ")"
            + ")";

    public GroceryDao(Context context) {
        dbHelper = new DatabaseHelper(context);
    }

    public void open() {
        database = dbHelper.getWritableDatabase();
    }

    public void close() {
        dbHelper.close();
    }

    /**
     * Insert a new grocery item into the database
     *
     * @param item The grocery item to insert
     * @return The ID of the newly inserted item, or -1 if an error occurred
     */
    public long insertGroceryItem(GroceryItem item) {
        try {
            ContentValues values = new ContentValues();
            values.put(DatabaseHelper.COLUMN_GROCERY_NAME, item.getName());
            values.put(DatabaseHelper.COLUMN_GROCERY_CATEGORY, item.getCategory());
            values.put(DatabaseHelper.COLUMN_GROCERY_QUANTITY, item.getQuantity());
            values.put(DatabaseHelper.COLUMN_GROCERY_UNIT, item.getUnit());
            values.put(DatabaseHelper.COLUMN_GROCERY_IS_PURCHASED, item.isPurchased() ? 1 : 0);
            values.put(DatabaseHelper.COLUMN_GROCERY_USER_ID, item.getUserId());
            if (item.getMealId() > 0) {
                values.put(DatabaseHelper.COLUMN_GROCERY_MEAL_ID, item.getMealId());
            }

            return database.insert(DatabaseHelper.TABLE_GROCERY_ITEMS, null, values);
        } catch (Exception e) {
            Log.e(TAG, "Error inserting grocery item: " + e.getMessage(), e);
            return -1;
        }
    }

    /**
     * Update an existing grocery item in the database
     *
     * @param item The grocery item to update
     * @return true if the update was successful, false otherwise
     */
    public boolean updateGroceryItem(GroceryItem item) {
        try {
            ContentValues values = new ContentValues();
            values.put(DatabaseHelper.COLUMN_GROCERY_NAME, item.getName());
            values.put(DatabaseHelper.COLUMN_GROCERY_CATEGORY, item.getCategory());
            values.put(DatabaseHelper.COLUMN_GROCERY_QUANTITY, item.getQuantity());
            values.put(DatabaseHelper.COLUMN_GROCERY_UNIT, item.getUnit());
            values.put(DatabaseHelper.COLUMN_GROCERY_IS_PURCHASED, item.isPurchased() ? 1 : 0);

            return database.update(
                    DatabaseHelper.TABLE_GROCERY_ITEMS,
                    values,
                    DatabaseHelper.COLUMN_ID + " = ?",
                    new String[]{String.valueOf(item.getId())}) > 0;
        } catch (Exception e) {
            Log.e(TAG, "Error updating grocery item: " + e.getMessage(), e);
            return false;
        }
    }

    /**
     * Update the purchased status of a grocery item
     *
     * @param itemId     The ID of the grocery item
     * @param purchased  The new purchased status
     * @return true if the update was successful, false otherwise
     */
    public boolean updateGroceryItemPurchasedStatus(long itemId, boolean purchased) {
        try {
            ContentValues values = new ContentValues();
            values.put(DatabaseHelper.COLUMN_GROCERY_IS_PURCHASED, purchased ? 1 : 0);

            return database.update(
                    DatabaseHelper.TABLE_GROCERY_ITEMS,
                    values,
                    DatabaseHelper.COLUMN_ID + " = ?",
                    new String[]{String.valueOf(itemId)}) > 0;
        } catch (Exception e) {
            Log.e(TAG, "Error updating grocery item purchased status: " + e.getMessage(), e);
            return false;
        }
    }

    /**
     * Delete a grocery item from the database
     *
     * @param id The ID of the grocery item to delete
     * @return true if the deletion was successful, false otherwise
     */
    public boolean deleteGroceryItem(long id) {
        try {
            return database.delete(
                    DatabaseHelper.TABLE_GROCERY_ITEMS,
                    DatabaseHelper.COLUMN_ID + " = ?",
                    new String[]{String.valueOf(id)}) > 0;
        } catch (Exception e) {
            Log.e(TAG, "Error deleting grocery item: " + e.getMessage(), e);
            return false;
        }
    }

    /**
     * Delete all grocery items for a user
     *
     * @param userId The ID of the user
     * @return true if the deletion was successful, false otherwise
     */
    public boolean deleteAllGroceryItems(long userId) {
        try {
            return database.delete(
                    DatabaseHelper.TABLE_GROCERY_ITEMS,
                    DatabaseHelper.COLUMN_GROCERY_USER_ID + " = ?",
                    new String[]{String.valueOf(userId)}) > 0;
        } catch (Exception e) {
            Log.e(TAG, "Error deleting all grocery items: " + e.getMessage(), e);
            return false;
        }
    }

    /**
     * Delete all purchased grocery items for a user
     *
     * @param userId The ID of the user
     * @return true if the deletion was successful, false otherwise
     */
    public boolean deletePurchasedGroceryItems(long userId) {
        try {
            return database.delete(
                    DatabaseHelper.TABLE_GROCERY_ITEMS,
                    DatabaseHelper.COLUMN_GROCERY_USER_ID + " = ? AND " + 
                            DatabaseHelper.COLUMN_GROCERY_IS_PURCHASED + " = 1",
                    new String[]{String.valueOf(userId)}) > 0;
        } catch (Exception e) {
            Log.e(TAG, "Error deleting purchased grocery items: " + e.getMessage(), e);
            return false;
        }
    }

    /**
     * Get a grocery item by its ID
     *
     * @param id The ID of the grocery item
     * @return The grocery item, or null if not found
     */
    public GroceryItem getGroceryItemById(long id) {
        GroceryItem item = null;
        try {
            Cursor cursor = database.query(
                    DatabaseHelper.TABLE_GROCERY_ITEMS,
                    null,
                    DatabaseHelper.COLUMN_ID + " = ?",
                    new String[]{String.valueOf(id)},
                    null, null, null);

            if (cursor != null && cursor.moveToFirst()) {
                item = cursorToGroceryItem(cursor);
                cursor.close();
            }
        } catch (Exception e) {
            Log.e(TAG, "Error getting grocery item by ID: " + e.getMessage(), e);
        }
        return item;
    }

    /**
     * Get all grocery items for a user
     *
     * @param userId The ID of the user
     * @return A list of grocery items
     */
    public List<GroceryItem> getGroceryItemsByUserId(long userId) {
        List<GroceryItem> items = new ArrayList<>();
        try {
            Cursor cursor = database.query(
                    DatabaseHelper.TABLE_GROCERY_ITEMS,
                    null,
                    DatabaseHelper.COLUMN_GROCERY_USER_ID + " = ?",
                    new String[]{String.valueOf(userId)},
                    null, null,
                    DatabaseHelper.COLUMN_GROCERY_CATEGORY + ", " + 
                            DatabaseHelper.COLUMN_GROCERY_NAME);

            if (cursor != null && cursor.moveToFirst()) {
                do {
                    GroceryItem item = cursorToGroceryItem(cursor);
                    items.add(item);
                } while (cursor.moveToNext());
                cursor.close();
            }
        } catch (Exception e) {
            Log.e(TAG, "Error getting grocery items by user ID: " + e.getMessage(), e);
        }
        return items;
    }

    /**
     * Generate a grocery list from meals
     *
     * @param meals  The list of meals to generate the grocery list from
     * @param userId The ID of the user
     * @return true if the operation was successful, false otherwise
     */
    public boolean generateGroceryListFromMeals(List<Meal> meals, long userId) {
        try {
            Log.d(TAG, "Generating grocery list from " + meals.size() + " meals for user ID: " + userId);
            
            // Clear existing grocery list for this user
            deleteAllGroceryItems(userId);
            
            // Add ingredients from all meals to grocery list
            for (Meal meal : meals) {
                Log.d(TAG, "Processing meal: " + meal.getName() + " (ID: " + meal.getId() + ")");
                
                // Load ingredients if they're not already loaded
                List<Ingredient> ingredients = meal.getIngredients();
                if (ingredients == null || ingredients.isEmpty()) {
                    Log.d(TAG, "Meal has no ingredients loaded, loading them now");
                    ingredients = dbHelper.getIngredientsForMeal(meal.getId());
                    meal.setIngredients(ingredients);
                }
                
                if (ingredients == null || ingredients.isEmpty()) {
                    Log.d(TAG, "No ingredients found for meal: " + meal.getName());
                    continue;
                }
                
                Log.d(TAG, "Found " + ingredients.size() + " ingredients for meal: " + meal.getName());
                
                for (Ingredient ingredient : ingredients) {
                    // Check if similar item already exists in the grocery list
                    GroceryItem existingItem = findSimilarGroceryItem(ingredient.getName(), ingredient.getUnit(), userId);
                    
                    if (existingItem != null) {
                        // Update existing item quantity
                        existingItem.setQuantity(existingItem.getQuantity() + ingredient.getQuantity());
                        updateGroceryItem(existingItem);
                        Log.d(TAG, "Updated existing grocery item: " + existingItem.getName() + 
                                " to quantity: " + existingItem.getQuantity() + " " + existingItem.getUnit());
                    } else {
                        // Create new grocery item
                        GroceryItem newItem = new GroceryItem();
                        newItem.setName(ingredient.getName());
                        newItem.setQuantity(ingredient.getQuantity());
                        newItem.setUnit(ingredient.getUnit());
                        newItem.setUserId(userId);
                        newItem.setMealId(meal.getId());
                        newItem.setCategory(categorizeIngredient(ingredient.getName()));
                        
                        long itemId = insertGroceryItem(newItem);
                        Log.d(TAG, "Added new grocery item: " + newItem.getName() + 
                                " with quantity: " + newItem.getQuantity() + " " + newItem.getUnit() +
                                " (ID: " + itemId + ")");
                    }
                }
            }
            
            Log.d(TAG, "Grocery list generation completed successfully");
            return true;
        } catch (Exception e) {
            Log.e(TAG, "Error generating grocery list: " + e.getMessage(), e);
            return false;
        }
    }

    /**
     * Find a similar grocery item in the database (same name and unit)
     *
     * @param name   The name of the ingredient
     * @param unit   The unit of the ingredient
     * @param userId The ID of the user
     * @return The existing grocery item if found, null otherwise
     */
    private GroceryItem findSimilarGroceryItem(String name, String unit, long userId) {
        GroceryItem item = null;
        try {
            Cursor cursor = database.query(
                    DatabaseHelper.TABLE_GROCERY_ITEMS,
                    null,
                    DatabaseHelper.COLUMN_GROCERY_NAME + " = ? AND " +
                            DatabaseHelper.COLUMN_GROCERY_UNIT + " = ? AND " +
                            DatabaseHelper.COLUMN_GROCERY_USER_ID + " = ?",
                    new String[]{name, unit, String.valueOf(userId)},
                    null, null, null);

            if (cursor != null && cursor.moveToFirst()) {
                item = cursorToGroceryItem(cursor);
                cursor.close();
            }
        } catch (Exception e) {
            Log.e(TAG, "Error finding similar grocery item: " + e.getMessage(), e);
        }
        return item;
    }

    /**
     * Categorize an ingredient based on its name
     * This is a simple implementation and could be improved with a more comprehensive list
     *
     * @param ingredientName The name of the ingredient
     * @return The category of the ingredient
     */
    private String categorizeIngredient(String ingredientName) {
        String lowerName = ingredientName.toLowerCase();
        
        // Vegetables
        if (lowerName.contains("carrot") || lowerName.contains("potato") || 
                lowerName.contains("onion") || lowerName.contains("garlic") || 
                lowerName.contains("tomato") || lowerName.contains("pepper") || 
                lowerName.contains("broccoli") || lowerName.contains("lettuce")) {
            return "Vegetables";
        }
        
        // Fruits
        if (lowerName.contains("apple") || lowerName.contains("banana") || 
                lowerName.contains("orange") || lowerName.contains("grape") || 
                lowerName.contains("strawberry") || lowerName.contains("lemon") || 
                lowerName.contains("lime")) {
            return "Fruits";
        }
        
        // Proteins
        if (lowerName.contains("chicken") || lowerName.contains("beef") || 
                lowerName.contains("pork") || lowerName.contains("fish") || 
                lowerName.contains("tofu") || lowerName.contains("egg") || 
                lowerName.contains("bean")) {
            return "Proteins";
        }
        
        // Dairy
        if (lowerName.contains("milk") || lowerName.contains("cheese") || 
                lowerName.contains("yogurt") || lowerName.contains("butter") || 
                lowerName.contains("cream")) {
            return "Dairy";
        }
        
        // Grains
        if (lowerName.contains("rice") || lowerName.contains("pasta") || 
                lowerName.contains("bread") || lowerName.contains("wheat") || 
                lowerName.contains("flour") || lowerName.contains("oat")) {
            return "Grains";
        }
        
        // Spices and Herbs
        if (lowerName.contains("salt") || lowerName.contains("pepper") || 
                lowerName.contains("cumin") || lowerName.contains("thyme") || 
                lowerName.contains("basil") || lowerName.contains("oregano") || 
                lowerName.contains("cinnamon")) {
            return "Spices & Herbs";
        }
        
        // Default category
        return "Other";
    }

    /**
     * Convert a cursor to a GroceryItem object
     *
     * @param cursor The cursor to convert
     * @return The GroceryItem object
     */
    private GroceryItem cursorToGroceryItem(Cursor cursor) {
        return cursorToGroceryItemStatic(cursor);
    }

    /**
     * Convert a cursor to a GroceryItem object (static version)
     */
    private static GroceryItem cursorToGroceryItemStatic(Cursor cursor) {
        long id = cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_ID));
        String name = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_GROCERY_NAME));
        String category = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_GROCERY_CATEGORY));
        float quantity = cursor.getFloat(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_GROCERY_QUANTITY));
        String unit = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_GROCERY_UNIT));
        boolean isPurchased = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_GROCERY_IS_PURCHASED)) == 1;
        long userId = cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_GROCERY_USER_ID));
        
        // Meal ID might be null, so handle it carefully
        long mealId = -1;
        int mealIdColumnIndex = cursor.getColumnIndex(DatabaseHelper.COLUMN_GROCERY_MEAL_ID);
        if (mealIdColumnIndex != -1 && !cursor.isNull(mealIdColumnIndex)) {
            mealId = cursor.getLong(mealIdColumnIndex);
        }
        
        return new GroceryItem(id, name, category, quantity, unit, isPurchased, userId, mealId);
    }

    /**
     * Get all grocery items for a user (static version)
     */
    public static List<GroceryItem> getGroceryItemsByUserId(SQLiteDatabase db, long userId) {
        List<GroceryItem> items = new ArrayList<>();
        try {
            Cursor cursor = db.query(
                    DatabaseHelper.TABLE_GROCERY_ITEMS,
                    null,
                    DatabaseHelper.COLUMN_GROCERY_USER_ID + " = ?",
                    new String[]{String.valueOf(userId)},
                    null, null,
                    DatabaseHelper.COLUMN_GROCERY_CATEGORY + ", " + 
                            DatabaseHelper.COLUMN_GROCERY_NAME);

            if (cursor != null && cursor.moveToFirst()) {
                do {
                    GroceryItem item = cursorToGroceryItemStatic(cursor);
                    items.add(item);
                } while (cursor.moveToNext());
                cursor.close();
            }
        } catch (Exception e) {
            Log.e(TAG, "Error getting grocery items by user ID: " + e.getMessage(), e);
        }
        return items;
    }
    
    /**
     * Insert a grocery item (static version)
     */
    public static long insertGroceryItem(SQLiteDatabase db, GroceryItem item) {
        try {
            ContentValues values = new ContentValues();
            values.put(DatabaseHelper.COLUMN_GROCERY_NAME, item.getName());
            values.put(DatabaseHelper.COLUMN_GROCERY_CATEGORY, item.getCategory());
            values.put(DatabaseHelper.COLUMN_GROCERY_QUANTITY, item.getQuantity());
            values.put(DatabaseHelper.COLUMN_GROCERY_UNIT, item.getUnit());
            values.put(DatabaseHelper.COLUMN_GROCERY_IS_PURCHASED, item.isPurchased() ? 1 : 0);
            values.put(DatabaseHelper.COLUMN_GROCERY_USER_ID, item.getUserId());
            if (item.getMealId() > 0) {
                values.put(DatabaseHelper.COLUMN_GROCERY_MEAL_ID, item.getMealId());
            }

            return db.insert(DatabaseHelper.TABLE_GROCERY_ITEMS, null, values);
        } catch (Exception e) {
            Log.e(TAG, "Error inserting grocery item: " + e.getMessage(), e);
            return -1;
        }
    }
    
    /**
     * Update a grocery item's purchased status (static version)
     */
    public static boolean updateGroceryItemPurchasedStatus(SQLiteDatabase db, long itemId, boolean purchased) {
        try {
            ContentValues values = new ContentValues();
            values.put(DatabaseHelper.COLUMN_GROCERY_IS_PURCHASED, purchased ? 1 : 0);

            return db.update(
                    DatabaseHelper.TABLE_GROCERY_ITEMS,
                    values,
                    DatabaseHelper.COLUMN_ID + " = ?",
                    new String[]{String.valueOf(itemId)}) > 0;
        } catch (Exception e) {
            Log.e(TAG, "Error updating grocery item purchased status: " + e.getMessage(), e);
            return false;
        }
    }
    
    /**
     * Delete a grocery item (static version)
     */
    public static boolean deleteGroceryItem(SQLiteDatabase db, long id) {
        try {
            return db.delete(
                    DatabaseHelper.TABLE_GROCERY_ITEMS,
                    DatabaseHelper.COLUMN_ID + " = ?",
                    new String[]{String.valueOf(id)}) > 0;
        } catch (Exception e) {
            Log.e(TAG, "Error deleting grocery item: " + e.getMessage(), e);
            return false;
        }
    }
    
    /**
     * Delete all grocery items for a user (static version)
     */
    public static boolean deleteAllGroceryItems(SQLiteDatabase db, long userId) {
        try {
            return db.delete(
                    DatabaseHelper.TABLE_GROCERY_ITEMS,
                    DatabaseHelper.COLUMN_GROCERY_USER_ID + " = ?",
                    new String[]{String.valueOf(userId)}) > 0;
        } catch (Exception e) {
            Log.e(TAG, "Error deleting all grocery items: " + e.getMessage(), e);
            return false;
        }
    }
    
    /**
     * Delete all purchased grocery items for a user (static version)
     */
    public static boolean deletePurchasedGroceryItems(SQLiteDatabase db, long userId) {
        try {
            return db.delete(
                    DatabaseHelper.TABLE_GROCERY_ITEMS,
                    DatabaseHelper.COLUMN_GROCERY_USER_ID + " = ? AND " + 
                            DatabaseHelper.COLUMN_GROCERY_IS_PURCHASED + " = 1",
                    new String[]{String.valueOf(userId)}) > 0;
        } catch (Exception e) {
            Log.e(TAG, "Error deleting purchased grocery items: " + e.getMessage(), e);
            return false;
        }
    }
    
    /**
     * Generate grocery list from meals (static version)
     *
     * @param db      The database connection
     * @param meals   The list of meals to generate the grocery list from
     * @param userId  The ID of the user
     * @return true if the operation was successful, false otherwise
     */
    public static boolean generateGroceryListFromMeals(SQLiteDatabase db, List<Meal> meals, long userId) {
        try {
            Log.d(TAG, "Static: Generating grocery list from " + meals.size() + " meals for user ID: " + userId);
            
            // Clear existing grocery list for this user
            deleteAllGroceryItems(db, userId);
            
            // Add ingredients from all meals to grocery list
            DatabaseHelper dbHelper = null;
            for (Meal meal : meals) {
                Log.d(TAG, "Static: Processing meal: " + meal.getName() + " (ID: " + meal.getId() + ")");
                
                // Load ingredients if they're not already loaded
                List<Ingredient> ingredients = meal.getIngredients();
                if (ingredients == null || ingredients.isEmpty()) {
                    Log.d(TAG, "Static: Meal has no ingredients loaded, loading them now");
                    // We need a DatabaseHelper instance to load ingredients
                    if (dbHelper == null) {
                        // Create a temporary helper just to access getIngredientsForMeal
                        // This is not ideal but works for static method context
                        dbHelper = new DatabaseHelper(null);
                    }
                    ingredients = dbHelper.getIngredientsForMeal(meal.getId());
                    meal.setIngredients(ingredients);
                }
                
                if (ingredients == null || ingredients.isEmpty()) {
                    Log.d(TAG, "Static: No ingredients found for meal: " + meal.getName());
                    continue;
                }
                
                Log.d(TAG, "Static: Found " + ingredients.size() + " ingredients for meal: " + meal.getName());
                
                for (Ingredient ingredient : ingredients) {
                    // Check if a similar item already exists in the grocery list
                    Cursor cursor = db.query(
                            DatabaseHelper.TABLE_GROCERY_ITEMS,
                            null,
                            DatabaseHelper.COLUMN_GROCERY_NAME + " = ? AND " +
                                    DatabaseHelper.COLUMN_GROCERY_UNIT + " = ? AND " +
                                    DatabaseHelper.COLUMN_GROCERY_USER_ID + " = ?",
                            new String[]{ingredient.getName(), ingredient.getUnit(), String.valueOf(userId)},
                            null, null, null);
                    
                    if (cursor != null && cursor.moveToFirst()) {
                        // Update existing item
                        GroceryItem existingItem = cursorToGroceryItemStatic(cursor);
                        cursor.close();
                        
                        // Update quantity
                        ContentValues values = new ContentValues();
                        values.put(DatabaseHelper.COLUMN_GROCERY_QUANTITY, 
                                existingItem.getQuantity() + ingredient.getQuantity());
                        
                        db.update(
                                DatabaseHelper.TABLE_GROCERY_ITEMS,
                                values,
                                DatabaseHelper.COLUMN_ID + " = ?",
                                new String[]{String.valueOf(existingItem.getId())});
                        
                        Log.d(TAG, "Static: Updated existing grocery item: " + existingItem.getName() + 
                                " to quantity: " + (existingItem.getQuantity() + ingredient.getQuantity()) + 
                                " " + existingItem.getUnit());
                    } else {
                        if (cursor != null) {
                            cursor.close();
                        }
                        
                        // Add new item
                        ContentValues values = new ContentValues();
                        values.put(DatabaseHelper.COLUMN_GROCERY_NAME, ingredient.getName());
                        values.put(DatabaseHelper.COLUMN_GROCERY_QUANTITY, ingredient.getQuantity());
                        values.put(DatabaseHelper.COLUMN_GROCERY_UNIT, ingredient.getUnit());
                        values.put(DatabaseHelper.COLUMN_GROCERY_USER_ID, userId);
                        values.put(DatabaseHelper.COLUMN_GROCERY_MEAL_ID, meal.getId());
                        
                        // Categorize ingredient
                        String category = categorizeIngredientStatic(ingredient.getName());
                        values.put(DatabaseHelper.COLUMN_GROCERY_CATEGORY, category);
                        
                        long itemId = db.insert(DatabaseHelper.TABLE_GROCERY_ITEMS, null, values);
                        Log.d(TAG, "Static: Added new grocery item: " + ingredient.getName() + 
                                " with quantity: " + ingredient.getQuantity() + " " + ingredient.getUnit() +
                                " (ID: " + itemId + ")");
                    }
                }
            }
            
            Log.d(TAG, "Static: Grocery list generation completed successfully");
            return true;
        } catch (Exception e) {
            Log.e(TAG, "Static: Error generating grocery list: " + e.getMessage(), e);
            return false;
        }
    }
    
    /**
     * Categorize an ingredient (static version)
     */
    private static String categorizeIngredientStatic(String ingredientName) {
        String lowerName = ingredientName.toLowerCase();
        
        // Fruits
        if (lowerName.contains("apple") || lowerName.contains("banana") || 
                lowerName.contains("orange") || lowerName.contains("berry") || 
                lowerName.contains("fruit") || lowerName.contains("grape") || 
                lowerName.contains("melon") || lowerName.contains("lemon") || 
                lowerName.contains("lime") || lowerName.contains("peach") || 
                lowerName.contains("pear") || lowerName.contains("mango")) {
            return "Fruits";
        }
        
        // Vegetables
        if (lowerName.contains("carrot") || lowerName.contains("broccoli") || 
                lowerName.contains("potato") || lowerName.contains("tomato") || 
                lowerName.contains("onion") || lowerName.contains("pepper") || 
                lowerName.contains("lettuce") || lowerName.contains("spinach") || 
                lowerName.contains("vegetable") || lowerName.contains("garlic") || 
                lowerName.contains("celery") || lowerName.contains("cucumber")) {
            return "Vegetables";
        }
        
        // Dairy
        if (lowerName.contains("milk") || lowerName.contains("cheese") || 
                lowerName.contains("yogurt") || lowerName.contains("cream") || 
                lowerName.contains("butter") || lowerName.contains("dairy")) {
            return "Dairy";
        }
        
        // Meat
        if (lowerName.contains("beef") || lowerName.contains("chicken") || 
                lowerName.contains("pork") || lowerName.contains("turkey") || 
                lowerName.contains("meat") || lowerName.contains("fish") || 
                lowerName.contains("steak") || lowerName.contains("sausage")) {
            return "Meat & Fish";
        }
        
        // Grains
        if (lowerName.contains("bread") || lowerName.contains("pasta") || 
                lowerName.contains("rice") || lowerName.contains("cereal") || 
                lowerName.contains("flour") || lowerName.contains("grain") || 
                lowerName.contains("oat") || lowerName.contains("wheat") || 
                lowerName.contains("barley") || lowerName.contains("corn")) {
            return "Grains & Bread";
        }
        
        // Spices
        if (lowerName.contains("salt") || lowerName.contains("pepper") || 
                lowerName.contains("spice") || lowerName.contains("oregano") || 
                lowerName.contains("basil") || lowerName.contains("thyme") || 
                lowerName.contains("rosemary") || lowerName.contains("cinnamon") || 
                lowerName.contains("chili") || lowerName.contains("cumin") || 
                lowerName.contains("curry") || lowerName.contains("paprika")) {
            return "Spices & Herbs";
        }
        
        // Default
        return "Other";
    }
} 