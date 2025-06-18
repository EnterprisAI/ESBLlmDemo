package com.esb.llm.ESBLlmDemo.model;

import java.time.LocalDateTime;
import java.util.List;

public class TargetOrder {
    private String id;
    private String customerInfo;
    private String customerContact;
    private LocalDateTime purchaseDate;
    private String status;
    private List<TargetOrderItem> items;
    private String deliveryAddress;
    private String paymentDetails;
    private double totalAmount;

    // Constructors
    public TargetOrder() {}

    public TargetOrder(String id, String customerInfo, String customerContact, LocalDateTime purchaseDate,
                      String status, List<TargetOrderItem> items, String deliveryAddress,
                      String paymentDetails, double totalAmount) {
        this.id = id;
        this.customerInfo = customerInfo;
        this.customerContact = customerContact;
        this.purchaseDate = purchaseDate;
        this.status = status;
        this.items = items;
        this.deliveryAddress = deliveryAddress;
        this.paymentDetails = paymentDetails;
        this.totalAmount = totalAmount;
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getCustomerInfo() {
        return customerInfo;
    }

    public void setCustomerInfo(String customerInfo) {
        this.customerInfo = customerInfo;
    }

    public String getCustomerContact() {
        return customerContact;
    }

    public void setCustomerContact(String customerContact) {
        this.customerContact = customerContact;
    }

    public LocalDateTime getPurchaseDate() {
        return purchaseDate;
    }

    public void setPurchaseDate(LocalDateTime purchaseDate) {
        this.purchaseDate = purchaseDate;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public List<TargetOrderItem> getItems() {
        return items;
    }

    public void setItems(List<TargetOrderItem> items) {
        this.items = items;
    }

    public String getDeliveryAddress() {
        return deliveryAddress;
    }

    public void setDeliveryAddress(String deliveryAddress) {
        this.deliveryAddress = deliveryAddress;
    }

    public String getPaymentDetails() {
        return paymentDetails;
    }

    public void setPaymentDetails(String paymentDetails) {
        this.paymentDetails = paymentDetails;
    }

    public double getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(double totalAmount) {
        this.totalAmount = totalAmount;
    }

    // Inner class
    public static class TargetOrderItem {
        private String productId;
        private String productName;
        private int qty;
        private double price;
        private double subtotal;

        public TargetOrderItem() {}

        public TargetOrderItem(String productId, String productName, int qty, double price) {
            this.productId = productId;
            this.productName = productName;
            this.qty = qty;
            this.price = price;
            this.subtotal = qty * price;
        }

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

        public int getQty() {
            return qty;
        }

        public void setQty(int qty) {
            this.qty = qty;
        }

        public double getPrice() {
            return price;
        }

        public void setPrice(double price) {
            this.price = price;
        }

        public double getSubtotal() {
            return subtotal;
        }

        public void setSubtotal(double subtotal) {
            this.subtotal = subtotal;
        }
    }
} 