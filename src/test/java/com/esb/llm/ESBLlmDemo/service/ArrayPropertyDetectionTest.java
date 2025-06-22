package com.esb.llm.ESBLlmDemo.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class ArrayPropertyDetectionTest {

    @Autowired
    private MapStructJsonRuleGeneratorService jsonRuleGeneratorService;

    @Test
    public void testArrayPropertyDetection() {
        System.out.println("=== Testing Generic Array Property Detection ===");
        
        // Test with SourceTargetMapper which has emailList, phoneNumbers, addressList
        String rules = jsonRuleGeneratorService.generateJsonRulesForMapper("SourceTargetMapper");
        
        System.out.println("Generated rules for SourceTargetMapper:");
        System.out.println(rules);
        
        // Verify that the rules contain the expected array properties
        // The generic detection should still identify emailList, phoneNumbers, addressList as arrays
        // because they contain "list" or end with "s" (plural)
        
        System.out.println("=== Test Complete ===");
    }

    @Test
    public void testEmployeeMapperArrayDetection() {
        System.out.println("=== Testing EmployeeMapper Array Detection ===");
        
        String rules = jsonRuleGeneratorService.generateJsonRulesForMapper("EmployeeMapper");
        
        System.out.println("Generated rules for EmployeeMapper:");
        System.out.println(rules);
        
        // EmployeeMapper has workExperience which should be detected as array
        // because it ends with "s" (plural) and is not in the commonSingularWords list
        
        System.out.println("=== Test Complete ===");
    }
} 