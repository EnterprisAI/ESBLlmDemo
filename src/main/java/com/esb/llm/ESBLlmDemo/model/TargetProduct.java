package com.esb.llm.ESBLlmDemo.model;

import java.util.List;

public class TargetProduct {
    private String id;
    private String name;
    private double price;
    private String category;
    private List<TargetProductSpec> specs;
    private int availableStock;
    private String location;
    private boolean available;

    // Constructors
    public TargetProduct() {}

    public TargetProduct(String id, String name, double price, String category, 
                        List<TargetProductSpec> specs, int availableStock, 
                        String location, boolean available) {
        this.id = id;
        this.name = name;
        this.price = price;
        this.category = category;
        this.specs = specs;
        this.availableStock = availableStock;
        this.location = location;
        this.available = available;
    }

    // Getters and Setters
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

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public List<TargetProductSpec> getSpecs() {
        return specs;
    }

    public void setSpecs(List<TargetProductSpec> specs) {
        this.specs = specs;
    }

    public int getAvailableStock() {
        return availableStock;
    }

    public void setAvailableStock(int availableStock) {
        this.availableStock = availableStock;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public boolean isAvailable() {
        return available;
    }

    public void setAvailable(boolean available) {
        this.available = available;
    }

    // Inner class
    public static class TargetProductSpec {
        private String key;
        private String value;
        private String unit;

        public TargetProductSpec() {}

        public TargetProductSpec(String key, String value, String unit) {
            this.key = key;
            this.value = value;
            this.unit = unit;
        }

        public String getKey() {
            return key;
        }

        public void setKey(String key) {
            this.key = key;
        }

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }

        public String getUnit() {
            return unit;
        }

        public void setUnit(String unit) {
            this.unit = unit;
        }
    }
} 