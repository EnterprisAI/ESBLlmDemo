package com.esb.llm.ESBLlmDemo.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class JsonTransformationServiceTest {

    @Autowired
    private JsonTransformationService transformationService;

    @Test
    public void testJsonTransformation() {
        // Source JSON from the requirements
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
                  },
                  {
                    "company": "XYZ Solutions",
                    "role": "Junior Developer",
                    "startDate": "2017-01-01",
                    "endDate": "2017-12-31",
                    "location": "Boston"
                  }
                ]
              },
              {
                "employeeId": "E002",
                "name": "Jane Smith",
                "gender": "Female",
                "age": 28,
                "address": {
                  "street": "456 Oak Avenue",
                  "city": "San Francisco",
                  "state": "CA",
                  "zipcode": "94105",
                  "country": "USA"
                },
                "officeDetails": {
                  "department": "Marketing",
                  "designation": "Marketing Manager",
                  "location": "San Francisco Office",
                  "employeeType": "Contract"
                },
                "workExperience": [
                  {
                    "company": "ABC Tech",
                    "role": "Software Engineer",
                    "startDate": "2018-01-01",
                    "endDate": "2019-12-31",
                    "location": "Chicago"
                  },
                  {
                    "company": "XYZ Solutions",
                    "role": "Junior Developer",
                    "startDate": "2017-01-01",
                    "endDate": "2017-12-31",
                    "location": "Boston"
                  }
                ]
              }
            ]
            """;

        // Expected target JSON from the requirements
        String expectedTargetJson = """
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
                  },
                  {
                    "company": "XYZ Solutions"
                  }
                ]
              },
              {
                "employeeId": "E002",
                "employeename": "Jane Smith",
                "age": 28,
                "gender": "Female",
                "emplocation": "USA",
                "officelocation": "San Francisco Office",
                "workExperience": [
                  {
                    "company": "ABC Tech"
                  },
                  {
                    "company": "XYZ Solutions"
                  }
                ]
              }
            ]
            """;

        try {
            // Transform the JSON
            String transformedJson = transformationService.transformJson(sourceJson);
            
            // Debug output
            System.out.println("=== DEBUG OUTPUT ===");
            System.out.println("Source JSON:");
            System.out.println(sourceJson);
            System.out.println("\nTransformed JSON:");
            System.out.println(transformedJson);
            System.out.println("\nExpected JSON:");
            System.out.println(expectedTargetJson);
            System.out.println("=== END DEBUG ===");
            
            // Remove whitespace for comparison
            String normalizedTransformed = transformedJson.replaceAll("\\s", "");
            String normalizedExpected = expectedTargetJson.replaceAll("\\s", "");
            
            // Assert the transformation is correct
            assertEquals(normalizedExpected, normalizedTransformed, 
                "Transformed JSON should match expected output");
            
            System.out.println("✅ SUCCESS: JSON transformation test passed!");
            
        } catch (Exception e) {
            fail("Transformation failed with exception: " + e.getMessage());
        }
    }

    @Test
    public void testValidateSourceJson() {
        // Test valid JSON
        String validJson = "[{\"employeeId\": \"E001\", \"name\": \"John Doe\"}]";
        assertTrue(transformationService.validateSourceJson(validJson), 
            "Valid JSON should pass validation");

        // Test invalid JSON
        String invalidJson = "invalid json";
        assertFalse(transformationService.validateSourceJson(invalidJson), 
            "Invalid JSON should fail validation");

        // Test empty JSON
        String emptyJson = "";
        assertFalse(transformationService.validateSourceJson(emptyJson), 
            "Empty JSON should fail validation");
    }
} 