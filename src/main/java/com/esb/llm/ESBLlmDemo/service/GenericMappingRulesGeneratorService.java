package com.esb.llm.ESBLlmDemo.service;

import com.esb.llm.ESBLlmDemo.config.MappingRules;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class GenericMappingRulesGeneratorService {

    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Generate mapping rules by comparing source and target JSON structures
     * @param sourceJson Source JSON string
     * @param targetJson Target JSON string
     * @return MappingRules object
     */
    public MappingRules generateMappingRulesFromJson(String sourceJson, String targetJson) {
        try {
            JsonNode sourceNode = objectMapper.readTree(sourceJson);
            JsonNode targetNode = objectMapper.readTree(targetJson);
            
            MappingRules rules = new MappingRules();
            rules.setSourceContentType("JSON");
            rules.setTargetContentType("JSON");
            
            List<MappingRules.ConversionRule> conversionRules = new ArrayList<>();
            
            // Determine if we're dealing with arrays or objects
            if (sourceNode.isArray() && targetNode.isArray()) {
                // Array to array mapping
                conversionRules.add(createArrayMappingRule(sourceNode, targetNode));
            } else if (sourceNode.isObject() && targetNode.isObject()) {
                // Object to object mapping
                conversionRules.add(createObjectMappingRule(sourceNode, targetNode));
            } else {
                // Mixed or direct mapping
                conversionRules.add(createDirectMappingRule(sourceNode, targetNode));
            }
            
            rules.setConversionRules(conversionRules);
            return rules;
            
        } catch (Exception e) {
            throw new RuntimeException("Error generating mapping rules from JSON: " + e.getMessage(), e);
        }
    }

    /**
     * Generate mapping rules from a single JSON structure (for documentation)
     * @param jsonStructure JSON structure to analyze
     * @return MappingRules object
     */
    public MappingRules generateMappingRulesFromStructure(String jsonStructure) {
        try {
            JsonNode node = objectMapper.readTree(jsonStructure);
            
            MappingRules rules = new MappingRules();
            rules.setSourceContentType("JSON");
            rules.setTargetContentType("JSON");
            
            List<MappingRules.ConversionRule> conversionRules = new ArrayList<>();
            
            if (node.isArray()) {
                conversionRules.add(createStructureAnalysisRule(node, "RootArray"));
            } else if (node.isObject()) {
                conversionRules.add(createStructureAnalysisRule(node, "RootObject"));
            }
            
            rules.setConversionRules(conversionRules);
            return rules;
            
        } catch (Exception e) {
            throw new RuntimeException("Error generating mapping rules from structure: " + e.getMessage(), e);
        }
    }

    /**
     * Create mapping rule for array to array transformation
     */
    private MappingRules.ConversionRule createArrayMappingRule(JsonNode sourceArray, JsonNode targetArray) {
        MappingRules.ConversionRule rule = new MappingRules.ConversionRule();
        rule.setPropID("RootArray");
        rule.setSourceLocation("$");
        rule.setTargetLocation("$");
        rule.setArray(true);
        
        if (!sourceArray.isEmpty() && !targetArray.isEmpty()) {
            // Analyze first elements to determine item mapping
            JsonNode sourceItem = sourceArray.get(0);
            JsonNode targetItem = targetArray.get(0);
            
            if (sourceItem != null && targetItem != null) {
                rule.setItems(analyzeObjectMapping(sourceItem, targetItem, "ArrayItem"));
            }
        }
        
        return rule;
    }

    /**
     * Create mapping rule for object to object transformation
     */
    private MappingRules.ConversionRule createObjectMappingRule(JsonNode sourceObject, JsonNode targetObject) {
        MappingRules.ConversionRule rule = new MappingRules.ConversionRule();
        rule.setPropID("RootObject");
        rule.setSourceLocation("$");
        rule.setTargetLocation("$");
        rule.setArray(false);
        rule.setItems(analyzeObjectMapping(sourceObject, targetObject, "Root"));
        
        return rule;
    }

    /**
     * Create direct mapping rule for mixed types
     */
    private MappingRules.ConversionRule createDirectMappingRule(JsonNode source, JsonNode target) {
        MappingRules.ConversionRule rule = new MappingRules.ConversionRule();
        rule.setPropID("DirectMapping");
        rule.setSourceLocation("$");
        rule.setTargetLocation("$");
        rule.setArray(false);
        rule.setItems(null);
        
        return rule;
    }

    /**
     * Create structure analysis rule
     */
    private MappingRules.ConversionRule createStructureAnalysisRule(JsonNode node, String propId) {
        MappingRules.ConversionRule rule = new MappingRules.ConversionRule();
        rule.setPropID(propId);
        rule.setSourceLocation("$");
        rule.setTargetLocation("$");
        rule.setArray(node.isArray());
        
        if (node.isObject()) {
            rule.setItems(analyzeObjectStructure(node, propId));
        } else if (node.isArray() && !node.isEmpty()) {
            JsonNode firstItem = node.get(0);
            if (firstItem != null && firstItem.isObject()) {
                rule.setItems(analyzeObjectStructure(firstItem, "ArrayItem"));
            }
        }
        
        return rule;
    }

    /**
     * Analyze object mapping between source and target
     */
    private List<MappingRules.ConversionRule> analyzeObjectMapping(JsonNode sourceObject, JsonNode targetObject, String prefix) {
        List<MappingRules.ConversionRule> items = new ArrayList<>();
        
        Iterator<Map.Entry<String, JsonNode>> targetFields = targetObject.fields();
        while (targetFields.hasNext()) {
            Map.Entry<String, JsonNode> targetField = targetFields.next();
            String targetFieldName = targetField.getKey();
            JsonNode targetFieldValue = targetField.getValue();
            
            // Try to find matching source field
            String sourceFieldName = findMatchingSourceField(sourceObject, targetFieldName);
            
            if (sourceFieldName != null) {
                JsonNode sourceFieldValue = sourceObject.get(sourceFieldName);
                items.add(createFieldMappingRule(sourceFieldName, targetFieldName, sourceFieldValue, targetFieldValue, prefix));
            } else {
                // Create a placeholder rule for unmapped fields
                items.add(createPlaceholderRule(targetFieldName, targetFieldValue, prefix));
            }
        }
        
        return items;
    }

    /**
     * Analyze object structure for documentation
     */
    private List<MappingRules.ConversionRule> analyzeObjectStructure(JsonNode object, String prefix) {
        List<MappingRules.ConversionRule> items = new ArrayList<>();
        
        Iterator<Map.Entry<String, JsonNode>> fields = object.fields();
        while (fields.hasNext()) {
            Map.Entry<String, JsonNode> field = fields.next();
            String fieldName = field.getKey();
            JsonNode fieldValue = field.getValue();
            
            items.add(createStructureFieldRule(fieldName, fieldValue, prefix));
        }
        
        return items;
    }

    /**
     * Find matching source field for a target field
     */
    private String findMatchingSourceField(JsonNode sourceObject, String targetFieldName) {
        // Direct match
        if (sourceObject.has(targetFieldName)) {
            return targetFieldName;
        }
        
        // Case-insensitive match
        Iterator<String> sourceFieldNames = sourceObject.fieldNames();
        while (sourceFieldNames.hasNext()) {
            String sourceFieldName = sourceFieldNames.next();
            if (sourceFieldName.equalsIgnoreCase(targetFieldName)) {
                return sourceFieldName;
            }
        }
        
        // Partial match (e.g., "name" matches "employeename")
        sourceFieldNames = sourceObject.fieldNames();
        while (sourceFieldNames.hasNext()) {
            String sourceFieldName = sourceFieldNames.next();
            if (sourceFieldName.toLowerCase().contains(targetFieldName.toLowerCase()) ||
                targetFieldName.toLowerCase().contains(sourceFieldName.toLowerCase())) {
                return sourceFieldName;
            }
        }
        
        return null;
    }

    /**
     * Create field mapping rule
     */
    private MappingRules.ConversionRule createFieldMappingRule(String sourceFieldName, String targetFieldName, 
                                                              JsonNode sourceValue, JsonNode targetValue, String prefix) {
        MappingRules.ConversionRule rule = new MappingRules.ConversionRule();
        rule.setPropID(generatePropId(targetFieldName, prefix));
        rule.setSourceLocation(sourceFieldName);
        rule.setTargetLocation(targetFieldName);
        
        if (sourceValue.isArray() && targetValue.isArray()) {
            rule.setArray(true);
            if (!sourceValue.isEmpty() && !targetValue.isEmpty()) {
                rule.setItems(analyzeObjectMapping(sourceValue.get(0), targetValue.get(0), prefix + "_" + targetFieldName));
            }
        } else if (sourceValue.isObject() && targetValue.isObject()) {
            rule.setArray(false);
            rule.setItems(analyzeObjectMapping(sourceValue, targetValue, prefix + "_" + targetFieldName));
        } else {
            rule.setArray(false);
            rule.setItems(null);
        }
        
        return rule;
    }

    /**
     * Create placeholder rule for unmapped fields
     */
    private MappingRules.ConversionRule createPlaceholderRule(String targetFieldName, JsonNode targetValue, String prefix) {
        MappingRules.ConversionRule rule = new MappingRules.ConversionRule();
        rule.setPropID(generatePropId(targetFieldName, prefix) + "_UNMAPPED");
        rule.setSourceLocation("UNKNOWN");
        rule.setTargetLocation(targetFieldName);
        rule.setArray(targetValue.isArray());
        rule.setItems(null);
        
        return rule;
    }

    /**
     * Create structure field rule for documentation
     */
    private MappingRules.ConversionRule createStructureFieldRule(String fieldName, JsonNode fieldValue, String prefix) {
        MappingRules.ConversionRule rule = new MappingRules.ConversionRule();
        rule.setPropID(generatePropId(fieldName, prefix));
        rule.setSourceLocation(fieldName);
        rule.setTargetLocation(fieldName);
        rule.setArray(fieldValue.isArray());
        
        if (fieldValue.isObject()) {
            rule.setItems(analyzeObjectStructure(fieldValue, prefix + "_" + fieldName));
        } else if (fieldValue.isArray() && !fieldValue.isEmpty()) {
            JsonNode firstItem = fieldValue.get(0);
            if (firstItem != null && firstItem.isObject()) {
                rule.setItems(analyzeObjectStructure(firstItem, prefix + "_" + fieldName));
            }
        } else {
            rule.setItems(null);
        }
        
        return rule;
    }

    /**
     * Generate prop ID from field name and prefix
     */
    private String generatePropId(String fieldName, String prefix) {
        String baseName = fieldName.toUpperCase().replaceAll("[^A-Z0-9]", "_");
        return prefix.toUpperCase() + "_" + baseName;
    }

    /**
     * Generate mapping rules as JSON string
     */
    public String generateMappingRulesAsJson(String sourceJson, String targetJson) {
        try {
            MappingRules rules = generateMappingRulesFromJson(sourceJson, targetJson);
            return convertMappingRulesToJson(rules);
        } catch (Exception e) {
            throw new RuntimeException("Error generating JSON rules: " + e.getMessage(), e);
        }
    }

    /**
     * Generate structure analysis as JSON string
     */
    public String generateStructureAnalysisAsJson(String jsonStructure) {
        try {
            MappingRules rules = generateMappingRulesFromStructure(jsonStructure);
            return convertMappingRulesToJson(rules);
        } catch (Exception e) {
            throw new RuntimeException("Error generating structure analysis: " + e.getMessage(), e);
        }
    }

    /**
     * Convert MappingRules object to JSON string
     */
    private String convertMappingRulesToJson(MappingRules rules) {
        StringBuilder json = new StringBuilder();
        json.append("{\n");
        json.append("  \"sourceContentType\": \"").append(rules.getSourceContentType()).append("\",\n");
        json.append("  \"targetContentType\": \"").append(rules.getTargetContentType()).append("\",\n");
        json.append("  \"conversionRules\": [\n");
        
        for (int i = 0; i < rules.getConversionRules().size(); i++) {
            MappingRules.ConversionRule rule = rules.getConversionRules().get(i);
            json.append(convertConversionRuleToJson(rule, "    "));
            if (i < rules.getConversionRules().size() - 1) {
                json.append(",");
            }
            json.append("\n");
        }
        
        json.append("  ]\n");
        json.append("}");
        
        return json.toString();
    }

    /**
     * Convert ConversionRule to JSON string
     */
    private String convertConversionRuleToJson(MappingRules.ConversionRule rule, String indent) {
        StringBuilder json = new StringBuilder();
        json.append(indent).append("{\n");
        json.append(indent).append("  \"propID\": \"").append(rule.getPropID()).append("\",\n");
        json.append(indent).append("  \"sourceLocation\": \"").append(rule.getSourceLocation()).append("\",\n");
        json.append(indent).append("  \"targetLocation\": \"").append(rule.getTargetLocation()).append("\",\n");
        json.append(indent).append("  \"isArray\": ").append(rule.isArray()).append(",\n");
        
        if (rule.getItems() != null && !rule.getItems().isEmpty()) {
            json.append(indent).append("  \"items\": [\n");
            for (int i = 0; i < rule.getItems().size(); i++) {
                MappingRules.ConversionRule item = rule.getItems().get(i);
                json.append(convertConversionRuleToJson(item, indent + "    "));
                if (i < rule.getItems().size() - 1) {
                    json.append(",");
                }
                json.append("\n");
            }
            json.append(indent).append("  ]\n");
        } else {
            json.append(indent).append("  \"items\": null\n");
        }
        
        json.append(indent).append("}");
        return json.toString();
    }
} 