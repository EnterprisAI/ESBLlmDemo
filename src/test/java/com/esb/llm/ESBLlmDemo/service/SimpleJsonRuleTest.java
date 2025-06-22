package com.esb.llm.ESBLlmDemo.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class SimpleJsonRuleTest {

    @Autowired
    private MapStructJsonRuleGeneratorService jsonRuleGeneratorService;

    @Test
    public void testSourceTargetMapper() {
        System.out.println("=== Testing SourceTargetMapper ===");
        
        try {
            String rules = jsonRuleGeneratorService.generateJsonRulesForMapper("SourceTargetMapper");
            System.out.println("Generated rules:");
            System.out.println(rules);
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
        }
        
        System.out.println("=== Test Complete ===");
    }

    @Test
    public void testEmployeeMapper() {
        System.out.println("=== Testing EmployeeMapper ===");
        
        try {
            String rules = jsonRuleGeneratorService.generateJsonRulesForMapper("EmployeeMapper");
            System.out.println("Generated rules:");
            System.out.println(rules);
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
        }
        
        System.out.println("=== Test Complete ===");
    }
} 