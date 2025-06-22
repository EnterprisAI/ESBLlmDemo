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
     * Create a prompt for Groq
     */
    private String createGroqPrompt(String mapperName, String mapperCode) {
        return String.format("""
            You are an expert JSON rule generator for MapStruct mappers. Analyze the following MapStruct mapper interface and generate comprehensive JSON conversion rules.
            
            MAPPER: %s
            
            SOURCE CODE:
            %s
            
            REQUIREMENTS:
            1. Extract ALL @Mapping annotations and their source/target properties
            2. Identify ALL method parameters and return types to understand source and target structures
            3. Detect collections (List, Set, arrays) and mark as isArray=true
            4. Include @AfterMapping methods and their effects on target properties
            5. Consider helper classes mentioned in @Mapper(uses = {...}) and their impact
            6. Include all properties that should be mapped (both explicit and implicit)
            7. Handle nested object mappings and complex transformations
            8. Consider custom business logic in helper methods
            
            Generate a comprehensive JSON structure in this exact format:
            {
              "sourceContentType": "JSON",
              "targetContentType": "JSON",
              "conversionRules": [
                {
                  "propID": "MAIN_MAPPING",
                  "sourceLocation": "$",
                  "targetLocation": "$",
                  "isArray": false,
                  "items": [
                    {
                      "propID": "PROPERTY_NAME",
                      "sourceLocation": "sourceProperty",
                      "targetLocation": "targetProperty",
                      "isArray": false
                    }
                  ]
                }
              ]
            }
            
            IMPORTANT GUIDELINES:
            - Use actual property names from the mapper code
            - Set isArray=true for List, Set, or array types
            - Include all properties that should be mapped
            - Consider @AfterMapping methods for computed properties
            - Handle helper class transformations
            - Be comprehensive and include all mapping scenarios
            - Use descriptive propID names based on the actual mapping purpose
            
            Generate the JSON rules based on the actual mapper code provided above.
            """, mapperName, mapperCode);
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
} 