package com.esb.llm.ESBLlmDemo.service;

import com.esb.llm.ESBLlmDemo.mapper.OrderMapper;
import com.esb.llm.ESBLlmDemo.model.Order;
import com.esb.llm.ESBLlmDemo.model.TargetOrder;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class OrderMapperTest {

    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    public void testOrderMapping() throws Exception {
        // Create source order
        Order.OrderItem item1 = new Order.OrderItem("ITEM001", "Laptop", 1, 999.99);
        Order.OrderItem item2 = new Order.OrderItem("ITEM002", "Mouse", 2, 29.99);
        List<Order.OrderItem> items = Arrays.asList(item1, item2);
        
        Order.OrderShipping shipping = new Order.OrderShipping("123 Main St", "New York", "NY", "10001", "Express", 15.00);
        Order.OrderPayment payment = new Order.OrderPayment("Credit Card", "1234", "Paid", 1075.97);
        
        Order sourceOrder = new Order("ORD001", "CUST001", "John Doe", "john@example.com", 
                                    LocalDateTime.now(), "Confirmed", items, shipping, payment);

        // Map to target
        TargetOrder targetOrder = orderMapper.orderToTargetOrder(sourceOrder);

        // Verify mapping
        assertNotNull(targetOrder);
        assertEquals("ORD001", targetOrder.getId());
        assertEquals("John Doe", targetOrder.getCustomerInfo());
        assertEquals("john@example.com", targetOrder.getCustomerContact());
        assertEquals("Confirmed", targetOrder.getStatus());
        assertEquals(2, targetOrder.getItems().size());
        assertTrue(targetOrder.getDeliveryAddress().contains("123 Main St"));
        assertTrue(targetOrder.getPaymentDetails().contains("Credit Card"));
        assertTrue(targetOrder.getTotalAmount() > 0);

        // Convert to JSON for verification
        String sourceJson = objectMapper.writeValueAsString(sourceOrder);
        String targetJson = objectMapper.writeValueAsString(targetOrder);

        System.out.println("=== Order Mapping Test ===");
        System.out.println("Source JSON:");
        System.out.println(sourceJson);
        System.out.println("\nTarget JSON:");
        System.out.println(targetJson);
        System.out.println("=== Test Complete ===\n");
    }
} 