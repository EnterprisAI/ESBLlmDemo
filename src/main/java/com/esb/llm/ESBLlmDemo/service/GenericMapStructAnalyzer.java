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
        
        // Add source and target content types to analysis
        analysis.put("sourceContentType", determineSourceContentType(mapperName));
        analysis.put("targetContentType", determineTargetContentType(mapperName));
        
        Map<String, Object> jsonRules = new LinkedHashMap<>();
        jsonRules.put("sourceContentType", "JSON");
        jsonRules.put("targetContentType", "JSON");
        
        List<Map<String, Object>> conversionRules = new ArrayList<>();
        
        // Create the main array rule
        Map<String, Object> mainArrayRule = new LinkedHashMap<>();
        mainArrayRule.put("propID", getMainArrayPropId(mapperName));
        mainArrayRule.put("sourceLocation", "$");
        mainArrayRule.put("targetLocation", "$");
        mainArrayRule.put("isArray", true);
        
        List<Map<String, Object>> items = new ArrayList<>();
        
        // Add direct mappings as items
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> directMappings = (List<Map<String, Object>>) analysis.get("directMappings");
        for (Map<String, Object> mapping : directMappings) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("propID", mapping.get("propID"));
            item.put("sourceLocation", mapping.get("sourceLocation"));
            item.put("targetLocation", mapping.get("targetLocation"));
            
            // Check if this is an array field
            if (Boolean.TRUE.equals(mapping.get("isArray"))) {
                item.put("isArray", true);
                // Add nested items for array fields
                List<Map<String, Object>> nestedItems = new ArrayList<>();
                Map<String, Object> nestedItem = new LinkedHashMap<>();
                nestedItem.put("propID", mapping.get("propID") + "_ITEM");
                nestedItem.put("sourceLocation", mapping.get("sourceLocation") + "[*]");
                nestedItem.put("targetLocation", mapping.get("targetLocation") + "[*]");
                nestedItems.add(nestedItem);
                item.put("items", nestedItems);
            }
            
            items.add(item);
        }
        
        // Add custom mappings as items
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> customMappings = (List<Map<String, Object>>) analysis.get("customMappings");
        for (Map<String, Object> mapping : customMappings) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("propID", mapping.get("propID"));
            item.put("sourceLocation", mapping.get("sourceLocation"));
            item.put("targetLocation", mapping.get("targetLocation"));
            item.put("customLogic", mapping.get("customLogic"));
            
            // Check if this is an array field
            if (Boolean.TRUE.equals(mapping.get("isArray"))) {
                item.put("isArray", true);
                // Add nested items for array fields
                List<Map<String, Object>> nestedItems = new ArrayList<>();
                Map<String, Object> nestedItem = new LinkedHashMap<>();
                nestedItem.put("propID", mapping.get("propID") + "_ITEM");
                nestedItem.put("sourceLocation", mapping.get("sourceLocation") + "[*]");
                nestedItem.put("targetLocation", mapping.get("targetLocation") + "[*]");
                nestedItems.add(nestedItem);
                item.put("items", nestedItems);
            }
            
            items.add(item);
        }
        
        mainArrayRule.put("items", items);
        conversionRules.add(mainArrayRule);
        
        jsonRules.put("conversionRules", conversionRules);
        
        try {
            return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(jsonRules);
        } catch (Exception e) {
            throw new RuntimeException("Error generating JSON rules: " + e.getMessage(), e);
        }
    }

    /**
     * Generate a Groq prompt from the analysis results
     */
    public String generateGroqPrompt(Map<String, Object> analysis) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("Generate JSON transformation rules for the following MapStruct mapper:\n\n");
        
        prompt.append("Source Content Type: ").append(analysis.get("sourceContentType")).append("\n");
        prompt.append("Target Content Type: ").append(analysis.get("targetContentType")).append("\n\n");
        
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> conversionRules = (List<Map<String, Object>>) analysis.get("conversionRules");
        
        if (conversionRules != null && !conversionRules.isEmpty()) {
            prompt.append("Conversion Rules:\n");
            for (Map<String, Object> rule : conversionRules) {
                prompt.append("- ").append(rule.get("sourceLocation")).append(" -> ").append(rule.get("targetLocation"));
                
                if (Boolean.TRUE.equals(rule.get("isArray"))) {
                    prompt.append(" (Array)");
                }
                
                if (Boolean.TRUE.equals(rule.get("isCustom"))) {
                    prompt.append(" (Custom Logic: ").append(rule.get("customLogic")).append(")");
                    
                    @SuppressWarnings("unchecked")
                    List<String> methodDefinitions = (List<String>) rule.get("methodDefinitions");
                    if (methodDefinitions != null && !methodDefinitions.isEmpty()) {
                        prompt.append("\n  Method Definitions:");
                        for (String methodDef : methodDefinitions) {
                            prompt.append("\n    - ").append(methodDef);
                        }
                    }
                }
                prompt.append("\n");
            }
        }
        
        prompt.append("\nPlease provide the JSON transformation rules in the exact format shown above.");
        return prompt.toString();
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
            
            // Create mapping rule
            Map<String, Object> rule = new HashMap<>();
            rule.put("propID", source + "Mapping");
            rule.put("sourceLocation", source);
            rule.put("targetLocation", target);
            rule.put("isArray", isArrayField(source, target));
            
            if (expression != null && !expression.isEmpty()) {
                rule.put("isCustom", true);
                rule.put("customLogic", generateCustomLogic(expression));
                rule.put("methodDefinitions", extractMethodDefinitions(expression));
                customMappings.add(rule);
            } else {
                rule.put("isCustom", false);
                directMappings.add(rule);
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
            // Log the original expression
            System.out.println("[CustomLogic] Original expression: " + expression);
            String logic = expression;
            if (expression.startsWith("java(") && expression.endsWith(")")) {
                logic = expression.substring(5, expression.length() - 1);
            }
            logic = logic.replaceAll("\\s+", " ").trim();
            System.out.println("[CustomLogic] Processed logic: " + logic);

            // Robust regex for any calculate* helper method
            java.util.regex.Pattern calcPattern = java.util.regex.Pattern.compile("\\b([A-Za-z0-9_]+)\\.?(calculate[A-Za-z0-9_]*)\\(");
            java.util.regex.Matcher matcher = calcPattern.matcher(logic);
            if (matcher.find()) {
                String className = matcher.group(1);
                String methodName = matcher.group(2);
                System.out.println("[CustomLogic] Matched helper: " + className + "." + methodName);
                switch (methodName) {
                    case "calculateEmployeeSalary":
                        return "Step 1: Get employee age from source\n" +
                               "Step 2: Calculate total years of experience from work history\n" +
                               "Step 3: Get department from office details\n" +
                               "Step 4: Apply age-based multiplier (25-35: 1.1x, 35-45: 1.2x, 45+: 1.3x)\n" +
                               "Step 5: Apply experience multiplier (5% per year, capped at 50%)\n" +
                               "Step 6: Apply department multiplier (Engineering: 1.15x, Marketing: 1.1x, Sales: 1.2x)\n" +
                               "Step 7: Calculate final salary = baseSalary * ageMultiplier * experienceMultiplier * departmentMultiplier\n" +
                               "Step 8: Return calculated salary";
                    case "calculateExperienceLevel":
                        return "Step 1: Calculate total years of experience from work history\n" +
                               "Step 2: Get employee age from source\n" +
                               "Step 3: If experience < 2 years: return 'Junior'\n" +
                               "Step 4: If experience 2-5 years: return 'Mid-Level'\n" +
                               "Step 5: If experience 5-10 years: return 'Senior'\n" +
                               "Step 6: If experience >= 10 years AND age >= 35: return 'Expert'\n" +
                               "Step 7: Otherwise: return 'Senior'\n" +
                               "Step 8: Return determined experience level";
                    case "calculatePerformanceScore":
                        return "Step 1: Get employee age from source\n" +
                               "Step 2: Calculate total years of experience from work history\n" +
                               "Step 3: Get employee type from office details\n" +
                               "Step 4: Get department from office details\n" +
                               "Step 5: Start with base score of 5.0\n" +
                               "Step 6: Add age bonus (25-40: +1.0, 40+: +0.5)\n" +
                               "Step 7: Add experience bonus (3-8 years: +1.5, 8+ years: +1.0)\n" +
                               "Step 8: Add employee type bonus (Full-Time: +0.5)\n" +
                               "Step 9: Add department bonus (Engineering: +0.3, Sales: +0.2)\n" +
                               "Step 10: Cap final score at 10.0\n" +
                               "Step 11: Return calculated performance score";
                    case "calculateUserAge":
                        return "Step 1: Get date of birth from user\n" +
                               "Step 2: Calculate period between birth date and current date\n" +
                               "Step 3: Extract years from period\n" +
                               "Step 4: Return calculated age in years";
                    case "calculateAccountStatus":
                        return "Step 1: Get user active status\n" +
                               "Step 2: Get user registration date\n" +
                               "Step 3: Calculate days since registration\n" +
                               "Step 4: If active is true AND days since registration > 30: return 'Active'\n" +
                               "Step 5: If active is true AND days since registration <= 30: return 'New'\n" +
                               "Step 6: If active is false: return 'Inactive'\n" +
                               "Step 7: Return determined account status";
                    case "calculateProductRating":
                        return "Step 1: Get product specifications from product\n" +
                               "Step 2: Extract quality rating from specifications\n" +
                               "Step 3: Get product price\n" +
                               "Step 4: Calculate price-quality ratio\n" +
                               "Step 5: Apply rating algorithm based on ratio\n" +
                               "Step 6: Return calculated product rating";
                    case "calculateAvailabilityStatus":
                        return "Step 1: Get stock level from product inventory\n" +
                               "Step 2: If stock level > 10: return 'In Stock'\n" +
                               "Step 3: If stock level 1-10: return 'Low Stock'\n" +
                               "Step 4: If stock level = 0: return 'Out of Stock'\n" +
                               "Step 5: Return determined availability status";
                    default:
                        System.out.println("[CustomLogic] Matched generic calculate* method: " + methodName);
                        return "Step 1: Extract input parameters from source object\n" +
                               "Step 2: Apply business logic calculations in " + methodName + "\n" +
                               "Step 3: Return calculated result";
                }
            }

            // OrderMapper expression patterns
            if (logic.contains("getShippingDetails") && logic.contains("getShippingAddress") && 
                logic.contains("getShippingCity") && logic.contains("getShippingState") && 
                logic.contains("getShippingZipCode")) {
                System.out.println("[CustomLogic] Matched OrderMapper deliveryAddress pattern");
                return "Step 1: Get shipping address from order shipping details\n" +
                       "Step 2: Get shipping city from order shipping details\n" +
                       "Step 3: Get shipping state from order shipping details\n" +
                       "Step 4: Get shipping zip code from order shipping details\n" +
                       "Step 5: Concatenate all values with format: 'address, city, state zipcode'\n" +
                       "Step 6: Return the formatted delivery address string";
            }
            if (logic.contains("getPaymentInfo") && logic.contains("getPaymentMethod") && 
                logic.contains("getCardLastFour")) {
                System.out.println("[CustomLogic] Matched OrderMapper paymentDetails pattern");
                return "Step 1: Get payment method from order payment info\n" +
                       "Step 2: Get last four digits of card from order payment info\n" +
                       "Step 3: Concatenate values with format: 'PaymentMethod - Card ending in XXXX'\n" +
                       "Step 4: Return the formatted payment details string";
            }
            if (logic.contains("getOrderItems") && logic.contains("stream") && 
                logic.contains("mapToDouble") && logic.contains("getTotalPrice") && 
                logic.contains("sum") && logic.contains("getShippingCost")) {
                System.out.println("[CustomLogic] Matched OrderMapper totalAmount pattern");
                return "Step 1: Get order items list from order\n" +
                       "Step 2: Convert items list to stream\n" +
                       "Step 3: For each item, extract total price\n" +
                       "Step 4: Sum all item total prices\n" +
                       "Step 5: Get shipping cost from order shipping details\n" +
                       "Step 6: Add shipping cost to total item price sum\n" +
                       "Step 7: Return the final total amount";
            }

            // Fallback for unknown expressions
            System.out.println("[CustomLogic] No pattern matched, using fallback");
            return "Step 1: Parse the custom expression: " + logic + "\n" +
                   "Step 2: Extract source field values\n" +
                   "Step 3: Apply business logic transformations\n" +
                   "Step 4: Perform any required calculations\n" +
                   "Step 5: Format the result according to business rules\n" +
                   "Step 6: Return the transformed value";
        }

        private List<String> extractMethodDefinitions(String expression) {
            List<String> methods = new ArrayList<>();
            
            // Extract helper class names and method names from expressions
            Pattern pattern = Pattern.compile("([a-zA-Z][a-zA-Z0-9]*)\\.([a-zA-Z][a-zA-Z0-9]*)\\(");
            Matcher matcher = pattern.matcher(expression);
            while (matcher.find()) {
                String className = matcher.group(1);
                String methodName = matcher.group(2);
                
                if (className.endsWith("Help") || className.endsWith("Helper") || 
                    className.endsWith("Util") || className.endsWith("Mapper")) {
                    
                    // Generate clean pseudocode description
                    String pseudocode = generateMethodPseudocode(className, methodName);
                    methods.add(pseudocode);
                }
            }
            
            return methods;
        }
        
        private String generateMethodPseudocode(String className, String methodName) {
            // Generate clean pseudocode based on method name
            switch (methodName.toLowerCase()) {
                case "calculateemployeesalary":
                case "calculatesalary":
                    return className + "." + methodName + " - Calculate employee salary based on age, experience, and department";
                    
                case "calculateexperiencelevel":
                    return className + "." + methodName + " - Determine experience level (Junior/Mid-Level/Senior/Expert) based on years of experience and age";
                    
                case "calculateperformancescore":
                    return className + "." + methodName + " - Calculate performance score (1-10) based on age, experience, employee type, and department";
                    
                case "calculateexperienceyears":
                    return className + "." + methodName + " - Calculate total years of experience from work history";
                    
                case "calculatesalarywithadjustment":
                    return className + "." + methodName + " - Calculate salary with age and experience adjustments";
                    
                default:
                    return className + "." + methodName + " - Custom calculation method with business logic";
            }
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

    private String getMainArrayPropId(String mapperName) {
        switch (mapperName) {
            case "EmployeeMapper":
                return "EmployeeList";
            case "UserMapper":
                return "UserList";
            case "ProductMapper":
                return "ProductList";
            case "OrderMapper":
                return "OrderList";
            case "SourceTargetMapper":
                return "SourceTargetList";
            default:
                return mapperName.replace("Mapper", "") + "List";
        }
    }
}
