package com.example.mealmateyubraj.fragments;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.ItemTouchHelper;
import android.graphics.Canvas;

import com.example.mealmateyubraj.R;
import com.example.mealmateyubraj.adapters.GroceryAdapter;
import com.example.mealmateyubraj.database.DatabaseHelper;
import com.example.mealmateyubraj.models.GroceryItem;
import com.example.mealmateyubraj.utils.SharedPrefsUtil;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;
import android.widget.AutoCompleteTextView;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.List;
import java.util.ArrayList;
import java.util.stream.Collectors;

public class GroceryFragment extends Fragment {
    private static final String TAG = "GroceryFragment";
    
    private RecyclerView rvGroceries;
    private TextView tvEmptyGroceries;
    private FloatingActionButton fabAddGrocery;
    private GroceryAdapter adapter;
    private DatabaseHelper dbHelper;
    private long userId;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView called");
        return inflater.inflate(R.layout.fragment_grocery, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Log.d(TAG, "onViewCreated called");
        
        try {
            initializeViews(view);
            setupRecyclerView();
            setupFab();
            loadGroceryItems();
        } catch (Exception e) {
            Log.e(TAG, "Error in onViewCreated: " + e.getMessage(), e);
            Toast.makeText(getContext(), "Error initializing grocery list", Toast.LENGTH_SHORT).show();
        }
    }

    private void initializeViews(View view) {
        Log.d(TAG, "Initializing views");
        rvGroceries = view.findViewById(R.id.rv_groceries);
        tvEmptyGroceries = view.findViewById(R.id.tv_empty_groceries);
        fabAddGrocery = view.findViewById(R.id.fab_add_grocery);
        
        // Optional: Set up share button if it exists in the layout
        View btnShare = view.findViewById(R.id.btn_share_grocery);
        if (btnShare != null) {
            btnShare.setOnClickListener(v -> showSharingOptionsDialog());
        }
        
        dbHelper = new DatabaseHelper(getContext());
        userId = SharedPrefsUtil.getUserId(getContext());
    }

    private void setupRecyclerView() {
        Log.d(TAG, "Setting up RecyclerView");
        adapter = new GroceryAdapter(getContext());
        rvGroceries.setLayoutManager(new LinearLayoutManager(getContext()));
        rvGroceries.setAdapter(adapter);
    }

    private void setupFab() {
        Log.d(TAG, "Setting up FAB");
        fabAddGrocery.setOnClickListener(v -> showAddGroceryDialog());
    }

    private void loadGroceryItems() {
        Log.d(TAG, "Loading grocery items for user ID: " + userId);
        try {
            List<GroceryItem> items = dbHelper.getGroceryItemsByUserId(userId);
            
            Log.d(TAG, "Loaded " + items.size() + " grocery items");
            if (!items.isEmpty()) {
                for (GroceryItem item : items) {
                    Log.d(TAG, "Item: " + item.getName() + " (" + item.getQuantity() + " " + item.getUnit() + ")");
                }
            }
            
            adapter.setItems(items);
            adapter.notifyDataSetChanged(); // Make sure to notify adapter
            updateEmptyState(items.isEmpty());
            
            // Setup click listener if not already set
            adapter.setOnGroceryItemClickListener(new GroceryAdapter.OnGroceryItemClickListener() {
                @Override
                public void onCheckboxClicked(GroceryItem item, boolean isChecked) {
                    Log.d(TAG, "Checkbox clicked for item: " + item.getName() + ", checked: " + isChecked);
                    item.setPurchased(isChecked);
                    boolean success = dbHelper.updateGroceryItem(item);
                    if (success) {
                        adapter.updateItem(item);
                        Toast.makeText(getContext(), 
                            isChecked ? "Item marked as purchased" : "Item marked as unpurchased", 
                            Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(getContext(), "Failed to update item status", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onDeleteClicked(GroceryItem item) {
                    showDeleteConfirmationDialog(item);
                }
                
                @Override
                public void onEditClicked(GroceryItem item) {
                    showEditGroceryDialog(item);
                }
            });
        } catch (Exception e) {
            Log.e(TAG, "Error loading grocery items: " + e.getMessage(), e);
            updateEmptyState(true);
        }
    }

    private void updateEmptyState(boolean isEmpty) {
        Log.d(TAG, "Updating empty state: " + isEmpty);
        if (isEmpty) {
            tvEmptyGroceries.setVisibility(View.VISIBLE);
            rvGroceries.setVisibility(View.GONE);
        } else {
            tvEmptyGroceries.setVisibility(View.GONE);
            rvGroceries.setVisibility(View.VISIBLE);
        }
    }

    private void showAddGroceryDialog() {
        Log.d(TAG, "Showing add grocery dialog");
        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_add_grocery, null);
        
        TextInputEditText etItemName = dialogView.findViewById(R.id.et_item_name);
        TextInputEditText etQuantity = dialogView.findViewById(R.id.et_quantity);
        AutoCompleteTextView actCategory = dialogView.findViewById(R.id.act_category);
        AutoCompleteTextView actUnit = dialogView.findViewById(R.id.act_unit);

        // Set up the category dropdown
        String[] categories = getResources().getStringArray(R.array.categories);
        ArrayAdapter<String> categoryAdapter = new ArrayAdapter<>(
                getContext(), android.R.layout.simple_dropdown_item_1line, categories);
        actCategory.setAdapter(categoryAdapter);
        actCategory.setText("Other", false);  // Default category

        // Set up the unit dropdown
        String[] units = getResources().getStringArray(R.array.units);
        ArrayAdapter<String> unitAdapter = new ArrayAdapter<>(
                getContext(), android.R.layout.simple_dropdown_item_1line, units);
        actUnit.setAdapter(unitAdapter);
        actUnit.setText("pcs", false);  // Default unit

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Add Grocery Item")
                .setView(dialogView)
                .setPositiveButton("Add", (dialog, which) -> {
                    String name = etItemName.getText().toString().trim();
                    String quantityStr = etQuantity.getText().toString().trim();
                    String category = actCategory.getText().toString().trim();
                    String unit = actUnit.getText().toString().trim();

                    if (name.isEmpty() || quantityStr.isEmpty()) {
                        Toast.makeText(getContext(), "Please fill all fields", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    try {
                        double quantity = Double.parseDouble(quantityStr);
                        GroceryItem item = new GroceryItem(name, quantity, unit, userId);
                        item.setCategory(category);
                        long id = dbHelper.addGroceryItem(item);
                        if (id != -1) {
                            item.setId(id);
                            loadGroceryItems();
                            Toast.makeText(getContext(), "Item added successfully", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(getContext(), "Failed to add item", Toast.LENGTH_SHORT).show();
                        }
                    } catch (NumberFormatException e) {
                        Toast.makeText(getContext(), "Invalid quantity", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "onResume called");
        loadGroceryItems();
    }

    private void showSharingOptionsDialog() {
        // Get only purchased items
        List<GroceryItem> purchasedItems = adapter.getItems().stream()
                .filter(GroceryItem::isPurchased)
                .collect(Collectors.toList());

        if (purchasedItems.isEmpty()) {
            Toast.makeText(requireContext(), "No purchased items to share", Toast.LENGTH_SHORT).show();
            return;
        }

        // Format the text to share
        StringBuilder shareText = new StringBuilder("Purchased Items:\n\n");
        for (GroceryItem item : purchasedItems) {
            shareText.append("✓ ").append(item.getName())
                    .append(" (").append(item.getQuantity())
                    .append(" ").append(item.getUnit()).append(")\n");
        }

        // Create share intent
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_TEXT, shareText.toString());

        // Create chooser with specific apps
        Intent chooser = Intent.createChooser(shareIntent, "Share via");
        
        // Add specific messaging apps to the chooser
        List<Intent> targetedIntents = new ArrayList<>();
        
        // Add SMS intent
        Intent smsIntent = new Intent(Intent.ACTION_SEND);
        smsIntent.setType("text/plain");
        smsIntent.putExtra(Intent.EXTRA_TEXT, shareText.toString());
        smsIntent.setPackage("com.android.mms");
        targetedIntents.add(smsIntent);
        
        // Add WhatsApp intent
        Intent whatsappIntent = new Intent(Intent.ACTION_SEND);
        whatsappIntent.setType("text/plain");
        whatsappIntent.putExtra(Intent.EXTRA_TEXT, shareText.toString());
        whatsappIntent.setPackage("com.whatsapp");
        targetedIntents.add(whatsappIntent);
        
        // Add Telegram intent
        Intent telegramIntent = new Intent(Intent.ACTION_SEND);
        telegramIntent.setType("text/plain");
        telegramIntent.putExtra(Intent.EXTRA_TEXT, shareText.toString());
        telegramIntent.setPackage("org.telegram.messenger");
        targetedIntents.add(telegramIntent);
        
        // Add Facebook Messenger intent
        Intent messengerIntent = new Intent(Intent.ACTION_SEND);
        messengerIntent.setType("text/plain");
        messengerIntent.putExtra(Intent.EXTRA_TEXT, shareText.toString());
        messengerIntent.setPackage("com.facebook.orca");
        targetedIntents.add(messengerIntent);
        
        // Add other messaging apps
        Intent otherMessagingIntent = new Intent(Intent.ACTION_SEND);
        otherMessagingIntent.setType("text/plain");
        otherMessagingIntent.putExtra(Intent.EXTRA_TEXT, shareText.toString());
        targetedIntents.add(otherMessagingIntent);
        
        // Create array of intents
        Intent[] intents = targetedIntents.toArray(new Intent[0]);
        
        // Create and show chooser with all intents
        Intent finalChooser = Intent.createChooser(targetedIntents.remove(0), "Share via");
        finalChooser.putExtra(Intent.EXTRA_INITIAL_INTENTS, intents);
        
        try {
            startActivity(finalChooser);
        } catch (Exception e) {
            Toast.makeText(requireContext(), "No messaging apps found", Toast.LENGTH_SHORT).show();
        }
    }

    private void showDeleteConfirmationDialog(GroceryItem item) {
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Delete Item")
                .setMessage("Are you sure you want to delete " + item.getName() + "?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    boolean success = dbHelper.deleteGroceryItemById(item.getId());
                    if (success) {
                        adapter.removeItem(item);
                        Toast.makeText(getContext(), "Item deleted successfully", Toast.LENGTH_SHORT).show();
                        // Check if we need to update empty state
                        if (adapter.getItemCount() == 0) {
                            updateEmptyState(true);
                        }
                    } else {
                        Toast.makeText(getContext(), "Failed to delete item", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void showEditGroceryDialog(GroceryItem item) {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_add_grocery, null);

        TextInputEditText etItemName = dialogView.findViewById(R.id.et_item_name);
        TextInputEditText etQuantity = dialogView.findViewById(R.id.et_quantity);
        AutoCompleteTextView actCategory = dialogView.findViewById(R.id.act_category);
        AutoCompleteTextView actUnit = dialogView.findViewById(R.id.act_unit);

        // Pre-fill the fields with existing values
        etItemName.setText(item.getName());
        etQuantity.setText(String.valueOf(item.getQuantity()));
        actCategory.setText(item.getCategory(), false);
        actUnit.setText(item.getUnit(), false);

        // Set up the category dropdown
        String[] categories = getResources().getStringArray(R.array.categories);
        ArrayAdapter<String> categoryAdapter = new ArrayAdapter<>(
                getContext(), android.R.layout.simple_dropdown_item_1line, categories);
        actCategory.setAdapter(categoryAdapter);

        // Set up the unit dropdown
        String[] units = getResources().getStringArray(R.array.units);
        ArrayAdapter<String> unitAdapter = new ArrayAdapter<>(
                getContext(), android.R.layout.simple_dropdown_item_1line, units);
        actUnit.setAdapter(unitAdapter);

        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Edit Grocery Item")
                .setView(dialogView)
                .setPositiveButton("Save", (dialog, which) -> {
                    String name = etItemName.getText().toString().trim();
                    String quantityStr = etQuantity.getText().toString().trim();
                    String category = actCategory.getText().toString().trim();
                    String unit = actUnit.getText().toString().trim();

                    if (name.isEmpty() || quantityStr.isEmpty()) {
                        Toast.makeText(getContext(), "Please fill all fields", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    try {
                        double quantity = Double.parseDouble(quantityStr);
                        item.setName(name);
                        item.setQuantity(quantity);
                        item.setCategory(category);
                        item.setUnit(unit);

                        boolean success = dbHelper.updateGroceryItem(item);
                        if (success) {
                            adapter.updateItem(item);
                            Toast.makeText(getContext(), "Item updated successfully", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(getContext(), "Failed to update item", Toast.LENGTH_SHORT).show();
                        }
                    } catch (NumberFormatException e) {
                        Toast.makeText(getContext(), "Invalid quantity", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void shareGroceryList() {
        try {
            // Get only purchased items
            List<GroceryItem> purchasedItems = new ArrayList<>();
            for (GroceryItem item : adapter.getItems()) {
                if (item.isPurchased()) {
                    purchasedItems.add(item);
                }
            }

            if (purchasedItems.isEmpty()) {
                Toast.makeText(getContext(), "No purchased items to share", Toast.LENGTH_SHORT).show();
                return;
            }

            // Create the share text
            StringBuilder shareText = new StringBuilder("Purchased Items:\n\n");
            for (GroceryItem item : purchasedItems) {
                shareText.append("✓ ").append(item.getName())
                        .append(" (").append(item.getQuantity())
                        .append(" ").append(item.getUnit())
                        .append(")\n");
            }

            // Create and start the share intent
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("text/plain");
            shareIntent.putExtra(Intent.EXTRA_TEXT, shareText.toString());
            startActivity(Intent.createChooser(shareIntent, "Share Purchased Items"));

        } catch (Exception e) {
            Log.e(TAG, "Error sharing grocery list: " + e.getMessage(), e);
            Toast.makeText(getContext(), "Error sharing grocery list", Toast.LENGTH_SHORT).show();
        }
    }
} 