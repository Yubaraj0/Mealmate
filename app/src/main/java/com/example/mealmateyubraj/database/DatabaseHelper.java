package com.example.mealmateyubraj.database;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import android.content.ContentValues;

import com.example.mealmateyubraj.models.Meal;
import com.example.mealmateyubraj.models.GroceryItem;
import com.example.mealmateyubraj.models.Ingredient;
import com.example.mealmateyubraj.models.User;

import java.util.ArrayList;
import java.util.List;

public class DatabaseHelper extends SQLiteOpenHelper {
    private static final String TAG = "DatabaseHelper";
    private static final String DATABASE_NAME = "mealmate.db";
    private static final int DATABASE_VERSION = 4;

    // Tables
    public static final String TABLE_MEALS = "meals";
    public static final String TABLE_INGREDIENTS = "ingredients";
    public static final String TABLE_MEAL_INGREDIENTS = "meal_ingredients";
    public static final String TABLE_USERS = "users";
    public static final String TABLE_GROCERY_ITEMS = "grocery_items";

    // Common column names
    public static final String COLUMN_ID = "id";
    
    // Users table columns
    public static final String COLUMN_EMAIL = "email";
    public static final String COLUMN_PASSWORD = "password";
    public static final String COLUMN_USERNAME = "username";
    public static final String COLUMN_REMEMBER_ME = "remember_me";
    
    // Meals table columns
    public static final String COLUMN_NAME = "name";
    public static final String COLUMN_DESCRIPTION = "description";
    public static final String COLUMN_CATEGORY = "category";
    public static final String COLUMN_IMAGE_PATH = "image_path";
    public static final String COLUMN_USER_ID = "user_id";
    public static final String COLUMN_PREP_TIME = "prep_time";
    public static final String COLUMN_INSTRUCTIONS = "instructions";
    
    // Ingredients table columns
    public static final String COLUMN_INGREDIENT_NAME = "name";
    public static final String COLUMN_INGREDIENT_UNIT = "unit";
    public static final String COLUMN_INGREDIENT_QUANTITY = "quantity";
    
    // Meal-Ingredients junction table columns
    public static final String COLUMN_MEAL_ID = "meal_id";
    public static final String COLUMN_INGREDIENT_ID = "ingredient_id";
    public static final String COLUMN_QUANTITY = "quantity";
    
    // Grocery items table columns
    public static final String COLUMN_GROCERY_NAME = "name";
    public static final String COLUMN_GROCERY_CATEGORY = "category";
    public static final String COLUMN_GROCERY_QUANTITY = "quantity";
    public static final String COLUMN_GROCERY_UNIT = "unit";
    public static final String COLUMN_GROCERY_IS_PURCHASED = "is_purchased";
    public static final String COLUMN_GROCERY_USER_ID = "user_id";
    public static final String COLUMN_GROCERY_MEAL_ID = "meal_id";

    // Constants for grocery table
    private static final String TABLE_GROCERIES = "groceries";
    private static final String COLUMN_GROCERY_ID = "id";

    // Create table statements
    private static final String CREATE_TABLE_MEALS = "CREATE TABLE " + TABLE_MEALS + " (" +
            COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
            COLUMN_NAME + " TEXT NOT NULL, " +
            COLUMN_DESCRIPTION + " TEXT, " +
            COLUMN_CATEGORY + " TEXT, " +
            COLUMN_IMAGE_PATH + " TEXT, " +
            COLUMN_USER_ID + " INTEGER, " +
            COLUMN_PREP_TIME + " INTEGER, " +
            COLUMN_INSTRUCTIONS + " TEXT);";

    private static final String CREATE_TABLE_INGREDIENTS = "CREATE TABLE " + TABLE_INGREDIENTS + " (" +
            COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
            COLUMN_INGREDIENT_NAME + " TEXT NOT NULL, " +
            COLUMN_INGREDIENT_UNIT + " TEXT);";

    private static final String CREATE_TABLE_MEAL_INGREDIENTS = "CREATE TABLE " + TABLE_MEAL_INGREDIENTS + " (" +
            COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
            COLUMN_MEAL_ID + " INTEGER, " +
            COLUMN_INGREDIENT_ID + " INTEGER, " +
            COLUMN_QUANTITY + " REAL, " +
            "FOREIGN KEY (" + COLUMN_MEAL_ID + ") REFERENCES " + TABLE_MEALS + "(" + COLUMN_ID + "), " +
            "FOREIGN KEY (" + COLUMN_INGREDIENT_ID + ") REFERENCES " + TABLE_INGREDIENTS + "(" + COLUMN_ID + "));";
            
    private static final String CREATE_TABLE_USERS = "CREATE TABLE " + TABLE_USERS + "("
            + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
            + COLUMN_USERNAME + " TEXT,"
            + COLUMN_EMAIL + " TEXT UNIQUE NOT NULL,"
            + COLUMN_PASSWORD + " TEXT NOT NULL,"
            + COLUMN_REMEMBER_ME + " INTEGER DEFAULT 0"
            + ")";
            
    private static final String CREATE_TABLE_GROCERY_ITEMS = "CREATE TABLE " + TABLE_GROCERY_ITEMS + " (" +
            COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
            COLUMN_GROCERY_NAME + " TEXT NOT NULL, " +
            COLUMN_GROCERY_CATEGORY + " TEXT, " +
            COLUMN_GROCERY_QUANTITY + " REAL, " +
            COLUMN_GROCERY_UNIT + " TEXT, " +
            COLUMN_GROCERY_IS_PURCHASED + " INTEGER DEFAULT 0, " +
            COLUMN_GROCERY_USER_ID + " INTEGER, " +
            COLUMN_GROCERY_MEAL_ID + " INTEGER, " +
            "FOREIGN KEY (" + COLUMN_GROCERY_USER_ID + ") REFERENCES " + TABLE_USERS + "(" + COLUMN_ID + "), " +
            "FOREIGN KEY (" + COLUMN_GROCERY_MEAL_ID + ") REFERENCES " + TABLE_MEALS + "(" + COLUMN_ID + "));";

    // Create grocery table
    private static final String CREATE_TABLE_GROCERIES = "CREATE TABLE " + TABLE_GROCERIES + "("
            + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
            + COLUMN_GROCERY_NAME + " TEXT,"
            + COLUMN_GROCERY_QUANTITY + " REAL,"
            + COLUMN_GROCERY_UNIT + " TEXT,"
            + COLUMN_GROCERY_USER_ID + " INTEGER,"
            + "FOREIGN KEY(" + COLUMN_GROCERY_USER_ID + ") REFERENCES " + TABLE_USERS + "(" + COLUMN_ID + ")"
            + ")";
    
    private Context context;

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        Log.d(TAG, "DatabaseHelper initialized");
        this.context = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        Log.d(TAG, "Creating database tables");
        try {
            db.execSQL(CREATE_TABLE_USERS);
            Log.d(TAG, "Created users table");
            
            // Create tables
            db.execSQL(CREATE_TABLE_MEALS);
            Log.d(TAG, "Created meals table");
            
            db.execSQL(CREATE_TABLE_INGREDIENTS);
            Log.d(TAG, "Created ingredients table");
            
            db.execSQL(CREATE_TABLE_MEAL_INGREDIENTS);
            Log.d(TAG, "Created meal_ingredients table");
            
            db.execSQL(CREATE_TABLE_GROCERY_ITEMS);
            Log.d(TAG, "Created grocery_items table");
            
            db.execSQL(CREATE_TABLE_GROCERIES);
            Log.d(TAG, "Created groceries table");
            
            Log.i(TAG, "Successfully created all tables");
            
            // Verify table creation
            verifyTablesExist(db);
        } catch (Exception e) {
            Log.e(TAG, "Error creating tables: " + e.getMessage(), e);
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.d(TAG, "Upgrading database from version " + oldVersion + " to " + newVersion);
        try {
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_USERS);
            
            if (oldVersion < 2) {
                // Add grocery table in version 2
                db.execSQL(CREATE_TABLE_GROCERY_ITEMS);
                Log.d(TAG, "Added grocery items table in upgrade to version 2");
            }
            
            if (oldVersion < 3) {
                // Check if user_id column exists in meals table
                boolean hasUserIdColumn = false;
                Cursor cursor = db.rawQuery("PRAGMA table_info(" + TABLE_MEALS + ")", null);
                if (cursor != null) {
                    if (cursor.moveToFirst()) {
                        do {
                            String columnName = cursor.getString(cursor.getColumnIndex("name"));
                            if (COLUMN_USER_ID.equals(columnName)) {
                                hasUserIdColumn = true;
                                break;
                            }
                        } while (cursor.moveToNext());
                    }
                    cursor.close();
                }
                
                // Add user_id column if it doesn't exist
                if (!hasUserIdColumn) {
                    Log.d(TAG, "Adding user_id column to meals table");
                    db.execSQL("ALTER TABLE " + TABLE_MEALS + " ADD COLUMN " + COLUMN_USER_ID + " INTEGER;");
                    Log.d(TAG, "Successfully added user_id column to meals table");
                }
            }
            
            if (oldVersion < 4) {
                // Check if prep_time column exists in meals table
                boolean hasPrepTimeColumn = false;
                Cursor cursor = db.rawQuery("PRAGMA table_info(" + TABLE_MEALS + ")", null);
                if (cursor != null) {
                    if (cursor.moveToFirst()) {
                        do {
                            String columnName = cursor.getString(cursor.getColumnIndex("name"));
                            if (COLUMN_PREP_TIME.equals(columnName)) {
                                hasPrepTimeColumn = true;
                                break;
                            }
                        } while (cursor.moveToNext());
                    }
                    cursor.close();
                }
                
                // Add prep_time column if it doesn't exist
                if (!hasPrepTimeColumn) {
                    Log.d(TAG, "Adding prep_time column to meals table");
                    db.execSQL("ALTER TABLE " + TABLE_MEALS + " ADD COLUMN " + COLUMN_PREP_TIME + " INTEGER;");
                    Log.d(TAG, "Successfully added prep_time column to meals table");
                }
            }
            
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_GROCERIES);
            
            Log.d(TAG, "Database upgrade completed successfully");
            onCreate(db);
        } catch (Exception e) {
            Log.e(TAG, "Error upgrading database: " + e.getMessage());
        }
    }
    
    @Override
    public void onOpen(SQLiteDatabase db) {
        super.onOpen(db);
        if (!db.isReadOnly()) {
            // Enable foreign key constraints
            db.execSQL("PRAGMA foreign_keys=ON;");
            Log.d(TAG, "Enabled foreign key constraints");
            
            // Verify tables exist when database is opened
            verifyTablesExist(db);
        }
    }
    
    /**
     * Verifies that all tables exist in the database
     */
    private void verifyTablesExist(SQLiteDatabase db) {
        try {
            // Check if tables exist
            Cursor cursor = db.rawQuery(
                    "SELECT name FROM sqlite_master WHERE type='table' AND name=?",
                    new String[]{TABLE_USERS});
            
            boolean usersTableExists = cursor != null && cursor.getCount() > 0;
            Log.d(TAG, "Users table exists: " + usersTableExists);
            
            if (cursor != null) {
                cursor.close();
            }
            
            // If users table doesn't exist, try to create it
            if (!usersTableExists) {
                Log.w(TAG, "Users table not found, creating it now");
                db.execSQL(CREATE_TABLE_USERS);
            }
            
            // Verify meals table
            cursor = db.rawQuery(
                    "SELECT name FROM sqlite_master WHERE type='table' AND name=?",
                    new String[]{TABLE_MEALS});
            
            boolean mealsTableExists = cursor != null && cursor.getCount() > 0;
            Log.d(TAG, "Meals table exists: " + mealsTableExists);
            
            if (cursor != null) {
                cursor.close();
            }
            
            // If meals table doesn't exist, try to create it
            if (!mealsTableExists) {
                Log.w(TAG, "Meals table not found, creating it now");
                db.execSQL(CREATE_TABLE_MEALS);
            }
            
            // Similarly check for other tables
            cursor = db.rawQuery(
                    "SELECT name FROM sqlite_master WHERE type='table' AND name=?",
                    new String[]{TABLE_INGREDIENTS});
                    
            boolean ingredientsTableExists = cursor != null && cursor.getCount() > 0;
            Log.d(TAG, "Ingredients table exists: " + ingredientsTableExists);
            
            if (cursor != null) {
                cursor.close();
            }
            
            if (!ingredientsTableExists) {
                Log.w(TAG, "Ingredients table not found, creating it now");
                db.execSQL(CREATE_TABLE_INGREDIENTS);
            }
            
            cursor = db.rawQuery(
                    "SELECT name FROM sqlite_master WHERE type='table' AND name=?",
                    new String[]{TABLE_MEAL_INGREDIENTS});
                    
            boolean mealIngredientsTableExists = cursor != null && cursor.getCount() > 0;
            Log.d(TAG, "Meal-Ingredients table exists: " + mealIngredientsTableExists);
            
            if (cursor != null) {
                cursor.close();
            }
            
            if (!mealIngredientsTableExists) {
                Log.w(TAG, "Meal-Ingredients table not found, creating it now");
                db.execSQL(CREATE_TABLE_MEAL_INGREDIENTS);
            }
            
            cursor = db.rawQuery(
                    "SELECT name FROM sqlite_master WHERE type='table' AND name=?",
                    new String[]{TABLE_GROCERY_ITEMS});
                    
            boolean groceryItemsTableExists = cursor != null && cursor.getCount() > 0;
            Log.d(TAG, "Grocery items table exists: " + groceryItemsTableExists);
            
            if (cursor != null) {
                cursor.close();
            }
            
            if (!groceryItemsTableExists) {
                Log.w(TAG, "Grocery items table not found, creating it now");
                db.execSQL(CREATE_TABLE_GROCERY_ITEMS);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error verifying tables: " + e.getMessage(), e);
        }
    }
    
    // Methods needed by AddEditMealActivity and MealsFragment
    
    public long insertMeal(Meal meal) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.beginTransaction();
        try {
            // First insert the meal
            ContentValues mealValues = new ContentValues();
            mealValues.put(COLUMN_NAME, meal.getName());
            mealValues.put(COLUMN_DESCRIPTION, meal.getDescription());
            mealValues.put(COLUMN_CATEGORY, meal.getCategory());
            mealValues.put(COLUMN_IMAGE_PATH, meal.getImagePath());
            mealValues.put(COLUMN_USER_ID, meal.getUserId());
            mealValues.put(COLUMN_PREP_TIME, meal.getPrepTime());
            mealValues.put(COLUMN_INSTRUCTIONS, meal.getInstructions());
            
            long mealId = db.insert(TABLE_MEALS, null, mealValues);
            
            if (mealId != -1 && meal.getIngredients() != null) {
                // Save ingredients
                for (Ingredient ingredient : meal.getIngredients()) {
                    // First insert or update the ingredient
                    ContentValues ingredientValues = new ContentValues();
                    ingredientValues.put(COLUMN_INGREDIENT_NAME, ingredient.getName());
                    ingredientValues.put(COLUMN_INGREDIENT_UNIT, ingredient.getUnit());
                    
                    // Check if ingredient already exists
                    Cursor cursor = db.query(TABLE_INGREDIENTS,
                            new String[]{COLUMN_ID},
                            COLUMN_INGREDIENT_NAME + " = ? AND " + COLUMN_INGREDIENT_UNIT + " = ?",
                            new String[]{ingredient.getName(), ingredient.getUnit()},
                            null, null, null);
                    
                    long ingredientId;
                    if (cursor != null && cursor.moveToFirst()) {
                        ingredientId = cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_ID));
                        cursor.close();
                    } else {
                        ingredientId = db.insert(TABLE_INGREDIENTS, null, ingredientValues);
                    }
                    
                    if (ingredientId != -1) {
                        // Now create the meal-ingredient relationship
                        ContentValues relationValues = new ContentValues();
                        relationValues.put(COLUMN_MEAL_ID, mealId);
                        relationValues.put(COLUMN_INGREDIENT_ID, ingredientId);
                        relationValues.put(COLUMN_QUANTITY, ingredient.getQuantity());
                        
                        db.insert(TABLE_MEAL_INGREDIENTS, null, relationValues);
                    }
                }
            }
            
            db.setTransactionSuccessful();
            return mealId;
        } catch (Exception e) {
            Log.e(TAG, "Error inserting meal: " + e.getMessage());
            return -1;
        } finally {
            db.endTransaction();
        }
    }
    
    public boolean updateMeal(Meal meal) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.beginTransaction();
        try {
            // First update the meal
            ContentValues mealValues = new ContentValues();
            mealValues.put(COLUMN_NAME, meal.getName());
            mealValues.put(COLUMN_DESCRIPTION, meal.getDescription());
            mealValues.put(COLUMN_CATEGORY, meal.getCategory());
            mealValues.put(COLUMN_IMAGE_PATH, meal.getImagePath());
            mealValues.put(COLUMN_USER_ID, meal.getUserId());
            mealValues.put(COLUMN_PREP_TIME, meal.getPrepTime());
            mealValues.put(COLUMN_INSTRUCTIONS, meal.getInstructions());
            
            int mealResult = db.update(TABLE_MEALS, mealValues,
                    COLUMN_ID + " = ?",
                    new String[]{String.valueOf(meal.getId())});
            
            if (mealResult > 0 && meal.getIngredients() != null) {
                // Delete existing meal-ingredient relationships
                db.delete(TABLE_MEAL_INGREDIENTS,
                        COLUMN_MEAL_ID + " = ?",
                        new String[]{String.valueOf(meal.getId())});
                
                // Save new ingredients
                for (Ingredient ingredient : meal.getIngredients()) {
                    // First insert or update the ingredient
                    ContentValues ingredientValues = new ContentValues();
                    ingredientValues.put(COLUMN_INGREDIENT_NAME, ingredient.getName());
                    ingredientValues.put(COLUMN_INGREDIENT_UNIT, ingredient.getUnit());
                    
                    // Check if ingredient already exists
                    Cursor cursor = db.query(TABLE_INGREDIENTS,
                            new String[]{COLUMN_ID},
                            COLUMN_INGREDIENT_NAME + " = ? AND " + COLUMN_INGREDIENT_UNIT + " = ?",
                            new String[]{ingredient.getName(), ingredient.getUnit()},
                            null, null, null);
                    
                    long ingredientId;
                    if (cursor != null && cursor.moveToFirst()) {
                        ingredientId = cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_ID));
                        cursor.close();
                    } else {
                        ingredientId = db.insert(TABLE_INGREDIENTS, null, ingredientValues);
                    }
                    
                    if (ingredientId != -1) {
                        // Now create the meal-ingredient relationship
                        ContentValues relationValues = new ContentValues();
                        relationValues.put(COLUMN_MEAL_ID, meal.getId());
                        relationValues.put(COLUMN_INGREDIENT_ID, ingredientId);
                        relationValues.put(COLUMN_QUANTITY, ingredient.getQuantity());
                        
                        db.insert(TABLE_MEAL_INGREDIENTS, null, relationValues);
                    }
                }
            }
            
            db.setTransactionSuccessful();
            return true;
        } catch (Exception e) {
            Log.e(TAG, "Error updating meal: " + e.getMessage());
            return false;
        } finally {
            db.endTransaction();
        }
    }
    
    public boolean deleteMeal(long id) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.beginTransaction();
        try {
            // First delete any grocery items referencing this meal
            db.delete(TABLE_GROCERY_ITEMS, COLUMN_GROCERY_MEAL_ID + " = ?", new String[]{String.valueOf(id)});
            
            // Then delete all meal-ingredient relationships
            db.delete(TABLE_MEAL_INGREDIENTS, COLUMN_MEAL_ID + " = ?", new String[]{String.valueOf(id)});
            
            // Finally delete the meal
            int result = db.delete(TABLE_MEALS, COLUMN_ID + " = ?", new String[]{String.valueOf(id)});
            
            db.setTransactionSuccessful();
            return result > 0;
        } catch (Exception e) {
            Log.e(TAG, "Error deleting meal: " + e.getMessage());
            return false;
        } finally {
            db.endTransaction();
        }
    }
    
    public Meal getMealById(long id) {
        SQLiteDatabase db = this.getReadableDatabase();
        Meal meal = null;
        
        try {
            Cursor cursor = db.query(TABLE_MEALS,
                    null,
                    COLUMN_ID + " = ?",
                    new String[]{String.valueOf(id)},
                    null, null, null);

            if (cursor != null && cursor.moveToFirst()) {
                meal = new Meal(
                    cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NAME)),
                    cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DESCRIPTION)),
                    cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_CATEGORY)),
                    cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_USER_ID))
                );
                meal.setId(cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_ID)));
                meal.setImagePath(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_IMAGE_PATH)));
                meal.setPrepTime(cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_PREP_TIME)));
                cursor.close();
            }
        } catch (Exception e) {
            Log.e(TAG, "Error getting meal by ID: " + e.getMessage());
        }
        
        return meal;
    }
    
    public List<Meal> getMealsByUserId(long userId) {
        // This is a custom implementation since it's not in MealDao
        List<Meal> meals = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        
        Cursor cursor = db.query(TABLE_MEALS,
                null,
                COLUMN_USER_ID + " = ?",
                new String[]{String.valueOf(userId)},
                null, null, null);
        
        if (cursor != null && cursor.moveToFirst()) {
            do {
                long id = cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_ID));
                String name = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NAME));
                String description = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DESCRIPTION));
                String category = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_CATEGORY));
                String imagePath = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_IMAGE_PATH));
                
                Meal meal = new Meal(name, description, category, userId);
                meal.setId(id);
                meal.setImagePath(imagePath);
                
                // Get prep time if column exists
                int prepTimeColumnIndex = cursor.getColumnIndex(COLUMN_PREP_TIME);
                if (prepTimeColumnIndex != -1) {
                    int prepTime = cursor.getInt(prepTimeColumnIndex);
                    meal.setPrepTime(prepTime);
                }

                // Get instructions if column exists
                int instructionsColumnIndex = cursor.getColumnIndex(COLUMN_INSTRUCTIONS);
                if (instructionsColumnIndex != -1) {
                    String instructions = cursor.getString(instructionsColumnIndex);
                    meal.setInstructions(instructions);
                }

                // Load ingredients for this meal
                List<Ingredient> ingredients = getIngredientsForMeal(id);
                meal.setIngredients(ingredients);
                
                meals.add(meal);
            } while (cursor.moveToNext());
            cursor.close();
        }
        
        return meals;
    }
    
    // Grocery list related methods
    
    /**
     * Get all grocery items for a user
     * @param userId ID of the user
     * @return List of GroceryItem objects
     */
    public List<GroceryItem> getGroceryItemsByUserId(long userId) {
        List<GroceryItem> groceryItems = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        
        try {
            Log.d(TAG, "Getting grocery items for user ID: " + userId);
            
            // Use the correct table name
            String selectQuery = "SELECT * FROM " + TABLE_GROCERY_ITEMS +
                    " WHERE " + COLUMN_GROCERY_USER_ID + " = ?";
            
            Cursor cursor = db.rawQuery(selectQuery, new String[]{String.valueOf(userId)});
            
            int count = cursor.getCount();
            Log.d(TAG, "Found " + count + " grocery items");
            
            if (cursor.moveToFirst()) {
                do {
                    GroceryItem item = new GroceryItem();
                    item.setId(cursor.getLong(cursor.getColumnIndex(COLUMN_ID)));
                    item.setName(cursor.getString(cursor.getColumnIndex(COLUMN_GROCERY_NAME)));
                    item.setCategory(cursor.getString(cursor.getColumnIndex(COLUMN_GROCERY_CATEGORY)));
                    item.setQuantity(cursor.getFloat(cursor.getColumnIndex(COLUMN_GROCERY_QUANTITY)));
                    item.setUnit(cursor.getString(cursor.getColumnIndex(COLUMN_GROCERY_UNIT)));
                    item.setPurchased(cursor.getInt(cursor.getColumnIndex(COLUMN_GROCERY_IS_PURCHASED)) == 1);
                    item.setUserId(userId);
                    
                    // Get meal ID if present
                    int mealIdColumnIndex = cursor.getColumnIndex(COLUMN_GROCERY_MEAL_ID);
                    if (mealIdColumnIndex != -1 && !cursor.isNull(mealIdColumnIndex)) {
                        item.setMealId(cursor.getLong(mealIdColumnIndex));
                    }
                    
                    groceryItems.add(item);
                    Log.d(TAG, "Added grocery item: " + item.getName() + 
                           " (" + item.getQuantity() + " " + item.getUnit() + ")");
                } while (cursor.moveToNext());
            }
            cursor.close();
        } catch (Exception e) {
            Log.e(TAG, "Error getting grocery items: " + e.getMessage(), e);
        }
        
        return groceryItems;
    }
    
    /**
     * Insert a new grocery item
     * @param item GroceryItem to insert
     * @return ID of the inserted item or -1 if failed
     */
    public long insertGroceryItem(GroceryItem item) {
        SQLiteDatabase db = this.getWritableDatabase();
        return GroceryDao.insertGroceryItem(db, item);
    }
    
    /**
     * Update a grocery item's purchased status
     * @param itemId ID of the item to update
     * @param purchased New purchased status
     * @return true if update was successful, false otherwise
     */
    public boolean updateGroceryItemPurchasedStatus(long itemId, boolean purchased) {
        SQLiteDatabase db = this.getWritableDatabase();
        return GroceryDao.updateGroceryItemPurchasedStatus(db, itemId, purchased);
    }
    
    /**
     * Delete a grocery item
     * @param itemId ID of the item to delete
     * @return true if deletion was successful, false otherwise
     */
    public boolean deleteGroceryItemById(long id) {
        SQLiteDatabase db = this.getWritableDatabase();
        int result = db.delete(TABLE_GROCERY_ITEMS, COLUMN_ID + " = ?", new String[]{String.valueOf(id)});
        Log.d(TAG, "Deleted grocery item with ID: " + id + ", result: " + result);
        return result > 0;
    }
    
    /**
     * Delete all purchased grocery items for a user
     * @param userId ID of the user
     * @return true if deletion was successful, false otherwise
     */
    public boolean deletePurchasedGroceryItems(long userId) {
        SQLiteDatabase db = this.getWritableDatabase();
        return GroceryDao.deletePurchasedGroceryItems(db, userId);
    }
    
    /**
     * Delete all grocery items for a user
     * @param userId ID of the user
     * @return true if deletion was successful, false otherwise
     */
    public boolean deleteAllGroceryItems(long userId) {
        SQLiteDatabase db = this.getWritableDatabase();
        return GroceryDao.deleteAllGroceryItems(db, userId);
    }
    
    /**
     * Generate a grocery list from the selected meals
     * 
     * @param meals  List of meals to include in the grocery list
     * @param userId User ID to associate the grocery items with
     * @return true if successful, false otherwise
     */
    public boolean generateGroceryListFromMeals(List<Meal> meals, long userId) {
        SQLiteDatabase db = this.getWritableDatabase();
        
        try {
            Log.d(TAG, "Generating grocery list from " + meals.size() + " meals for user ID: " + userId);
            
            // Use a transaction for better performance and atomicity
            db.beginTransaction();
            
            // Clear existing grocery items for this user
            int deleted = db.delete(TABLE_GROCERY_ITEMS, COLUMN_GROCERY_USER_ID + " = ?", 
                new String[]{String.valueOf(userId)});
            Log.d(TAG, "Deleted " + deleted + " existing grocery items");
            
            int groceriesAdded = 0;
            
            // Process each meal
            for (Meal meal : meals) {
                Log.d(TAG, "Processing meal: " + meal.getName() + " (ID: " + meal.getId() + ")");
                
                // Get ingredients for the meal
                List<Ingredient> ingredients = getIngredientsForMeal(meal.getId());
                if (ingredients.isEmpty()) {
                    Log.d(TAG, "No ingredients found for meal: " + meal.getName());
                    continue;
                }
                
                Log.d(TAG, "Found " + ingredients.size() + " ingredients");
                
                // Add each ingredient to the grocery list
                for (Ingredient ingredient : ingredients) {
                    // Check if this ingredient is already in the grocery list
                    Cursor cursor = db.query(
                            TABLE_GROCERY_ITEMS,
                            null,
                            COLUMN_GROCERY_NAME + " = ? AND " + 
                            COLUMN_GROCERY_UNIT + " = ? AND " + 
                            COLUMN_GROCERY_USER_ID + " = ?",
                            new String[]{
                                ingredient.getName(), 
                                ingredient.getUnit(), 
                                String.valueOf(userId)
                            },
                            null, null, null);
                    
                    try {
                        if (cursor != null && cursor.moveToFirst()) {
                            // Update existing item
                            long itemId = cursor.getLong(cursor.getColumnIndex(COLUMN_ID));
                            float currentQuantity = cursor.getFloat(cursor.getColumnIndex(COLUMN_GROCERY_QUANTITY));
                            float newQuantity = currentQuantity + (float)ingredient.getQuantity();
                            
                            ContentValues values = new ContentValues();
                            values.put(COLUMN_GROCERY_QUANTITY, newQuantity);
                            
                            int updated = db.update(
                                    TABLE_GROCERY_ITEMS,
                                    values,
                                    COLUMN_ID + " = ?",
                                    new String[]{String.valueOf(itemId)});
                            
                            Log.d(TAG, "Updated existing item: " + ingredient.getName() + 
                                    ", new quantity: " + newQuantity + " " + ingredient.getUnit() + 
                                    ", result: " + updated);
                        } else {
                            // Add new item
                            ContentValues values = new ContentValues();
                            values.put(COLUMN_GROCERY_NAME, ingredient.getName());
                            values.put(COLUMN_GROCERY_QUANTITY, ingredient.getQuantity());
                            values.put(COLUMN_GROCERY_UNIT, ingredient.getUnit());
                            values.put(COLUMN_GROCERY_IS_PURCHASED, 0); // Not purchased by default
                            values.put(COLUMN_GROCERY_USER_ID, userId);
                            values.put(COLUMN_GROCERY_MEAL_ID, meal.getId());
                            
                            // Set a default category based on the ingredient name
                            String category = "Other";
                            String lowerName = ingredient.getName().toLowerCase();
                            
                            if (lowerName.contains("vegetable") || lowerName.contains("carrot") || 
                                    lowerName.contains("tomato") || lowerName.contains("onion")) {
                                category = "Vegetables";
                            } else if (lowerName.contains("fruit") || lowerName.contains("apple") || 
                                    lowerName.contains("banana") || lowerName.contains("orange")) {
                                category = "Fruits";
                            } else if (lowerName.contains("meat") || lowerName.contains("chicken") || 
                                    lowerName.contains("beef") || lowerName.contains("fish")) {
                                category = "Meat & Fish";
                            } else if (lowerName.contains("dairy") || lowerName.contains("milk") || 
                                    lowerName.contains("cheese") || lowerName.contains("yogurt")) {
                                category = "Dairy";
                            } else if (lowerName.contains("bread") || lowerName.contains("pasta") || 
                                    lowerName.contains("rice") || lowerName.contains("flour")) {
                                category = "Grains";
                            } else if (lowerName.contains("spice") || lowerName.contains("herb") || 
                                    lowerName.contains("salt") || lowerName.contains("pepper")) {
                                category = "Spices & Herbs";
                            }
                            
                            values.put(COLUMN_GROCERY_CATEGORY, category);
                            
                            long newId = db.insert(TABLE_GROCERY_ITEMS, null, values);
                            Log.d(TAG, "Added new item: " + ingredient.getName() + 
                                    ", quantity: " + ingredient.getQuantity() + " " + ingredient.getUnit() + 
                                    ", category: " + category + 
                                    ", ID: " + newId);
                            
                            groceriesAdded++;
                        }
                    } finally {
                        if (cursor != null) {
                            cursor.close();
                        }
                    }
                }
            }
            
            // Mark the transaction as successful
            db.setTransactionSuccessful();
            Log.d(TAG, "Successfully generated grocery list with " + groceriesAdded + " new items");
            return true;
        } catch (Exception e) {
            Log.e(TAG, "Error generating grocery list: " + e.getMessage(), e);
            return false;
        } finally {
            // End the transaction
            if (db.inTransaction()) {
                db.endTransaction();
            }
        }
    }

    /**
     * Ensure the user_id column exists in the meals table
     * This is a safety method that can be called to ensure the column exists
     */
    public void ensureUserIdColumnExists() {
        SQLiteDatabase db = this.getWritableDatabase();
        try {
            // Check if user_id column exists in meals table
            boolean hasUserIdColumn = false;
            boolean hasInstructionsColumn = false;
            Cursor cursor = db.rawQuery("PRAGMA table_info(" + TABLE_MEALS + ")", null);
            if (cursor != null) {
                if (cursor.moveToFirst()) {
                    do {
                        String columnName = cursor.getString(cursor.getColumnIndex("name"));
                        if (COLUMN_USER_ID.equals(columnName)) {
                            hasUserIdColumn = true;
                        }
                        if (COLUMN_INSTRUCTIONS.equals(columnName)) {
                            hasInstructionsColumn = true;
                        }
                    } while (cursor.moveToNext());
                }
                cursor.close();
            }
            
            // Add user_id column if it doesn't exist
            if (!hasUserIdColumn) {
                Log.d(TAG, "Adding user_id column to meals table");
                db.execSQL("ALTER TABLE " + TABLE_MEALS + " ADD COLUMN " + COLUMN_USER_ID + " INTEGER;");
                Log.d(TAG, "Successfully added user_id column to meals table");
            } else {
                Log.d(TAG, "user_id column already exists in meals table");
            }
            
            // Add instructions column if it doesn't exist
            if (!hasInstructionsColumn) {
                Log.d(TAG, "Adding instructions column to meals table");
                db.execSQL("ALTER TABLE " + TABLE_MEALS + " ADD COLUMN " + COLUMN_INSTRUCTIONS + " TEXT;");
                Log.d(TAG, "Successfully added instructions column to meals table");
            } else {
                Log.d(TAG, "instructions column already exists in meals table");
            }
            
            // Check if prep_time column exists in meals table
            boolean hasPrepTimeColumn = false;
            cursor = db.rawQuery("PRAGMA table_info(" + TABLE_MEALS + ")", null);
            if (cursor != null) {
                if (cursor.moveToFirst()) {
                    do {
                        String columnName = cursor.getString(cursor.getColumnIndex("name"));
                        if (COLUMN_PREP_TIME.equals(columnName)) {
                            hasPrepTimeColumn = true;
                            break;
                        }
                    } while (cursor.moveToNext());
                }
                cursor.close();
            }
            
            // Add prep_time column if it doesn't exist
            if (!hasPrepTimeColumn) {
                Log.d(TAG, "Adding prep_time column to meals table");
                db.execSQL("ALTER TABLE " + TABLE_MEALS + " ADD COLUMN " + COLUMN_PREP_TIME + " INTEGER;");
                Log.d(TAG, "Successfully added prep_time column to meals table");
            } else {
                Log.d(TAG, "prep_time column already exists in meals table");
            }
        } catch (Exception e) {
            Log.e(TAG, "Error ensuring columns exist: " + e.getMessage(), e);
        }
    }

    /**
     * Get all ingredients for a specific meal
     * 
     * @param mealId The ID of the meal
     * @return A list of Ingredient objects
     */
    public List<Ingredient> getIngredientsForMeal(long mealId) {
        List<Ingredient> ingredients = new ArrayList<>();
        SQLiteDatabase db = null;
        Cursor cursor = null;
        
        try {
            db = this.getReadableDatabase();
            String query = "SELECT i.*, mi.quantity FROM " + TABLE_INGREDIENTS + " i " +
                    "INNER JOIN " + TABLE_MEAL_INGREDIENTS + " mi " +
                    "ON i." + COLUMN_ID + " = mi." + COLUMN_INGREDIENT_ID + " " +
                    "WHERE mi." + COLUMN_MEAL_ID + " = ?";
            cursor = db.rawQuery(query, new String[]{String.valueOf(mealId)});
            
            if (cursor != null && cursor.moveToFirst()) {
                do {
                    int idIndex = cursor.getColumnIndex(COLUMN_ID);
                    int nameIndex = cursor.getColumnIndex(COLUMN_INGREDIENT_NAME);
                    int quantityIndex = cursor.getColumnIndex(COLUMN_QUANTITY);
                    int unitIndex = cursor.getColumnIndex(COLUMN_INGREDIENT_UNIT);
                    
                    long id = cursor.getLong(idIndex);
                    String name = cursor.getString(nameIndex);
                    double quantity = cursor.getDouble(quantityIndex);
                    String unit = cursor.getString(unitIndex);
                    
                    Ingredient ingredient = new Ingredient(id, name, quantity, unit, mealId);
                    ingredients.add(ingredient);
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            Log.e(TAG, "Error getting ingredients for meal: " + e.getMessage(), e);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        
        return ingredients;
    }

    // Add a grocery item
    public long addGroceryItem(GroceryItem item) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_GROCERY_NAME, item.getName());
        values.put(COLUMN_GROCERY_CATEGORY, item.getCategory()); 
        values.put(COLUMN_GROCERY_QUANTITY, item.getQuantity());
        values.put(COLUMN_GROCERY_UNIT, item.getUnit());
        values.put(COLUMN_GROCERY_IS_PURCHASED, item.isPurchased() ? 1 : 0);
        values.put(COLUMN_GROCERY_USER_ID, item.getUserId());
        if (item.getMealId() > 0) {
            values.put(COLUMN_GROCERY_MEAL_ID, item.getMealId());
        }
        
        long id = db.insert(TABLE_GROCERY_ITEMS, null, values);
        Log.d(TAG, "Added grocery item with ID: " + id + ", name: " + item.getName());
        return id;
    }

    // Update a grocery item
    public boolean updateGroceryItem(GroceryItem item) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_GROCERY_NAME, item.getName());
        values.put(COLUMN_GROCERY_CATEGORY, item.getCategory());
        values.put(COLUMN_GROCERY_QUANTITY, item.getQuantity());
        values.put(COLUMN_GROCERY_UNIT, item.getUnit());
        values.put(COLUMN_GROCERY_IS_PURCHASED, item.isPurchased() ? 1 : 0);
        
        int result = db.update(TABLE_GROCERY_ITEMS, values, 
            COLUMN_ID + " = ? AND " + COLUMN_GROCERY_USER_ID + " = ?", 
            new String[]{String.valueOf(item.getId()), String.valueOf(item.getUserId())});
        
        Log.d(TAG, "Updated grocery item with ID: " + item.getId() + ", result: " + result);
        return result > 0;
    }

    // Delete a grocery item
    public boolean deleteGroceryItemForUser(long id, long userId) {
        SQLiteDatabase db = this.getWritableDatabase();
        int result = db.delete(TABLE_GROCERY_ITEMS, 
            COLUMN_ID + " = ? AND " + COLUMN_GROCERY_USER_ID + " = ?", 
            new String[]{String.valueOf(id), String.valueOf(userId)});
        
        Log.d(TAG, "Deleted grocery item with ID: " + id + ", user ID: " + userId + ", result: " + result);
        return result > 0;
    }

    public User authenticateUser(String email, String password) {
        SQLiteDatabase db = this.getReadableDatabase();
        User user = null;

        try {
            String[] columns = {COLUMN_ID, COLUMN_EMAIL};
            String selection = COLUMN_EMAIL + " = ? AND " + COLUMN_PASSWORD + " = ?";
            String[] selectionArgs = {email, password};

            Cursor cursor = db.query(TABLE_USERS, columns, selection, selectionArgs, null, null, null);

            if (cursor != null && cursor.moveToFirst()) {
                user = new User();
                user.setId(cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_ID)));
                user.setEmail(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_EMAIL)));
                cursor.close();
            }
        } catch (Exception e) {
            Log.e(TAG, "Error authenticating user: " + e.getMessage());
        }

        return user;
    }

    public long registerUser(String email, String password) {
        SQLiteDatabase db = this.getWritableDatabase();
        long userId = -1;

        try {
            ContentValues values = new ContentValues();
            values.put(COLUMN_EMAIL, email);
            values.put(COLUMN_PASSWORD, password);

            userId = db.insert(TABLE_USERS, null, values);
        } catch (Exception e) {
            Log.e(TAG, "Error registering user: " + e.getMessage());
        }

        return userId;
    }

    public User getUserById(long userId) {
        User user = null;
        SQLiteDatabase db = this.getReadableDatabase();
        
        try {
            Cursor cursor = db.query(
                TABLE_USERS,
                null,
                COLUMN_ID + "=?",
                new String[]{String.valueOf(userId)},
                null,
                null,
                null
            );
            
            if (cursor.moveToFirst()) {
                user = new User();
                user.setId(cursor.getLong(cursor.getColumnIndex(COLUMN_ID)));
                user.setUsername(cursor.getString(cursor.getColumnIndex(COLUMN_USERNAME)));
                user.setEmail(cursor.getString(cursor.getColumnIndex(COLUMN_EMAIL)));
                user.setPassword(cursor.getString(cursor.getColumnIndex(COLUMN_PASSWORD)));
                user.setRememberMe(cursor.getInt(cursor.getColumnIndex(COLUMN_REMEMBER_ME)) == 1);
            }
            
            cursor.close();
        } catch (Exception e) {
            Log.e(TAG, "Error getting user by ID: " + e.getMessage());
        }
        
        return user;
    }
} 