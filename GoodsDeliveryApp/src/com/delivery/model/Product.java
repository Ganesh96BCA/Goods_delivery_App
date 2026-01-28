package com.delivery.model;

public class Product {

    private int id;
    private String name;
    private String category;
    private String imagePath;
    private String unit;

    public Product(int id, String name, String category,
                   String imagePath, String unit) {
        this.id = id;
        this.name = name;
        this.category = category;
        this.imagePath = imagePath;
        this.unit = unit;
    }

    // ✅ ADD THIS
    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getCategory() {
        return category;
    }

    public String getImagePath() {
        return imagePath;
    }

    public String getUnit() {
        return unit;
    }
}
