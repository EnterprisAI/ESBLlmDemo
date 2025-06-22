package com.esb.llm.ESBLlmDemo.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class EnhancedMapStructJsonRuleGeneratorServiceTest {

    @Autowired
    private EnhancedMapStructJsonRuleGeneratorService enhancedJsonRuleGeneratorService;

    @Test
    public void testGenerateJsonRulesForEmployeeMapper() {
        System.out.println("=== Testing Enhanced EmployeeMapper JSON Rule Generation ===");
        
        String rules = enhancedJsonRuleGeneratorService.generateJsonRulesForMapper("EmployeeMapper");
        
        assertNotNull(rules);
        assertTrue(rules.contains("sourceContentType"));
        assertTrue(rules.contains("targetContentType"));
        assertTrue(rules.contains("conversionRules"));
        
        System.out.println("Generated rules for EmployeeMapper:");
        System.out.println(rules);
        
        // Print what we're looking for vs what we found
        System.out.println("Looking for: EMPLOYEE_ID or employeeId");
        System.out.println("Contains EMPLOYEE_ID: " + rules.contains("EMPLOYEE_ID"));
        System.out.println("Contains employeeId: " + rules.contains("employeeId"));
        System.out.println("Contains employeename: " + rules.contains("employeename"));
        System.out.println("Contains emplocation: " + rules.contains("emplocation"));
        System.out.println("Contains officelocation: " + rules.contains("officelocation"));
        
        // Should contain employee-specific mappings from @Mappings annotation
        assertTrue(rules.contains("EMPLOYEE_ID") || rules.contains("employeeId") || 
                  rules.contains("employeename") || rules.contains("emplocation") || 
                  rules.contains("officelocation"), 
                  "Expected to find employee mapping fields but found: " + rules);
        
        System.out.println("=== Test Complete ===\n");
    }

    @Test
    public void testGenerateJsonRulesForSourceTargetMapper() {
        System.out.println("=== Testing Enhanced SourceTargetMapper JSON Rule Generation ===");
        
        String rules = enhancedJsonRuleGeneratorService.generateJsonRulesForMapper("SourceTargetMapper");
        
        assertNotNull(rules);
        assertTrue(rules.contains("sourceContentType"));
        assertTrue(rules.contains("targetContentType"));
        assertTrue(rules.contains("conversionRules"));
        
        // Should contain SourceTargetMapper specific mappings
        assertTrue(rules.contains("USER_ID") || rules.contains("userId"));
        assertTrue(rules.contains("EMAILS") || rules.contains("emails"));
        assertTrue(rules.contains("PHONE_NUMBER_LIST") || rules.contains("phoneNumberList"));
        
        System.out.println("Generated rules for SourceTargetMapper:");
        System.out.println(rules);
        System.out.println("=== Test Complete ===\n");
    }

    @Test
    public void testGenerateJsonRulesWithGroq() {
        System.out.println("=== Testing Groq Integration ===");
        
        String mapperName = "EmployeeMapper";
        
        try {
            String rules = enhancedJsonRuleGeneratorService.generateJsonRulesWithGroq(mapperName);
            
            assertNotNull(rules);
            System.out.println("Generated rules with Groq:");
            System.out.println(rules);
        } catch (Exception e) {
            System.out.println("Groq test failed (expected if Groq API is not accessible): " + e.getMessage());
        }
        
        System.out.println("=== Test Complete ===\n");
    }

    @Test
    public void testGenerateJsonRulesForAllMappers() {
        System.out.println("=== Testing Enhanced JSON Rule Generation for All Mappers ===");
        
        Map<String, String> allRules = enhancedJsonRuleGeneratorService.generateJsonRulesForAllMappers();
        
        assertNotNull(allRules);
        assertFalse(allRules.isEmpty());
        
        // Should contain all available mappers
        List<String> availableMappers = enhancedJsonRuleGeneratorService.getAvailableMappers();
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
        
        List<String> mappers = enhancedJsonRuleGeneratorService.getAvailableMappers();
        
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
            enhancedJsonRuleGeneratorService.generateJsonRulesForMapper("InvalidMapper");
        });
        
        assertTrue(exception.getMessage().contains("Mapper class not found") || 
                  exception.getMessage().contains("Error"));
        
        System.out.println("Expected error: " + exception.getMessage());
        System.out.println("=== Test Complete ===\n");
    }

    @Test
    public void testExtractFieldMappingsForSourceTargetMapper() throws Exception {
        System.out.println("=== Testing extractFieldMappings for SourceTargetMapper ===");
        // Use reflection to access the private method
        java.lang.reflect.Method method = EnhancedMapStructJsonRuleGeneratorService.class.getDeclaredMethod("extractFieldMappings", String.class);
        method.setAccessible(true);
        String result = (String) method.invoke(enhancedJsonRuleGeneratorService, "SourceTargetMapper");
        System.out.println(result);
        // Assert that the expected mappings are present
        assertTrue(result.contains("id -> userId"), "Should contain id -> userId");
        assertTrue(result.contains("emailList -> emails"), "Should contain emailList -> emails");
        assertTrue(result.contains("phoneNumbers -> phoneNumberList"), "Should contain phoneNumbers -> phoneNumberList");
        // Should NOT contain salary, age, doj, class, etc.
        assertFalse(result.contains("salary -> salary"), "Should NOT contain salary -> salary");
        assertFalse(result.contains("age -> age"), "Should NOT contain age -> age");
        assertFalse(result.contains("doj -> doj"), "Should NOT contain doj -> doj");
        System.out.println("=== Test Complete ===\n");
    }
} 