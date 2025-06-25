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

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.*;
import java.util.stream.Collectors;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.AnnotationExpr;
import com.github.javaparser.ast.expr.MemberValuePair;
import com.github.javaparser.ast.expr.StringLiteralExpr;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

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
            String prompt = createGroqPrompt(mapperName, null);
            
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
     * Extract only the fields that are explicitly mapped via @Mapping annotations using JavaParser
     */
    private String extractFieldMappings(String mapperName) {
        try {
            System.out.println("=== EXTRACTING FIELD MAPPINGS FOR: " + mapperName + " ===");
            
            // Find the source file for the mapper
            String sourceFilePath = findMapperSourceFile(mapperName);
            System.out.println("Source file path: " + sourceFilePath);
            
            if (sourceFilePath == null) {
                System.out.println("Could not find source file for mapper: " + mapperName);
                return "NO EXPLICIT MAPPINGS FOUND\nCould not locate source file for mapper: " + mapperName;
            }
            
            // Parse the source file
            System.out.println("Parsing source file with JavaParser...");
            CompilationUnit cu = StaticJavaParser.parse(new File(sourceFilePath));
            System.out.println("Successfully parsed source file");
            
            // Extract mappings from the parsed code
            MappingExtractor extractor = new MappingExtractor();
            extractor.visit(cu, null);
            
            List<String> mappingPairs = extractor.getMappingPairs();
            System.out.println("Found " + mappingPairs.size() + " mapping pairs");
            
            if (mappingPairs.isEmpty()) {
                System.out.println("=== EXTRACTED FIELD MAPPINGS RESULT ===");
                System.out.println("NO EXPLICIT MAPPINGS FOUND");
                System.out.println("No @Mapping annotations found in the mapper.");
                System.out.println("=== END EXTRACTED FIELD MAPPINGS ===");
                return "NO EXPLICIT MAPPINGS FOUND\nNo @Mapping annotations found in the mapper.";
            }
            
            StringBuilder result = new StringBuilder();
            result.append("EXACT MAPPINGS (from @Mapping annotations):\n");
            for (String pair : mappingPairs) {
                result.append(pair).append("\n");
            }
            
            System.out.println("=== EXTRACTED FIELD MAPPINGS RESULT ===");
            System.out.println(result.toString());
            System.out.println("=== END EXTRACTED FIELD MAPPINGS ===");
            
            return result.toString();
            
        } catch (Exception e) {
            System.err.println("Error extracting field mappings: " + e.getMessage());
            e.printStackTrace();
            return "ERROR: " + e.getMessage();
        }
    }
    
    /**
     * Find the source file for the given mapper name
     */
    private String findMapperSourceFile(String mapperName) {
        // Try to find the source file in the standard Maven/Gradle structure
        String[] possiblePaths = {
            "src/main/java/com/esb/llm/ESBLlmDemo/mapper/" + mapperName + ".java",
            "src/main/java/com/esb/llm/ESBLlmDemo/mapper/" + mapperName + ".java"
        };
        
        for (String path : possiblePaths) {
            File file = new File(path);
            if (file.exists()) {
                return file.getAbsolutePath();
            }
        }
        
        // If not found, try to search in the project directory
        try {
            Path projectRoot = Paths.get(".").toAbsolutePath().normalize();
            Path mapperPath = projectRoot.resolve("src/main/java/com/esb/llm/ESBLlmDemo/mapper/" + mapperName + ".java");
            if (Files.exists(mapperPath)) {
                return mapperPath.toString();
            }
        } catch (Exception e) {
            System.err.println("Error searching for mapper file: " + e.getMessage());
        }
        
        return null;
    }
    
    /**
     * JavaParser visitor to extract @Mapping annotations
     */
    private static class MappingExtractor extends VoidVisitorAdapter<Void> {
        private List<String> mappingPairs = new ArrayList<>();
        
        @Override
        public void visit(MethodDeclaration md, Void arg) {
            // Check for @Mapping annotations on the method
            for (AnnotationExpr annotation : md.getAnnotations()) {
                if (annotation.getNameAsString().equals("Mapping")) {
                    String source = "";
                    String target = "";
                    
                    // Handle @Mapping annotation with multiple members
                    if (annotation.isNormalAnnotationExpr()) {
                        var normalAnnotation = annotation.asNormalAnnotationExpr();
                        for (var pair : normalAnnotation.getPairs()) {
                            if (pair.getNameAsString().equals("source")) {
                                source = pair.getValue().asStringLiteralExpr().getValue();
                            } else if (pair.getNameAsString().equals("target")) {
                                target = pair.getValue().asStringLiteralExpr().getValue();
                            }
                        }
                    }
                    
                    if (!source.isEmpty() && !target.isEmpty()) {
                        mappingPairs.add(source + " -> " + target);
                    }
                }
            }
            
            super.visit(md, arg);
        }
        
        public List<String> getMappingPairs() {
            return mappingPairs;
        }
    }

    /**
     * Create a prompt for Groq with extracted field mappings (without full source code)
     */
    private String createGroqPrompt(String mapperName, String mapperCode) {
        String fieldMappings = extractFieldMappings(mapperName);
        System.out.println("=== EXTRACTED FIELD MAPPINGS FOR GROQ ===");
        System.out.println(fieldMappings);
        System.out.println("=== END EXTRACTED FIELD MAPPINGS ===");
        
        return String.format("""
            You are an expert JSON rule generator for MapStruct mappers. Analyze the following MapStruct mapper and generate comprehensive JSON conversion rules.
            
            MAPPER: %s
            
            EXTRACTED FIELD MAPPINGS:
            %s
            
            CRITICAL REQUIREMENTS:
            1. Extract ONLY @Mapping annotations and their source/target properties
            2. **MOST IMPORTANT**: Include ONLY properties that have explicit @Mapping annotations:
               - ONLY fields with @Mapping(source = "...", target = "...") annotations
               - **DO NOT include fields that exist in source/target but are not explicitly mapped**
               - **DO NOT include fields from @AfterMapping methods unless they have explicit @Mapping**
               - **DO NOT include fields from helper classes unless they have explicit @Mapping**
            3. Handle collections (List, Set, arrays) and mark as isArray=true
            4. **ONLY create mapping rules for explicitly mapped fields**
            
            FIELD DETECTION STRATEGY:
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
            1. Extract all @Mapping annotations and their source/target properties (see extracted mappings)
            2. **IGNORE @AfterMapping methods unless they have explicit @Mapping annotations**
            3. **IGNORE helper classes unless they have explicit @Mapping annotations**
            4. Detect collections and nested objects
            5. Generate separate mapping rules for each EXPLICITLY MAPPED field only
            
            Generate the JSON rules based on the extracted mappings provided above, ensuring ONLY fields with explicit @Mapping annotations are included as separate mappings.
            """, mapperName, fieldMappings);
    }

    /**
     * Call Groq API
     */
    public String callGroq(String prompt) {
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
            String prompt = createOllamaPrompt(mapperName, null);
            
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
     * Create a prompt for Ollama - optimized for Mistral model with extracted field mappings (without full source code)
     */
    private String createOllamaPrompt(String mapperName, String mapperCode) {
        String fieldMappings = extractFieldMappings(mapperName);
        System.out.println("=== EXTRACTED FIELD MAPPINGS FOR OLLAMA ===");
        System.out.println(fieldMappings);
        System.out.println("=== END EXTRACTED FIELD MAPPINGS ===");
        
        return String.format("""
Extract mapping rules from this MapStruct mapper:

MAPPER: %s

EXTRACTED FIELD MAPPINGS:
%s

CRITICAL INSTRUCTIONS:
1. ONLY use the source/target field pairs listed in the 'EXACT MAPPINGS' section above.
2. DO NOT include any fields that are NOT listed in the exact mappings.
3. The exact mappings show EXACTLY which fields have explicit @Mapping annotations.
4. If the exact mappings section is '(none)', return an empty conversionRules array.

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

MANDATORY RULES:
- ONLY include fields that appear in the exact mappings above.
- If exact mappings show '(none)', return empty conversionRules: [].
- DO NOT include fields from @AfterMapping methods unless they appear in exact mappings.
- DO NOT include fields that exist in source/target classes but are not in exact mappings.
- Use the exact field names from the exact mappings.

CRITICAL: Return ONLY the complete JSON. Do not include any explanations, partial results, or multiple JSON blocks. Include ONLY fields from the exact mappings above.
""", mapperName, fieldMappings);
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

    /**
     * Extract custom mappings and their linked methods from mapper interfaces
     * This method identifies custom mapping methods and traces their method chains
     */
    public String extractCustomMappings(String mapperName) {
        try {
            System.out.println("=== EXTRACTING CUSTOM MAPPINGS FOR: " + mapperName + " ===");
            
            // Find the source file for the mapper
            String sourceFilePath = findMapperSourceFile(mapperName);
            System.out.println("Source file path: " + sourceFilePath);
            
            if (sourceFilePath == null) {
                System.out.println("Could not find source file for mapper: " + mapperName);
                return "NO CUSTOM MAPPINGS FOUND\nCould not locate source file for mapper: " + mapperName;
            }
            
            // Parse the source file
            System.out.println("Parsing source file with JavaParser...");
            CompilationUnit cu = StaticJavaParser.parse(new File(sourceFilePath));
            System.out.println("Successfully parsed source file");
            
            // Extract custom mappings from the parsed code
            CustomMappingExtractor extractor = new CustomMappingExtractor();
            extractor.visit(cu, null);
            
            List<CustomMappingInfo> customMappings = extractor.getCustomMappings();
            System.out.println("Found " + customMappings.size() + " custom mappings");
            
            // Find helper classes and extract their methods
            List<String> helperClasses = extractor.getHelperClasses();
            Map<String, List<String>> helperMethods = new HashMap<>();
            
            for (String helperClass : helperClasses) {
                List<String> methods = extractHelperClassMethods(helperClass);
                helperMethods.put(helperClass, methods);
            }
            
            if (customMappings.isEmpty()) {
                System.out.println("=== EXTRACTED CUSTOM MAPPINGS RESULT ===");
                System.out.println("NO CUSTOM MAPPINGS FOUND");
                System.out.println("No custom mapping methods found in the mapper.");
                System.out.println("=== END EXTRACTED CUSTOM MAPPINGS ===");
                return "NO CUSTOM MAPPINGS FOUND\nNo custom mapping methods found in the mapper.";
            }
            
            StringBuilder result = new StringBuilder();
            result.append("CUSTOM MAPPINGS (with linked methods):\n");
            result.append("=====================================\n\n");
            
            for (CustomMappingInfo mapping : customMappings) {
                result.append("CUSTOM MAPPING: ").append(mapping.getMappingName()).append("\n");
                result.append("Target Field: ").append(mapping.getTargetField()).append("\n");
                result.append("Main Method: ").append(mapping.getMainMethod()).append("\n");
                result.append("Method Chain:\n");
                
                for (String method : mapping.getMethodChain()) {
                    result.append("  -> ").append(method).append("\n");
                }
                
                result.append("Method Definitions:\n");
                for (String methodDef : mapping.getMethodDefinitions()) {
                    result.append("  ").append(methodDef).append("\n");
                }
                result.append("\n");
            }
            
            // Add helper class information
            if (!helperMethods.isEmpty()) {
                result.append("HELPER CLASSES AND METHODS:\n");
                result.append("===========================\n\n");
                
                for (Map.Entry<String, List<String>> entry : helperMethods.entrySet()) {
                    result.append("Helper Class: ").append(entry.getKey()).append("\n");
                    result.append("Methods:\n");
                    for (String method : entry.getValue()) {
                        result.append("  ").append(method).append("\n");
                    }
                    result.append("\n");
                }
            }
            
            System.out.println("=== EXTRACTED CUSTOM MAPPINGS RESULT ===");
            System.out.println(result.toString());
            System.out.println("=== END EXTRACTED CUSTOM MAPPINGS ===");
            
            return result.toString();
            
        } catch (Exception e) {
            System.err.println("Error extracting custom mappings: " + e.getMessage());
            e.printStackTrace();
            return "ERROR: " + e.getMessage();
        }
    }

    /**
     * Extract methods from a helper class
     */
    private List<String> extractHelperClassMethods(String helperClassName) {
        List<String> methods = new ArrayList<>();
        try {
            String helperFilePath = findHelperClassSourceFile(helperClassName);
            if (helperFilePath != null) {
                CompilationUnit cu = StaticJavaParser.parse(new File(helperFilePath));
                HelperMethodExtractor extractor = new HelperMethodExtractor();
                extractor.visit(cu, null);
                methods = extractor.getMethods();
            }
        } catch (Exception e) {
            System.err.println("Error extracting methods from helper class " + helperClassName + ": " + e.getMessage());
        }
        return methods;
    }

    /**
     * Find the source file for a helper class
     */
    private String findHelperClassSourceFile(String helperClassName) {
        String[] possiblePaths = {
            "src/main/java/com/esb/llm/ESBLlmDemo/mapper/" + helperClassName + ".java",
            "src/main/java/com/esb/llm/ESBLlmDemo/service/" + helperClassName + ".java",
            "src/main/java/com/esb/llm/ESBLlmDemo/util/" + helperClassName + ".java"
        };
        
        for (String path : possiblePaths) {
            File file = new File(path);
            if (file.exists()) {
                return file.getAbsolutePath();
            }
        }
        
        return null;
    }

    /**
     * JavaParser visitor to extract methods from helper classes
     */
    private static class HelperMethodExtractor extends VoidVisitorAdapter<Void> {
        private List<String> methods = new ArrayList<>();
        
        @Override
        public void visit(MethodDeclaration md, Void arg) {
            String methodName = md.getNameAsString();
            String methodBody = md.getBody().map(body -> body.toString()).orElse("");
            methods.add(methodName + ": " + methodBody);
            super.visit(md, arg);
        }
        
        public List<String> getMethods() {
            return methods;
        }
    }

    /**
     * JavaParser visitor to extract custom mappings and their method chains
     */
    private static class CustomMappingExtractor extends VoidVisitorAdapter<Void> {
        private List<CustomMappingInfo> customMappings = new ArrayList<>();
        private Map<String, String> methodDefinitions = new HashMap<>();
        private List<String> helperClasses = new ArrayList<>();
        private Set<String> methodsReferencedInExpressions = new HashSet<>();
        
        @Override
        public void visit(MethodDeclaration md, Void arg) {
            String methodName = md.getNameAsString();
            String methodBody = md.getBody().map(body -> body.toString()).orElse("");
            
            // Store method definition
            methodDefinitions.put(methodName, methodBody);
            
            // Check for @Mapping annotations with expressions
            for (AnnotationExpr annotation : md.getAnnotations()) {
                if (annotation.getNameAsString().equals("Mapping")) {
                    String target = "";
                    String expression = "";
                    
                    // Handle @Mapping annotation with multiple members
                    if (annotation.isNormalAnnotationExpr()) {
                        var normalAnnotation = annotation.asNormalAnnotationExpr();
                        for (var pair : normalAnnotation.getPairs()) {
                            if (pair.getNameAsString().equals("target")) {
                                target = pair.getValue().asStringLiteralExpr().getValue();
                            } else if (pair.getNameAsString().equals("expression")) {
                                expression = pair.getValue().asStringLiteralExpr().getValue();
                            }
                        }
                    }
                    
                    // If this is a custom mapping with expression
                    if (!target.isEmpty() && !expression.isEmpty() && expression.contains("java(")) {
                        String referencedMethod = extractMethodFromExpression(expression);
                        if (!referencedMethod.isEmpty()) {
                            methodsReferencedInExpressions.add(referencedMethod);
                        }
                        
                        CustomMappingInfo mapping = new CustomMappingInfo();
                        mapping.setMappingName(md.getNameAsString());
                        mapping.setTargetField(target);
                        mapping.setMainMethod(referencedMethod);
                        
                        // Find method chain
                        List<String> methodChain = findMethodChain(referencedMethod, methodDefinitions);
                        mapping.setMethodChain(methodChain);
                        
                        // Get method definitions
                        List<String> methodDefs = new ArrayList<>();
                        for (String method : methodChain) {
                            if (methodDefinitions.containsKey(method)) {
                                methodDefs.add(method + ": " + methodDefinitions.get(method));
                            }
                        }
                        mapping.setMethodDefinitions(methodDefs);
                        
                        customMappings.add(mapping);
                    }
                }
            }
            
            // Check for helper class usage in method body
            if (!methodBody.isEmpty()) {
                List<String> foundHelpers = findHelperClassesInMethod(methodBody);
                helperClasses.addAll(foundHelpers);
            }
            
            super.visit(md, arg);
        }
        
        @Override
        public void visit(com.github.javaparser.ast.body.ClassOrInterfaceDeclaration cd, Void arg) {
            // Check for @Mapper annotation to find helper classes
            for (AnnotationExpr annotation : cd.getAnnotations()) {
                if (annotation.getNameAsString().equals("Mapper")) {
                    if (annotation.isNormalAnnotationExpr()) {
                        var normalAnnotation = annotation.asNormalAnnotationExpr();
                        for (var pair : normalAnnotation.getPairs()) {
                            if (pair.getNameAsString().equals("uses")) {
                                // Extract helper classes from uses parameter
                                String usesValue = pair.getValue().toString();
                                if (usesValue.contains("UserMapperHelp")) {
                                    helperClasses.add("UserMapperHelp");
                                }
                            }
                        }
                    }
                }
            }
            super.visit(cd, arg);
        }
        
        public List<CustomMappingInfo> getCustomMappings() {
            return customMappings;
        }
        
        public List<String> getHelperClasses() {
            return helperClasses.stream().distinct().collect(Collectors.toList());
        }
        
        private String extractMethodFromExpression(String expression) {
            // Extract method name from java() expression
            if (expression.contains("java(")) {
                String javaCode = expression.substring(expression.indexOf("java(") + 5, expression.lastIndexOf(")"));
                // Find method call pattern
                String[] parts = javaCode.split("\\.");
                if (parts.length > 0) {
                    String lastPart = parts[parts.length - 1];
                    if (lastPart.contains("(")) {
                        return lastPart.substring(0, lastPart.indexOf("("));
                    }
                }
            }
            return "";
        }
        
        private List<String> findMethodChain(String startMethod, Map<String, String> methodDefs) {
            List<String> chain = new ArrayList<>();
            Set<String> visited = new HashSet<>();
            
            findMethodChainRecursive(startMethod, methodDefs, chain, visited);
            return chain;
        }
        
        private void findMethodChainRecursive(String methodName, Map<String, String> methodDefs, 
                                            List<String> chain, Set<String> visited) {
            if (visited.contains(methodName) || !methodDefs.containsKey(methodName)) {
                return;
            }
            
            visited.add(methodName);
            chain.add(methodName);
            
            String methodBody = methodDefs.get(methodName);
            
            // Find method calls in the body
            String[] lines = methodBody.split("\n");
            for (String line : lines) {
                // Look for method calls (simplified pattern matching)
                if (line.contains("(") && line.contains(")") && !line.contains("if") && !line.contains("for")) {
                    String[] words = line.split("\\s+");
                    for (String word : words) {
                        if (word.contains("(") && word.contains(")") && !word.startsWith("(")) {
                            String potentialMethod = word.substring(0, word.indexOf("("));
                            if (methodDefs.containsKey(potentialMethod) && !visited.contains(potentialMethod)) {
                                findMethodChainRecursive(potentialMethod, methodDefs, chain, visited);
                            }
                        }
                    }
                }
            }
        }
        
        private List<String> findHelperClassesInMethod(String methodBody) {
            List<String> helpers = new ArrayList<>();
            
            // Look for patterns like "new UserMapperHelp()" or "helper.calculateAdjustedSalary"
            String[] lines = methodBody.split("\n");
            for (String line : lines) {
                // Pattern for "new ClassName()"
                if (line.contains("new ") && line.contains("()")) {
                    String[] parts = line.split("new ");
                    for (String part : parts) {
                        if (part.contains("()")) {
                            String className = part.substring(0, part.indexOf("("));
                            if (className.contains(".")) {
                                className = className.substring(className.lastIndexOf(".") + 1);
                            }
                            if (className.endsWith("Help") || className.endsWith("Helper") || 
                                className.endsWith("Mapper") || className.endsWith("Util")) {
                                helpers.add(className);
                            }
                        }
                    }
                }
                
                // Pattern for "helper.methodName" or "className.methodName"
                if (line.contains(".") && line.contains("(")) {
                    String[] parts = line.split("\\.");
                    if (parts.length > 1) {
                        String potentialHelper = parts[0].trim();
                        if (potentialHelper.endsWith("Help") || potentialHelper.endsWith("Helper") || 
                            potentialHelper.endsWith("Mapper") || potentialHelper.endsWith("Util")) {
                            helpers.add(potentialHelper);
                        }
                    }
                }
            }
            
            return helpers;
        }
    }
    
    /**
     * Data class to hold custom mapping information
     */
    private static class CustomMappingInfo {
        private String mappingName;
        private String targetField;
        private String mainMethod;
        private List<String> methodChain;
        private List<String> methodDefinitions;
        
        // Getters and setters
        public String getMappingName() { return mappingName; }
        public void setMappingName(String mappingName) { this.mappingName = mappingName; }
        
        public String getTargetField() { return targetField; }
        public void setTargetField(String targetField) { this.targetField = targetField; }
        
        public String getMainMethod() { return mainMethod; }
        public void setMainMethod(String mainMethod) { this.mainMethod = mainMethod; }
        
        public List<String> getMethodChain() { return methodChain; }
        public void setMethodChain(List<String> methodChain) { this.methodChain = methodChain; }
        
        public List<String> getMethodDefinitions() { return methodDefinitions; }
        public void setMethodDefinitions(List<String> methodDefinitions) { this.methodDefinitions = methodDefinitions; }
    }

    /**
     * Create a comprehensive prompt that includes both direct mappings and custom mappings
     */
    public String createComprehensivePrompt(String mapperName) {
        try {
            String directMappings = extractFieldMappings(mapperName);
            String customMappings = extractCustomMappings(mapperName);
            
            StringBuilder prompt = new StringBuilder();
            prompt.append("You are an expert JSON rule generator for MapStruct mappers. Analyze the following MapStruct mapper and generate comprehensive JSON conversion rules.\n\n");
            prompt.append("MAPPER: ").append(mapperName).append("\n\n");
            
            prompt.append("DIRECT MAPPINGS (from @Mapping annotations):\n");
            prompt.append("===========================================\n");
            prompt.append(directMappings).append("\n\n");
            
            prompt.append("CUSTOM MAPPINGS (with linked methods):\n");
            prompt.append("=====================================\n");
            prompt.append(customMappings).append("\n\n");
            
            prompt.append("CRITICAL REQUIREMENTS:\n");
            prompt.append("1. Extract ONLY @Mapping annotations and their source/target properties\n");
            prompt.append("2. **MOST IMPORTANT**: Include ONLY properties that have explicit @Mapping annotations:\n");
            prompt.append("   - ONLY fields with @Mapping(source = \"...\", target = \"...\") annotations\n");
            prompt.append("   - **DO NOT include fields that exist in source/target but are not explicitly mapped**\n");
            prompt.append("   - **DO NOT include fields from @AfterMapping methods unless they have explicit @Mapping**\n");
            prompt.append("   - **DO NOT include fields from helper classes unless they have explicit @Mapping**\n");
            prompt.append("3. Handle collections (List, Set, arrays) and mark as isArray=true\n");
            prompt.append("4. **ONLY create mapping rules for explicitly mapped fields**\n");
            prompt.append("5. **For custom mappings, include the custom logic as method definitions**\n\n");
            
            prompt.append("FIELD DETECTION STRATEGY:\n");
            prompt.append("- Look for fields that are EXPLICITLY mapped with @Mapping annotations ONLY\n");
            prompt.append("- **IGNORE fields that are referenced in @AfterMapping methods but not explicitly mapped**\n");
            prompt.append("- **IGNORE fields that are used in helper class calculations but not explicitly mapped**\n");
            prompt.append("- **ONLY include fields that have explicit @Mapping annotations**\n");
            prompt.append("- **CREATE SEPARATE MAPPINGS for each explicitly mapped field**\n");
            prompt.append("- **For custom mappings, include the method chain and definitions**\n\n");
            
            prompt.append("Generate a comprehensive JSON structure in this exact format:\n");
            prompt.append("{\n");
            prompt.append("  \"sourceContentType\": \"com.esb.llm.ESBLlmDemo.model.SourceClassName\",\n");
            prompt.append("  \"targetContentType\": \"com.esb.llm.ESBLlmDemo.model.TargetClassName\",\n");
            prompt.append("  \"conversionRules\": [\n");
            prompt.append("    {\n");
            prompt.append("      \"propID\": \"PROPERTY_NAME_MAPPING\",\n");
            prompt.append("      \"sourceLocation\": \"sourceProperty\",\n");
            prompt.append("      \"targetLocation\": \"targetProperty\",\n");
            prompt.append("      \"isArray\": false\n");
            prompt.append("    }\n");
            prompt.append("  ],\n");
            prompt.append("  \"customMappings\": [\n");
            prompt.append("    {\n");
            prompt.append("      \"targetField\": \"fieldName\",\n");
            prompt.append("      \"methodChain\": [\"method1\", \"method2\"],\n");
            prompt.append("      \"methodDefinitions\": [\"method1: body\", \"method2: body\"]\n");
            prompt.append("    }\n");
            prompt.append("  ]\n");
            prompt.append("}\n\n");
            
            prompt.append("IMPORTANT GUIDELINES:\n");
            prompt.append("- Use actual property names from the @Mapping annotations ONLY\n");
            prompt.append("- Set isArray=true for List, Set, or array types\n");
            prompt.append("- **INCLUDE ONLY FIELDS that have explicit @Mapping annotations**\n");
            prompt.append("- **IGNORE @AfterMapping methods unless fields are explicitly mapped**\n");
            prompt.append("- **IGNORE helper class transformations unless fields are explicitly mapped**\n");
            prompt.append("- Be comprehensive but only for explicitly mapped fields\n");
            prompt.append("- Use descriptive propID names based on the actual mapping purpose\n");
            prompt.append("- **DO NOT include fields that exist in source/target but are not explicitly mapped**\n");
            prompt.append("- **CREATE SEPARATE MAPPING RULES for each explicitly mapped field**\n");
            prompt.append("- Use the actual class names from the mapper interface\n");
            prompt.append("- **For custom mappings, include the complete method chain and definitions**\n\n");
            
            prompt.append("ANALYSIS STEPS:\n");
            prompt.append("1. Extract all @Mapping annotations and their source/target properties (see direct mappings)\n");
            prompt.append("2. **IGNORE @AfterMapping methods unless they have explicit @Mapping annotations**\n");
            prompt.append("3. **IGNORE helper classes unless they have explicit @Mapping annotations**\n");
            prompt.append("4. Detect collections and nested objects\n");
            prompt.append("5. Generate separate mapping rules for each EXPLICITLY MAPPED field only\n");
            prompt.append("6. **Include custom mappings with their method chains and definitions**\n\n");
            
            prompt.append("Generate the JSON rules based on the extracted mappings provided above, ensuring ONLY fields with explicit @Mapping annotations are included as separate mappings, and custom mappings are properly documented with their method chains.");
            
            return prompt.toString();
            
        } catch (Exception e) {
            System.err.println("Error creating comprehensive prompt: " + e.getMessage());
            e.printStackTrace();
            return "ERROR: " + e.getMessage();
        }
    }

    /**
     * Enhanced custom mapping extraction that also finds default methods with custom logic
     */
    public String extractCustomMappingsEnhanced(String mapperName) {
        try {
            System.out.println("=== EXTRACTING ENHANCED CUSTOM MAPPINGS FOR: " + mapperName + " ===");
            
            // Find the source file for the mapper
            String sourceFilePath = findMapperSourceFile(mapperName);
            System.out.println("Source file path: " + sourceFilePath);
            
            if (sourceFilePath == null) {
                System.out.println("Could not find source file for mapper: " + mapperName);
                return "NO CUSTOM MAPPINGS FOUND\nCould not locate source file for mapper: " + mapperName;
            }
            
            // Parse the source file
            System.out.println("Parsing source file with JavaParser...");
            CompilationUnit cu = StaticJavaParser.parse(new File(sourceFilePath));
            System.out.println("Successfully parsed source file");
            
            // Extract custom mappings from the parsed code
            EnhancedCustomMappingExtractor extractor = new EnhancedCustomMappingExtractor();
            extractor.visit(cu, null);
            
            List<CustomMappingInfo> customMappings = extractor.getCustomMappings();
            System.out.println("Found " + customMappings.size() + " custom mappings");
            
            // Find helper classes and extract their methods
            List<String> helperClasses = extractor.getHelperClasses();
            Map<String, List<String>> helperMethods = new HashMap<>();
            
            for (String helperClass : helperClasses) {
                List<String> methods = extractHelperClassMethods(helperClass);
                helperMethods.put(helperClass, methods);
            }
            
            if (customMappings.isEmpty()) {
                System.out.println("=== EXTRACTED ENHANCED CUSTOM MAPPINGS RESULT ===");
                System.out.println("NO CUSTOM MAPPINGS FOUND");
                System.out.println("No custom mapping methods found in the mapper.");
                System.out.println("=== END EXTRACTED ENHANCED CUSTOM MAPPINGS ===");
                return "NO CUSTOM MAPPINGS FOUND\nNo custom mapping methods found in the mapper.";
            }
            
            StringBuilder result = new StringBuilder();
            result.append("ENHANCED CUSTOM MAPPINGS (with linked methods):\n");
            result.append("==============================================\n\n");
            
            for (CustomMappingInfo mapping : customMappings) {
                result.append("CUSTOM MAPPING: ").append(mapping.getMappingName()).append("\n");
                result.append("Target Field: ").append(mapping.getTargetField()).append("\n");
                result.append("Main Method: ").append(mapping.getMainMethod()).append("\n");
                result.append("Method Chain:\n");
                
                for (String method : mapping.getMethodChain()) {
                    result.append("  -> ").append(method).append("\n");
                }
                
                result.append("Method Definitions:\n");
                for (String methodDef : mapping.getMethodDefinitions()) {
                    result.append("  ").append(methodDef).append("\n");
                }
                result.append("\n");
            }
            
            // Add helper class information
            if (!helperMethods.isEmpty()) {
                result.append("HELPER CLASSES AND METHODS:\n");
                result.append("===========================\n\n");
                
                for (Map.Entry<String, List<String>> entry : helperMethods.entrySet()) {
                    result.append("Helper Class: ").append(entry.getKey()).append("\n");
                    result.append("Methods:\n");
                    for (String method : entry.getValue()) {
                        result.append("  ").append(method).append("\n");
                    }
                    result.append("\n");
                }
            }
            
            System.out.println("=== EXTRACTED ENHANCED CUSTOM MAPPINGS RESULT ===");
            System.out.println(result.toString());
            System.out.println("=== END EXTRACTED ENHANCED CUSTOM MAPPINGS ===");
            
            return result.toString();
            
        } catch (Exception e) {
            System.err.println("Error extracting enhanced custom mappings: " + e.getMessage());
            e.printStackTrace();
            return "ERROR: " + e.getMessage();
        }
    }

    /**
     * Enhanced JavaParser visitor to extract custom mappings including default methods
     */
    private static class EnhancedCustomMappingExtractor extends VoidVisitorAdapter<Void> {
        private List<CustomMappingInfo> customMappings = new ArrayList<>();
        private Map<String, String> methodDefinitions = new HashMap<>();
        private List<String> helperClasses = new ArrayList<>();
        
        @Override
        public void visit(MethodDeclaration md, Void arg) {
            String methodName = md.getNameAsString();
            String methodBody = md.getBody().map(body -> body.toString()).orElse("");
            
            // Store method definition
            methodDefinitions.put(methodName, methodBody);
            
            // Check for @Mapping annotations with expressions
            for (AnnotationExpr annotation : md.getAnnotations()) {
                if (annotation.getNameAsString().equals("Mapping")) {
                    String target = "";
                    String expression = "";
                    
                    // Handle @Mapping annotation with multiple members
                    if (annotation.isNormalAnnotationExpr()) {
                        var normalAnnotation = annotation.asNormalAnnotationExpr();
                        for (var pair : normalAnnotation.getPairs()) {
                            if (pair.getNameAsString().equals("target")) {
                                target = pair.getValue().asStringLiteralExpr().getValue();
                            } else if (pair.getNameAsString().equals("expression")) {
                                expression = pair.getValue().asStringLiteralExpr().getValue();
                            }
                        }
                    }
                    
                    // If this is a custom mapping with expression
                    if (!target.isEmpty() && !expression.isEmpty() && expression.contains("java(")) {
                        CustomMappingInfo mapping = new CustomMappingInfo();
                        mapping.setMappingName(md.getNameAsString());
                        mapping.setTargetField(target);
                        mapping.setMainMethod(extractMethodFromExpression(expression));
                        
                        // Find method chain
                        List<String> methodChain = findMethodChain(mapping.getMainMethod(), methodDefinitions);
                        mapping.setMethodChain(methodChain);
                        
                        // Get method definitions
                        List<String> methodDefs = new ArrayList<>();
                        for (String method : methodChain) {
                            if (methodDefinitions.containsKey(method)) {
                                methodDefs.add(method + ": " + methodDefinitions.get(method));
                            }
                        }
                        mapping.setMethodDefinitions(methodDefs);
                        
                        customMappings.add(mapping);
                    }
                }
            }
            
            // Check for default methods with custom logic (not explicitly mapped)
            if (md.isDefault() && !methodBody.isEmpty() && hasCustomLogic(methodBody)) {
                // This is a default method with custom logic that might be used in mappings
                CustomMappingInfo mapping = new CustomMappingInfo();
                mapping.setMappingName(methodName);
                mapping.setTargetField("DEFAULT_METHOD");
                mapping.setMainMethod(methodName);
                
                // Find method chain
                List<String> methodChain = findMethodChain(methodName, methodDefinitions);
                mapping.setMethodChain(methodChain);
                
                // Get method definitions
                List<String> methodDefs = new ArrayList<>();
                for (String method : methodChain) {
                    if (methodDefinitions.containsKey(method)) {
                        methodDefs.add(method + ": " + methodDefinitions.get(method));
                    }
                }
                mapping.setMethodDefinitions(methodDefs);
                
                customMappings.add(mapping);
            }
            
            // Check for helper class usage in method body
            if (!methodBody.isEmpty()) {
                List<String> foundHelpers = findHelperClassesInMethod(methodBody);
                helperClasses.addAll(foundHelpers);
            }
            
            super.visit(md, arg);
        }
        
        public List<CustomMappingInfo> getCustomMappings() {
            return customMappings;
        }
        
        public List<String> getHelperClasses() {
            return helperClasses.stream().distinct().collect(Collectors.toList());
        }
        
        private boolean hasCustomLogic(String methodBody) {
            // Check if method body contains custom logic (not just simple assignments)
            return methodBody.contains("new ") || 
                   methodBody.contains("(") && methodBody.contains(")") ||
                   methodBody.contains("if") || 
                   methodBody.contains("for") ||
                   methodBody.contains("return") && methodBody.contains("(");
        }
        
        // ... existing helper methods from CustomMappingExtractor ...
        private String extractMethodFromExpression(String expression) {
            // Extract method name from java() expression
            if (expression.contains("java(")) {
                String javaCode = expression.substring(expression.indexOf("java(") + 5, expression.lastIndexOf(")"));
                // Find method call pattern
                String[] parts = javaCode.split("\\.");
                if (parts.length > 0) {
                    String lastPart = parts[parts.length - 1];
                    if (lastPart.contains("(")) {
                        return lastPart.substring(0, lastPart.indexOf("("));
                    }
                }
            }
            return "";
        }
        
        private List<String> findMethodChain(String startMethod, Map<String, String> methodDefs) {
            List<String> chain = new ArrayList<>();
            Set<String> visited = new HashSet<>();
            
            findMethodChainRecursive(startMethod, methodDefs, chain, visited);
            return chain;
        }
        
        private void findMethodChainRecursive(String methodName, Map<String, String> methodDefs, 
                                            List<String> chain, Set<String> visited) {
            if (visited.contains(methodName) || !methodDefs.containsKey(methodName)) {
                return;
            }
            
            visited.add(methodName);
            chain.add(methodName);
            
            String methodBody = methodDefs.get(methodName);
            
            // Find method calls in the body
            String[] lines = methodBody.split("\n");
            for (String line : lines) {
                // Look for method calls (simplified pattern matching)
                if (line.contains("(") && line.contains(")") && !line.contains("if") && !line.contains("for")) {
                    String[] words = line.split("\\s+");
                    for (String word : words) {
                        if (word.contains("(") && word.contains(")") && !word.startsWith("(")) {
                            String potentialMethod = word.substring(0, word.indexOf("("));
                            if (methodDefs.containsKey(potentialMethod) && !visited.contains(potentialMethod)) {
                                findMethodChainRecursive(potentialMethod, methodDefs, chain, visited);
                            }
                        }
                    }
                }
            }
        }
        
        private List<String> findHelperClassesInMethod(String methodBody) {
            List<String> helpers = new ArrayList<>();
            
            // Look for patterns like "new UserMapperHelp()" or "helper.calculateAdjustedSalary"
            String[] lines = methodBody.split("\n");
            for (String line : lines) {
                // Pattern for "new ClassName()"
                if (line.contains("new ") && line.contains("()")) {
                    String[] parts = line.split("new ");
                    for (String part : parts) {
                        if (part.contains("()")) {
                            String className = part.substring(0, part.indexOf("("));
                            if (className.contains(".")) {
                                className = className.substring(className.lastIndexOf(".") + 1);
                            }
                            if (className.endsWith("Help") || className.endsWith("Helper") || 
                                className.endsWith("Mapper") || className.endsWith("Util")) {
                                helpers.add(className);
                            }
                        }
                    }
                }
                
                // Pattern for "helper.methodName" or "className.methodName"
                if (line.contains(".") && line.contains("(")) {
                    String[] parts = line.split("\\.");
                    if (parts.length > 1) {
                        String potentialHelper = parts[0].trim();
                        if (potentialHelper.endsWith("Help") || potentialHelper.endsWith("Helper") || 
                            potentialHelper.endsWith("Mapper") || potentialHelper.endsWith("Util")) {
                            helpers.add(potentialHelper);
                        }
                    }
                }
            }
            
            return helpers;
        }
    }
} 