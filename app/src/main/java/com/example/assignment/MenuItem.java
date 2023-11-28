package com.example.assignment;

// MenuItem.java

public class MenuItem {
    public String name;
    public String imageUrl;
    public double price;

    public MenuItem() {
        // Default constructor required for calls to DataSnapshot.getValue(MenuItem.class)
    }

    public MenuItem(String name, String imageUrl, double price) {
        this.name = name;
        this.imageUrl = imageUrl;
        this.price = price;
    }
}

