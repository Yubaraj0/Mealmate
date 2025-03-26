package com.example.mealmateyubraj;

public class Meal {
    private String id;
    private String name;
    private long timestamp;
    private String userId;

    // Required empty constructor for Firestore
    public Meal() {}

    public Meal(String name, long timestamp, String userId) {
        this.name = name;
        this.timestamp = timestamp;
        this.userId = userId;
    }

    // Getters and setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }
} 