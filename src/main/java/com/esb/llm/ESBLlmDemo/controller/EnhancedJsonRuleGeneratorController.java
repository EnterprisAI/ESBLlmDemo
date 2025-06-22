package com.esb.llm.ESBLlmDemo.controller;

import com.esb.llm.ESBLlmDemo.service.EnhancedMapStructJsonRuleGeneratorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/enhanced-json-rules")
@CrossOrigin(origins = "*")
public class EnhancedJsonRuleGeneratorController {

    @Autowired
    private EnhancedMapStructJsonRuleGeneratorService enhancedJsonRuleGeneratorService;

    /**
     * Generate JSON rules for a specific mapper using enhanced reflection
     */
    @GetMapping("/generate/{mapperName}")
    public ResponseEntity<String> generateJsonRulesForMapper(@PathVariable String mapperName) {
        try {
            String rules = enhancedJsonRuleGeneratorService.generateJsonRulesForMapper(mapperName);
            return ResponseEntity.ok(rules);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }

    /**
     * Generate JSON rules using Groq API with enhanced prompt
     */
    @PostMapping("/generate-with-groq")
    public ResponseEntity<String> generateJsonRulesWithGroq(@RequestBody Map<String, String> request) {
        try {
            String mapperName = request.get("mapperName");
            String mapperCode = request.get("mapperCode");
            
            if (mapperName == null || mapperCode == null) {
                return ResponseEntity.badRequest().body("Error: mapperName and mapperCode are required");
            }
            
            String rules = enhancedJsonRuleGeneratorService.generateJsonRulesWithGroq(mapperName);
            return ResponseEntity.ok(rules);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }

    /**
     * Generate JSON rules for all available mappers
     */
    @GetMapping("/generate-all")
    public ResponseEntity<Map<String, String>> generateJsonRulesForAllMappers() {
        try {
            Map<String, String> allRules = enhancedJsonRuleGeneratorService.generateJsonRulesForAllMappers();
            return ResponseEntity.ok(allRules);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Get list of available mappers
     */
    @GetMapping("/mappers")
    public ResponseEntity<List<String>> getAvailableMappers() {
        try {
            List<String> mappers = enhancedJsonRuleGeneratorService.getAvailableMappers();
            return ResponseEntity.ok(mappers);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(List.of("Error: " + e.getMessage()));
        }
    }

    /**
     * Health check endpoint
     */
    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("Enhanced JSON Rule Generator Service is running");
    }

    /**
     * Generate JSON rules using Groq by just passing the mapper class name
     */
    @PostMapping("/generate-with-groq-class")
    public ResponseEntity<String> generateJsonRulesWithGroqByClass(@RequestBody Map<String, String> request) {
        try {
            String mapperName = request.get("mapperName");
            if (mapperName == null) {
                return ResponseEntity.badRequest().body("Error: mapperName is required");
            }
            String rules = enhancedJsonRuleGeneratorService.generateJsonRulesWithGroqByClassName(mapperName);
            return ResponseEntity.ok(rules);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }
} 