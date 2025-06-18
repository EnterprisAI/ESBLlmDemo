package com.esb.llm.ESBLlmDemo.model;

import java.util.List;

public class Product {
    private String productId;
    private String productName;
    private double productPrice;
    private ProductCategory productCategory;
    private List<ProductSpecification> productSpecifications;
    private ProductInventory productInventory;

    // Constructors
    public Product() {}

    public Product(String productId, String productName, double productPrice, 
                   ProductCategory productCategory, List<ProductSpecification> productSpecifications, 
                   ProductInventory productInventory) {
        this.productId = productId;
        this.productName = productName;
        this.productPrice = productPrice;
        this.productCategory = productCategory;
        this.productSpecifications = productSpecifications;
        this.productInventory = productInventory;
    }

    // Getters and Setters
    public String getProductId() {
        return productId;
    }

    public void setProductId(String productId) {
        this.productId = productId;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public double getProductPrice() {
        return productPrice;
    }

    public void setProductPrice(double productPrice) {
        this.productPrice = productPrice;
    }

    public ProductCategory getProductCategory() {
        return productCategory;
    }

    public void setProductCategory(ProductCategory productCategory) {
        this.productCategory = productCategory;
    }

    public List<ProductSpecification> getProductSpecifications() {
        return productSpecifications;
    }

    public void setProductSpecifications(List<ProductSpecification> productSpecifications) {
        this.productSpecifications = productSpecifications;
    }

    public ProductInventory getProductInventory() {
        return productInventory;
    }

    public void setProductInventory(ProductInventory productInventory) {
        this.productInventory = productInventory;
    }

    // Inner classes
    public static class ProductCategory {
        private String categoryId;
        private String categoryName;
        private String categoryDescription;

        public ProductCategory() {}

        public ProductCategory(String categoryId, String categoryName, String categoryDescription) {
            this.categoryId = categoryId;
            this.categoryName = categoryName;
            this.categoryDescription = categoryDescription;
        }

        public String getCategoryId() {
            return categoryId;
        }

        public void setCategoryId(String categoryId) {
            this.categoryId = categoryId;
        }

        public String getCategoryName() {
            return categoryName;
        }

        public void setCategoryName(String categoryName) {
            this.categoryName = categoryName;
        }

        public String getCategoryDescription() {
            return categoryDescription;
        }

        public void setCategoryDescription(String categoryDescription) {
            this.categoryDescription = categoryDescription;
        }
    }

    public static class ProductSpecification {
        private String specKey;
        private String specValue;
        private String specUnit;

        public ProductSpecification() {}

        public ProductSpecification(String specKey, String specValue, String specUnit) {
            this.specKey = specKey;
            this.specValue = specValue;
            this.specUnit = specUnit;
        }

        public String getSpecKey() {
            return specKey;
        }

        public void setSpecKey(String specKey) {
            this.specKey = specKey;
        }

        public String getSpecValue() {
            return specValue;
        }

        public void setSpecValue(String specValue) {
            this.specValue = specValue;
        }

        public String getSpecUnit() {
            return specUnit;
        }

        public void setSpecUnit(String specUnit) {
            this.specUnit = specUnit;
        }
    }

    public static class ProductInventory {
        private int stockLevel;
        private String warehouseLocation;
        private String supplierInfo;

        public ProductInventory() {}

        public ProductInventory(int stockLevel, String warehouseLocation, String supplierInfo) {
            this.stockLevel = stockLevel;
            this.warehouseLocation = warehouseLocation;
            this.supplierInfo = supplierInfo;
        }

        public int getStockLevel() {
            return stockLevel;
        }

        public void setStockLevel(int stockLevel) {
            this.stockLevel = stockLevel;
        }

        public String getWarehouseLocation() {
            return warehouseLocation;
        }

        public void setWarehouseLocation(String warehouseLocation) {
            this.warehouseLocation = warehouseLocation;
        }

        public String getSupplierInfo() {
            return supplierInfo;
        }

        public void setSupplierInfo(String supplierInfo) {
            this.supplierInfo = supplierInfo;
        }
    }
} 