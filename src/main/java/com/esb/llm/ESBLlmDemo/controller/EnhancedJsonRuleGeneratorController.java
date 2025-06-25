package com.esb.llm.ESBLlmDemo.controller;

import com.esb.llm.ESBLlmDemo.service.EnhancedMapStructJsonRuleGeneratorService;
import com.esb.llm.ESBLlmDemo.service.GenericMapStructAnalyzer;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
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

    @Autowired
    private GenericMapStructAnalyzer genericMapStructAnalyzer;
    
    @Autowired
    private ObjectMapper objectMapper;

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

    /**
     * Generate JSON rules using Ollama API with enhanced prompt
     */
    @PostMapping("/generate-with-ollama")
    public ResponseEntity<String> generateJsonRulesWithOllama(@RequestBody Map<String, String> request) {
        try {
            String mapperName = request.get("mapperName");
            String mapperCode = request.get("mapperCode");
            
            if (mapperName == null || mapperCode == null) {
                return ResponseEntity.badRequest().body("Error: mapperName and mapperCode are required");
            }
            
            String rules = enhancedJsonRuleGeneratorService.generateJsonRulesWithOllama(mapperName);
            return ResponseEntity.ok(rules);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }

    /**
     * Generate JSON rules using Ollama by just passing the mapper class name
     */
    @PostMapping("/generate-with-ollama-class")
    public ResponseEntity<String> generateJsonRulesWithOllamaByClass(@RequestBody Map<String, String> request) {
        try {
            String mapperName = request.get("mapperName");
            if (mapperName == null) {
                return ResponseEntity.badRequest().body("Error: mapperName is required");
            }
            String rules = enhancedJsonRuleGeneratorService.generateJsonRulesWithOllamaByClassName(mapperName);
            return ResponseEntity.ok(rules);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }

    /**
     * Extract custom mappings and their linked methods from a mapper
     */
    @GetMapping("/custom-mappings/{mapperName}")
    public ResponseEntity<String> extractCustomMappings(@PathVariable String mapperName) {
        try {
            String customMappings = enhancedJsonRuleGeneratorService.extractCustomMappings(mapperName);
            return ResponseEntity.ok(customMappings);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Error extracting custom mappings: " + e.getMessage());
        }
    }

    /**
     * Extract enhanced custom mappings including default methods
     */
    @GetMapping("/enhanced-custom-mappings/{mapperName}")
    public ResponseEntity<String> extractEnhancedCustomMappings(@PathVariable String mapperName) {
        try {
            String customMappings = enhancedJsonRuleGeneratorService.extractCustomMappingsEnhanced(mapperName);
            return ResponseEntity.ok(customMappings);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Error extracting enhanced custom mappings: " + e.getMessage());
        }
    }

    /**
     * Create comprehensive prompt with both direct and custom mappings
     */
    @GetMapping("/comprehensive-prompt/{mapperName}")
    public ResponseEntity<String> createComprehensivePrompt(@PathVariable String mapperName) {
        try {
            String prompt = enhancedJsonRuleGeneratorService.createComprehensivePrompt(mapperName);
            return ResponseEntity.ok(prompt);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Error creating comprehensive prompt: " + e.getMessage());
        }
    }

    /**
     * Generate JSON rules with comprehensive prompt including custom mappings
     */
    @PostMapping("/generate-with-custom-mappings/{mapperName}")
    public ResponseEntity<String> generateJsonRulesWithCustomMappings(@PathVariable String mapperName) {
        try {
            String prompt = enhancedJsonRuleGeneratorService.createComprehensivePrompt(mapperName);
            String response = enhancedJsonRuleGeneratorService.callGroq(prompt);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Error generating JSON rules with custom mappings: " + e.getMessage());
        }
    }

    /**
     * Get combined JSON rules for direct and custom mappings
     */
    @GetMapping("/combined-json-rules/{mapperName}")
    public ResponseEntity<String> getCombinedJsonRules(@PathVariable String mapperName) {
        try {
            String json = enhancedJsonRuleGeneratorService.generateCombinedJsonRules(mapperName);
            return ResponseEntity.ok(json);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Error generating combined JSON rules: " + e.getMessage());
        }
    }

    /**
     * Get Groq prompt with combined mappings
     */
    @GetMapping("/groq-prompt-combined/{mapperName}")
    public ResponseEntity<String> getGroqPromptCombined(@PathVariable String mapperName) {
        try {
            String prompt = enhancedJsonRuleGeneratorService.createGroqPromptCombined(mapperName);
            return ResponseEntity.ok(prompt);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Error generating Groq prompt: " + e.getMessage());
        }
    }

    /**
     * Test the generic MapStruct analyzer with any mapper
     */
    @GetMapping("/generic-analyzer/{mapperName}")
    public ResponseEntity<String> analyzeMapperGeneric(@PathVariable String mapperName) {
        try {
            String jsonRules = genericMapStructAnalyzer.generateJsonRules(mapperName);
            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(jsonRules);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error analyzing mapper " + mapperName + ": " + e.getMessage());
        }
    }

    /**
     * Get detailed analysis of any mapper using the generic analyzer
     */
    @GetMapping("/generic-analysis/{mapperName}")
    public ResponseEntity<String> getDetailedAnalysis(@PathVariable String mapperName) {
        try {
            Map<String, Object> analysis = genericMapStructAnalyzer.analyzeMapper(mapperName);
            String jsonAnalysis = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(analysis);
            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(jsonAnalysis);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error analyzing mapper " + mapperName + ": " + e.getMessage());
        }
    }
} 