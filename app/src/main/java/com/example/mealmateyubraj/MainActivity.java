package com.example.mealmateyubraj;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.example.mealmateyubraj.auth.LoginActivity;
import com.example.mealmateyubraj.database.DatabaseHelper;
import com.example.mealmateyubraj.database.MealDao;
import com.example.mealmateyubraj.database.UserDao;
import com.example.mealmateyubraj.models.User;
import com.example.mealmateyubraj.utils.SessionManager;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    
    private DatabaseHelper dbHelper;
    private MealDao mealDao;
    private SessionManager sessionManager;
    private MaterialToolbar toolbar;
    private BottomNavigationView bottomNav;
    private UserDao userDao;
    private NavController navController;
    private FirebaseAuth auth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        try {
            setContentView(R.layout.activity_main);
            
            // Initialize views
            toolbar = findViewById(R.id.topAppBar);
            bottomNav = findViewById(R.id.bottom_navigation);
            setSupportActionBar(toolbar);
            
            // Initialize database
            dbHelper = new DatabaseHelper(this);
            mealDao = new MealDao(this);
            userDao = new UserDao(this);
            
            // Initialize session manager
            sessionManager = new SessionManager(this);
            
            // Initialize Firebase
            auth = FirebaseAuth.getInstance();
            db = FirebaseFirestore.getInstance();
            
            // Check authentication
            FirebaseUser currentUser = auth.getCurrentUser();
            if (currentUser == null) {
                startActivity(new Intent(this, LoginActivity.class));
                finish();
                return;
            }
            
            // Initialize Navigation
            navController = Navigation.findNavController(this, R.id.nav_host_fragment);
            
            // Setup Bottom Navigation
            NavigationUI.setupWithNavController(bottomNav, navController);

            // Setup Action Bar with Navigation
            AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.navigation_home,
                R.id.navigation_meals,
                R.id.navigation_groceries,
                R.id.navigation_profile
            ).build();
            NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
            
            loadUserData(currentUser.getUid());
            
        } catch (Exception e) {
            Log.e(TAG, "Error initializing MainActivity: " + e.getMessage());
            Toast.makeText(this, "An error occurred while starting the main screen. Returning to login.", Toast.LENGTH_LONG).show();
            navigateToLogin();
        }
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        try {
            getMenuInflater().inflate(R.menu.main_menu, menu);
            return true;
        } catch (Exception e) {
            Log.e(TAG, "Error creating options menu: " + e.getMessage());
            return false;
        }
    }
    
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int itemId = item.getItemId();
        
        if (itemId == R.id.action_logout) {
            logout();
            return true;
        }
        
        return super.onOptionsItemSelected(item);
    }
    
    private void logout() {
        try {
            // Clear user session
            sessionManager.clearSession();
            
            // Clear remember me in database
            userDao.open();
            User user = userDao.getUserByEmail(sessionManager.getUserEmail());
            if (user != null) {
                userDao.updateRememberMe(user.getId(), false);
            }
            userDao.close();
            
            // Sign out from Firebase
            auth.signOut();
            
            // Navigate to login screen
            navigateToLogin();
        } catch (Exception e) {
            Log.e(TAG, "Error during logout: " + e.getMessage());
            navigateToLogin(); // Navigate to login even if there's an error
        }
    }
    
    private void navigateToLogin() {
        Intent intent = new Intent(MainActivity.this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        try {
            mealDao.open();
        } catch (Exception e) {
            Log.e(TAG, "Error opening database in onResume: " + e.getMessage());
        }
    }
    
    @Override
    protected void onPause() {
        super.onPause();
        try {
            mealDao.close();
        } catch (Exception e) {
            Log.e(TAG, "Error closing database in onPause: " + e.getMessage());
        }
    }

    private void loadUserData(String userId) {
        db.collection("users").document(userId).get()
            .addOnSuccessListener(documentSnapshot -> {
                if (documentSnapshot.exists()) {
                    User user = documentSnapshot.toObject(User.class);
                    if (user != null) {
                        userDao.open();
                        // Check if user already exists
                        User existingUser = userDao.getUserByEmail(user.getEmail());
                        if (existingUser != null) {
                            // Update existing user's fields
                            existingUser.setUsername(user.getUsername());
                            existingUser.setEmail(user.getEmail());
                            existingUser.setPassword(user.getPassword());
                            existingUser.setRememberMe(user.isRememberMe());
                            userDao.updateUser(existingUser);
                        } else {
                            // Insert new user
                            userDao.insertUser(user);
                        }
                        userDao.close();
                        updateNavigationHeader(user);
                    }
                }
            })
            .addOnFailureListener(e -> Log.e(TAG, "Error loading user data", e));
    }
    
    private void updateNavigationHeader(User user) {
        try {
            Log.d(TAG, "User data loaded: " + user.getUsername() + ", " + user.getEmail());
            
            // Update toolbar title with username
            if (toolbar != null && user.getUsername() != null) {
                toolbar.setTitle("Welcome, " + user.getUsername());
            }
        } catch (Exception e) {
            Log.e(TAG, "Error updating navigation header: " + e.getMessage());
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        return navController.navigateUp() || super.onSupportNavigateUp();
    }
}