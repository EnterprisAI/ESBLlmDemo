package com.esb.llm.ESBLlmDemo.controller;

import com.esb.llm.ESBLlmDemo.config.MappingRules;
import com.esb.llm.ESBLlmDemo.service.JsonTransformationService;
import com.esb.llm.ESBLlmDemo.service.MappingRulesGeneratorService;
import com.esb.llm.ESBLlmDemo.service.GenericMappingRulesGeneratorService;
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

    @Autowired
    private GenericMappingRulesGeneratorService genericMappingRulesGeneratorService;

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
     * Generate mapping rules by comparing source and target JSON structures
     * @param requestBody The request body containing source and target JSON
     * @return The generated mapping rules as JSON string
     */
    @PostMapping("/rules/generate-from-json")
    public ResponseEntity<Map<String, Object>> generateMappingRulesFromJson(@RequestBody Map<String, Object> requestBody) {
        try {
            String sourceJson = (String) requestBody.get("sourceJson");
            String targetJson = (String) requestBody.get("targetJson");
            
            if (sourceJson == null || sourceJson.trim().isEmpty()) {
                return ResponseEntity.badRequest()
                    .body(Map.of("error", "Source JSON is required"));
            }
            
            if (targetJson == null || targetJson.trim().isEmpty()) {
                return ResponseEntity.badRequest()
                    .body(Map.of("error", "Target JSON is required"));
            }

            String jsonRules = genericMappingRulesGeneratorService.generateMappingRulesAsJson(sourceJson, targetJson);
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
     * Analyze JSON structure and generate mapping rules for documentation
     * @param requestBody The request body containing JSON structure
     * @return The generated structure analysis as JSON string
     */
    @PostMapping("/rules/analyze-structure")
    public ResponseEntity<Map<String, Object>> analyzeJsonStructure(@RequestBody Map<String, Object> requestBody) {
        try {
            String jsonStructure = (String) requestBody.get("jsonStructure");
            
            if (jsonStructure == null || jsonStructure.trim().isEmpty()) {
                return ResponseEntity.badRequest()
                    .body(Map.of("error", "JSON structure is required"));
            }

            String structureAnalysis = genericMappingRulesGeneratorService.generateStructureAnalysisAsJson(jsonStructure);
            return ResponseEntity.ok(Map.of(
                "success", true,
                "structureAnalysis", structureAnalysis
            ));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                .body(Map.of("error", "Failed to analyze JSON structure: " + e.getMessage()));
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
     * Health check endpoint
     * @return Health status
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        return ResponseEntity.ok(Map.of("status", "UP", "service", "JSON Transformation Service"));
    }
} 