package com.esb.llm.ESBLlmDemo.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class JsonTransformationServiceTest implements CommandLineRunner {

    @Autowired
    private JsonTransformationService transformationService;

    @Override
    public void run(String... args) throws Exception {
        System.out.println("=== JSON Transformation Service Test ===");
        
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
            System.out.println("Source JSON:");
            System.out.println(sourceJson);
            System.out.println("\n" + "=".repeat(80) + "\n");
            
            System.out.println("Transforming JSON...");
            String transformedJson = transformationService.transformJson(sourceJson);
            
            System.out.println("Transformed JSON:");
            System.out.println(transformedJson);
            System.out.println("\n" + "=".repeat(80) + "\n");
            
            System.out.println("Expected Target JSON:");
            System.out.println(expectedTargetJson);
            System.out.println("\n" + "=".repeat(80) + "\n");
            
            // Compare the results
            if (transformedJson.replaceAll("\\s", "").equals(expectedTargetJson.replaceAll("\\s", ""))) {
                System.out.println("✅ SUCCESS: Transformation matches expected output!");
            } else {
                System.out.println("❌ FAILURE: Transformation does not match expected output!");
                System.out.println("Differences found between transformed and expected JSON.");
            }
            
        } catch (Exception e) {
            System.err.println("❌ ERROR: Transformation failed: " + e.getMessage());
            e.printStackTrace();
        }
        
        System.out.println("\n=== Test Complete ===");
    }
} 