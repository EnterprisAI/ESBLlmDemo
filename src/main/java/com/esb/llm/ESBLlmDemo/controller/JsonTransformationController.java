package com.esb.llm.ESBLlmDemo.controller;

import com.esb.llm.ESBLlmDemo.config.MappingRules;
import com.esb.llm.ESBLlmDemo.service.JsonTransformationService;
import com.esb.llm.ESBLlmDemo.service.MappingRulesGeneratorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/transform")
@CrossOrigin(origins = "*")
public class JsonTransformationController {

    @Autowired
    private JsonTransformationService transformationService;

    @Autowired
    private MappingRulesGeneratorService mappingRulesGeneratorService;

    /**
     * Transform source JSON to target JSON using default mapping rules
     * @param requestBody The request body containing source JSON
     * @return The transformed JSON
     */
    @PostMapping("/json")
    public ResponseEntity<Map<String, Object>> transformJson(@RequestBody Map<String, Object> requestBody) {
        try {
            String sourceJson = (String) requestBody.get("sourceJson");
            
            if (sourceJson == null || sourceJson.trim().isEmpty()) {
                return ResponseEntity.badRequest()
                    .body(Map.of("error", "Source JSON is required"));
            }

            // Validate source JSON
            if (!transformationService.validateSourceJson(sourceJson)) {
                return ResponseEntity.badRequest()
                    .body(Map.of("error", "Invalid source JSON format"));
            }

            // Transform the JSON
            String transformedJson = transformationService.transformJson(sourceJson);
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "transformedJson", transformedJson
            ));
            
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                .body(Map.of("error", "Transformation failed: " + e.getMessage()));
        }
    }

    /**
     * Get the default mapping rules
     * @return The default mapping rules
     */
    @GetMapping("/rules/default")
    public ResponseEntity<MappingRules> getDefaultMappingRules() {
        return ResponseEntity.ok(MappingRules.getDefaultMappingRules());
    }

    /**
     * Generate mapping rules from MapStruct annotations
     * @return The generated mapping rules as JSON string
     */
    @GetMapping("/rules/generate")
    public ResponseEntity<Map<String, Object>> generateMappingRulesFromMapStruct() {
        try {
            String jsonRules = mappingRulesGeneratorService.generateMappingRulesAsJson();
            return ResponseEntity.ok(Map.of(
                "success", true,
                "mappingRules", jsonRules
            ));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                .body(Map.of("error", "Failed to generate mapping rules: " + e.getMessage()));
        }
    }

    /**
     * Health check endpoint
     * @return Health status
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        return ResponseEntity.ok(Map.of("status", "UP", "service", "JSON Transformation Service"));
    }
} 