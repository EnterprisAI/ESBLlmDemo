package com.esb.llm.ESBLlmDemo.service;

import com.esb.llm.ESBLlmDemo.config.MappingRules;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class GenericMappingRulesGeneratorServiceTest {

    @Autowired
    private GenericMappingRulesGeneratorService genericMappingRulesGeneratorService;

    @Test
    public void testGenerateMappingRulesFromJson() {
        try {
            System.out.println("=== Testing Generic Mapping Rules Generation ===");
            
            String sourceJson = """
                [
                  {
                    "employeeId": "E001",
                    "name": "John Doe",
                    "gender": "Male",
                    "age": 30,
                    "address": {
                      "street": "123 Main St",
                      "city": "New York",
                      "state": "NY",
                      "zipcode": "10001",
                      "country": "USA"
                    },
                    "officeDetails": {
                      "department": "Engineering",
                      "designation": "Software Developer",
                      "location": "New York HQ",
                      "employeeType": "Full-Time"
                    },
                    "workExperience": [
                      {
                        "company": "ABC Tech",
                        "role": "Software Engineer",
                        "startDate": "2018-01-01",
                        "endDate": "2020-12-31",
                        "location": "Chicago"
                      }
                    ]
                  }
                ]
                """;

            String targetJson = """
                [
                  {
                    "employeeId": "E001",
                    "employeename": "John Doe",
                    "age": 30,
                    "gender": "Male",
                    "emplocation": "USA",
                    "officelocation": "New York HQ",
                    "workExperience": [
                      {
                        "company": "ABC Tech"
                      }
                    ]
                  }
                ]
                """;

            // Generate mapping rules
            MappingRules rules = genericMappingRulesGeneratorService.generateMappingRulesFromJson(sourceJson, targetJson);
            
            // Verify basic structure
            assertNotNull(rules, "Generated rules should not be null");
            assertEquals("JSON", rules.getSourceContentType(), "Source content type should be JSON");
            assertEquals("JSON", rules.getTargetContentType(), "Target content type should be JSON");
            assertNotNull(rules.getConversionRules(), "Conversion rules should not be null");
            assertEquals(1, rules.getConversionRules().size(), "Should have one main conversion rule");
            
            // Verify the main RootArray rule
            MappingRules.ConversionRule rootRule = rules.getConversionRules().get(0);
            assertEquals("RootArray", rootRule.getPropID(), "Main rule should be RootArray");
            assertEquals("$", rootRule.getSourceLocation(), "Source location should be $");
            assertEquals("$", rootRule.getTargetLocation(), "Target location should be $");
            assertTrue(rootRule.isArray(), "RootArray should be an array");
            
            // Print the generated rules
            System.out.println("Generated Mapping Rules from JSON comparison:");
            System.out.println(genericMappingRulesGeneratorService.generateMappingRulesAsJson(sourceJson, targetJson));
            
            System.out.println("✅ SUCCESS: Generic mapping rules generated successfully!");
            
        } catch (Exception e) {
            fail("Failed to generate mapping rules: " + e.getMessage());
        }
    }

    @Test
    public void testGenerateStructureAnalysis() {
        try {
            System.out.println("=== Testing JSON Structure Analysis ===");
            
            String jsonStructure = """
                {
                  "productId": "P001",
                  "productName": "Laptop",
                  "price": 999.99,
                  "category": {
                    "id": "C001",
                    "name": "Electronics",
                    "description": "Electronic devices"
                  },
                  "specifications": [
                    {
                      "key": "RAM",
                      "value": "16GB",
                      "unit": "GB"
                    },
                    {
                      "key": "Storage",
                      "value": "512GB",
                      "unit": "GB"
                    }
                  ],
                  "tags": ["laptop", "computer", "electronics"]
                }
                """;

            // Generate structure analysis
            MappingRules rules = genericMappingRulesGeneratorService.generateMappingRulesFromStructure(jsonStructure);
            
            // Verify basic structure
            assertNotNull(rules, "Generated rules should not be null");
            assertEquals("JSON", rules.getSourceContentType(), "Source content type should be JSON");
            assertEquals("JSON", rules.getTargetContentType(), "Target content type should be JSON");
            assertNotNull(rules.getConversionRules(), "Conversion rules should not be null");
            assertEquals(1, rules.getConversionRules().size(), "Should have one main conversion rule");
            
            // Verify the main RootObject rule
            MappingRules.ConversionRule rootRule = rules.getConversionRules().get(0);
            assertEquals("RootObject", rootRule.getPropID(), "Main rule should be RootObject");
            assertEquals("$", rootRule.getSourceLocation(), "Source location should be $");
            assertEquals("$", rootRule.getTargetLocation(), "Target location should be $");
            assertFalse(rootRule.isArray(), "RootObject should not be an array");
            
            // Print the generated structure analysis
            System.out.println("Generated Structure Analysis:");
            System.out.println(genericMappingRulesGeneratorService.generateStructureAnalysisAsJson(jsonStructure));
            
            System.out.println("✅ SUCCESS: JSON structure analysis generated successfully!");
            
        } catch (Exception e) {
            fail("Failed to generate structure analysis: " + e.getMessage());
        }
    }

    @Test
    public void testGenerateMappingRulesAsJson() {
        try {
            String sourceJson = """
                {
                  "id": "123",
                  "title": "Sample Title",
                  "description": "Sample Description",
                  "metadata": {
                    "created": "2023-01-01",
                    "updated": "2023-01-02"
                  }
                }
                """;

            String targetJson = """
                {
                  "documentId": "123",
                  "documentTitle": "Sample Title",
                  "documentDescription": "Sample Description",
                  "documentMetadata": {
                    "creationDate": "2023-01-01",
                    "lastModified": "2023-01-02"
                  }
                }
                """;

            String jsonRules = genericMappingRulesGeneratorService.generateMappingRulesAsJson(sourceJson, targetJson);
            
            // Verify JSON structure
            assertNotNull(jsonRules, "Generated JSON should not be null");
            assertTrue(jsonRules.contains("sourceContentType"), "Should contain sourceContentType");
            assertTrue(jsonRules.contains("targetContentType"), "Should contain targetContentType");
            assertTrue(jsonRules.contains("conversionRules"), "Should contain conversionRules");
            assertTrue(jsonRules.contains("RootObject"), "Should contain RootObject");
            
            System.out.println("Generated JSON Rules for Object-to-Object mapping:");
            System.out.println(jsonRules);
            
            System.out.println("✅ SUCCESS: JSON mapping rules generated successfully!");
            
        } catch (Exception e) {
            fail("Failed to generate JSON rules: " + e.getMessage());
        }
    }

    @Test
    public void testComplexNestedStructure() {
        try {
            System.out.println("=== Testing Complex Nested Structure ===");
            
            String complexJson = """
                {
                  "orderId": "ORD001",
                  "customer": {
                    "id": "CUST001",
                    "name": "John Doe",
                    "email": "john@example.com",
                    "address": {
                      "street": "123 Main St",
                      "city": "New York",
                      "state": "NY",
                      "zipcode": "10001"
                    }
                  },
                  "items": [
                    {
                      "productId": "PROD001",
                      "name": "Laptop",
                      "quantity": 2,
                      "price": 999.99,
                      "options": {
                        "color": "Black",
                        "warranty": "2 years"
                      }
                    },
                    {
                      "productId": "PROD002",
                      "name": "Mouse",
                      "quantity": 1,
                      "price": 29.99,
                      "options": {
                        "color": "White",
                        "warranty": "1 year"
                      }
                    }
                  ],
                  "totals": {
                    "subtotal": 2029.97,
                    "tax": 162.40,
                    "shipping": 15.00,
                    "total": 2207.37
                  }
                }
                """;

            String structureAnalysis = genericMappingRulesGeneratorService.generateStructureAnalysisAsJson(complexJson);
            
            // Verify structure analysis
            assertNotNull(structureAnalysis, "Generated structure analysis should not be null");
            assertTrue(structureAnalysis.contains("orderId"), "Should contain orderId");
            assertTrue(structureAnalysis.contains("customer"), "Should contain customer");
            assertTrue(structureAnalysis.contains("items"), "Should contain items");
            assertTrue(structureAnalysis.contains("totals"), "Should contain totals");
            
            System.out.println("Complex Structure Analysis:");
            System.out.println(structureAnalysis);
            
            System.out.println("✅ SUCCESS: Complex nested structure analysis generated successfully!");
            
        } catch (Exception e) {
            fail("Failed to analyze complex structure: " + e.getMessage());
        }
    }
} 