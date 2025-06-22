package com.esb.llm.ESBLlmDemo.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class EnhancedMapStructJsonRuleGeneratorService {

    @Autowired
    private ObjectMapper objectMapper;

    private final RestTemplate restTemplate = new RestTemplate();
    private static final String GROQ_BASE_URL = "https://api.groq.com/openai/v1/chat/completions";
    private static final String GROQ_API_KEY = "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";
    private static final String GROQ_MODEL = "llama3-8b-8192";
    
    // Ollama configuration
    private static final String OLLAMA_BASE_URL = "http://localhost:11434/api/generate";
    private static final String OLLAMA_MODEL = "mistral";

    /**
     * Generate JSON conversion rules from a MapStruct mapper interface
     */
    public String generateJsonRulesFromMapper(String mapperClassName) {
        try {
            Class<?> mapperClass = Class.forName(mapperClassName);
            return generateJsonRulesFromMapperClass(mapperClass);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("Mapper class not found: " + mapperClassName, e);
        }
    }

    /**
     * Generate JSON conversion rules from a MapStruct mapper class
     */
    public String generateJsonRulesFromMapperClass(Class<?> mapperClass) {
        try {
            Map<String, Object> rules = new LinkedHashMap<>();
            rules.put("sourceContentType", "JSON");
            rules.put("targetContentType", "JSON");
            
            List<Map<String, Object>> conversionRules = new ArrayList<>();
            
            // Find all mapping methods in the mapper
            List<Method> mappingMethods = findAllMappingMethods(mapperClass);
            
            for (Method method : mappingMethods) {
                Map<String, Object> mainRule = analyzeMappingMethod(method);
                if (mainRule != null) {
                    conversionRules.add(mainRule);
                }
            }
            
            rules.put("conversionRules", conversionRules);
            
            return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(rules);
        } catch (Exception e) {
            throw new RuntimeException("Error generating JSON rules from mapper: " + e.getMessage(), e);
        }
    }

    /**
     * Find all mapping methods in the mapper interface
     */
    private List<Method> findAllMappingMethods(Class<?> mapperClass) {
        List<Method> mappingMethods = new ArrayList<>();
        Method[] methods = mapperClass.getDeclaredMethods();
        
        for (Method method : methods) {
            // Skip default methods and methods without parameters
            if (method.isDefault() || method.getParameterCount() == 0) {
                continue;
            }
            
            // Include methods with @Mapping annotations or methods that return target types
            if (hasMappingAnnotations(method) || isMappingMethod(method)) {
                mappingMethods.add(method);
            }
        }
        
        return mappingMethods;
    }

    /**
     * Check if a method is a mapping method based on its signature
     */
    private boolean isMappingMethod(Method method) {
        String methodName = method.getName().toLowerCase();
        return methodName.contains("to") || methodName.contains("map") || methodName.contains("convert");
    }

    /**
     * Check if a method has @Mapping annotations
     */
    private boolean hasMappingAnnotations(Method method) {
        return method.isAnnotationPresent(Mapping.class) || 
               method.isAnnotationPresent(Mappings.class) ||
               Arrays.stream(method.getAnnotations())
                     .anyMatch(annotation -> annotation.annotationType().getSimpleName().contains("Mapping"));
    }

    /**
     * Analyze a mapping method and generate conversion rules
     */
    private Map<String, Object> analyzeMappingMethod(Method method) {
        Map<String, Object> rule = new LinkedHashMap<>();
        
        // Determine if it's an array mapping
        boolean isArray = isArrayMapping(method);
        String propId = generatePropId(method);
        
        rule.put("propID", propId);
        rule.put("sourceLocation", "$");
        rule.put("targetLocation", "$");
        rule.put("isArray", isArray);
        
        // Analyze @Mapping annotations
        List<Map<String, Object>> items = analyzeMappingAnnotations(method);
        if (!items.isEmpty()) {
            rule.put("items", items);
        }
        
        return rule;
    }

    /**
     * Check if the mapping is for an array/collection
     */
    private boolean isArrayMapping(Method method) {
        Type returnType = method.getGenericReturnType();
        
        if (returnType instanceof ParameterizedType) {
            ParameterizedType paramType = (ParameterizedType) returnType;
            return List.class.isAssignableFrom((Class<?>) paramType.getRawType()) ||
                   Collection.class.isAssignableFrom((Class<?>) paramType.getRawType());
        }
        
        return returnType instanceof Class && ((Class<?>) returnType).isArray();
    }

    /**
     * Generate a property ID for the mapping rule
     */
    private String generatePropId(Method method) {
        String methodName = method.getName();
        
        if (methodName.contains("To")) {
            String[] parts = methodName.split("To");
            if (parts.length > 0) {
                String sourceName = parts[0];
                if (sourceName.length() > 0) {
                    return sourceName.substring(0, 1).toUpperCase() + sourceName.substring(1) + "List";
                }
            }
        }
        
        return methodName.substring(0, 1).toUpperCase() + methodName.substring(1) + "List";
    }

    /**
     * Analyze @Mapping annotations and generate items
     */
    private List<Map<String, Object>> analyzeMappingAnnotations(Method method) {
        List<Map<String, Object>> items = new ArrayList<>();
        
        System.out.println("Analyzing method: " + method.getName());
        System.out.println("Method annotations: " + Arrays.toString(method.getAnnotations()));
        
        // Check for @Mappings annotation (multiple mappings)
        Mappings mappingsAnnotation = method.getAnnotation(Mappings.class);
        if (mappingsAnnotation != null) {
            System.out.println("Found @Mappings annotation with " + mappingsAnnotation.value().length + " mappings");
            for (Mapping mapping : mappingsAnnotation.value()) {
                System.out.println("Processing mapping: source='" + mapping.source() + "', target='" + mapping.target() + "'");
                Map<String, Object> item = createMappingItem(mapping);
                if (item != null) {
                    items.add(item);
                }
            }
        } else {
            System.out.println("No @Mappings annotation found");
        }
        
        // Check for individual @Mapping annotations
        Mapping[] mappings = method.getAnnotationsByType(Mapping.class);
        System.out.println("Found " + mappings.length + " individual @Mapping annotations");
        for (Mapping mapping : mappings) {
            System.out.println("Processing individual mapping: source='" + mapping.source() + "', target='" + mapping.target() + "'");
            Map<String, Object> item = createMappingItem(mapping);
            if (item != null) {
                items.add(item);
            }
        }
        
        // If no @Mapping annotations found, try to infer from method parameters and return type
        if (items.isEmpty()) {
            System.out.println("No mapping annotations found, falling back to inference");
            items = inferMappingsFromMethod(method);
        }
        
        System.out.println("Generated " + items.size() + " items from annotations");
        return items;
    }

    /**
     * Create a mapping item from a @Mapping annotation
     */
    private Map<String, Object> createMappingItem(Mapping mapping) {
        Map<String, Object> item = new LinkedHashMap<>();
        
        String source = mapping.source();
        String target = mapping.target();
        String expression = mapping.expression();
        
        // Skip if both source and target are empty
        if (source.isEmpty() && target.isEmpty() && expression.isEmpty()) {
            return null;
        }
        
        // Generate property ID
        String propId = generatePropertyId(source, target);
        item.put("propID", propId);
        
        // Set source location
        if (!expression.isEmpty()) {
            item.put("sourceLocation", "EXPRESSION: " + expression);
        } else if (!source.isEmpty()) {
            item.put("sourceLocation", source);
        } else if (!target.isEmpty()) {
            item.put("sourceLocation", target); // Fallback
        } else {
            return null; // Skip empty mappings
        }
        
        // Set target location
        item.put("targetLocation", target);
        
        // Determine if it's an array
        boolean isArray = isArrayProperty(source, target);
        item.put("isArray", isArray);
        
        // Add nested items for complex mappings
        if (isComplexMapping(source)) {
            List<Map<String, Object>> nestedItems = generateNestedItems(source);
            if (!nestedItems.isEmpty()) {
                item.put("items", nestedItems);
            }
        }
        
        return item;
    }

    /**
     * Generate a property ID from source and target
     */
    private String generatePropertyId(String source, String target) {
        if (!source.isEmpty()) {
            // Convert source path to property ID
            String[] parts = source.split("\\.");
            String lastPart = parts[parts.length - 1];
            return lastPart.toUpperCase();
        } else if (!target.isEmpty()) {
            return target.toUpperCase();
        }
        return "PROPERTY";
    }

    /**
     * Check if a property is an array/collection
     */
    private boolean isArrayProperty(String source, String target) {
        String propertyName = !target.isEmpty() ? target : source;
        
        // Check for common collection patterns (generic)
        if (propertyName.toLowerCase().contains("list") || 
            propertyName.toLowerCase().contains("array") ||
            propertyName.toLowerCase().contains("collection") ||
            propertyName.toLowerCase().contains("items") ||
            propertyName.toLowerCase().contains("elements") ||
            propertyName.toLowerCase().contains("set") ||
            propertyName.toLowerCase().contains("map")) {
            return true;
        }
        
        // Check for plural forms (generic plural detection)
        if (propertyName.endsWith("s") && !propertyName.endsWith("ss")) {
            String[] commonSingularWords = {"class", "status", "address", "process", "access", "success"};
            if (!Arrays.asList(commonSingularWords).contains(propertyName.toLowerCase())) {
                return true;
            }
        }
        
        return false;
    }

    /**
     * Check if a mapping is complex (nested object)
     */
    private boolean isComplexMapping(String source) {
        return source.contains(".") && !source.contains("List") && !source.contains("Array");
    }

    /**
     * Generate nested items for complex mappings
     */
    private List<Map<String, Object>> generateNestedItems(String source) {
        List<Map<String, Object>> items = new ArrayList<>();
        
        String[] parts = source.split("\\.");
        if (parts.length > 1) {
            for (int i = 1; i < parts.length; i++) {
                String propertyName = parts[i];
                
                Map<String, Object> item = new LinkedHashMap<>();
                item.put("propID", propertyName.toUpperCase());
                item.put("sourceLocation", propertyName);
                item.put("targetLocation", propertyName);
                item.put("isArray", false);
                
                items.add(item);
            }
        }
        
        return items;
    }

    /**
     * Infer mappings from method signature when no @Mapping annotations are present
     */
    private List<Map<String, Object>> inferMappingsFromMethod(Method method) {
        List<Map<String, Object>> items = new ArrayList<>();
        
        if (method.getParameterCount() > 0) {
            Class<?> sourceClass = method.getParameterTypes()[0];
            Class<?> targetClass = method.getReturnType();
            
            items.addAll(inferCommonFieldMappings(sourceClass, targetClass));
        }
        
        return items;
    }

    /**
     * Infer common field mappings between source and target classes
     */
    private List<Map<String, Object>> inferCommonFieldMappings(Class<?> sourceClass, Class<?> targetClass) {
        List<Map<String, Object>> items = new ArrayList<>();
        
        try {
            Method[] sourceMethods = sourceClass.getMethods();
            Method[] targetMethods = targetClass.getMethods();
            
            Set<String> sourceFields = Arrays.stream(sourceMethods)
                .filter(m -> m.getName().startsWith("get") && m.getParameterCount() == 0)
                .map(m -> m.getName().substring(3).toLowerCase())
                .collect(Collectors.toSet());
            
            Set<String> targetFields = Arrays.stream(targetMethods)
                .filter(m -> m.getName().startsWith("get") && m.getParameterCount() == 0)
                .map(m -> m.getName().substring(3).toLowerCase())
                .collect(Collectors.toSet());
            
            sourceFields.retainAll(targetFields);
            
            for (String field : sourceFields) {
                Map<String, Object> item = new LinkedHashMap<>();
                item.put("propID", field.toUpperCase());
                item.put("sourceLocation", field);
                item.put("targetLocation", field);
                item.put("isArray", false);
                items.add(item);
            }
        } catch (Exception e) {
            // If reflection fails, return empty list
        }
        
        return items;
    }

    /**
     * Read mapper source file
     */
    private String readMapperSourceFile(String mapperName) {
        try {
            String javaFileName = mapperName + ".java";
            String javaFilePath = "src/main/java/com/esb/llm/ESBLlmDemo/mapper/" + javaFileName;
            java.nio.file.Path path = java.nio.file.Paths.get(javaFilePath);
            if (!java.nio.file.Files.exists(path)) {
                throw new RuntimeException("Mapper source file not found: " + javaFilePath);
            }
            return java.nio.file.Files.readString(path);
        } catch (Exception e) {
            throw new RuntimeException("Error reading mapper source file: " + e.getMessage(), e);
        }
    }

    /**
     * Generate JSON rules using Groq API with enhanced prompt
     */
    public String generateJsonRulesWithGroq(String mapperName) {
        try {
            String mapperCode = readMapperSourceFile(mapperName);
            if (mapperCode == null) {
                throw new RuntimeException("Could not read mapper source file for: " + mapperName);
            }

            String prompt = createGroqPrompt(mapperName, mapperCode);
            
            // Debug logging
            System.out.println("=== GROQ PROMPT ===");
            System.out.println(prompt);
            System.out.println("=== END PROMPT ===");

            String groqResponse = callGroq(prompt);
            
            // Debug logging
            System.out.println("=== GROQ RESPONSE ===");
            System.out.println(groqResponse);
            System.out.println("=== END RESPONSE ===");

            return parseGroqResponse(groqResponse);
        } catch (Exception e) {
            throw new RuntimeException("Error generating JSON rules with Groq for " + mapperName, e);
        }
    }

    /**
     * Extract only the fields that are explicitly mapped via @Mapping annotations
     */
    private String extractFieldMappings(String mapperName, String mapperCode) {
        try {
            Class<?> mapperClass = Class.forName("com.esb.llm.ESBLlmDemo.mapper." + mapperName);
            StringBuilder mappings = new StringBuilder();
            Set<String> mappedFields = new HashSet<>();
            
            // Find all mapping methods
            List<Method> mappingMethods = findAllMappingMethods(mapperClass);
            
            for (Method method : mappingMethods) {
                mappings.append("Method: ").append(method.getName()).append("\n");
                
                // Extract ONLY @Mapping annotations - these are explicitly mapped
                List<Map<String, Object>> mappingItems = analyzeMappingAnnotations(method);
                if (!mappingItems.isEmpty()) {
                    mappings.append("Explicit @Mapping annotations:\n");
                    for (Map<String, Object> item : mappingItems) {
                        String source = (String) item.get("sourceLocation");
                        String target = (String) item.get("targetLocation");
                        Boolean isArray = (Boolean) item.get("isArray");
                        
                        if (source != null && target != null) {
                            mappings.append("  - ").append(source).append(" -> ").append(target);
                            if (isArray != null && isArray) {
                                mappings.append(" (ARRAY)");
                            }
                            mappings.append("\n");
                            
                            // Add to mapped fields set - ONLY explicitly mapped fields
                            mappedFields.add(source);
                            mappedFields.add(target);
                        }
                    }
                } else {
                    mappings.append("  No explicit @Mapping annotations found\n");
                }
                
                mappings.append("\n");
            }
            
            // Add summary of ONLY explicitly mapped fields
            if (!mappedFields.isEmpty()) {
                mappings.append("=== SUMMARY OF EXPLICITLY MAPPED FIELDS ===\n");
                mappings.append("Only these fields are explicitly mapped via @Mapping annotations:\n");
                for (String field : mappedFields) {
                    mappings.append("  - ").append(field).append("\n");
                }
                mappings.append("Total explicitly mapped fields: ").append(mappedFields.size()).append("\n");
                mappings.append("==========================================\n\n");
            } else {
                mappings.append("=== NO EXPLICIT MAPPINGS FOUND ===\n");
                mappings.append("No @Mapping annotations found in the mapper.\n");
                mappings.append("================================\n\n");
            }
            
            return mappings.toString();
        } catch (Exception e) {
            return "Could not extract field mappings: " + e.getMessage();
        }
    }
    
    /**
     * Create a prompt for Groq with extracted field mappings
     */
    private String createGroqPrompt(String mapperName, String mapperCode) {
        String fieldMappings = extractFieldMappings(mapperName, mapperCode);
        System.out.println("Groq prompt filed: "+ fieldMappings);
        
        return String.format("""
            You are an expert JSON rule generator for MapStruct mappers. Analyze the following MapStruct mapper interface and generate comprehensive JSON conversion rules.
            
            MAPPER: %s
            SOURCE CODE:
            %s
            
            EXTRACTED FIELD MAPPINGS:
            %s
            
            CRITICAL REQUIREMENTS:
            1. Extract ONLY @Mapping annotations and their source/target properties
            2. Identify method parameters and return types to understand source and target structures
            3. Detect collections (List, Set, arrays) and mark as isArray=true
            4. **MOST IMPORTANT**: Include ONLY properties that have explicit @Mapping annotations:
               - ONLY fields with @Mapping(source = "...", target = "...") annotations
               - **DO NOT include fields that exist in source/target but are not explicitly mapped**
               - **DO NOT include fields from @AfterMapping methods unless they have explicit @Mapping**
               - **DO NOT include fields from helper classes unless they have explicit @Mapping**
            5. Handle nested object mappings and complex transformations
            6. **ONLY create mapping rules for explicitly mapped fields**
            
            FIELD DETECTION STRATEGY:
            - Analyze the mapper interface to identify source and target classes
            - Look for fields that are EXPLICITLY mapped with @Mapping annotations ONLY
            - **IGNORE fields that are referenced in @AfterMapping methods but not explicitly mapped**
            - **IGNORE fields that are used in helper class calculations but not explicitly mapped**
            - **ONLY include fields that have explicit @Mapping annotations**
            - **CREATE SEPARATE MAPPINGS for each explicitly mapped field**
            
            Generate a comprehensive JSON structure in this exact format:
            {
              "sourceContentType": "com.esb.llm.ESBLlmDemo.model.SourceClassName",
              "targetContentType": "com.esb.llm.ESBLlmDemo.model.TargetClassName",
              "conversionRules": [
                {
                  "propID": "PROPERTY_NAME_MAPPING",
                  "sourceLocation": "sourceProperty",
                  "targetLocation": "targetProperty",
                  "isArray": false
                }
              ]
            }
            
            IMPORTANT GUIDELINES:
            - Use actual property names from the @Mapping annotations ONLY
            - Set isArray=true for List, Set, or array types
            - **INCLUDE ONLY FIELDS that have explicit @Mapping annotations**
            - **IGNORE @AfterMapping methods unless fields are explicitly mapped**
            - **IGNORE helper class transformations unless fields are explicitly mapped**
            - Be comprehensive but only for explicitly mapped fields
            - Use descriptive propID names based on the actual mapping purpose
            - **DO NOT include fields that exist in source/target but are not explicitly mapped**
            - **CREATE SEPARATE MAPPING RULES for each explicitly mapped field**
            - Use the actual class names from the mapper interface
            
            ANALYSIS STEPS:
            1. Identify the source class from method parameters
            2. Identify the target class from method return types
            3. Extract all @Mapping annotations and their source/target properties (see extracted mappings)
            4. **IGNORE @AfterMapping methods unless they have explicit @Mapping annotations**
            5. **IGNORE helper classes unless they have explicit @Mapping annotations**
            6. Detect collections and nested objects
            7. Generate separate mapping rules for each EXPLICITLY MAPPED field only
            
            Generate the JSON rules based on the actual mapper code and extracted mappings provided above, ensuring ONLY fields with explicit @Mapping annotations are included as separate mappings.
            """, mapperName, mapperCode, fieldMappings);
    }

    /**
     * Call Groq API
     */
    private String callGroq(String prompt) {
        try {
            // Create headers with API key
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(GROQ_API_KEY);
            
            // Create request body in Groq format
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("model", GROQ_MODEL);
            requestBody.put("messages", Arrays.asList(
                Map.of("role", "user", "content", prompt)
            ));
            requestBody.put("temperature", 0.1);
            requestBody.put("max_tokens", 4000);
            
            HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);
            
            ResponseEntity<Map> response = restTemplate.postForEntity(GROQ_BASE_URL, request, Map.class);
            
            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                Map<String, Object> body = response.getBody();
                if (body.containsKey("choices")) {
                    List<Map<String, Object>> choices = (List<Map<String, Object>>) body.get("choices");
                    if (!choices.isEmpty()) {
                        Map<String, Object> choice = choices.get(0);
                        if (choice.containsKey("message")) {
                            Map<String, Object> message = (Map<String, Object>) choice.get("message");
                            if (message.containsKey("content")) {
                                return (String) message.get("content");
                            }
                        }
                    }
                }
            }
            
            return "Error: No valid response from Groq";
        } catch (Exception e) {
            return "Error calling Groq: " + e.getMessage();
        }
    }

    /**
     * Parse Groq response
     */
    private String parseGroqResponse(String response) {
        int startIndex = response.indexOf("{");
        int endIndex = response.lastIndexOf("}") + 1;
        
        if (startIndex >= 0 && endIndex > startIndex) {
            return response.substring(startIndex, endIndex);
        }
        
        return response;
    }

    /**
     * Generate JSON rules for a specific mapper by name
     */
    public String generateJsonRulesForMapper(String mapperName) {
        String mapperClassName = "com.esb.llm.ESBLlmDemo.mapper." + mapperName;
        return generateJsonRulesFromMapper(mapperClassName);
    }

    /**
     * Get list of available mappers in the project
     */
    public List<String> getAvailableMappers() {
        return Arrays.asList(
            "EmployeeMapper",
            "ProductMapper", 
            "OrderMapper",
            "UserMapper",
            "SourceTargetMapper"
        );
    }

    /**
     * Generate JSON rules for all available mappers
     */
    public Map<String, String> generateJsonRulesForAllMappers() {
        Map<String, String> allRules = new LinkedHashMap<>();
        
        for (String mapperName : getAvailableMappers()) {
            try {
                String rules = generateJsonRulesForMapper(mapperName);
                allRules.put(mapperName, rules);
            } catch (Exception e) {
                allRules.put(mapperName, "Error: " + e.getMessage());
            }
        }
        
        return allRules;
    }

    /**
     * Generate JSON rules for a mapper by class name, reading the source code and using Groq
     */
    public String generateJsonRulesWithGroqByClassName(String mapperName) {
        try {
            return generateJsonRulesWithGroq(mapperName);
        } catch (Exception e) {
            throw new RuntimeException("Error reading mapper source or generating rules: " + e.getMessage(), e);
        }
    }

    /**
     * Generate JSON rules using Ollama API with enhanced prompt
     */
    public String generateJsonRulesWithOllama(String mapperName) {
        try {
            String mapperCode = readMapperSourceFile(mapperName);
            if (mapperCode == null) {
                throw new RuntimeException("Could not read mapper source file for: " + mapperName);
            }

            String prompt = createOllamaPrompt(mapperName, mapperCode);
            
            // Debug logging
            System.out.println("=== OLLAMA PROMPT ===");
            System.out.println(prompt);
            System.out.println("=== END PROMPT ===");

            String ollamaResponse = callOllama(prompt);
            
            // Debug logging
            System.out.println("=== OLLAMA RESPONSE ===");
            System.out.println(ollamaResponse);
            System.out.println("=== END RESPONSE ===");

            return parseOllamaResponse(ollamaResponse);
        } catch (Exception e) {
            throw new RuntimeException("Error generating JSON rules with Ollama for " + mapperName, e);
        }
    }

    /**
     * Create a prompt for Ollama - optimized for Mistral model with extracted field mappings
     */
    private String createOllamaPrompt(String mapperName, String mapperCode) {
        String fieldMappings = extractFieldMappings(mapperName, mapperCode);
        
        return String.format("""
Extract mapping rules from this MapStruct mapper:

MAPPER: %s
CODE:
%s

EXTRACTED FIELD MAPPINGS:
%s

INSTRUCTIONS:
1. Find ONLY @Mapping annotations and their source/target properties (see extracted mappings above)
2. **ONLY include fields that have explicit @Mapping annotations**
3. Create ONE mapping rule for EACH explicitly mapped field

IMPORTANT: Use the extracted field mappings above to ensure you include ONLY the fields that have explicit @Mapping annotations. The extracted mappings show the exact source->target field relationships found in the code. DO NOT include fields that exist in source/target classes but are not explicitly mapped. DO NOT include fields from @AfterMapping methods unless they have explicit @Mapping annotations.

OUTPUT FORMAT - Return ONLY this complete JSON structure:
{
  "sourceContentType": "FULL_SOURCE_CLASS_NAME",
  "targetContentType": "FULL_TARGET_CLASS_NAME",
  "conversionRules": [
    {
      "propID": "sourceFieldName_mapping",
      "sourceLocation": "sourceFieldName",
      "targetLocation": "targetFieldName",
      "isArray": false
    }
  ]
}

CRITICAL: Return ONLY the complete JSON. Do not include any explanations, partial results, or multiple JSON blocks. Include ONLY fields from explicit @Mapping annotations as shown in the extracted mappings above. DO NOT include unmapped fields or fields from @AfterMapping methods without explicit @Mapping annotations.
""", mapperName, mapperCode, fieldMappings);
    }

    /**
     * Call Ollama API
     */
    private String callOllama(String prompt) {
        try {
            // Create headers
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            
            // Create request body in Ollama format
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("model", OLLAMA_MODEL);
            requestBody.put("prompt", prompt);
            requestBody.put("stream", false);
            
            HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);
            
            ResponseEntity<Map> response = restTemplate.postForEntity(OLLAMA_BASE_URL, request, Map.class);
            
            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                Map<String, Object> body = response.getBody();
                if (body.containsKey("response")) {
                    return (String) body.get("response");
                }
            }
            
            return "Error: No valid response from Ollama";
        } catch (Exception e) {
            return "Error calling Ollama: " + e.getMessage();
        }
    }

    /**
     * Parse Ollama response
     */
    private String parseOllamaResponse(String response) {
        int startIndex = response.indexOf("{");
        int endIndex = response.lastIndexOf("}") + 1;
        
        if (startIndex >= 0 && endIndex > startIndex) {
            return response.substring(startIndex, endIndex);
        }
        
        return response;
    }

    /**
     * Generate JSON rules for a mapper by class name, reading the source code and using Ollama
     */
    public String generateJsonRulesWithOllamaByClassName(String mapperName) {
        try {
            return generateJsonRulesWithOllama(mapperName);
        } catch (Exception e) {
            throw new RuntimeException("Error reading mapper source or generating rules: " + e.getMessage(), e);
        }
    }
} 