package com.example.mealmateyubraj.utils;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.widget.Toast;

import com.example.mealmateyubraj.models.GroceryItem;

import java.util.List;

/**
 * Utility class for sharing grocery lists through various methods
 */
public class GroceryShareUtils {
    private static final String TAG = "GroceryShareUtils";

    /**
     * Format a grocery list as text
     *
     * @param groceryItems List of grocery items
     * @param appName Name of the app to add to the signature
     * @return Formatted text string
     */
    public static String formatGroceryListAsText(List<GroceryItem> groceryItems, String appName) {
        StringBuilder messageBuilder = new StringBuilder("My Grocery List\n\n");
        
        // Group by category
        String currentCategory = null;
        
        for (GroceryItem item : groceryItems) {
            if (!item.getCategory().equals(currentCategory)) {
                currentCategory = item.getCategory();
                messageBuilder.append("\n").append(currentCategory).append(":\n");
            }
            
            messageBuilder.append("- ")
                    .append(String.format("%.1f %s %s", item.getQuantity(), item.getUnit(), item.getName()))
                    .append(item.isPurchased() ? " (✓)" : "")
                    .append("\n");
        }
        
        // Add app promo at the end
        messageBuilder.append("\n\nSent from ").append(appName).append(" app");
        
        return messageBuilder.toString();
    }

    /**
     * Share grocery list via SMS app
     * 
     * @param context App context
     * @param phoneNumber Recipient phone number
     * @param groceryItems List of grocery items
     * @param appName Name of the app
     */
    public static void shareViaSms(Context context, String phoneNumber, List<GroceryItem> groceryItems, String appName) {
        try {
            String message = formatGroceryListAsText(groceryItems, appName);
            
            Intent intent = new Intent(Intent.ACTION_SENDTO);
            intent.setData(Uri.parse("smsto:" + phoneNumber));
            intent.putExtra("sms_body", message);
            context.startActivity(intent);
            
            Toast.makeText(context, "Opening SMS app", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Log.e(TAG, "Error sharing via SMS: " + e.getMessage(), e);
            Toast.makeText(context, "Error opening SMS app", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Share grocery list via email
     * 
     * @param context App context
     * @param groceryItems List of grocery items
     * @param appName Name of the app
     */
    public static void shareViaEmail(Context context, List<GroceryItem> groceryItems, String appName) {
        try {
            String subject = "Grocery List";
            String message = formatGroceryListAsText(groceryItems, appName);
            
            Intent intent = new Intent(Intent.ACTION_SENDTO);
            intent.setData(Uri.parse("mailto:")); // only email apps should handle this
            intent.putExtra(Intent.EXTRA_SUBJECT, subject);
            intent.putExtra(Intent.EXTRA_TEXT, message);
            
            if (intent.resolveActivity(context.getPackageManager()) != null) {
                context.startActivity(intent);
                Toast.makeText(context, "Opening email app", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(context, "No email app found", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Log.e(TAG, "Error sharing via email: " + e.getMessage(), e);
            Toast.makeText(context, "Error opening email app", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Share grocery list via WhatsApp
     * 
     * @param context App context
     * @param groceryItems List of grocery items
     * @param appName Name of the app
     */
    public static void shareViaWhatsApp(Context context, List<GroceryItem> groceryItems, String appName) {
        try {
            String message = formatGroceryListAsText(groceryItems, appName);
            
            Intent whatsappIntent = new Intent(Intent.ACTION_SEND);
            whatsappIntent.setType("text/plain");
            whatsappIntent.setPackage("com.whatsapp");
            whatsappIntent.putExtra(Intent.EXTRA_TEXT, message);
            
            try {
                context.startActivity(whatsappIntent);
                Toast.makeText(context, "Opening WhatsApp", Toast.LENGTH_SHORT).show();
            } catch (android.content.ActivityNotFoundException ex) {
                Toast.makeText(context, "WhatsApp not installed", Toast.LENGTH_SHORT).show();
                // If WhatsApp is not installed, open general sharing
                shareViaOtherApps(context, groceryItems, appName);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error sharing via WhatsApp: " + e.getMessage(), e);
            Toast.makeText(context, "Error opening WhatsApp", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Copy grocery list to clipboard
     * 
     * @param context App context
     * @param groceryItems List of grocery items
     * @param appName Name of the app
     */
    public static void copyToClipboard(Context context, List<GroceryItem> groceryItems, String appName) {
        try {
            String groceryListText = formatGroceryListAsText(groceryItems, appName);
            
            ClipboardManager clipboard = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
            ClipData clip = ClipData.newPlainText("Grocery List", groceryListText);
            clipboard.setPrimaryClip(clip);
            
            Toast.makeText(context, "Grocery list copied to clipboard", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Log.e(TAG, "Error copying to clipboard: " + e.getMessage(), e);
            Toast.makeText(context, "Error copying to clipboard", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Share grocery list via any available sharing app
     * 
     * @param context App context
     * @param groceryItems List of grocery items
     * @param appName Name of the app
     */
    public static void shareViaOtherApps(Context context, List<GroceryItem> groceryItems, String appName) {
        try {
            String groceryListText = formatGroceryListAsText(groceryItems, appName);
            
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("text/plain");
            shareIntent.putExtra(Intent.EXTRA_SUBJECT, "Grocery List");
            shareIntent.putExtra(Intent.EXTRA_TEXT, groceryListText);
            
            context.startActivity(Intent.createChooser(shareIntent, "Share Grocery List via"));
        } catch (Exception e) {
            Log.e(TAG, "Error sharing via other apps: " + e.getMessage(), e);
            Toast.makeText(context, "Error sharing grocery list", Toast.LENGTH_SHORT).show();
        }
    }
} 