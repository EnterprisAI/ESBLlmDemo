package com.esb.llm.ESBLlmDemo.service;

import com.esb.llm.ESBLlmDemo.config.MappingRules;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class MappingRulesGeneratorServiceTest {

    @Autowired
    private MappingRulesGeneratorService mappingRulesGeneratorService;

    @Test
    public void testGenerateMappingRulesFromMapStruct() {
        try {
            System.out.println("=== Testing Reverse Engineering of Mapping Rules ===");
            
            // Generate mapping rules from MapStruct
            MappingRules rules = mappingRulesGeneratorService.generateMappingRulesFromMapStruct();
            
            // Verify basic structure
            assertNotNull(rules, "Generated rules should not be null");
            assertEquals("JSON", rules.getSourceContentType(), "Source content type should be JSON");
            assertEquals("JSON", rules.getTargetContentType(), "Target content type should be JSON");
            assertNotNull(rules.getConversionRules(), "Conversion rules should not be null");
            assertEquals(1, rules.getConversionRules().size(), "Should have one main conversion rule");
            
            // Verify the main EmployeeList rule
            MappingRules.ConversionRule employeeRule = rules.getConversionRules().get(0);
            assertEquals("EmployeeList", employeeRule.getPropID(), "Main rule should be EmployeeList");
            assertEquals("$", employeeRule.getSourceLocation(), "Source location should be $");
            assertEquals("$", employeeRule.getTargetLocation(), "Target location should be $");
            assertTrue(employeeRule.isArray(), "EmployeeList should be an array");
            
            // Verify items
            assertNotNull(employeeRule.getItems(), "Employee rule should have items");
            assertEquals(7, employeeRule.getItems().size(), "Should have 7 mapping items");
            
            // Print the generated rules
            System.out.println("Generated Mapping Rules:");
            System.out.println(mappingRulesGeneratorService.generateMappingRulesAsJson());
            
            System.out.println("✅ SUCCESS: Mapping rules generated successfully from MapStruct!");
            
        } catch (Exception e) {
            fail("Failed to generate mapping rules: " + e.getMessage());
        }
    }

    @Test
    public void testGenerateMappingRulesAsJson() {
        try {
            String jsonRules = mappingRulesGeneratorService.generateMappingRulesAsJson();
            
            // Verify JSON structure
            assertNotNull(jsonRules, "Generated JSON should not be null");
            assertTrue(jsonRules.contains("sourceContentType"), "Should contain sourceContentType");
            assertTrue(jsonRules.contains("targetContentType"), "Should contain targetContentType");
            assertTrue(jsonRules.contains("conversionRules"), "Should contain conversionRules");
            assertTrue(jsonRules.contains("EmployeeList"), "Should contain EmployeeList");
            assertTrue(jsonRules.contains("EMPLOYEE_ID"), "Should contain EMPLOYEE_ID");
            assertTrue(jsonRules.contains("EMPLOYEE_NAME"), "Should contain EMPLOYEE_NAME");
            assertTrue(jsonRules.contains("address.country"), "Should contain address.country mapping");
            assertTrue(jsonRules.contains("officeDetails.location"), "Should contain officeDetails.location mapping");
            assertTrue(jsonRules.contains("workExperience"), "Should contain workExperience mapping");
            
            System.out.println("Generated JSON Rules:");
            System.out.println(jsonRules);
            
            System.out.println("✅ SUCCESS: JSON mapping rules generated successfully!");
            
        } catch (Exception e) {
            fail("Failed to generate JSON rules: " + e.getMessage());
        }
    }
} 