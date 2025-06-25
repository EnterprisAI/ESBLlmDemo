package com.esb.llm.ESBLlmDemo.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.AnnotationExpr;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Generic MapStruct Analyzer that can work with any MapStruct mapper
 * without hardcoded dependencies or assumptions about specific mapper names.
 */
@Service
public class GenericMapStructAnalyzer {

    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Analyze any MapStruct mapper and extract all mapping information
     */
    public Map<String, Object> analyzeMapper(String mapperName) {
        try {
            String sourceCode = findMapperSourceFile(mapperName);
            if (sourceCode == null) {
                throw new RuntimeException("Mapper source file not found: " + mapperName);
            }

            CompilationUnit cu = StaticJavaParser.parse(sourceCode);
            
            Map<String, Object> analysis = new LinkedHashMap<>();
            analysis.put("mapperName", mapperName);
            
            // Extract all mapping information
            MappingAnalyzer mappingAnalyzer = new MappingAnalyzer();
            cu.accept(mappingAnalyzer, null);
            
            analysis.put("directMappings", mappingAnalyzer.getDirectMappings());
            analysis.put("customMappings", mappingAnalyzer.getCustomMappings());
            analysis.put("helperClasses", mappingAnalyzer.getHelperClasses());
            analysis.put("methodDefinitions", mappingAnalyzer.getMethodDefinitions());
            
            // Analyze helper classes if found
            Map<String, Object> helperAnalysis = new HashMap<>();
            for (String helperClass : mappingAnalyzer.getHelperClasses()) {
                helperAnalysis.put(helperClass, analyzeHelperClass(helperClass));
            }
            analysis.put("helperClassAnalysis", helperAnalysis);
            
            return analysis;
            
        } catch (Exception e) {
            throw new RuntimeException("Error analyzing mapper " + mapperName + ": " + e.getMessage(), e);
        }
    }

    /**
     * Generate JSON rules for any mapper
     */
    public String generateJsonRules(String mapperName) {
        Map<String, Object> analysis = analyzeMapper(mapperName);
        
        Map<String, Object> jsonRules = new LinkedHashMap<>();
        jsonRules.put("sourceContentType", determineSourceContentType(mapperName));
        jsonRules.put("targetContentType", determineTargetContentType(mapperName));
        
        List<Map<String, Object>> conversionRules = new ArrayList<>();
        
        // Add direct mappings
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> directMappings = (List<Map<String, Object>>) analysis.get("directMappings");
        for (Map<String, Object> mapping : directMappings) {
            Map<String, Object> rule = new LinkedHashMap<>();
            rule.put("propID", mapping.get("propID"));
            rule.put("sourceLocation", mapping.get("sourceLocation"));
            rule.put("targetLocation", mapping.get("targetLocation"));
            rule.put("isArray", mapping.get("isArray"));
            rule.put("isCustom", false);
            conversionRules.add(rule);
        }
        
        // Add custom mappings
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> customMappings = (List<Map<String, Object>>) analysis.get("customMappings");
        for (Map<String, Object> mapping : customMappings) {
            Map<String, Object> rule = new LinkedHashMap<>();
            rule.put("propID", mapping.get("propID"));
            rule.put("sourceLocation", mapping.get("sourceLocation"));
            rule.put("targetLocation", mapping.get("targetLocation"));
            rule.put("isArray", mapping.get("isArray"));
            rule.put("isCustom", true);
            rule.put("customLogic", mapping.get("customLogic"));
            rule.put("methodDefinitions", mapping.get("methodDefinitions"));
            conversionRules.add(rule);
        }
        
        jsonRules.put("conversionRules", conversionRules);
        
        try {
            return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(jsonRules);
        } catch (Exception e) {
            throw new RuntimeException("Error generating JSON rules: " + e.getMessage(), e);
        }
    }

    /**
     * Generic mapping analyzer that works with any MapStruct mapper
     */
    private static class MappingAnalyzer extends VoidVisitorAdapter<Void> {
        private List<Map<String, Object>> directMappings = new ArrayList<>();
        private List<Map<String, Object>> customMappings = new ArrayList<>();
        private List<String> helperClasses = new ArrayList<>();
        private Map<String, String> methodDefinitions = new HashMap<>();

        @Override
        public void visit(ClassOrInterfaceDeclaration cd, Void arg) {
            // Extract helper classes from @Mapper annotation
            for (AnnotationExpr annotation : cd.getAnnotations()) {
                if (annotation.getNameAsString().equals("Mapper")) {
                    extractHelperClassesFromMapper(annotation);
                }
            }
            super.visit(cd, arg);
        }

        @Override
        public void visit(MethodDeclaration md, Void arg) {
            String methodName = md.getNameAsString();
            String methodBody = md.getBody().map(body -> body.toString()).orElse("");
            
            // Store method definition
            methodDefinitions.put(methodName, methodBody);
            
            // Analyze all @Mapping annotations
            for (AnnotationExpr annotation : md.getAnnotations()) {
                if (annotation.getNameAsString().equals("Mapping")) {
                    analyzeMappingAnnotation(annotation, md);
                } else if (annotation.getNameAsString().equals("Mappings")) {
                    analyzeMappingsAnnotation(annotation, md);
                }
            }
            
            super.visit(md, arg);
        }

        private void extractHelperClassesFromMapper(AnnotationExpr annotation) {
            if (annotation.isNormalAnnotationExpr()) {
                var normalAnnotation = annotation.asNormalAnnotationExpr();
                for (var pair : normalAnnotation.getPairs()) {
                    if (pair.getNameAsString().equals("uses")) {
                        String usesValue = pair.getValue().toString();
                        // Generic helper class detection - look for any class ending with Help, Helper, Util, etc.
                        Pattern pattern = Pattern.compile("([A-Z][a-zA-Z]*(?:Help|Helper|Util|Mapper|Service))");
                        Matcher matcher = pattern.matcher(usesValue);
                        while (matcher.find()) {
                            helperClasses.add(matcher.group(1));
                        }
                    }
                }
            }
        }

        private void analyzeMappingsAnnotation(AnnotationExpr annotation, MethodDeclaration md) {
            if (annotation.isNormalAnnotationExpr()) {
                var normalAnnotation = annotation.asNormalAnnotationExpr();
                for (var pair : normalAnnotation.getPairs()) {
                    if (pair.getNameAsString().equals("value")) {
                        if (pair.getValue().isArrayInitializerExpr()) {
                            var arrayExpr = pair.getValue().asArrayInitializerExpr();
                            for (var element : arrayExpr.getValues()) {
                                if (element.isAnnotationExpr()) {
                                    analyzeMappingAnnotation(element.asAnnotationExpr(), md);
                                }
                            }
                        }
                    }
                }
            } else if (annotation.isSingleMemberAnnotationExpr()) {
                var singleMember = annotation.asSingleMemberAnnotationExpr();
                if (singleMember.getMemberValue().isArrayInitializerExpr()) {
                    var arrayExpr = singleMember.getMemberValue().asArrayInitializerExpr();
                    for (var element : arrayExpr.getValues()) {
                        if (element.isAnnotationExpr()) {
                            analyzeMappingAnnotation(element.asAnnotationExpr(), md);
                        }
                    }
                }
            }
        }

        private void analyzeMappingAnnotation(AnnotationExpr annotation, MethodDeclaration md) {
            String source = "";
            String target = "";
            String expression = "";
            
            if (annotation.isNormalAnnotationExpr()) {
                var normalAnnotation = annotation.asNormalAnnotationExpr();
                for (var pair : normalAnnotation.getPairs()) {
                    if (pair.getNameAsString().equals("source")) {
                        if (pair.getValue().isStringLiteralExpr()) {
                            source = pair.getValue().asStringLiteralExpr().getValue();
                        }
                    } else if (pair.getNameAsString().equals("target")) {
                        if (pair.getValue().isStringLiteralExpr()) {
                            target = pair.getValue().asStringLiteralExpr().getValue();
                        }
                    } else if (pair.getNameAsString().equals("expression")) {
                        if (pair.getValue().isStringLiteralExpr()) {
                            expression = pair.getValue().asStringLiteralExpr().getValue();
                        }
                    }
                }
            }
            
            // Create mapping entry
            Map<String, Object> mapping = new LinkedHashMap<>();
            mapping.put("propID", generatePropertyId(source, target));
            mapping.put("sourceLocation", source.isEmpty() ? "inferred" : source);
            mapping.put("targetLocation", target.isEmpty() ? "inferred" : target);
            mapping.put("isArray", isArrayField(source, target));
            
            if (!expression.isEmpty()) {
                // This is a custom mapping
                mapping.put("customLogic", generateCustomLogic(expression));
                mapping.put("methodDefinitions", extractMethodDefinitions(expression));
                customMappings.add(mapping);
            } else {
                // This is a direct mapping
                directMappings.add(mapping);
            }
        }

        private String generatePropertyId(String source, String target) {
            if (!source.isEmpty()) {
                String[] parts = source.split("\\.");
                String lastPart = parts[parts.length - 1];
                return lastPart.toUpperCase();
            } else if (!target.isEmpty()) {
                return target.toUpperCase();
            }
            return "PROPERTY";
        }

        private boolean isArrayField(String source, String target) {
            String propertyName = !target.isEmpty() ? target : source;
            
            // Generic array detection patterns
            String[] arrayPatterns = {"list", "array", "collection", "items", "elements", "set", "map"};
            for (String pattern : arrayPatterns) {
                if (propertyName.toLowerCase().contains(pattern)) {
                    return true;
                }
            }
            
            // Plural detection
            if (propertyName.endsWith("s") && !propertyName.endsWith("ss")) {
                String[] commonSingularWords = {"class", "status", "address", "process", "access", "success"};
                if (!Arrays.asList(commonSingularWords).contains(propertyName.toLowerCase())) {
                    return true;
                }
            }
            
            return false;
        }

        private String generateCustomLogic(String expression) {
            // Generic custom logic generation
            if (expression.startsWith("java(") && expression.endsWith(")")) {
                String logic = expression.substring(5, expression.length() - 1);
                
                // Replace common patterns with readable descriptions
                logic = logic.replaceAll("source\\.get([A-Z][a-zA-Z]*)\\(\\)", "get $1 from source");
                logic = logic.replaceAll("helper\\.([a-zA-Z]+)\\([^)]*\\)", "call helper method $1");
                logic = logic.replaceAll("new ([A-Z][a-zA-Z]*)\\(\\)", "create new $1 instance");
                
                return "Custom calculation: " + logic;
            }
            return "Custom expression: " + expression;
        }

        private List<String> extractMethodDefinitions(String expression) {
            List<String> methods = new ArrayList<>();
            
            // Generic method extraction from expressions
            Pattern pattern = Pattern.compile("([a-zA-Z][a-zA-Z0-9]*)\\.[a-zA-Z][a-zA-Z0-9]*\\(");
            Matcher matcher = pattern.matcher(expression);
            while (matcher.find()) {
                String className = matcher.group(1);
                if (className.endsWith("Help") || className.endsWith("Helper") || 
                    className.endsWith("Util") || className.endsWith("Mapper")) {
                    methods.add(className + " helper methods");
                }
            }
            
            return methods;
        }

        // Getters
        public List<Map<String, Object>> getDirectMappings() { return directMappings; }
        public List<Map<String, Object>> getCustomMappings() { return customMappings; }
        public List<String> getHelperClasses() { return helperClasses; }
        public Map<String, String> getMethodDefinitions() { return methodDefinitions; }
    }

    /**
     * Analyze helper class generically
     */
    private Map<String, Object> analyzeHelperClass(String helperClassName) {
        try {
            String sourceCode = findHelperClassSourceFile(helperClassName);
            if (sourceCode == null) {
                return Map.of("error", "Helper class source not found");
            }

            CompilationUnit cu = StaticJavaParser.parse(sourceCode);
            HelperAnalyzer helperAnalyzer = new HelperAnalyzer();
            cu.accept(helperAnalyzer, null);
            
            Map<String, Object> analysis = new LinkedHashMap<>();
            analysis.put("className", helperClassName);
            analysis.put("methods", helperAnalyzer.getMethods());
            analysis.put("methodSignatures", helperAnalyzer.getMethodSignatures());
            
            return analysis;
            
        } catch (Exception e) {
            return Map.of("error", "Error analyzing helper class: " + e.getMessage());
        }
    }

    /**
     * Generic helper class analyzer
     */
    private static class HelperAnalyzer extends VoidVisitorAdapter<Void> {
        private List<String> methods = new ArrayList<>();
        private Map<String, String> methodSignatures = new HashMap<>();

        @Override
        public void visit(MethodDeclaration md, Void arg) {
            String methodName = md.getNameAsString();
            String methodBody = md.getBody().map(body -> body.toString()).orElse("");
            
            methods.add(methodName);
            methodSignatures.put(methodName, md.getDeclarationAsString());
            
            super.visit(md, arg);
        }

        public List<String> getMethods() { return methods; }
        public Map<String, String> getMethodSignatures() { return methodSignatures; }
    }

    // Utility methods
    private String findMapperSourceFile(String mapperName) {
        String[] possiblePaths = {
            "src/main/java/com/esb/llm/ESBLlmDemo/mapper/" + mapperName + ".java",
            "src/main/java/com/esb/llm/ESBLlmDemo/" + mapperName + ".java",
            mapperName + ".java"
        };
        
        for (String path : possiblePaths) {
            try {
                Path filePath = Paths.get(path);
                if (Files.exists(filePath)) {
                    return Files.readString(filePath);
                }
            } catch (IOException e) {
                // Continue to next path
            }
        }
        return null;
    }

    private String findHelperClassSourceFile(String helperClassName) {
        return findMapperSourceFile(helperClassName);
    }

    private String determineSourceContentType(String mapperName) {
        // Generic source content type determination
        return "com.esb.llm.ESBLlmDemo.model." + mapperName.replace("Mapper", "");
    }

    private String determineTargetContentType(String mapperName) {
        // Generic target content type determination
        return "com.esb.llm.ESBLlmDemo.model.Target" + mapperName.replace("Mapper", "");
    }
}
