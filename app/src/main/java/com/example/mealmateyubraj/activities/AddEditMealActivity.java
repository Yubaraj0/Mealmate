package com.example.mealmateyubraj.activities;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.LayoutAnimationController;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mealmateyubraj.MainActivity;
import com.example.mealmateyubraj.R;
import com.example.mealmateyubraj.adapters.IngredientAdapter;
import com.example.mealmateyubraj.adapters.InstructionAdapter;
import com.example.mealmateyubraj.adapters.RecentMealAdapter;
import com.example.mealmateyubraj.database.DatabaseHelper;
import com.example.mealmateyubraj.models.Ingredient;
import com.example.mealmateyubraj.models.Meal;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputLayout;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class AddEditMealActivity extends AppCompatActivity 
        implements RecentMealAdapter.OnRecentMealClickListener, 
                   IngredientAdapter.OnIngredientClickListener,
                   InstructionAdapter.OnInstructionClickListener {
    private static final String TAG = "AddEditMealActivity";
    
    // UI Components
    private TextInputLayout tilMealName;
    private EditText etMealName;
    private EditText etMealDescription;
    private Spinner spinnerCategory;
    private Button btnSave;
    private Button btnAddIngredient;
    private RecyclerView recyclerViewIngredients;
    private TextView tvEmptyIngredients;
    private RecyclerView recyclerViewRecentMeals;
    private FloatingActionButton fabAddMealDetails;
    private TextView tvNoMeals;
    
    // Dialog components
    private AlertDialog mealDialog;
    private EditText etDialogMealName;
    private EditText etDialogInstructions;
    private EditText etDialogPrepTime;
    private Spinner spinnerDialogCategory;
    private RecyclerView rvDialogIngredients;
    private TextView tvDialogEmptyIngredients;
    private Button btnDialogAddIngredient;
    private Button btnDialogAddInstruction;
    private Button btnDialogSave;
    private Button btnDialogCancel;
    private RecyclerView rvDialogInstructions;
    private TextView tvDialogEmptyInstructions;
    private List<String> instructionsList;
    
    // Data
    private String mode;
    private long mealId;
    private long userId;
    private Meal currentMeal;
    private List<Ingredient> ingredientList;
    private DatabaseHelper databaseHelper;
    private IngredientAdapter ingredientAdapter;
    private RecentMealAdapter recentMealAdapter;
    
    // Gallery image selection
    private static final int REQUEST_STORAGE_PERMISSION = 1002;
    private static final int REQUEST_MEDIA_IMAGES_PERMISSION = 1003;
    private static final int REQUEST_IMAGE_GALLERY = 1001;
    private String selectedImagePath;
    
    /**
     * Create intent to start this activity
     *
     * @param context The context to use
     * @param mode    The mode (add, edit, view)
     * @param userId  The user ID
     * @return The intent
     */
    public static Intent createIntent(Context context, String mode, long userId) {
        Intent intent = new Intent(context, AddEditMealActivity.class);
        intent.putExtra("mode", mode);
        intent.putExtra("user_id", userId);
        return intent;
    }
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate called");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_edit_meal);
        
        try {
            Log.d(TAG, "Starting AddEditMealActivity initialization");
            
            // Initialize database helper
            databaseHelper = new DatabaseHelper(this);
            
            // Ensure database structure is up-to-date
            databaseHelper.ensureUserIdColumnExists();
            
            // Get intent data
            mode = getIntent().getStringExtra("mode");
            if (mode == null) mode = "add";
            
            userId = getIntent().getLongExtra("user_id", -1);
            Log.d(TAG, "Mode: " + mode + ", User ID: " + userId);
            
            if (userId == -1) {
                Log.e(TAG, "Invalid user ID received");
                Toast.makeText(this, "Error: Invalid user ID", Toast.LENGTH_SHORT).show();
                finish();
                return;
            }
            
            // Initialize UI
            initializeUI();
            setupToolbar();
            
            // Initialize data for new meal
            currentMeal = new Meal();
            currentMeal.setUserId(userId);
            ingredientList = new ArrayList<>();
            instructionsList = new ArrayList<>();
            
            // If in edit mode, load the meal data
            if ("edit".equals(mode)) {
                mealId = getIntent().getLongExtra("meal_id", -1);
                if (mealId > 0) {
                    // Load meal from database
                    currentMeal = databaseHelper.getMealById(mealId);
                    
                    if (currentMeal != null) {
                        Log.d(TAG, "Loaded meal: " + currentMeal.getName());
                        
                        // Set the fields with the meal data
                        etMealName.setText(currentMeal.getName());
                        spinnerCategory.setSelection(ArrayAdapter.createFromResource(
                                this, R.array.meal_categories, android.R.layout.simple_dropdown_item_1line).getPosition(currentMeal.getCategory()));
                        etDialogInstructions.setText(currentMeal.getInstructions());
                        etDialogPrepTime.setText(String.valueOf(currentMeal.getPrepTime()));
                        
                        // Load ingredients
                        ingredientList = currentMeal.getIngredients();
                        if (ingredientList == null) {
                            ingredientList = new ArrayList<>();
                        }
                        
                        // Handle instructions
                        if (currentMeal.getInstructions() != null && !currentMeal.getInstructions().isEmpty()) {
                            instructionsList = parseInstructionsToList(currentMeal.getInstructions());
                        }
                        
                        // Load image if available
                        String imagePath = currentMeal.getImagePath();
                        if (imagePath != null && !imagePath.isEmpty()) {
                            try {
                                File imgFile = new File(imagePath);
                                if (imgFile.exists()) {
                                    Bitmap bitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
                                    if (bitmap != null) {
                                        ImageView ivMealImagePreview = findViewById(R.id.iv_meal_image_preview);
                                        ivMealImagePreview.setImageBitmap(bitmap);
                                        ivMealImagePreview.setVisibility(View.VISIBLE);
                                        selectedImagePath = imagePath;
                                    }
                                }
                            } catch (Exception e) {
                                Log.e(TAG, "Error loading image: " + e.getMessage());
                            }
                        }
                    } else {
                        Log.e(TAG, "Failed to load meal with ID: " + mealId);
                        Toast.makeText(this, "Error: Failed to load meal", Toast.LENGTH_SHORT).show();
                        finish();
                        return;
                    }
                }
            }
            
            Log.d(TAG, "AddEditMealActivity initialization completed successfully");
        } catch (Exception e) {
            Log.e(TAG, "Error initializing AddEditMealActivity: " + e.getMessage(), e);
            Toast.makeText(this, "Error initializing Add Meal screen: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }
    
    /**
     * Initialize the UI elements
     */
    private void initializeUI() {
        try {
            Log.d(TAG, "Initializing UI elements");
            
            // Basic UI Components for main screen
            tvNoMeals = findViewById(R.id.tv_no_meals);
            recyclerViewRecentMeals = findViewById(R.id.rv_recent_meals);
            fabAddMealDetails = findViewById(R.id.fab_add_meal_details);
            etMealDescription = findViewById(R.id.et_instructions);
            
            // Set up recent meals RecyclerView
            setupRecentMealsRecyclerView();
            
            // Set up FAB click listener
            fabAddMealDetails.setOnClickListener(v -> showMealDetailsDialog());
            
            Log.d(TAG, "UI elements initialized successfully");
        } catch (Exception e) {
            Log.e(TAG, "Error initializing UI: " + e.getMessage(), e);
            Toast.makeText(this, "Error setting up the UI: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
    
    /**
     * Set up the toolbar
     */
    private void setupToolbar() {
        try {
            Log.d(TAG, "Setting up toolbar");
            
            Toolbar toolbar = findViewById(R.id.toolbar);
            setSupportActionBar(toolbar);
            
            if (getSupportActionBar() != null) {
                getSupportActionBar().setDisplayHomeAsUpEnabled(true);
                getSupportActionBar().setDisplayShowHomeEnabled(true);
                
                // Set title based on mode
                if ("add".equals(mode)) {
                    getSupportActionBar().setTitle("Add Meal");
                } else if ("edit".equals(mode)) {
                    getSupportActionBar().setTitle("Edit Meal");
                } else {
                    getSupportActionBar().setTitle("View Meal");
                }
            }
            
            Log.d(TAG, "Toolbar setup complete");
        } catch (Exception e) {
            Log.e(TAG, "Error setting up toolbar: " + e.getMessage(), e);
            Toast.makeText(this, "Error setting up the toolbar", Toast.LENGTH_SHORT).show();
        }
    }
    
    /**
     * Set up ingredients RecyclerView
     */
    private void setupRecyclerView() {
        RecyclerView rvRecentMeals = findViewById(R.id.rv_recent_meals);
        rvRecentMeals.setLayoutManager(new LinearLayoutManager(this));
        
        // Initialize with empty list, will be populated in loadRecentMeals()
        recentMealAdapter = new RecentMealAdapter(this, new ArrayList<>(), this);
        rvRecentMeals.setAdapter(recentMealAdapter);
    }
    
    /**
     * Set up the RecyclerView for recent meals
     */
    private void setupRecentMealsRecyclerView() {
        try {
            // Set up layout manager
            LinearLayoutManager layoutManager = new LinearLayoutManager(this);
            recyclerViewRecentMeals.setLayoutManager(layoutManager);
            
            // Add item decoration for spacing
            int spacing = getResources().getDimensionPixelSize(R.dimen.item_spacing);
            recyclerViewRecentMeals.addItemDecoration(new RecyclerView.ItemDecoration() {
                @Override
                public void getItemOffsets(@NonNull android.graphics.Rect outRect, @NonNull View view,
                                          @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
                    outRect.bottom = spacing;
                }
            });
            
            // Set up adapter with empty list initially
            recentMealAdapter = new RecentMealAdapter(this, new ArrayList<>(), this);
            recyclerViewRecentMeals.setAdapter(recentMealAdapter);
            
            // Load recent meals
            loadRecentMeals();
            
        } catch (Exception e) {
            Log.e(TAG, "Error setting up recent meals RecyclerView: " + e.getMessage(), e);
        }
    }
    
    /**
     * Show the meal details dialog
     */
    private void showMealDetailsDialog() {
        try {
            // Create dialog
            AlertDialog.Builder builder = new MaterialAlertDialogBuilder(this);
            View dialogView = getLayoutInflater().inflate(R.layout.dialog_add_edit_meal, null);
            builder.setView(dialogView);

            // Initialize dialog views
            etDialogMealName = dialogView.findViewById(R.id.et_meal_name);
            etDialogInstructions = dialogView.findViewById(R.id.et_instruction);
            etDialogPrepTime = dialogView.findViewById(R.id.et_prep_time);
            spinnerDialogCategory = dialogView.findViewById(R.id.spinner_category);
            rvDialogIngredients = dialogView.findViewById(R.id.rv_ingredients);
            tvDialogEmptyIngredients = dialogView.findViewById(R.id.tv_empty_ingredients);
            btnDialogAddIngredient = dialogView.findViewById(R.id.btn_add_ingredient);
            btnDialogSave = dialogView.findViewById(R.id.btn_save);
            btnDialogCancel = dialogView.findViewById(R.id.btn_cancel);
            
            // Initialize instruction views
            RecyclerView rvInstructions = dialogView.findViewById(R.id.rv_instructions);
            TextView tvEmptyInstructions = dialogView.findViewById(R.id.tv_empty_instructions);
            Button btnAddInstruction = dialogView.findViewById(R.id.btn_add_instruction);

            // Initialize image views
            ImageView ivMealImage = dialogView.findViewById(R.id.iv_meal_image);
            Button btnAddImage = dialogView.findViewById(R.id.btn_add_image);
            
            // Set up category spinner
            ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                    R.array.meal_categories, android.R.layout.simple_spinner_item);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinnerDialogCategory.setAdapter(adapter);
            
            // Set up ingredients RecyclerView
            LinearLayoutManager layoutManager = new LinearLayoutManager(this);
            rvDialogIngredients.setLayoutManager(layoutManager);
            ingredientAdapter = new IngredientAdapter(this, ingredientList);
            rvDialogIngredients.setAdapter(ingredientAdapter);

            // Set up instructions RecyclerView
            LinearLayoutManager instructionsLayoutManager = new LinearLayoutManager(this);
            rvInstructions.setLayoutManager(instructionsLayoutManager);
            InstructionAdapter instructionAdapter = new InstructionAdapter(this, instructionsList, this);
            rvInstructions.setAdapter(instructionAdapter);

            // If editing, populate fields with existing data
            if ("edit".equals(mode) && currentMeal != null) {
                etDialogMealName.setText(currentMeal.getName());
                etDialogPrepTime.setText(String.valueOf(currentMeal.getPrepTime()));
                
                // Set category spinner selection
                String category = currentMeal.getCategory();
                if (category != null) {
                    int spinnerPosition = adapter.getPosition(category);
                    spinnerDialogCategory.setSelection(spinnerPosition);
                }

                // Set instructions
                if (currentMeal.getInstructions() != null && !currentMeal.getInstructions().isEmpty()) {
                    String[] steps = currentMeal.getInstructions().split("\n");
                    instructionsList.clear();
                    for (String step : steps) {
                        if (step.startsWith("• ")) {
                            step = step.substring(2);
                        }
                        instructionsList.add(step.trim());
                    }
                    instructionAdapter.notifyDataSetChanged();
                }

                // Set ingredients
                if (currentMeal.getIngredients() != null) {
                    ingredientList.clear();
                    ingredientList.addAll(currentMeal.getIngredients());
                    ingredientAdapter.notifyDataSetChanged();
                }

                // Load image if available
                if (currentMeal.getImagePath() != null && !currentMeal.getImagePath().isEmpty()) {
                    File imgFile = new File(currentMeal.getImagePath());
                    if (imgFile.exists()) {
                        Bitmap bitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
                        if (bitmap != null) {
                            ivMealImage.setImageBitmap(bitmap);
                        }
                    }
                }
            }

            // Update UI based on ingredients and instructions
            updateIngredientsList();
            updateInstructionsVisibility(instructionsList, rvInstructions, tvEmptyInstructions);

            // Set up button click listeners
            btnDialogAddIngredient.setOnClickListener(v -> showAddIngredientDialog());
            
            btnAddImage.setOnClickListener(v -> {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES) 
                            != PackageManager.PERMISSION_GRANTED) {
                        ActivityCompat.requestPermissions(this, 
                                new String[]{Manifest.permission.READ_MEDIA_IMAGES}, 
                                REQUEST_MEDIA_IMAGES_PERMISSION);
                    } else {
                        openGallery();
                    }
                } else {
                    if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) 
                            != PackageManager.PERMISSION_GRANTED) {
                        ActivityCompat.requestPermissions(this, 
                                new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 
                                REQUEST_STORAGE_PERMISSION);
                    } else {
                        openGallery();
                    }
                }
            });
            
            btnAddInstruction.setOnClickListener(v -> {
                String instruction = etDialogInstructions.getText().toString().trim();
                if (!instruction.isEmpty()) {
                    instructionsList.add(instruction);
                    instructionAdapter.notifyItemInserted(instructionsList.size() - 1);
                    etDialogInstructions.setText("");
                    updateInstructionsVisibility(instructionsList, rvInstructions, tvEmptyInstructions);
                }
            });
            
            btnDialogSave.setOnClickListener(v -> {
                saveMeal();
                if (mealDialog != null) {
                    mealDialog.dismiss();
                }
            });
            
            btnDialogCancel.setOnClickListener(v -> {
                if (mealDialog != null) {
                mealDialog.dismiss();
                }
            });

            // Show dialog
            mealDialog = builder.create();
            mealDialog.show();
        } catch (Exception e) {
            Log.e(TAG, "Error showing meal details dialog: " + e.getMessage());
            Toast.makeText(this, "Error showing meal details", Toast.LENGTH_SHORT).show();
        }
    }
    
    private void updateIngredientsList() {
        if (ingredientList != null && !ingredientList.isEmpty()) {
            rvDialogIngredients.setVisibility(View.VISIBLE);
            tvDialogEmptyIngredients.setVisibility(View.GONE);
        } else {
            rvDialogIngredients.setVisibility(View.GONE);
            tvDialogEmptyIngredients.setVisibility(View.VISIBLE);
        }
        if (ingredientAdapter != null) {
            ingredientAdapter.notifyDataSetChanged();
        }
    }
    
    private void saveMeal() {
        try {
            // Validate input
            String name = etDialogMealName.getText().toString().trim();
            String prepTimeStr = etDialogPrepTime.getText().toString().trim();
            String category = spinnerDialogCategory.getSelectedItem().toString();
            int prepTime = 0;
            
            if (!TextUtils.isEmpty(prepTimeStr)) {
                try {
                    prepTime = Integer.parseInt(prepTimeStr);
                } catch (NumberFormatException e) {
                    Log.e(TAG, "Error parsing prep time: " + e.getMessage());
                }
            }

            if (TextUtils.isEmpty(name)) {
                Toast.makeText(this, "Please enter a meal name", Toast.LENGTH_SHORT).show();
                return;
            }

            // Update or create meal object
            if (currentMeal == null) {
                currentMeal = new Meal(name, "", category, userId);
            } else {
                currentMeal.setName(name);
                currentMeal.setCategory(category);
            }
            
            // Set instructions
                StringBuilder instructionsBuilder = new StringBuilder();
            for (int i = 0; i < instructionsList.size(); i++) {
                instructionsBuilder.append("• ").append(instructionsList.get(i));
                if (i < instructionsList.size() - 1) {
                    instructionsBuilder.append("\n");
                }
            }
            currentMeal.setInstructions(instructionsBuilder.toString());
            
                    currentMeal.setPrepTime(prepTime);
            currentMeal.setIngredients(ingredientList);
            if (selectedImagePath != null) {
                currentMeal.setImagePath(selectedImagePath);
            }

            // Save to database
            long result;
            if ("edit".equals(mode)) {
                boolean updated = databaseHelper.updateMeal(currentMeal);
                result = updated ? currentMeal.getId() : -1;
                } else {
                result = databaseHelper.insertMeal(currentMeal);
                currentMeal.setId(result);
                }
                    
            if (result > 0) {
                // Refresh recent meals list
                List<Meal> recentMeals = databaseHelper.getMealsByUserId(userId);
                recentMealAdapter.setRecentMeals(recentMeals);
                    
                Toast.makeText(this, "Meal saved successfully", Toast.LENGTH_SHORT).show();
                } else {
                Toast.makeText(this, "Error saving meal", Toast.LENGTH_SHORT).show();
                }
        } catch (Exception e) {
            Log.e(TAG, "Error saving meal: " + e.getMessage(), e);
            Toast.makeText(this, "Error saving meal: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
    
    /**
     * Load the user's recent meals from the database
     */
    private void loadRecentMeals() {
        try {
            Log.d(TAG, "Loading recent meals from database");
            
            // Get all meals for current user from database
            List<Meal> recentMeals = databaseHelper.getMealsByUserId(userId);
            Log.d(TAG, "Retrieved " + recentMeals.size() + " meals from database");
            
            // If we're in edit mode, exclude the current meal
            if ("edit".equals(mode) && mealId > 0) {
                for (int i = 0; i < recentMeals.size(); i++) {
                    if (recentMeals.get(i).getId() == mealId) {
                        recentMeals.remove(i);
                        break;
                    }
                }
            }
            
            // Limit to the 10 most recent meals
            if (recentMeals.size() > 10) {
                recentMeals = recentMeals.subList(0, 10);
            }
            
            // Apply animation to RecyclerView items
            final int animationDuration = 300;
            recyclerViewRecentMeals.setLayoutAnimation(new LayoutAnimationController(
                    android.view.animation.AnimationUtils.loadAnimation(this, android.R.anim.fade_in), 
                    0.1f));
            
            // Update adapter with new data
            recentMealAdapter.setRecentMeals(recentMeals);
            recentMealAdapter.notifyDataSetChanged();
            recyclerViewRecentMeals.scheduleLayoutAnimation();
            
            // Show or hide the no meals text
            if (recentMeals.isEmpty()) {
                recyclerViewRecentMeals.setVisibility(View.GONE);
                tvNoMeals.setVisibility(View.VISIBLE);
            } else {
                recyclerViewRecentMeals.setVisibility(View.VISIBLE);
                tvNoMeals.setVisibility(View.GONE);
            }
            
            Log.d(TAG, "Recent meals list updated successfully");
        } catch (Exception e) {
            Log.e(TAG, "Error loading recent meals: " + e.getMessage(), e);
        }
    }
    
    private void configureForViewMode() {
        try {
            if ("view".equals(mode) && etMealName != null && spinnerCategory != null && tvEmptyIngredients != null) {
                etMealName.setEnabled(false);
                spinnerCategory.setEnabled(false);
                tvEmptyIngredients.setVisibility(View.GONE);
                
                if (btnAddIngredient != null) {
                    btnAddIngredient.setVisibility(View.GONE);
                }
                
                if (btnSave != null) {
                    btnSave.setVisibility(View.GONE);
                }
                
                // Comment out this section until IngredientAdapter is properly implemented
                /*if (ingredientAdapter != null) {
                    ingredientAdapter.setViewOnly(true);
                }*/
            }
        } catch (Exception e) {
            Log.e(TAG, "Error configuring for view mode: " + e.getMessage(), e);
        }
    }
    
    private void populateFields() {
        try {
            if (etMealName != null && currentMeal != null) {
                etMealName.setText(currentMeal.getName());
            }
            
            // Set category spinner position
            if (spinnerCategory != null && currentMeal != null) {
                String category = currentMeal.getCategory();
                if (category != null && !category.isEmpty()) {
                    ArrayAdapter<CharSequence> adapter = (ArrayAdapter<CharSequence>) spinnerCategory.getAdapter();
                    if (adapter != null) {
                        for (int i = 0; i < adapter.getCount(); i++) {
                            if (adapter.getItem(i).toString().equals(category)) {
                                spinnerCategory.setSelection(i);
                                break;
                            }
                        }
                    }
                }
            }
            
            Log.d(TAG, "Fields populated successfully");
        } catch (Exception e) {
            Log.e(TAG, "Error populating fields: " + e.getMessage(), e);
        }
    }
    
    /**
     * Handle the delete ingredient click
     */
    @Override
    public void onDeleteIngredient(int position) {
        try {
            if (ingredientList != null && position >= 0 && position < ingredientList.size()) {
                ingredientList.remove(position);
                
                // Find the RecyclerView and TextView in current dialog if it exists
                RecyclerView rvIngredients = null;
                TextView tvEmptyIngredients = null;
                
                if (mealDialog != null && mealDialog.isShowing()) {
                    rvIngredients = mealDialog.findViewById(R.id.rv_dialog_ingredients);
                    tvEmptyIngredients = mealDialog.findViewById(R.id.tv_dialog_empty_ingredients);
                }
                
                if (rvIngredients != null && tvEmptyIngredients != null) {
                    updateIngredientsVisibility(ingredientList, rvIngredients, tvEmptyIngredients);
                }
                
                Toast.makeText(this, R.string.ingredient_removed, Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Log.e(TAG, "Error removing ingredient: " + e.getMessage(), e);
            Toast.makeText(this, "Error removing ingredient", Toast.LENGTH_SHORT).show();
        }
    }
    
    @Override
    public void onDeleteInstruction(int position) {
            if (instructionsList != null && position >= 0 && position < instructionsList.size()) {
                instructionsList.remove(position);
                
                // Find the RecyclerView in current dialog if it exists
            RecyclerView rvInstructions = mealDialog.findViewById(R.id.rv_instructions);
            TextView tvEmptyInstructions = mealDialog.findViewById(R.id.tv_empty_instructions);
                
                if (rvInstructions != null && tvEmptyInstructions != null) {
                    // Notify adapter of data change
                    RecyclerView.Adapter<?> instructionsAdapter = rvInstructions.getAdapter();
                    if (instructionsAdapter != null) {
                        instructionsAdapter.notifyDataSetChanged();
                    }
                    
                    // Update visibility
                    updateInstructionsVisibility(instructionsList, rvInstructions, tvEmptyInstructions);
                }
        }
    }
    
    @Override
    public boolean onSupportNavigateUp() {
        Log.d(TAG, "onSupportNavigateUp called");
        onBackPressed();
        return true;
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Log.d(TAG, "onOptionsItemSelected called with item ID: " + item.getItemId());
        if (item.getItemId() == android.R.id.home) {
            Log.d(TAG, "Home button clicked, navigating back");
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
    
    /**
     * Handle click on a recent meal
     */
    public void onMealItemClick(Meal meal) {
        try {
            // Confirm copy
            new AlertDialog.Builder(this)
                    .setTitle("Copy Meal")
                    .setMessage("Do you want to copy '" + meal.getName() + "' as a starting point for this meal?")
                    .setPositiveButton("Copy", (dialog, which) -> {
                        copyMealData(meal);
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
        } catch (Exception e) {
            Log.e(TAG, "Error handling meal item click: " + e.getMessage(), e);
            Toast.makeText(this, "Error copying meal", Toast.LENGTH_SHORT).show();
        }
    }
    
    /**
     * Handle edit button click on a recent meal
     */
    @Override
    public void onEditMealClick(Meal meal) {
        currentMeal = meal;
        mode = "edit";
        showMealDetailsDialog();
    }
    
    /**
     * Handle delete button click on a recent meal
     */
    @Override
    public void onDeleteMealClick(Meal meal) {
        // Handle delete meal click
        new AlertDialog.Builder(this)
                .setTitle("Delete Meal")
                .setMessage("Are you sure you want to delete this meal?")
                .setPositiveButton("Yes", (dialog, which) -> {
                    boolean success = databaseHelper.deleteMeal(meal.getId());
                    if (success) {
                        Toast.makeText(this, "Meal deleted successfully", Toast.LENGTH_SHORT).show();
                        loadRecentMeals();
                    } else {
                        Toast.makeText(this, "Failed to delete meal", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("No", null)
                .show();
    }
    
    /**
     * Copy data from an existing meal to the dialog form
     */
    private void copyMealData(Meal meal) {
        try {
            // Create a new dialog if it doesn't exist
            if (mealDialog == null || !mealDialog.isShowing()) {
                showMealDetailsDialog();
            }
            
            // Copy name with "(Copy)" suffix
            etDialogMealName.setText(meal.getName() + " (Copy)");
            
            // Copy instructions if available
            if (meal.getInstructions() != null && !meal.getInstructions().isEmpty()) {
                // Clear existing instructions
                instructionsList.clear();
                
                String instructions = meal.getInstructions();
                
                // Check if the instructions are already formatted with bullet points
                if (instructions.contains("• ")) {
                    // Split by bullet points
                    String[] instructionArray = instructions.split("• ");
                    for (String instruction : instructionArray) {
                        // Skip empty lines or just the first empty element
                        instruction = instruction.trim();
                        if (!instruction.isEmpty()) {
                            instructionsList.add(instruction);
                        }
                    }
                } else {
                    // If not formatted, just add as a single instruction
                    instructionsList.add(instructions);
                }
                
                // Update the RecyclerView for instructions if available
                RecyclerView rvInstructions = null;
                TextView tvEmptyInstructions = null;
                
                if (mealDialog != null && mealDialog.isShowing()) {
                    rvInstructions = mealDialog.findViewById(R.id.rv_dialog_instructions);
                    tvEmptyInstructions = mealDialog.findViewById(R.id.tv_dialog_empty_instructions);
                }
                
                if (rvInstructions != null && tvEmptyInstructions != null) {
                    // Notify adapter of data change
                    RecyclerView.Adapter<?> instructionsAdapter = rvInstructions.getAdapter();
                    if (instructionsAdapter != null) {
                        instructionsAdapter.notifyDataSetChanged();
                    }
                    
                    // Update visibility
                    updateInstructionsVisibility(instructionsList, rvInstructions, tvEmptyInstructions);
                }
            }
            
            // Set category in spinner
            if (meal.getCategory() != null && !meal.getCategory().isEmpty()) {
                ArrayAdapter<CharSequence> adapter = (ArrayAdapter<CharSequence>) spinnerDialogCategory.getAdapter();
                if (adapter != null) {
                    int position = adapter.getPosition(meal.getCategory());
                    if (position >= 0) {
                        spinnerDialogCategory.setSelection(position);
                    }
                }
            }
            
            // Copy ingredients
            if (meal.getIngredients() != null && !meal.getIngredients().isEmpty()) {
                ingredientList.clear();
                for (Ingredient ingredient : meal.getIngredients()) {
                    // Create a new ingredient (deep copy)
                    Ingredient newIngredient = new Ingredient();
                    newIngredient.setName(ingredient.getName());
                    newIngredient.setQuantity(ingredient.getQuantity());
                    newIngredient.setUnit(ingredient.getUnit());
                    ingredientList.add(newIngredient);
                }
                
                // Update ingredients visibility
                RecyclerView rvIngredients = null;
                TextView tvEmptyIngredients = null;
                
                if (mealDialog != null && mealDialog.isShowing()) {
                    rvIngredients = mealDialog.findViewById(R.id.rv_dialog_ingredients);
                    tvEmptyIngredients = mealDialog.findViewById(R.id.tv_dialog_empty_ingredients);
                    
                    if (rvIngredients != null && tvEmptyIngredients != null) {
                        // Notify adapter of data change
                        RecyclerView.Adapter adapter = rvIngredients.getAdapter();
                        if (adapter != null) {
                            adapter.notifyDataSetChanged();
                        }
                        
                        // Update visibility
                        updateIngredientsVisibility(ingredientList, rvIngredients, tvEmptyIngredients);
                    }
                }
            }
            
            Toast.makeText(this, "Meal data copied successfully", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Log.e(TAG, "Error copying meal data: " + e.getMessage(), e);
            Toast.makeText(this, "Error copying meal data", Toast.LENGTH_SHORT).show();
        }
    }
    
    /**
     * Parse instructions string into list
     */
    private List<String> parseInstructionsToList(String instructions) {
        try {
            instructionsList.clear();
            if (instructions.contains("• ")) {
                // Split by bullet points
                String[] instructionArray = instructions.split("• ");
                for (String instruction : instructionArray) {
                    // Skip empty lines or just whitespace
                    instruction = instruction.trim();
                    if (!instruction.isEmpty()) {
                        instructionsList.add(instruction);
                    }
                }
            } else {
                // If not formatted, just add as a single instruction
                instructionsList.add(instructions);
            }
            return instructionsList;
        } catch (Exception e) {
            Log.e(TAG, "Error parsing instructions: " + e.getMessage(), e);
            return null;
        }
    }

    private void updateIngredientsVisibility(List<Ingredient> ingredients, RecyclerView recyclerView, TextView emptyView) {
        if (ingredients != null && !ingredients.isEmpty()) {
            recyclerView.setVisibility(View.VISIBLE);
            emptyView.setVisibility(View.GONE);
        } else {
            recyclerView.setVisibility(View.GONE);
            emptyView.setVisibility(View.VISIBLE);
        }
    }

    public void updateInstructionsVisibility(List<String> instructions, RecyclerView recyclerView, TextView emptyView) {
        if (instructions == null || instructions.isEmpty()) {
            recyclerView.setVisibility(View.GONE);
            emptyView.setVisibility(View.VISIBLE);
        } else {
            recyclerView.setVisibility(View.VISIBLE);
            emptyView.setVisibility(View.GONE);
        }
    }

    private void showAddIngredientDialog() {
        AlertDialog.Builder builder = new MaterialAlertDialogBuilder(this);
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_add_ingredient, null);

        EditText etName = dialogView.findViewById(R.id.et_ingredient_name);
        EditText etQuantity = dialogView.findViewById(R.id.et_ingredient_quantity);
        EditText etUnit = dialogView.findViewById(R.id.et_ingredient_unit);

        builder.setTitle("Add Ingredient")
                .setView(dialogView)
                .setPositiveButton("Add", (dialog, which) -> {
                    String name = etName.getText().toString().trim();
                    String quantityStr = etQuantity.getText().toString().trim();
                    String unit = etUnit.getText().toString().trim();

                    if (name.isEmpty()) {
                        Toast.makeText(this, "Please enter ingredient name", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    double quantity = 0;
                    if (!quantityStr.isEmpty()) {
                        try {
                            quantity = Double.parseDouble(quantityStr);
                        } catch (NumberFormatException e) {
                            Toast.makeText(this, "Invalid quantity", Toast.LENGTH_SHORT).show();
                            return;
                        }
                    }

                    Ingredient ingredient = new Ingredient(name, quantity, unit);
                    ingredientList.add(ingredient);
                    updateIngredientsList();
                })
                .setNegativeButton("Cancel", null);

        builder.create().show();
    }

    private void openGallery() {
        Intent galleryIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(galleryIntent, REQUEST_IMAGE_GALLERY);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_IMAGE_GALLERY && resultCode == RESULT_OK && data != null) {
            Uri selectedImageUri = data.getData();
            if (selectedImageUri != null) {
                try {
                    // Get the image view from dialog
                    ImageView ivMealImage = mealDialog.findViewById(R.id.iv_meal_image);
                    if (ivMealImage != null) {
                        // Display selected image
                        Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), selectedImageUri);
                        ivMealImage.setImageBitmap(bitmap);

                        // Save image to app's private storage
                        String fileName = "meal_" + System.currentTimeMillis() + ".jpg";
                        File storageDir = new File(getFilesDir(), "meal_images");
                        if (!storageDir.exists()) {
                            storageDir.mkdirs();
                        }

                        File imageFile = new File(storageDir, fileName);
                        FileOutputStream fos = new FileOutputStream(imageFile);
                        bitmap.compress(Bitmap.CompressFormat.JPEG, 90, fos);
                        fos.close();

                        // Save image path
                        selectedImagePath = imageFile.getAbsolutePath();
                        if (currentMeal != null) {
                            currentMeal.setImagePath(selectedImagePath);
                        }
                    }
                } catch (IOException e) {
                    Log.e(TAG, "Error handling selected image: " + e.getMessage());
                    Toast.makeText(this, "Error processing image", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if ((requestCode == REQUEST_STORAGE_PERMISSION || requestCode == REQUEST_MEDIA_IMAGES_PERMISSION) 
                && grantResults.length > 0 
                && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            openGallery();
        } else {
            Toast.makeText(this, "Permission denied to access images", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onAddToCartClick(Meal meal) {
        // Check if meal has ingredients
        if (meal.getIngredients() == null || meal.getIngredients().isEmpty()) {
            Toast.makeText(this, R.string.no_ingredients_to_add, Toast.LENGTH_SHORT).show();
            return;
        }
        
        // Confirm adding to grocery list
        new AlertDialog.Builder(this)
                .setTitle(R.string.add_to_grocery_list)
                .setMessage(getString(R.string.add_meal_to_grocery, meal.getName()))
                .setPositiveButton(R.string.add, (dialog, which) -> {
                    // Create a list with just this one meal
                    List<Meal> mealsList = new ArrayList<>();
                    mealsList.add(meal);
                    
                    // Generate grocery list from the meal
                    boolean success = databaseHelper.generateGroceryListFromMeals(mealsList, userId);
                    
                    if (success) {
                        Toast.makeText(this, R.string.ingredients_added_to_grocery, Toast.LENGTH_SHORT).show();
                        
                        // Navigate to grocery screen
                        navigateToGroceryScreen();
                    } else {
                        Toast.makeText(this, R.string.failed_add_to_grocery, Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }
    
    /**
     * Navigate to the grocery screen
     */
    private void navigateToGroceryScreen() {
        try {
            // Try to launch GroceryListActivity
            Intent intent = new Intent(this, 
                    Class.forName("com.example.mealmateyubraj.activities.GroceryListActivity"));
            startActivity(intent);
        } catch (ClassNotFoundException e) {
            Log.e(TAG, "GroceryListActivity not found: " + e.getMessage());
            
            // If GroceryListActivity doesn't exist, go back to MainActivity and select grocery tab
            Intent intent = new Intent(this, MainActivity.class);
            intent.putExtra("select_groceries_tab", true);
            startActivity(intent);
        } catch (Exception e) {
            Log.e(TAG, "Error navigating to grocery screen: " + e.getMessage());
            Toast.makeText(this, "Error opening grocery list", Toast.LENGTH_SHORT).show();
        }
    }
} 