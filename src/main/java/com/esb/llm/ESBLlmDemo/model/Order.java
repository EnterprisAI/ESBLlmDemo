package com.esb.llm.ESBLlmDemo.model;

import java.time.LocalDateTime;
import java.util.List;

public class Order {
    private String orderId;
    private String customerId;
    private String customerName;
    private String customerEmail;
    private LocalDateTime orderDate;
    private String orderStatus;
    private List<OrderItem> orderItems;
    private OrderShipping shippingDetails;
    private OrderPayment paymentInfo;

    // Constructors
    public Order() {}

    public Order(String orderId, String customerId, String customerName, String customerEmail,
                 LocalDateTime orderDate, String orderStatus, List<OrderItem> orderItems,
                 OrderShipping shippingDetails, OrderPayment paymentInfo) {
        this.orderId = orderId;
        this.customerId = customerId;
        this.customerName = customerName;
        this.customerEmail = customerEmail;
        this.orderDate = orderDate;
        this.orderStatus = orderStatus;
        this.orderItems = orderItems;
        this.shippingDetails = shippingDetails;
        this.paymentInfo = paymentInfo;
    }

    // Getters and Setters
    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public String getCustomerId() {
        return customerId;
    }

    public void setCustomerId(String customerId) {
        this.customerId = customerId;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public String getCustomerEmail() {
        return customerEmail;
    }

    public void setCustomerEmail(String customerEmail) {
        this.customerEmail = customerEmail;
    }

    public LocalDateTime getOrderDate() {
        return orderDate;
    }

    public void setOrderDate(LocalDateTime orderDate) {
        this.orderDate = orderDate;
    }

    public String getOrderStatus() {
        return orderStatus;
    }

    public void setOrderStatus(String orderStatus) {
        this.orderStatus = orderStatus;
    }

    public List<OrderItem> getOrderItems() {
        return orderItems;
    }

    public void setOrderItems(List<OrderItem> orderItems) {
        this.orderItems = orderItems;
    }

    public OrderShipping getShippingDetails() {
        return shippingDetails;
    }

    public void setShippingDetails(OrderShipping shippingDetails) {
        this.shippingDetails = shippingDetails;
    }

    public OrderPayment getPaymentInfo() {
        return paymentInfo;
    }

    public void setPaymentInfo(OrderPayment paymentInfo) {
        this.paymentInfo = paymentInfo;
    }

    // Inner classes
    public static class OrderItem {
        private String itemId;
        private String productName;
        private int quantity;
        private double unitPrice;
        private double totalPrice;

        public OrderItem() {}

        public OrderItem(String itemId, String productName, int quantity, double unitPrice) {
            this.itemId = itemId;
            this.productName = productName;
            this.quantity = quantity;
            this.unitPrice = unitPrice;
            this.totalPrice = quantity * unitPrice;
        }

        public String getItemId() {
            return itemId;
        }

        public void setItemId(String itemId) {
            this.itemId = itemId;
        }

        public String getProductName() {
            return productName;
        }

        public void setProductName(String productName) {
            this.productName = productName;
        }

        public int getQuantity() {
            return quantity;
        }

        public void setQuantity(int quantity) {
            this.quantity = quantity;
        }

        public double getUnitPrice() {
            return unitPrice;
        }

        public void setUnitPrice(double unitPrice) {
            this.unitPrice = unitPrice;
        }

        public double getTotalPrice() {
            return totalPrice;
        }

        public void setTotalPrice(double totalPrice) {
            this.totalPrice = totalPrice;
        }
    }

    public static class OrderShipping {
        private String shippingAddress;
        private String shippingCity;
        private String shippingState;
        private String shippingZipCode;
        private String shippingMethod;
        private double shippingCost;

        public OrderShipping() {}

        public OrderShipping(String shippingAddress, String shippingCity, String shippingState,
                           String shippingZipCode, String shippingMethod, double shippingCost) {
            this.shippingAddress = shippingAddress;
            this.shippingCity = shippingCity;
            this.shippingState = shippingState;
            this.shippingZipCode = shippingZipCode;
            this.shippingMethod = shippingMethod;
            this.shippingCost = shippingCost;
        }

        public String getShippingAddress() {
            return shippingAddress;
        }

        public void setShippingAddress(String shippingAddress) {
            this.shippingAddress = shippingAddress;
        }

        public String getShippingCity() {
            return shippingCity;
        }

        public void setShippingCity(String shippingCity) {
            this.shippingCity = shippingCity;
        }

        public String getShippingState() {
            return shippingState;
        }

        public void setShippingState(String shippingState) {
            this.shippingState = shippingState;
        }

        public String getShippingZipCode() {
            return shippingZipCode;
        }

        public void setShippingZipCode(String shippingZipCode) {
            this.shippingZipCode = shippingZipCode;
        }

        public String getShippingMethod() {
            return shippingMethod;
        }

        public void setShippingMethod(String shippingMethod) {
            this.shippingMethod = shippingMethod;
        }

        public double getShippingCost() {
            return shippingCost;
        }

        public void setShippingCost(double shippingCost) {
            this.shippingCost = shippingCost;
        }
    }

    public static class OrderPayment {
        private String paymentMethod;
        private String cardLastFour;
        private String paymentStatus;
        private double amountPaid;

        public OrderPayment() {}

        public OrderPayment(String paymentMethod, String cardLastFour, String paymentStatus, double amountPaid) {
            this.paymentMethod = paymentMethod;
            this.cardLastFour = cardLastFour;
            this.paymentStatus = paymentStatus;
            this.amountPaid = amountPaid;
        }

        public String getPaymentMethod() {
            return paymentMethod;
        }

        public void setPaymentMethod(String paymentMethod) {
            this.paymentMethod = paymentMethod;
        }

        public String getCardLastFour() {
            return cardLastFour;
        }

        public void setCardLastFour(String cardLastFour) {
            this.cardLastFour = cardLastFour;
        }

        public String getPaymentStatus() {
            return paymentStatus;
        }

        public void setPaymentStatus(String paymentStatus) {
            this.paymentStatus = paymentStatus;
        }

        public double getAmountPaid() {
            return amountPaid;
        }

        public void setAmountPaid(double amountPaid) {
            this.amountPaid = amountPaid;
        }
    }
} 