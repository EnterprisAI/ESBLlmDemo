package com.esb.llm.ESBLlmDemo.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class CustomMappingDemoTest {

    @Autowired
    private EnhancedMapStructJsonRuleGeneratorService enhancedJsonRuleGeneratorService;

    @Test
    public void demonstrateCustomMappingExtraction() {
        System.out.println("=== DEMONSTRATING CUSTOM MAPPING EXTRACTION ===");
        
        // Test custom mappings extraction
        System.out.println("\n1. CUSTOM MAPPINGS EXTRACTION:");
        System.out.println("=================================");
        String customMappings = enhancedJsonRuleGeneratorService.extractCustomMappings("SourceTargetMapper");
        System.out.println(customMappings);
        
        // Test enhanced custom mappings extraction
        System.out.println("\n2. ENHANCED CUSTOM MAPPINGS EXTRACTION:");
        System.out.println("=========================================");
        String enhancedCustomMappings = enhancedJsonRuleGeneratorService.extractCustomMappingsEnhanced("SourceTargetMapper");
        System.out.println(enhancedCustomMappings);
        
        // Test comprehensive prompt creation
        System.out.println("\n3. COMPREHENSIVE PROMPT CREATION:");
        System.out.println("===================================");
        String comprehensivePrompt = enhancedJsonRuleGeneratorService.createComprehensivePrompt("SourceTargetMapper");
        System.out.println(comprehensivePrompt);
        
        System.out.println("\n=== DEMONSTRATION COMPLETE ===");
    }
} 