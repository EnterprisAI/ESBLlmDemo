package com.esb.llm.ESBLlmDemo.controller;

import com.esb.llm.ESBLlmDemo.service.MapStructJsonRuleGeneratorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/json-rules")
@CrossOrigin(origins = "*")
public class JsonRuleGeneratorController {

    @Autowired
    private MapStructJsonRuleGeneratorService jsonRuleGeneratorService;

    /**
     * Generate JSON rules for a specific mapper
     * @param mapperName The name of the mapper (e.g., "SourceTargetMapper", "EmployeeMapper")
     * @return JSON conversion rules
     */
    @GetMapping("/generate/{mapperName}")
    public ResponseEntity<String> generateJsonRulesForMapper(@PathVariable String mapperName) {
        try {
            String rules = jsonRuleGeneratorService.generateJsonRulesForMapper(mapperName);
            return ResponseEntity.ok(rules);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }

    /**
     * Generate JSON rules for all available mappers
     * @return Map of mapper names to their JSON rules
     */
    @GetMapping("/generate-all")
    public ResponseEntity<Map<String, String>> generateJsonRulesForAllMappers() {
        try {
            Map<String, String> allRules = jsonRuleGeneratorService.generateJsonRulesForAllMappers();
            return ResponseEntity.ok(allRules);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Get list of available mappers
     * @return List of available mapper names
     */
    @GetMapping("/mappers")
    public ResponseEntity<List<String>> getAvailableMappers() {
        try {
            List<String> mappers = jsonRuleGeneratorService.getAvailableMappers();
            return ResponseEntity.ok(mappers);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(List.of("Error: " + e.getMessage()));
        }
    }

    /**
     * Test the generator with a specific mapper and return detailed output
     * @param mapperName The name of the mapper to test
     * @return Test results with generated rules
     */
    @GetMapping("/test/{mapperName}")
    public ResponseEntity<String> testGeneratorWithMapper(@PathVariable String mapperName) {
        try {
            String result = jsonRuleGeneratorService.testGeneratorWithMapper(mapperName);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Test Error: " + e.getMessage());
        }
    }

    /**
     * Generate JSON rules from a custom mapper class name
     * @param requestBody Map containing "mapperClassName" key
     * @return JSON conversion rules
     */
    @PostMapping("/generate-from-class")
    public ResponseEntity<String> generateJsonRulesFromClassName(@RequestBody Map<String, String> requestBody) {
        try {
            String mapperClassName = requestBody.get("mapperClassName");
            if (mapperClassName == null || mapperClassName.trim().isEmpty()) {
                return ResponseEntity.badRequest().body("Error: mapperClassName is required");
            }
            
            String rules = jsonRuleGeneratorService.generateJsonRulesFromMapper(mapperClassName);
            return ResponseEntity.ok(rules);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }

    /**
     * Health check endpoint
     * @return Status message
     */
    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("JSON Rule Generator Service is running");
    }
} 