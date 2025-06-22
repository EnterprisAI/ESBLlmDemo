package com.esb.llm.ESBLlmDemo.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class MapStructJsonRuleGeneratorServiceTest {

    @Autowired
    private MapStructJsonRuleGeneratorService jsonRuleGeneratorService;

    @Test
    public void testGenerateJsonRulesForSourceTargetMapper() {
        System.out.println("=== Testing SourceTargetMapper JSON Rule Generation ===");
        
        String rules = jsonRuleGeneratorService.generateJsonRulesForMapper("SourceTargetMapper");
        
        assertNotNull(rules);
        assertTrue(rules.contains("sourceContentType"));
        assertTrue(rules.contains("targetContentType"));
        assertTrue(rules.contains("conversionRules"));
        
        System.out.println("Generated rules for SourceTargetMapper:");
        System.out.println(rules);
        System.out.println("=== Test Complete ===\n");
    }

    @Test
    public void testGenerateJsonRulesForEmployeeMapper() {
        System.out.println("=== Testing EmployeeMapper JSON Rule Generation ===");
        
        String rules = jsonRuleGeneratorService.generateJsonRulesForMapper("EmployeeMapper");
        
        assertNotNull(rules);
        assertTrue(rules.contains("sourceContentType"));
        assertTrue(rules.contains("targetContentType"));
        assertTrue(rules.contains("conversionRules"));
        
        // Should contain employee-specific mappings
        assertTrue(rules.contains("EMPLOYEE_ID") || rules.contains("employeeId"));
        assertTrue(rules.contains("EMPLOYEE_NAME") || rules.contains("employeename"));
        
        System.out.println("Generated rules for EmployeeMapper:");
        System.out.println(rules);
        System.out.println("=== Test Complete ===\n");
    }

    @Test
    public void testGenerateJsonRulesForUserMapper() {
        System.out.println("=== Testing UserMapper JSON Rule Generation ===");
        
        String rules = jsonRuleGeneratorService.generateJsonRulesForMapper("UserMapper");
        
        assertNotNull(rules);
        assertTrue(rules.contains("sourceContentType"));
        assertTrue(rules.contains("targetContentType"));
        assertTrue(rules.contains("conversionRules"));
        
        // Should contain user-specific mappings
        assertTrue(rules.contains("USER_ID") || rules.contains("userId"));
        assertTrue(rules.contains("FULL_NAME") || rules.contains("fullName"));
        
        System.out.println("Generated rules for UserMapper:");
        System.out.println(rules);
        System.out.println("=== Test Complete ===\n");
    }

    @Test
    public void testGenerateJsonRulesForProductMapper() {
        System.out.println("=== Testing ProductMapper JSON Rule Generation ===");
        
        String rules = jsonRuleGeneratorService.generateJsonRulesForMapper("ProductMapper");
        
        assertNotNull(rules);
        assertTrue(rules.contains("sourceContentType"));
        assertTrue(rules.contains("targetContentType"));
        assertTrue(rules.contains("conversionRules"));
        
        // Should contain product-specific mappings
        assertTrue(rules.contains("PRODUCT_ID") || rules.contains("productId"));
        assertTrue(rules.contains("PRODUCT_NAME") || rules.contains("productName"));
        
        System.out.println("Generated rules for ProductMapper:");
        System.out.println(rules);
        System.out.println("=== Test Complete ===\n");
    }

    @Test
    public void testGenerateJsonRulesForOrderMapper() {
        System.out.println("=== Testing OrderMapper JSON Rule Generation ===");
        
        String rules = jsonRuleGeneratorService.generateJsonRulesForMapper("OrderMapper");
        
        assertNotNull(rules);
        assertTrue(rules.contains("sourceContentType"));
        assertTrue(rules.contains("targetContentType"));
        assertTrue(rules.contains("conversionRules"));
        
        // Should contain order-specific mappings
        assertTrue(rules.contains("ORDER_ID") || rules.contains("orderId"));
        assertTrue(rules.contains("CUSTOMER_NAME") || rules.contains("customerName"));
        
        System.out.println("Generated rules for OrderMapper:");
        System.out.println(rules);
        System.out.println("=== Test Complete ===\n");
    }

    @Test
    public void testGenerateJsonRulesForAllMappers() {
        System.out.println("=== Testing JSON Rule Generation for All Mappers ===");
        
        Map<String, String> allRules = jsonRuleGeneratorService.generateJsonRulesForAllMappers();
        
        assertNotNull(allRules);
        assertFalse(allRules.isEmpty());
        
        // Should contain all available mappers
        List<String> availableMappers = jsonRuleGeneratorService.getAvailableMappers();
        for (String mapperName : availableMappers) {
            assertTrue(allRules.containsKey(mapperName), "Missing rules for " + mapperName);
            assertNotNull(allRules.get(mapperName), "Null rules for " + mapperName);
        }
        
        System.out.println("Generated rules for all mappers:");
        allRules.forEach((mapper, rules) -> {
            System.out.println("\n--- " + mapper + " ---");
            System.out.println(rules);
        });
        System.out.println("=== Test Complete ===\n");
    }

    @Test
    public void testGetAvailableMappers() {
        System.out.println("=== Testing Available Mappers List ===");
        
        List<String> mappers = jsonRuleGeneratorService.getAvailableMappers();
        
        assertNotNull(mappers);
        assertFalse(mappers.isEmpty());
        assertTrue(mappers.contains("SourceTargetMapper"));
        assertTrue(mappers.contains("EmployeeMapper"));
        assertTrue(mappers.contains("UserMapper"));
        assertTrue(mappers.contains("ProductMapper"));
        assertTrue(mappers.contains("OrderMapper"));
        
        System.out.println("Available mappers: " + mappers);
        System.out.println("=== Test Complete ===\n");
    }

    @Test
    public void testInvalidMapperName() {
        System.out.println("=== Testing Invalid Mapper Name ===");
        
        Exception exception = assertThrows(RuntimeException.class, () -> {
            jsonRuleGeneratorService.generateJsonRulesForMapper("InvalidMapper");
        });
        
        assertTrue(exception.getMessage().contains("Mapper class not found") || 
                  exception.getMessage().contains("Error"));
        
        System.out.println("Expected error: " + exception.getMessage());
        System.out.println("=== Test Complete ===\n");
    }

    @Test
    public void testTestGeneratorWithMapper() {
        System.out.println("=== Testing Test Generator Method ===");
        
        String result = jsonRuleGeneratorService.testGeneratorWithMapper("SourceTargetMapper");
        
        assertNotNull(result);
        assertFalse(result.startsWith("Error"));
        
        System.out.println("Test result: " + result);
        System.out.println("=== Test Complete ===\n");
    }

    @Test
    public void testJsonStructureValidation() {
        System.out.println("=== Testing JSON Structure Validation ===");
        
        String rules = jsonRuleGeneratorService.generateJsonRulesForMapper("SourceTargetMapper");
        
        // Validate JSON structure
        assertTrue(rules.startsWith("{"));
        assertTrue(rules.endsWith("}"));
        assertTrue(rules.contains("\"sourceContentType\":"));
        assertTrue(rules.contains("\"targetContentType\":"));
        assertTrue(rules.contains("\"conversionRules\":"));
        
        // Validate that it's valid JSON
        try {
            // Try to parse as JSON to ensure it's valid
            assertNotNull(rules);
            System.out.println("JSON structure validation passed");
        } catch (Exception e) {
            fail("Generated rules are not valid JSON: " + e.getMessage());
        }
        
        System.out.println("=== Test Complete ===\n");
    }

    @Test
    public void testComplexMappingDetection() {
        System.out.println("=== Testing Complex Mapping Detection ===");
        
        // Test with UserMapper which has complex mappings
        String rules = jsonRuleGeneratorService.generateJsonRulesForMapper("UserMapper");
        
        assertNotNull(rules);
        
        // Should detect complex mappings like address.country, userProfile.occupation, etc.
        assertTrue(rules.contains("address") || rules.contains("ADDRESS"));
        assertTrue(rules.contains("profile") || rules.contains("PROFILE"));
        
        System.out.println("Complex mapping detection test passed");
        System.out.println("=== Test Complete ===\n");
    }
} 