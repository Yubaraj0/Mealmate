package com.example.mealmateyubraj;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.google.android.material.card.MaterialCardView;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class HomeFragment extends Fragment {
    private TextView tvWelcome;
    private MaterialCardView cardAddMeal;
    private MaterialCardView cardGroceryList;
    private MaterialCardView cardManageItems;
    private MaterialButton buttonLogout;
    private FirebaseFirestore db;
    private FirebaseAuth auth;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.home_fragment, container, false);  // Updated with the new layout
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialize Firebase
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        // Initialize views
        tvWelcome = view.findViewById(R.id.welcomeText);
        cardAddMeal = view.findViewById(R.id.cardAddMeal);
        cardGroceryList = view.findViewById(R.id.cardGroceryList);
        cardManageItems = view.findViewById(R.id.cardManageItems);
        buttonLogout = view.findViewById(R.id.buttonLogout);

        // Set welcome message
        String userEmail = auth.getCurrentUser() != null ? auth.getCurrentUser().getEmail() : "User";
        tvWelcome.setText("Welcome, " + userEmail);

        // Set click listeners
        cardAddMeal.setOnClickListener(v ->
                Navigation.findNavController(requireView()).navigate(R.id.action_homeFragment_to_addMealFragment));

        cardGroceryList.setOnClickListener(v ->
                Navigation.findNavController(requireView()).navigate(R.id.action_homeFragment_to_groceryListFragment));

//        cardManageItems.setOnClickListener(v ->
//                Navigation.findNavController(requireView()).navigate(R.id.action_homeFragment_to_manageItemsFragment));
//
//        buttonLogout.setOnClickListener(v -> {
//            auth.signOut();
//            Toast.makeText(requireContext(), "Logged out successfully", Toast.LENGTH_SHORT).show();
//            Navigation.findNavController(requireView()).navigate(R.id.action_homeFragment_to_loginFragment);
//        });
    }
}
