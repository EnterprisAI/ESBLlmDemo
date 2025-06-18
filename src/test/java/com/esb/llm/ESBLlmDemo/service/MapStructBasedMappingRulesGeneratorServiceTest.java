package com.esb.llm.ESBLlmDemo.service;

import com.esb.llm.ESBLlmDemo.config.MappingRules;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class MapStructBasedMappingRulesGeneratorServiceTest {

    @Autowired
    private MapStructBasedMappingRulesGeneratorService mapStructBasedMappingRulesGeneratorService;

    @Test
    public void testGenerateProductMapperRules() {
        System.out.println("=== Testing ProductMapper Rules Generation ===");
        
        MappingRules rules = mapStructBasedMappingRulesGeneratorService.generateMappingRulesFromMapper("ProductMapper");
        
        assertNotNull(rules);
        assertEquals("JSON", rules.getSourceContentType());
        assertEquals("JSON", rules.getTargetContentType());
        assertNotNull(rules.getConversionRules());
        assertEquals(1, rules.getConversionRules().size());
        
        MappingRules.ConversionRule mainRule = rules.getConversionRules().get(0);
        assertEquals("ProductMapping", mainRule.getPropID());
        assertFalse(mainRule.isArray());
        assertNotNull(mainRule.getItems());
        assertEquals(8, mainRule.getItems().size()); // 8 mapping rules
        
        // Verify specific mappings
        boolean foundIdMapping = false;
        boolean foundExpressionMapping = false;
        
        for (MappingRules.ConversionRule item : mainRule.getItems()) {
            if (item.getPropID().equals("PRODUCT_ID")) {
                assertEquals("productId", item.getSourceLocation());
                assertEquals("id", item.getTargetLocation());
                foundIdMapping = true;
            }
            if (item.getPropID().equals("PRODUCT_AVAILABLE")) {
                assertTrue(item.getSourceLocation().startsWith("EXPRESSION:"));
                assertEquals("available", item.getTargetLocation());
                foundExpressionMapping = true;
            }
        }
        
        assertTrue(foundIdMapping, "Product ID mapping not found");
        assertTrue(foundExpressionMapping, "Product available expression mapping not found");
        
        String jsonRules = mapStructBasedMappingRulesGeneratorService.generateMappingRulesAsJson("ProductMapper");
        System.out.println("ProductMapper Rules:");
        System.out.println(jsonRules);
        System.out.println("=== Test Complete ===\n");
    }

    @Test
    public void testGenerateOrderMapperRules() {
        System.out.println("=== Testing OrderMapper Rules Generation ===");
        
        MappingRules rules = mapStructBasedMappingRulesGeneratorService.generateMappingRulesFromMapper("OrderMapper");
        
        assertNotNull(rules);
        assertEquals("JSON", rules.getSourceContentType());
        assertEquals("JSON", rules.getTargetContentType());
        assertNotNull(rules.getConversionRules());
        assertEquals(1, rules.getConversionRules().size());
        
        MappingRules.ConversionRule mainRule = rules.getConversionRules().get(0);
        assertEquals("OrderMapping", mainRule.getPropID());
        assertFalse(mainRule.isArray());
        assertNotNull(mainRule.getItems());
        assertEquals(9, mainRule.getItems().size()); // 9 mapping rules
        
        // Verify expression mappings
        boolean foundDeliveryAddressExpression = false;
        boolean foundTotalAmountExpression = false;
        
        for (MappingRules.ConversionRule item : mainRule.getItems()) {
            if (item.getPropID().equals("DELIVERY_ADDRESS")) {
                assertTrue(item.getSourceLocation().startsWith("EXPRESSION:"));
                assertEquals("deliveryAddress", item.getTargetLocation());
                foundDeliveryAddressExpression = true;
            }
            if (item.getPropID().equals("TOTAL_AMOUNT")) {
                assertTrue(item.getSourceLocation().startsWith("EXPRESSION:"));
                assertEquals("totalAmount", item.getTargetLocation());
                foundTotalAmountExpression = true;
            }
        }
        
        assertTrue(foundDeliveryAddressExpression, "Delivery address expression mapping not found");
        assertTrue(foundTotalAmountExpression, "Total amount expression mapping not found");
        
        String jsonRules = mapStructBasedMappingRulesGeneratorService.generateMappingRulesAsJson("OrderMapper");
        System.out.println("OrderMapper Rules:");
        System.out.println(jsonRules);
        System.out.println("=== Test Complete ===\n");
    }

    @Test
    public void testGenerateUserMapperRules() {
        System.out.println("=== Testing UserMapper Rules Generation ===");
        
        MappingRules rules = mapStructBasedMappingRulesGeneratorService.generateMappingRulesFromMapper("UserMapper");
        
        assertNotNull(rules);
        assertEquals("JSON", rules.getSourceContentType());
        assertEquals("JSON", rules.getTargetContentType());
        assertNotNull(rules.getConversionRules());
        assertEquals(1, rules.getConversionRules().size());
        
        MappingRules.ConversionRule mainRule = rules.getConversionRules().get(0);
        assertEquals("UserMapping", mainRule.getPropID());
        assertFalse(mainRule.isArray());
        assertNotNull(mainRule.getItems());
        assertEquals(9, mainRule.getItems().size()); // 9 mapping rules
        
        // Verify complex expression mappings
        boolean foundFullNameExpression = false;
        boolean foundAgeExpression = false;
        
        for (MappingRules.ConversionRule item : mainRule.getItems()) {
            if (item.getPropID().equals("FULL_NAME")) {
                assertTrue(item.getSourceLocation().startsWith("EXPRESSION:"));
                assertEquals("fullName", item.getTargetLocation());
                foundFullNameExpression = true;
            }
            if (item.getPropID().equals("AGE")) {
                assertTrue(item.getSourceLocation().startsWith("EXPRESSION:"));
                assertEquals("age", item.getTargetLocation());
                foundAgeExpression = true;
            }
        }
        
        assertTrue(foundFullNameExpression, "Full name expression mapping not found");
        assertTrue(foundAgeExpression, "Age expression mapping not found");
        
        String jsonRules = mapStructBasedMappingRulesGeneratorService.generateMappingRulesAsJson("UserMapper");
        System.out.println("UserMapper Rules:");
        System.out.println(jsonRules);
        System.out.println("=== Test Complete ===\n");
    }

    @Test
    public void testGetAvailableMappers() {
        System.out.println("=== Testing Available Mappers ===");
        
        var availableMappers = mapStructBasedMappingRulesGeneratorService.getAvailableMappers();
        
        assertNotNull(availableMappers);
        assertEquals(4, availableMappers.size());
        assertTrue(availableMappers.contains("ProductMapper"));
        assertTrue(availableMappers.contains("OrderMapper"));
        assertTrue(availableMappers.contains("UserMapper"));
        assertTrue(availableMappers.contains("EmployeeMapper"));
        
        System.out.println("Available Mappers: " + availableMappers);
        System.out.println("=== Test Complete ===\n");
    }

    @Test
    public void testInvalidMapperName() {
        System.out.println("=== Testing Invalid Mapper Name ===");
        
        Exception exception = assertThrows(RuntimeException.class, () -> {
            mapStructBasedMappingRulesGeneratorService.generateMappingRulesFromMapper("InvalidMapper");
        });
        
        assertTrue(exception.getMessage().contains("Unknown mapper: InvalidMapper"));
        System.out.println("Expected error: " + exception.getMessage());
        System.out.println("=== Test Complete ===\n");
    }
} 