package com.esb.llm.ESBLlmDemo.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class MapStructJsonRuleGeneratorService {

    @Autowired
    private ObjectMapper objectMapper;

    /**
     * Generate JSON conversion rules from a MapStruct mapper interface
     * @param mapperClassName The fully qualified class name of the mapper
     * @return JSON string representation of conversion rules
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
     * @param mapperClass The mapper class to analyze
     * @return JSON string representation of conversion rules
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
        // Check if method name suggests mapping (contains 'To' or similar patterns)
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
        
        // Extract meaningful name from method name
        if (methodName.contains("To")) {
            String[] parts = methodName.split("To");
            if (parts.length > 0) {
                String sourceName = parts[0];
                // Handle camelCase to proper case
                if (sourceName.length() > 0) {
                    return sourceName.substring(0, 1).toUpperCase() + sourceName.substring(1) + "List";
                }
            }
        }
        
        // Fallback to method name
        return methodName.substring(0, 1).toUpperCase() + methodName.substring(1) + "List";
    }

    /**
     * Analyze @Mapping annotations and generate items
     */
    private List<Map<String, Object>> analyzeMappingAnnotations(Method method) {
        List<Map<String, Object>> items = new ArrayList<>();
        
        // Check for @Mappings annotation (multiple mappings)
        if (method.isAnnotationPresent(Mappings.class)) {
            Mappings mappings = method.getAnnotation(Mappings.class);
            for (Mapping mapping : mappings.value()) {
                Map<String, Object> item = createMappingItem(mapping);
                if (item != null) {
                    items.add(item);
                }
            }
        }
        
        // Check for individual @Mapping annotations
        Mapping[] mappings = method.getAnnotationsByType(Mapping.class);
        for (Mapping mapping : mappings) {
            Map<String, Object> item = createMappingItem(mapping);
            if (item != null) {
                items.add(item);
            }
        }
        
        // If no @Mapping annotations found, try to infer from method parameters and return type
        if (items.isEmpty()) {
            items = inferMappingsFromMethod(method);
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
            
            // Try to infer mappings based on common field names
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
            // Get all getter methods from source class
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
            
            // Find common fields
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
        // Enhanced array detection - generic approach
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
            // Additional check to avoid false positives for common singular words ending with 's'
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
        
        // Enhanced nested item generation
        String[] parts = source.split("\\.");
        if (parts.length > 1) {
            // Create items for each level of nesting
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
     * Test the generator with a specific mapper
     */
    public String testGeneratorWithMapper(String mapperName) {
        try {
            String rules = generateJsonRulesForMapper(mapperName);
            System.out.println("Generated rules for " + mapperName + ":");
            System.out.println(rules);
            return rules;
        } catch (Exception e) {
            String error = "Error generating rules for " + mapperName + ": " + e.getMessage();
            System.err.println(error);
            return error;
        }
    }
} 