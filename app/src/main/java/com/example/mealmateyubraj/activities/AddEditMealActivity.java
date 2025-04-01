package com.example.mealmateyubraj.activities;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
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
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mealmateyubraj.R;
import com.example.mealmateyubraj.adapters.IngredientAdapter;
import com.example.mealmateyubraj.adapters.InstructionAdapter;
import com.example.mealmateyubraj.database.DatabaseHelper;
import com.example.mealmateyubraj.database.MealDao;
import com.example.mealmateyubraj.models.Ingredient;
import com.example.mealmateyubraj.models.Meal;
import com.example.mealmateyubraj.utils.SessionManager;
import com.google.android.material.textfield.TextInputLayout;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class AddEditMealActivity extends AppCompatActivity implements 
        InstructionAdapter.OnInstructionClickListener, 
        IngredientAdapter.OnIngredientClickListener {
    
    private static final String TAG = "AddEditMealActivity";
    private static final int REQUEST_IMAGE_GALLERY = 1001;
    private static final int REQUEST_STORAGE_PERMISSION = 1002;
    private static final int REQUEST_MEDIA_IMAGES_PERMISSION = 1003;
    
    // UI Components
    private TextInputLayout tilMealName;
    private EditText etMealName;
    private EditText etInstruction;
    private EditText etPrepTime;
    private EditText etIngredientName;
    private EditText etIngredientQuantity;
    private EditText etIngredientUnit;
    private Spinner spinnerCategory;
    private Button btnSave;
    private Button btnAddInstruction;
    private Button btnAddIngredient;
    private Button btnSelectImage;
    private ImageView ivMealImage;
    private RecyclerView rvInstructions;
    private RecyclerView rvIngredients;
    private TextView tvEmptyInstructions;
    private TextView tvEmptyIngredients;
    
    // Data
    private String mode;
    private long userId;
    private MealDao mealDao;
    private SessionManager sessionManager;
    private List<String> instructionsList;
    private List<Ingredient> ingredientsList;
    private InstructionAdapter instructionAdapter;
    private IngredientAdapter ingredientAdapter;
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
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_edit_meal);
        
        // Initialize session manager
        sessionManager = new SessionManager(this);
        
        // Initialize data
        instructionsList = new ArrayList<>();
        ingredientsList = new ArrayList<>();
        
        // Initialize views and database
        initializeViews();
        initializeDatabase();
        setupListeners();
        handleIntentData();
    }
    
    /**
     * Initialize the UI elements
     */
    private void initializeViews() {
        try {
            Log.d(TAG, "Initializing views");
            
            // Set up toolbar
            Toolbar toolbar = findViewById(R.id.toolbar);
            setSupportActionBar(toolbar);
            if (getSupportActionBar() != null) {
                getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            }
            
            // Find all view references
            tilMealName = findViewById(R.id.tilMealName);
            etMealName = findViewById(R.id.etMealName);
            etInstruction = findViewById(R.id.etInstruction);
            etPrepTime = findViewById(R.id.etPrepTime);
            etIngredientName = findViewById(R.id.etIngredientName);
            etIngredientQuantity = findViewById(R.id.etIngredientQuantity);
            etIngredientUnit = findViewById(R.id.etIngredientUnit);
            spinnerCategory = findViewById(R.id.spinnerCategory);
            btnSave = findViewById(R.id.btnSave);
            btnAddInstruction = findViewById(R.id.btnAddInstruction);
            btnAddIngredient = findViewById(R.id.btnAddIngredient);
            btnSelectImage = findViewById(R.id.btnSelectImage);
            ivMealImage = findViewById(R.id.ivMealImage);
            rvInstructions = findViewById(R.id.rvInstructions);
            tvEmptyInstructions = findViewById(R.id.tvEmptyInstructions);
            rvIngredients = findViewById(R.id.rvIngredients);
            tvEmptyIngredients = findViewById(R.id.tvEmptyIngredients);
            
            // Set up category spinner
            setupCategorySpinner();
            
            // Set up instructions RecyclerView
            setupInstructionsRecyclerView();
            
            // Set up ingredients RecyclerView
            setupIngredientsRecyclerView();
            
            Log.d(TAG, "Views initialized successfully");
        } catch (Exception e) {
            Log.e(TAG, "Error initializing views: " + e.getMessage(), e);
            Toast.makeText(this, "Error initializing views: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            finish();
        }
    }
    
    private void setupCategorySpinner() {
        String[] categories = {"Breakfast", "Lunch", "Dinner", "Snack", "Dessert"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
            this,
            android.R.layout.simple_spinner_item,
            categories
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCategory.setAdapter(adapter);
    }
    
    private void setupInstructionsRecyclerView() {
        rvInstructions.setLayoutManager(new LinearLayoutManager(this));
        instructionAdapter = new InstructionAdapter(this, instructionsList, this);
        rvInstructions.setAdapter(instructionAdapter);
        updateInstructionsVisibility();
    }
    
    private void setupIngredientsRecyclerView() {
        rvIngredients.setLayoutManager(new LinearLayoutManager(this));
        ingredientAdapter = new IngredientAdapter(this, ingredientsList);
        ingredientAdapter.setOnIngredientClickListener(this);
        rvIngredients.setAdapter(ingredientAdapter);
        updateIngredientsVisibility();
    }
    
    private void initializeDatabase() {
        try {
            Log.d(TAG, "Initializing database");
            
            // Initialize database helper and meal dao
            DatabaseHelper databaseHelper = new DatabaseHelper(this);
            mealDao = new MealDao(this);
            
            Log.d(TAG, "Database initialized successfully");
        } catch (Exception e) {
            Log.e(TAG, "Error initializing database: " + e.getMessage(), e);
            Toast.makeText(this, "Error initializing database: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
    
    private void setupListeners() {
        try {
            // Save button
            btnSave.setOnClickListener(v -> saveMeal());
            
            // Add instruction button
            btnAddInstruction.setOnClickListener(v -> addInstruction());
            
            // Add ingredient button
            btnAddIngredient.setOnClickListener(v -> addIngredient());
            
            // Select image button
            btnSelectImage.setOnClickListener(v -> selectImage());
            
            Log.d(TAG, "Listeners set up successfully");
        } catch (Exception e) {
            Log.e(TAG, "Error setting up listeners: " + e.getMessage(), e);
            Toast.makeText(this, "Error setting up listeners: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
    
    private void addInstruction() {
        String instruction = etInstruction.getText().toString().trim();
        if (!instruction.isEmpty()) {
            instructionsList.add(instruction);
            instructionAdapter.notifyItemInserted(instructionsList.size() - 1);
            etInstruction.setText("");
            updateInstructionsVisibility();
        } else {
            Toast.makeText(this, "Please enter an instruction", Toast.LENGTH_SHORT).show();
        }
    }
    
    private void updateInstructionsVisibility() {
        if (instructionsList.isEmpty()) {
            rvInstructions.setVisibility(View.GONE);
            tvEmptyInstructions.setVisibility(View.VISIBLE);
        } else {
            rvInstructions.setVisibility(View.VISIBLE);
            tvEmptyInstructions.setVisibility(View.GONE);
        }
    }
    
    private void addIngredient() {
        String name = etIngredientName.getText().toString().trim();
        String quantityStr = etIngredientQuantity.getText().toString().trim();
        String unit = etIngredientUnit.getText().toString().trim();
        
        if (name.isEmpty()) {
            Toast.makeText(this, "Please enter ingredient name", Toast.LENGTH_SHORT).show();
            return;
        }
        
        double quantity = 0;
        if (!quantityStr.isEmpty()) {
            try {
                quantity = Double.parseDouble(quantityStr);
            } catch (NumberFormatException e) {
                Toast.makeText(this, "Please enter a valid quantity", Toast.LENGTH_SHORT).show();
                return;
            }
        }
        
        Ingredient ingredient = new Ingredient(name, quantity, unit);
        ingredientsList.add(ingredient);
        ingredientAdapter.notifyDataSetChanged();
        
        // Clear fields
        etIngredientName.setText("");
        etIngredientQuantity.setText("");
        etIngredientUnit.setText("");
        
        updateIngredientsVisibility();
    }
    
    private void updateIngredientsVisibility() {
        if (ingredientsList.isEmpty()) {
            rvIngredients.setVisibility(View.GONE);
            tvEmptyIngredients.setVisibility(View.VISIBLE);
        } else {
            rvIngredients.setVisibility(View.VISIBLE);
            tvEmptyIngredients.setVisibility(View.GONE);
        }
    }
    
    private void selectImage() {
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
    
    private void saveMeal() {
        try {
            // Get input values
            String mealName = etMealName.getText().toString().trim();
            String category = spinnerCategory.getSelectedItem().toString();
            String prepTimeStr = etPrepTime.getText().toString().trim();
            
            // Validate input
            if (mealName.isEmpty()) {
                tilMealName.setError("Please enter a meal name");
                return;
            }
            
            // Create meal object
            Meal meal = new Meal();
            meal.setName(mealName);
            meal.setCategory(category);
            meal.setUserId(userId);
            
            // Set preparation time if provided
            if (!TextUtils.isEmpty(prepTimeStr)) {
                try {
                    int prepTime = Integer.parseInt(prepTimeStr);
                    meal.setPrepTime(prepTime);
                } catch (NumberFormatException e) {
                    Toast.makeText(this, "Invalid preparation time", Toast.LENGTH_SHORT).show();
                    return;
                }
            }
            
            // Set image path if available
            if (selectedImagePath != null && !selectedImagePath.isEmpty()) {
                meal.setImagePath(selectedImagePath);
            }
            
            // Set ingredients
            if (!ingredientsList.isEmpty()) {
                meal.setIngredients(ingredientsList);
            }
            
            // Set instructions
            if (!instructionsList.isEmpty()) {
                StringBuilder instructionsBuilder = new StringBuilder();
                for (int i = 0; i < instructionsList.size(); i++) {
                    instructionsBuilder.append(i + 1).append(". ").append(instructionsList.get(i));
                    if (i < instructionsList.size() - 1) {
                        instructionsBuilder.append("\n");
                    }
                }
                meal.setInstructions(instructionsBuilder.toString());
            }
            
            // Save to database
            long mealId = mealDao.insertMeal(meal);
            
            if (mealId != -1) {
                // Set result and finish
                setResult(RESULT_OK);
                Toast.makeText(this, "Meal saved successfully", Toast.LENGTH_SHORT).show();
                finish();
            } else {
                Toast.makeText(this, "Failed to save meal", Toast.LENGTH_SHORT).show();
            }
            
        } catch (Exception e) {
            Log.e(TAG, "Error saving meal: " + e.getMessage(), e);
            Toast.makeText(this, "Error saving meal: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
    
    @Override
    public void onDeleteInstruction(int position) {
        if (position >= 0 && position < instructionsList.size()) {
            instructionsList.remove(position);
            instructionAdapter.notifyDataSetChanged();
            updateInstructionsVisibility();
        }
    }
    
    @Override
    public void onDeleteIngredient(int position) {
        if (position >= 0 && position < ingredientsList.size()) {
            ingredientsList.remove(position);
            ingredientAdapter.notifyDataSetChanged();
            updateIngredientsVisibility();
        }
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
    
    /**
     * Method to handle intent data
     */
    private void handleIntentData() {
        try {
            Intent intent = getIntent();
            if (intent != null) {
                // Get mode and user ID
                mode = intent.getStringExtra("mode");
                userId = intent.getLongExtra("user_id", -1);
                
                // If no user ID provided, try to get from session
                if (userId == -1) {
                    userId = sessionManager.getUserId();
                }
                
                // Update activity title based on mode
                if (getSupportActionBar() != null) {
                    if ("add".equals(mode)) {
                        getSupportActionBar().setTitle("Add Meal");
                    } else if ("edit".equals(mode)) {
                        getSupportActionBar().setTitle("Edit Meal");
                    } else if ("view".equals(mode)) {
                        getSupportActionBar().setTitle("View Meal");
                    }
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error handling intent data: " + e.getMessage(), e);
            Toast.makeText(this, "Error handling intent data: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
}