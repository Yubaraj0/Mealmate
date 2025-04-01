package com.example.mealmateyubraj.fragments;

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
import androidx.appcompat.app.AlertDialog;
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
        
        // Set up share button
        MaterialButton btnShare = view.findViewById(R.id.btn_share_grocery);
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

        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(getContext());
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
                        addNewItem(name, quantity, unit, category);
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
        try {
            // Get all grocery items
            List<GroceryItem> groceryItems = adapter.getItems();

            if (groceryItems.isEmpty()) {
                Toast.makeText(requireContext(), "No grocery items to share", Toast.LENGTH_SHORT).show();
                return;
            }

            // Create a dialog with sharing options
            View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_share_options, null);
            
            MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Share Grocery List")
                .setView(dialogView)
                .setNegativeButton("Cancel", null);
            
            AlertDialog dialog = builder.create();
            dialog.show();
            
            // Set up click listeners for sharing options
            dialogView.findViewById(R.id.btn_share_all).setOnClickListener(v -> {
                com.example.mealmateyubraj.utils.GroceryShareUtils.shareViaOtherApps(
                    requireContext(), groceryItems, getString(R.string.app_name));
                dialog.dismiss();
            });
            
            dialogView.findViewById(R.id.btn_share_whatsapp).setOnClickListener(v -> {
                com.example.mealmateyubraj.utils.GroceryShareUtils.shareViaWhatsApp(
                    requireContext(), groceryItems, getString(R.string.app_name));
                dialog.dismiss();
            });
            
            dialogView.findViewById(R.id.btn_share_email).setOnClickListener(v -> {
                com.example.mealmateyubraj.utils.GroceryShareUtils.shareViaEmail(
                    requireContext(), groceryItems, getString(R.string.app_name));
                dialog.dismiss();
            });
            
            dialogView.findViewById(R.id.btn_share_sms).setOnClickListener(v -> {
                // Show phone number input dialog
                showPhoneNumberInputDialog(groceryItems);
                dialog.dismiss();
            });
            
            dialogView.findViewById(R.id.btn_copy_clipboard).setOnClickListener(v -> {
                com.example.mealmateyubraj.utils.GroceryShareUtils.copyToClipboard(
                    requireContext(), groceryItems, getString(R.string.app_name));
                dialog.dismiss();
            });
            
            dialogView.findViewById(R.id.btn_share_purchased).setOnClickListener(v -> {
                // Filter for purchased items only
                List<GroceryItem> purchasedItems = groceryItems.stream()
                    .filter(GroceryItem::isPurchased)
                    .collect(Collectors.toList());
                
                if (purchasedItems.isEmpty()) {
                    Toast.makeText(requireContext(), "No purchased items to share", Toast.LENGTH_SHORT).show();
                } else {
                    com.example.mealmateyubraj.utils.GroceryShareUtils.shareViaOtherApps(
                        requireContext(), purchasedItems, getString(R.string.app_name));
                }
                dialog.dismiss();
            });

        } catch (Exception e) {
            Log.e(TAG, "Error showing sharing options: " + e.getMessage(), e);
            Toast.makeText(requireContext(), "Error showing sharing options", Toast.LENGTH_SHORT).show();
        }
    }
    
    private void showPhoneNumberInputDialog(List<GroceryItem> groceryItems) {
        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_phone_number, null);
        EditText etPhoneNumber = dialogView.findViewById(R.id.et_phone_number);
        
        new MaterialAlertDialogBuilder(requireContext())
            .setTitle("Enter Phone Number")
            .setView(dialogView)
            .setPositiveButton("Send", (dialog, which) -> {
                String phoneNumber = etPhoneNumber.getText().toString().trim();
                if (!phoneNumber.isEmpty()) {
                    com.example.mealmateyubraj.utils.GroceryShareUtils.shareViaSms(
                        requireContext(), phoneNumber, groceryItems, getString(R.string.app_name));
                } else {
                    Toast.makeText(requireContext(), "Please enter a phone number", Toast.LENGTH_SHORT).show();
                }
            })
            .setNegativeButton("Cancel", null)
            .show();
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
                        editItem(item, name, quantity, unit, category);
                    } catch (NumberFormatException e) {
                        Toast.makeText(getContext(), "Invalid quantity", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void addNewItem(String name, double quantity, String unit, String category) {
        Log.d(TAG, "Adding new item: " + name + " (" + quantity + " " + unit + ")");
        try {
            GroceryItem newItem = new GroceryItem();
            newItem.setItemName(name);
            newItem.setQuantity(String.valueOf(quantity));
            newItem.setUnit(unit);
            newItem.setCategory(category);
            newItem.setUserId(userId);
            newItem.setPurchased(false);

            long itemId = dbHelper.addGroceryItem(newItem);
            if (itemId != -1) {
                newItem.setId(itemId);
                adapter.addItem(newItem);
                Toast.makeText(getContext(), "Item added successfully", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getContext(), "Failed to add item", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Log.e(TAG, "Error adding new item: " + e.getMessage(), e);
            Toast.makeText(getContext(), "Error adding item", Toast.LENGTH_SHORT).show();
        }
    }

    private void editItem(GroceryItem item, String name, double quantity, String unit, String category) {
        Log.d(TAG, "Editing item: " + name + " (" + quantity + " " + unit + ")");
        try {
            item.setItemName(name);
            item.setQuantity(String.valueOf(quantity));
            item.setUnit(unit);
            item.setCategory(category);

            boolean success = dbHelper.updateGroceryItem(item);
            if (success) {
                adapter.updateItem(item);
                Toast.makeText(getContext(), "Item updated successfully", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getContext(), "Failed to update item", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Log.e(TAG, "Error editing item: " + e.getMessage(), e);
            Toast.makeText(getContext(), "Error updating item", Toast.LENGTH_SHORT).show();
        }
    }
}