package com.example.mealmateyubraj.activities;

import android.Manifest;
import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Canvas;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.telephony.SmsManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mealmateyubraj.R;
import com.example.mealmateyubraj.adapters.GroceryAdapter;
import com.example.mealmateyubraj.adapters.MealSelectionAdapter;
import com.example.mealmateyubraj.database.DatabaseHelper;
import com.example.mealmateyubraj.models.GroceryItem;
import com.example.mealmateyubraj.models.Meal;
import com.example.mealmateyubraj.utils.GroceryShareUtils;
import com.example.mealmateyubraj.utils.SessionManager;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputLayout;
import com.google.android.material.textfield.TextInputEditText;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

public class GroceryListActivity extends AppCompatActivity implements SensorEventListener {
    private static final String TAG = "GroceryListActivity";
    private static final int SMS_PERMISSION_REQUEST_CODE = 123;
    private static final int CONTACTS_PERMISSION_REQUEST_CODE = 124;
    private static final int CONTACT_PICKER_RESULT = 1001;
    
    private Toolbar toolbar;
    private RecyclerView recyclerViewGrocery;
    private TextView tvEmptyList;
    private FloatingActionButton fabGenerateList;
    private FloatingActionButton fabSendList;
    private FloatingActionButton fabAddItem;
    
    private DatabaseHelper databaseHelper;
    private SessionManager sessionManager;
    private GroceryAdapter groceryAdapter;
    private long userId;
    
    // Common grocery categories
    private static final String[] CATEGORIES = {
        "Fruits", "Vegetables", "Dairy", "Meat", "Bakery", "Grains", 
        "Canned Goods", "Frozen Foods", "Snacks", "Beverages", "Condiments", 
        "Spices", "Baking", "Household", "Personal Care", "Other"
    };
    
    // Common units
    private static final String[] UNITS = {
        "g", "kg", "ml", "l", "pcs", "cups", "tbsp", "tsp", "oz", "lb"
    };
    
    // Shake detection constants
    private static final float SHAKE_THRESHOLD_GRAVITY = 2.7F;
    private static final int SHAKE_SLOP_TIME_MS = 500;
    private static final int SHAKE_COUNT_RESET_TIME_MS = 3000;

    private SensorManager sensorManager;
    private Sensor accelerometer;
    private long lastShakeTime;
    private int shakeCount;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_grocery_list);
        
        try {
            // Initialize components
            toolbar = findViewById(R.id.toolbar);
            recyclerViewGrocery = findViewById(R.id.recycler_view_grocery);
            tvEmptyList = findViewById(R.id.tv_empty_list);
            fabGenerateList = findViewById(R.id.fab_generate_list);
            fabSendList = findViewById(R.id.fab_send_list);
            fabAddItem = findViewById(R.id.fab_add_item);
            
            // Initialize shake detection
            sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
            if (sensorManager != null) {
                accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
            }
            
            // Set up toolbar
            setSupportActionBar(toolbar);
            if (getSupportActionBar() != null) {
                getSupportActionBar().setDisplayHomeAsUpEnabled(true);
                getSupportActionBar().setDisplayShowHomeEnabled(true);
            }
            
            // Initialize database helper and session manager
            databaseHelper = new DatabaseHelper(this);
            sessionManager = new SessionManager(this);
            
            // Check if user is logged in
            if (!sessionManager.isLoggedIn()) {
                Toast.makeText(this, "Please log in to access grocery list", Toast.LENGTH_SHORT).show();
                finish();
                return;
            }
            
            userId = sessionManager.getUserId();
            
            // Set up RecyclerView
            setupRecyclerView();
            
            // Load grocery items
            loadGroceryItems();
            
            // Set up FAB listeners
            fabGenerateList.setOnClickListener(v -> showSelectMealsDialog());
            fabSendList.setOnClickListener(v -> showSharingOptionsDialog());
            fabAddItem.setOnClickListener(v -> showAddItemDialog());
            
            // Show gesture controls hint
            showGestureControlsHint();
            
        } catch (Exception e) {
            Log.e(TAG, "Error initializing GroceryListActivity: " + e.getMessage(), e);
            Toast.makeText(this, "Error initializing grocery list. Please try again.", Toast.LENGTH_SHORT).show();
            finish();
        }
    }
    
    private void setupRecyclerView() {
        recyclerViewGrocery.setLayoutManager(new LinearLayoutManager(this));
        groceryAdapter = new GroceryAdapter(this);
        recyclerViewGrocery.setAdapter(groceryAdapter);
        
        // Set up swipe-to-delete
        setupSwipeToDelete();
        
        // Set up click listeners
        groceryAdapter.setOnGroceryItemClickListener(new GroceryAdapter.OnGroceryItemClickListener() {
            @Override
            public void onCheckboxClicked(GroceryItem item, boolean isChecked) {
                updateItemPurchasedStatus(item, isChecked);
            }
            
            @Override
            public void onDeleteClicked(GroceryItem item) {
                deleteGroceryItem(item);
            }
            
            @Override
            public void onEditClicked(GroceryItem item) {
                showEditItemDialog(item);
            }
        });
    }
    
    private void setupSwipeToDelete() {
        ItemTouchHelper.SimpleCallback simpleCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                return false;
            }
            
            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                int position = viewHolder.getAdapterPosition();
                if (position != RecyclerView.NO_POSITION) {
                    Object item = ((GroceryAdapter) recyclerViewGrocery.getAdapter()).getItemAt(position);
                    if (item instanceof GroceryItem) {
                        GroceryItem groceryItem = (GroceryItem) item;
                        if (direction == ItemTouchHelper.LEFT) {
                            // Swipe left to delete
                            deleteGroceryItem(groceryItem);
                        } else if (direction == ItemTouchHelper.RIGHT) {
                            // Swipe right to mark as purchased
                            boolean newStatus = !groceryItem.isPurchased(); // Toggle current status
                            updateItemPurchasedStatus(groceryItem, newStatus);
                            // Refresh the adapter to show the item correctly
                            groceryAdapter.notifyItemChanged(position);
                        }
                    } else {
                        // If it's a header, refresh the adapter to show the header again
                        loadGroceryItems();
                    }
                }
            }
            
            @Override
            public void onChildDraw(@NonNull Canvas c, @NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, 
                    float dX, float dY, int actionState, boolean isCurrentlyActive) {
                // Only enable swipe for grocery items, not headers
                if (viewHolder.getItemViewType() == GroceryAdapter.TYPE_ITEM) {
                    // Add visual cues for swipe direction
                    View itemView = viewHolder.itemView;
                    if (dX < 0) {
                        // Swipe left (delete) - show red background
                        c.clipRect(itemView.getRight() + dX, itemView.getTop(), 
                                itemView.getRight(), itemView.getBottom());
                        c.drawColor(ContextCompat.getColor(GroceryListActivity.this, android.R.color.holo_red_light));
                    } else if (dX > 0) {
                        // Swipe right (mark as purchased) - show green background
                        c.clipRect(itemView.getLeft(), itemView.getTop(), 
                                itemView.getLeft() + dX, itemView.getBottom());
                        c.drawColor(ContextCompat.getColor(GroceryListActivity.this, android.R.color.holo_green_light));
                    }
                    
                    super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
                } else {
                    super.onChildDraw(c, recyclerView, viewHolder, 0, dY, actionState, isCurrentlyActive);
                }
            }
            
            @Override
            public int getSwipeDirs(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder) {
                // Only enable swipe for grocery items, not headers
                if (viewHolder.getItemViewType() == GroceryAdapter.TYPE_HEADER) {
                    return 0;
                }
                return super.getSwipeDirs(recyclerView, viewHolder);
            }
        };
        
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(simpleCallback);
        itemTouchHelper.attachToRecyclerView(recyclerViewGrocery);
    }
    
    private void loadGroceryItems() {
        try {
            List<GroceryItem> groceryItems = databaseHelper.getGroceryItemsByUserId(userId);
            
            if (groceryItems.isEmpty()) {
                tvEmptyList.setVisibility(View.VISIBLE);
                recyclerViewGrocery.setVisibility(View.GONE);
            } else {
                tvEmptyList.setVisibility(View.GONE);
                recyclerViewGrocery.setVisibility(View.VISIBLE);
                groceryAdapter.setItems(groceryItems);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error loading grocery items: " + e.getMessage(), e);
            Toast.makeText(this, "Error loading grocery items", Toast.LENGTH_SHORT).show();
        }
    }
    
    private void updateItemPurchasedStatus(GroceryItem item, boolean purchased) {
        try {
            // Update the item's purchased status
            item.setPurchased(purchased);
            
            // Update the database
            boolean success = databaseHelper.updateGroceryItemPurchasedStatus(item.getId(), purchased);
            
            if (success) {
                // Update the adapter
                groceryAdapter.updateItem(item);
            } else {
                Log.e(TAG, "Failed to update purchased status in database");
                Toast.makeText(this, "Failed to update item status", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Log.e(TAG, "Error updating purchased status: " + e.getMessage(), e);
            Toast.makeText(this, "Error updating item status", Toast.LENGTH_SHORT).show();
        }
    }
    
    private void deleteGroceryItem(GroceryItem item) {
        try {
            // Delete from database
            boolean success = databaseHelper.deleteGroceryItemById(item.getId());
            
            if (success) {
                // Remove from adapter
                groceryAdapter.removeItem(item);
                
                // Check if list is now empty
                if (groceryAdapter.getItemCount() == 0) {
                    tvEmptyList.setVisibility(View.VISIBLE);
                    recyclerViewGrocery.setVisibility(View.GONE);
                }
            } else {
                Log.e(TAG, "Failed to delete grocery item from database");
                Toast.makeText(this, "Failed to delete item", Toast.LENGTH_SHORT).show();
                
                // Reload items since swipe might have removed the visual
                loadGroceryItems();
            }
        } catch (Exception e) {
            Log.e(TAG, "Error deleting grocery item: " + e.getMessage(), e);
            Toast.makeText(this, "Error deleting item", Toast.LENGTH_SHORT).show();
            
            // Reload items since swipe might have removed the visual
            loadGroceryItems();
        }
    }
    
    private void showSelectMealsDialog() {
        try {
            // Get meals from database
            List<Meal> meals = databaseHelper.getMealsByUserId(userId);
            
            if (meals.isEmpty()) {
                Toast.makeText(this, R.string.no_meals_for_grocery, Toast.LENGTH_SHORT).show();
                return;
            }
            
            // Inflate dialog view
            View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_select_meals, null);
            
            RecyclerView recyclerViewMeals = dialogView.findViewById(R.id.recycler_view_select_meals);
            TextView tvNoMeals = dialogView.findViewById(R.id.tv_no_meals);
            MaterialButton btnCancel = dialogView.findViewById(R.id.btn_cancel);
            MaterialButton btnGenerate = dialogView.findViewById(R.id.btn_generate);
            
            // Set up RecyclerView
            recyclerViewMeals.setLayoutManager(new LinearLayoutManager(this));
            MealSelectionAdapter adapter = new MealSelectionAdapter(this);
            recyclerViewMeals.setAdapter(adapter);
            
            adapter.setMeals(meals);
            
            // Create dialog
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setView(dialogView);
            AlertDialog dialog = builder.create();
            
            // Button click listeners
            btnCancel.setOnClickListener(v -> dialog.dismiss());
            
            btnGenerate.setOnClickListener(v -> {
                List<Meal> selectedMeals = adapter.getSelectedMeals();
                
                if (selectedMeals.isEmpty()) {
                    Toast.makeText(this, "Please select at least one meal", Toast.LENGTH_SHORT).show();
                    return;
                }
                
                // Generate grocery list
                generateGroceryList(selectedMeals);
                dialog.dismiss();
            });
            
            dialog.show();
            
        } catch (Exception e) {
            Log.e(TAG, "Error showing select meals dialog: " + e.getMessage(), e);
            Toast.makeText(this, "Error loading meals", Toast.LENGTH_SHORT).show();
        }
    }
    
    private void generateGroceryList(List<Meal> selectedMeals) {
        try {
            boolean success = databaseHelper.generateGroceryListFromMeals(selectedMeals, userId);
            
            if (success) {
                Toast.makeText(this, R.string.grocery_list_generated, Toast.LENGTH_SHORT).show();
                loadGroceryItems();
            } else {
                Toast.makeText(this, "Failed to generate grocery list", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Log.e(TAG, "Error generating grocery list: " + e.getMessage(), e);
            Toast.makeText(this, "Error generating grocery list", Toast.LENGTH_SHORT).show();
        }
    }
    
    private void showSharingOptionsDialog() {
        try {
            // Get grocery items
            List<GroceryItem> groceryItems = databaseHelper.getGroceryItemsByUserId(userId);
            
            if (groceryItems.isEmpty()) {
                Toast.makeText(this, R.string.empty_grocery_list, Toast.LENGTH_SHORT).show();
                return;
            }
            
            // Create the dialog
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Share Grocery List");
            
            String[] options = {"SMS", "Email", "WhatsApp", "Copy to Clipboard", "Other Apps"};
            
            builder.setItems(options, (dialog, which) -> {
                switch (which) {
                    case 0: // SMS
                        showSendSmsDialog();
                        break;
                    case 1: // Email
                        GroceryShareUtils.shareViaEmail(this, groceryItems, getString(R.string.app_name));
                        break;
                    case 2: // WhatsApp
                        GroceryShareUtils.shareViaWhatsApp(this, groceryItems, getString(R.string.app_name));
                        break;
                    case 3: // Copy to Clipboard
                        GroceryShareUtils.copyToClipboard(this, groceryItems, getString(R.string.app_name));
                        break;
                    case 4: // Other Apps
                        GroceryShareUtils.shareViaOtherApps(this, groceryItems, getString(R.string.app_name));
                        break;
                }
            });
            
            builder.setNegativeButton("Cancel", null);
            builder.show();
            
        } catch (Exception e) {
            Log.e(TAG, "Error showing sharing options: " + e.getMessage(), e);
            Toast.makeText(this, "Error preparing sharing options", Toast.LENGTH_SHORT).show();
        }
    }
    
    private void showSendSmsDialog() {
        try {
            // Get grocery items
            List<GroceryItem> groceryItems = databaseHelper.getGroceryItemsByUserId(userId);
            
            if (groceryItems.isEmpty()) {
                Toast.makeText(this, R.string.empty_grocery_list, Toast.LENGTH_SHORT).show();
                return;
            }
            
            // Inflate dialog view
            View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_send_sms, null);
            
            EditText etPhoneNumber = dialogView.findViewById(R.id.et_phone_number);
            MaterialButton btnCancel = dialogView.findViewById(R.id.btn_cancel);
            MaterialButton btnSend = dialogView.findViewById(R.id.btn_send);
            TextInputLayout tilPhoneNumber = dialogView.findViewById(R.id.til_phone_number);
            
            // Create dialog
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setView(dialogView);
            builder.setTitle(R.string.delegate_grocery_list);
            AlertDialog dialog = builder.create();
            
            // Set up contact selection icon click listener
            tilPhoneNumber.setEndIconOnClickListener(v -> {
                // Check for contacts permission
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS) != 
                        PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(this, 
                            new String[]{Manifest.permission.READ_CONTACTS}, 
                            CONTACTS_PERMISSION_REQUEST_CODE);
                } else {
                    openContactPicker();
                    dialog.dismiss();
                }
            });
            
            // Button click listeners
            btnCancel.setOnClickListener(v -> dialog.dismiss());
            
            btnSend.setOnClickListener(v -> {
                String phoneNumber = etPhoneNumber.getText().toString().trim();
                
                if (phoneNumber.isEmpty()) {
                    Toast.makeText(this, R.string.please_enter_phone_number, Toast.LENGTH_SHORT).show();
                    return;
                }
                
                // Use utility to send
                GroceryShareUtils.shareViaSms(this, phoneNumber, groceryItems, getString(R.string.app_name));
                dialog.dismiss();
            });
            
            dialog.show();
            
        } catch (Exception e) {
            Log.e(TAG, "Error showing send SMS dialog: " + e.getMessage(), e);
            Toast.makeText(this, "Error preparing to send SMS", Toast.LENGTH_SHORT).show();
        }
    }
    
    private void openContactPicker() {
        try {
            Intent contactPickerIntent = new Intent(Intent.ACTION_PICK, 
                    ContactsContract.CommonDataKinds.Phone.CONTENT_URI);
            startActivityForResult(contactPickerIntent, CONTACT_PICKER_RESULT);
        } catch (Exception e) {
            Log.e(TAG, "Error opening contact picker: " + e.getMessage(), e);
            Toast.makeText(this, "Error opening contacts", Toast.LENGTH_SHORT).show();
        }
    }
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        
        if (requestCode == CONTACT_PICKER_RESULT && resultCode == RESULT_OK) {
            try {
                ContentResolver resolver = getContentResolver();
                Uri contactUri = data.getData();
                
                String[] projection = new String[]{ContactsContract.CommonDataKinds.Phone.NUMBER,
                        ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME};
                
                Cursor cursor = resolver.query(contactUri, projection, null, null, null);
                
                if (cursor != null && cursor.moveToFirst()) {
                    int numberIndex = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER);
                    int nameIndex = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME);
                    
                    String number = cursor.getString(numberIndex);
                    String name = cursor.getString(nameIndex);
                    
                    cursor.close();
                    
                    // Show selected contact info
                    Toast.makeText(this, "Selected: " + name, Toast.LENGTH_SHORT).show();
                    Log.d(TAG, "Selected contact: " + name + " (" + number + ")");
                    
                    // Show SMS dialog with the selected number
                    showSendSmsDialogWithNumber(number);
                }
            } catch (Exception e) {
                Log.e(TAG, "Error processing contact selection: " + e.getMessage(), e);
                Toast.makeText(this, "Error selecting contact", Toast.LENGTH_SHORT).show();
            }
        }
    }
    
    private void showSendSmsDialogWithNumber(String phoneNumber) {
        try {
            // Get grocery items
            List<GroceryItem> groceryItems = databaseHelper.getGroceryItemsByUserId(userId);
            
            if (groceryItems.isEmpty()) {
                Toast.makeText(this, R.string.empty_grocery_list, Toast.LENGTH_SHORT).show();
                return;
            }
            
            // Inflate dialog view
            View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_send_sms, null);
            
            EditText etPhoneNumber = dialogView.findViewById(R.id.et_phone_number);
            MaterialButton btnCancel = dialogView.findViewById(R.id.btn_cancel);
            MaterialButton btnSend = dialogView.findViewById(R.id.btn_send);
            TextInputLayout tilPhoneNumber = dialogView.findViewById(R.id.til_phone_number);
            
            // Set the phone number
            etPhoneNumber.setText(phoneNumber);
            
            // Create dialog
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setView(dialogView);
            builder.setTitle(R.string.delegate_grocery_list);
            AlertDialog dialog = builder.create();
            
            // Set up contact selection icon click listener
            tilPhoneNumber.setEndIconOnClickListener(v -> {
                // Check for contacts permission
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS) != 
                        PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(this, 
                            new String[]{Manifest.permission.READ_CONTACTS}, 
                            CONTACTS_PERMISSION_REQUEST_CODE);
                } else {
                    openContactPicker();
                    dialog.dismiss();
                }
            });
            
            // Button click listeners
            btnCancel.setOnClickListener(v -> dialog.dismiss());
            
            btnSend.setOnClickListener(v -> {
                String number = etPhoneNumber.getText().toString().trim();
                
                if (number.isEmpty()) {
                    Toast.makeText(this, R.string.please_enter_phone_number, Toast.LENGTH_SHORT).show();
                    return;
                }
                
                // Use utility to send
                GroceryShareUtils.shareViaSms(this, number, groceryItems, getString(R.string.app_name));
                dialog.dismiss();
            });
            
            dialog.show();
            
        } catch (Exception e) {
            Log.e(TAG, "Error showing send SMS dialog: " + e.getMessage(), e);
            Toast.makeText(this, "Error preparing to send SMS", Toast.LENGTH_SHORT).show();
        }
    }
    
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        
        if (requestCode == SMS_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                showSendSmsDialog();
            } else {
                Toast.makeText(this, R.string.sms_permission_denied, Toast.LENGTH_SHORT).show();
            }
        } else if (requestCode == CONTACTS_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openContactPicker();
            } else {
                Toast.makeText(this, R.string.read_contacts_permission_denied, Toast.LENGTH_SHORT).show();
            }
        }
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        
        if (id == android.R.id.home) {
            onBackPressed();
            return true;
        } else if (id == R.id.action_clear_purchased) {
            showConfirmationDialog(R.string.confirm_clear_purchased, () -> {
                clearPurchasedItems();
            });
            return true;
        } else if (id == R.id.action_clear_all) {
            showConfirmationDialog(R.string.confirm_clear_items, () -> {
                clearAllItems();
            });
            return true;
        }
        
        return super.onOptionsItemSelected(item);
    }
    
    private void clearPurchasedItems() {
        try {
            boolean success = databaseHelper.deletePurchasedGroceryItems(userId);
            
            if (success) {
                Toast.makeText(this, R.string.purchased_items_cleared, Toast.LENGTH_SHORT).show();
                loadGroceryItems();
            } else {
                Toast.makeText(this, "No purchased items to clear", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Log.e(TAG, "Error clearing purchased items: " + e.getMessage(), e);
            Toast.makeText(this, "Error clearing purchased items", Toast.LENGTH_SHORT).show();
        }
    }
    
    private void clearAllItems() {
        try {
            boolean success = databaseHelper.deleteAllGroceryItems(userId);
            
            if (success) {
                Toast.makeText(this, R.string.grocery_list_cleared, Toast.LENGTH_SHORT).show();
                loadGroceryItems();
            } else {
                Toast.makeText(this, "No items to clear", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Log.e(TAG, "Error clearing all items: " + e.getMessage(), e);
            Toast.makeText(this, "Error clearing grocery list", Toast.LENGTH_SHORT).show();
        }
    }
    
    private void showConfirmationDialog(int messageResId, Runnable confirmAction) {
        new AlertDialog.Builder(this)
                .setMessage(messageResId)
                .setPositiveButton(R.string.confirm, (dialog, which) -> confirmAction.run())
                .setNegativeButton(R.string.cancel, null)
                .show();
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        // Register the sensor listener when the activity is resumed
        if (sensorManager != null && accelerometer != null) {
            sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_UI);
        }
        // Reload grocery items in case they changed
        loadGroceryItems();
    }
    
    @Override
    protected void onPause() {
        super.onPause();
        // Unregister the sensor listener when the activity is paused to save battery
        if (sensorManager != null) {
            sensorManager.unregisterListener(this);
        }
    }
    
    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            detectShake(event);
        }
    }
    
    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // Not used, but required to implement SensorEventListener
    }
    
    private void detectShake(SensorEvent event) {
        float x = event.values[0];
        float y = event.values[1];
        float z = event.values[2];

        float gX = x / SensorManager.GRAVITY_EARTH;
        float gY = y / SensorManager.GRAVITY_EARTH;
        float gZ = z / SensorManager.GRAVITY_EARTH;

        // gForce will be close to 1 when there is no movement
        float gForce = (float) Math.sqrt(gX * gX + gY * gY + gZ * gZ);

        if (gForce > SHAKE_THRESHOLD_GRAVITY) {
            final long now = System.currentTimeMillis();
            // Ignore shake events too close to each other (500ms)
            if (lastShakeTime + SHAKE_SLOP_TIME_MS > now) {
                return;
            }

            // Reset the shake count after 3 seconds of no shakes
            if (lastShakeTime + SHAKE_COUNT_RESET_TIME_MS < now) {
                shakeCount = 0;
            }

            lastShakeTime = now;
            shakeCount++;

            // Require at least 3 shakes to trigger action
            if (shakeCount >= 3) {
                // Reset counter
                shakeCount = 0;
                
                // Execute on UI thread
                runOnUiThread(() -> {
                    // Show confirmation dialog before clearing
                    showConfirmationDialog(R.string.confirm_clear_purchased, () -> {
                        clearPurchasedItems();
                    });
                });
            }
        }
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_grocery_list, menu);
        return true;
    }
    
    private void showAddItemDialog() {
        try {
            // Inflate dialog view
            View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_grocery_item, null);
            
            // Get views
            EditText etItemName = dialogView.findViewById(R.id.et_item_name);
            AutoCompleteTextView actvCategory = dialogView.findViewById(R.id.et_item_category);
            EditText etQuantity = dialogView.findViewById(R.id.et_item_quantity);
            AutoCompleteTextView actvUnit = dialogView.findViewById(R.id.et_item_unit);
            
            // Set up category dropdown
            ArrayAdapter<String> categoryAdapter = new ArrayAdapter<>(
                    this, android.R.layout.simple_dropdown_item_1line, CATEGORIES);
            actvCategory.setAdapter(categoryAdapter);
            actvCategory.setText("Other", false);  // Default category
            
            // Set up unit dropdown
            ArrayAdapter<String> unitAdapter = new ArrayAdapter<>(
                    this, android.R.layout.simple_dropdown_item_1line, UNITS);
            actvUnit.setAdapter(unitAdapter);
            actvUnit.setText("pcs", false);  // Default unit
            
            // Set default values
            etQuantity.setText("1");
            
            // Create dialog
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Add Grocery Item");
            builder.setView(dialogView);
            AlertDialog dialog = builder.create();
            
            // Set up button click listeners
            dialogView.findViewById(R.id.btn_cancel).setOnClickListener(v -> dialog.dismiss());
            
            dialogView.findViewById(R.id.btn_add).setOnClickListener(v -> {
                String name = etItemName.getText().toString().trim();
                String category = actvCategory.getText().toString().trim();
                String quantityStr = etQuantity.getText().toString().trim();
                String unit = actvUnit.getText().toString().trim();
                
                // Validate inputs
                if (TextUtils.isEmpty(name)) {
                    Toast.makeText(this, "Please enter an item name", Toast.LENGTH_SHORT).show();
                    return;
                }
                
                if (TextUtils.isEmpty(category)) {
                    category = "Other";
                }
                
                float quantity = 1f;
                try {
                    if (!TextUtils.isEmpty(quantityStr)) {
                        quantity = Float.parseFloat(quantityStr);
                    }
                } catch (NumberFormatException e) {
                    Toast.makeText(this, "Invalid quantity", Toast.LENGTH_SHORT).show();
                    return;
                }
                
                if (TextUtils.isEmpty(unit)) {
                    unit = "pcs";
                }
                
                // Create and add the grocery item
                addGroceryItem(name, category, quantity, unit);
                dialog.dismiss();
            });
            
            dialog.show();
            
        } catch (Exception e) {
            Log.e(TAG, "Error showing add item dialog: " + e.getMessage(), e);
            Toast.makeText(this, "Error adding item", Toast.LENGTH_SHORT).show();
        }
    }
    
    private void addGroceryItem(String name, String category, float quantity, String unit) {
        try {
            // Create the grocery item
            GroceryItem item = new GroceryItem();
            item.setName(name);
            item.setCategory(category);
            item.setQuantity(quantity);
            item.setUnit(unit);
            item.setPurchased(false);
            item.setUserId(userId);
            
            // Add to database
            long itemId = databaseHelper.insertGroceryItem(item);
            
            if (itemId > 0) {
                // Set the ID and reload the list
                item.setId(itemId);
                Toast.makeText(this, "Item added", Toast.LENGTH_SHORT).show();
                loadGroceryItems();
            } else {
                Toast.makeText(this, "Failed to add item", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Log.e(TAG, "Error adding grocery item: " + e.getMessage(), e);
            Toast.makeText(this, "Error adding item", Toast.LENGTH_SHORT).show();
        }
    }
    
    /**
     * Show a hint about available gesture controls to the user
     */
    private void showGestureControlsHint() {
        Toast.makeText(this, R.string.gesture_controls_hint, Toast.LENGTH_LONG).show();
    }
    
    private void showEditItemDialog(GroceryItem item) {
        try {
            // Inflate dialog view
            View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_edit_grocery, null);
            
            // Get views
            TextInputEditText etItemName = dialogView.findViewById(R.id.et_item_name);
            TextInputEditText etQuantity = dialogView.findViewById(R.id.et_quantity);
            AutoCompleteTextView actCategory = dialogView.findViewById(R.id.act_category);
            AutoCompleteTextView actUnit = dialogView.findViewById(R.id.act_unit);
            MaterialButton btnCancel = dialogView.findViewById(R.id.btn_cancel);
            MaterialButton btnSave = dialogView.findViewById(R.id.btn_save);
            
            // Set current values
            etItemName.setText(item.getName());
            etQuantity.setText(String.valueOf(item.getQuantity()));
            
            // Set up category dropdown
            ArrayAdapter<String> categoryAdapter = new ArrayAdapter<>(
                    this, android.R.layout.simple_dropdown_item_1line, CATEGORIES);
            actCategory.setAdapter(categoryAdapter);
            actCategory.setText(item.getCategory(), false);
            
            // Set up unit dropdown
            ArrayAdapter<String> unitAdapter = new ArrayAdapter<>(
                    this, android.R.layout.simple_dropdown_item_1line, UNITS);
            actUnit.setAdapter(unitAdapter);
            actUnit.setText(item.getUnit(), false);
            
            // Create dialog
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Edit Grocery Item");
            builder.setView(dialogView);
            AlertDialog dialog = builder.create();
            
            // Button click listeners
            btnCancel.setOnClickListener(v -> dialog.dismiss());
            
            btnSave.setOnClickListener(v -> {
                String name = etItemName.getText().toString().trim();
                String category = actCategory.getText().toString().trim();
                String quantityStr = etQuantity.getText().toString().trim();
                String unit = actUnit.getText().toString().trim();
                
                // Validate inputs
                if (TextUtils.isEmpty(name)) {
                    Toast.makeText(this, "Please enter an item name", Toast.LENGTH_SHORT).show();
                    return;
                }
                
                if (TextUtils.isEmpty(category)) {
                    category = "Other";
                }
                
                float quantity = 1f;
                try {
                    if (!TextUtils.isEmpty(quantityStr)) {
                        quantity = Float.parseFloat(quantityStr);
                    }
                } catch (NumberFormatException e) {
                    Toast.makeText(this, "Invalid quantity", Toast.LENGTH_SHORT).show();
                    return;
                }
                
                if (TextUtils.isEmpty(unit)) {
                    unit = "pcs";
                }
                
                // Update the grocery item
                updateGroceryItem(item, name, category, quantity, unit);
                dialog.dismiss();
            });
            
            dialog.show();
            
        } catch (Exception e) {
            Log.e(TAG, "Error showing edit item dialog: " + e.getMessage(), e);
            Toast.makeText(this, "Error editing item", Toast.LENGTH_SHORT).show();
        }
    }
    
    private void updateGroceryItem(GroceryItem item, String name, String category, float quantity, String unit) {
        try {
            // Update item values
            item.setName(name);
            item.setCategory(category);
            item.setQuantity(quantity);
            item.setUnit(unit);
            
            // Update the item in the database
            boolean success = databaseHelper.updateGroceryItem(item);
            if (success) {
                // Update the adapter
                groceryAdapter.updateItem(item);
                Toast.makeText(this, "Item updated successfully", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Failed to update item", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Log.e(TAG, "Error updating grocery item: " + e.getMessage(), e);
            Toast.makeText(this, "Error updating item", Toast.LENGTH_SHORT).show();
            
            // Reload items since update had an error
            loadGroceryItems();
        }
    }
} 