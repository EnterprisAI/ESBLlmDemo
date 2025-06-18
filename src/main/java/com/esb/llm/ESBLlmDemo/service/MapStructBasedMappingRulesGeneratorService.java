package com.esb.llm.ESBLlmDemo.service;

import com.esb.llm.ESBLlmDemo.config.MappingRules;
import com.esb.llm.ESBLlmDemo.mapper.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.lang.reflect.Method;
import java.util.*;

@Service
public class MapStructBasedMappingRulesGeneratorService {

    @Autowired
    private ObjectMapper objectMapper;

    /**
     * Generate mapping rules from a specific MapStruct mapper
     * @param mapperName The name of the mapper (e.g., "ProductMapper", "OrderMapper", "UserMapper")
     * @return MappingRules object
     */
    public MappingRules generateMappingRulesFromMapper(String mapperName) {
        try {
            switch (mapperName.toLowerCase()) {
                case "productmapper":
                    return generateProductMapperRules();
                case "ordermapper":
                    return generateOrderMapperRules();
                case "usermapper":
                    return generateUserMapperRules();
                case "employeemapper":
                    return generateEmployeeMapperRules();
                default:
                    throw new IllegalArgumentException("Unknown mapper: " + mapperName);
            }
        } catch (Exception e) {
            throw new RuntimeException("Error generating mapping rules from mapper " + mapperName + ": " + e.getMessage(), e);
        }
    }

    /**
     * Generate mapping rules for ProductMapper
     */
    private MappingRules generateProductMapperRules() {
        MappingRules rules = new MappingRules();
        rules.setSourceContentType("JSON");
        rules.setTargetContentType("JSON");

        // Main conversion rule for Product to TargetProduct
        MappingRules.ConversionRule productRule = new MappingRules.ConversionRule();
        productRule.setPropID("ProductMapping");
        productRule.setSourceLocation("$");
        productRule.setTargetLocation("$");
        productRule.setArray(false);

        // Items based on @Mapping annotations in ProductMapper
        List<MappingRules.ConversionRule> items = Arrays.asList(
            createMappingRule("PRODUCT_ID", "productId", "id", false),
            createMappingRule("PRODUCT_NAME", "productName", "name", false),
            createMappingRule("PRODUCT_PRICE", "productPrice", "price", false),
            createMappingRule("PRODUCT_CATEGORY", "productCategory.categoryName", "category", false),
            createMappingRule("PRODUCT_SPECS", "productSpecifications", "specs", true),
            createMappingRule("PRODUCT_STOCK", "productInventory.stockLevel", "availableStock", false),
            createMappingRule("PRODUCT_LOCATION", "productInventory.warehouseLocation", "location", false),
            createExpressionRule("PRODUCT_AVAILABLE", "available", "java(product.getProductInventory().getStockLevel() > 0)")
        );

        productRule.setItems(items);
        rules.setConversionRules(Arrays.asList(productRule));
        return rules;
    }

    /**
     * Generate mapping rules for OrderMapper
     */
    private MappingRules generateOrderMapperRules() {
        MappingRules rules = new MappingRules();
        rules.setSourceContentType("JSON");
        rules.setTargetContentType("JSON");

        // Main conversion rule for Order to TargetOrder
        MappingRules.ConversionRule orderRule = new MappingRules.ConversionRule();
        orderRule.setPropID("OrderMapping");
        orderRule.setSourceLocation("$");
        orderRule.setTargetLocation("$");
        orderRule.setArray(false);

        // Items based on @Mapping annotations in OrderMapper
        List<MappingRules.ConversionRule> items = Arrays.asList(
            createMappingRule("ORDER_ID", "orderId", "id", false),
            createMappingRule("CUSTOMER_NAME", "customerName", "customerInfo", false),
            createMappingRule("CUSTOMER_EMAIL", "customerEmail", "customerContact", false),
            createMappingRule("ORDER_DATE", "orderDate", "purchaseDate", false),
            createMappingRule("ORDER_STATUS", "orderStatus", "status", false),
            createMappingRule("ORDER_ITEMS", "orderItems", "items", true),
            createExpressionRule("DELIVERY_ADDRESS", "deliveryAddress", 
                "java(order.getShippingDetails().getShippingAddress() + \", \" + order.getShippingDetails().getShippingCity() + \", \" + order.getShippingDetails().getShippingState() + \" \" + order.getShippingDetails().getShippingZipCode())"),
            createExpressionRule("PAYMENT_DETAILS", "paymentDetails", 
                "java(order.getPaymentInfo().getPaymentMethod() + \" - Card ending in \" + order.getPaymentInfo().getCardLastFour())"),
            createExpressionRule("TOTAL_AMOUNT", "totalAmount", 
                "java(order.getOrderItems().stream().mapToDouble(item -> item.getTotalPrice()).sum() + order.getShippingDetails().getShippingCost())")
        );

        orderRule.setItems(items);
        rules.setConversionRules(Arrays.asList(orderRule));
        return rules;
    }

    /**
     * Generate mapping rules for UserMapper
     */
    private MappingRules generateUserMapperRules() {
        MappingRules rules = new MappingRules();
        rules.setSourceContentType("JSON");
        rules.setTargetContentType("JSON");

        // Main conversion rule for User to TargetUser
        MappingRules.ConversionRule userRule = new MappingRules.ConversionRule();
        userRule.setPropID("UserMapping");
        userRule.setSourceLocation("$");
        userRule.setTargetLocation("$");
        userRule.setArray(false);

        // Items based on @Mapping annotations in UserMapper
        List<MappingRules.ConversionRule> items = Arrays.asList(
            createMappingRule("USER_ID", "userId", "id", false),
            createExpressionRule("FULL_NAME", "fullName", "java(user.getFirstName() + \" \" + user.getLastName())"),
            createExpressionRule("CONTACT_INFO", "contactInfo", "java(user.getEmailAddress() + \" | \" + user.getPhoneNumber())"),
            createMappingRule("BIRTH_DATE", "dateOfBirth", "birthDate", false),
            createExpressionRule("ADDRESS", "address", 
                "java(user.getResidentialAddress().getStreetAddress() + \", \" + user.getResidentialAddress().getCityName() + \", \" + user.getResidentialAddress().getStateCode() + \" \" + user.getResidentialAddress().getPostalCode())"),
            createExpressionRule("PROFILE_INFO", "profileInfo", 
                "java(user.getUserProfile().getOccupation() + \" at \" + user.getUserProfile().getCompany())"),
            createMappingRule("USER_PREFERENCES", "userPreferences", "preferences", true),
            createExpressionRule("ACCOUNT_INFO", "accountInfo", 
                "java(user.getAccountDetails().getAccountType() + \" - \" + user.getAccountDetails().getAccountStatus())"),
            createExpressionRule("AGE", "age", "java(java.time.LocalDate.now().getYear() - user.getDateOfBirth().getYear())")
        );

        userRule.setItems(items);
        rules.setConversionRules(Arrays.asList(userRule));
        return rules;
    }

    /**
     * Generate mapping rules for EmployeeMapper
     */
    private MappingRules generateEmployeeMapperRules() {
        MappingRules rules = new MappingRules();
        rules.setSourceContentType("JSON");
        rules.setTargetContentType("JSON");

        // Main conversion rule for Employee to TargetEmployee
        MappingRules.ConversionRule employeeRule = new MappingRules.ConversionRule();
        employeeRule.setPropID("EmployeeMapping");
        employeeRule.setSourceLocation("$");
        employeeRule.setTargetLocation("$");
        employeeRule.setArray(false);

        // Items based on @Mapping annotations in EmployeeMapper
        List<MappingRules.ConversionRule> items = Arrays.asList(
            createMappingRule("EMPLOYEE_ID", "employeeId", "employeeId", false),
            createMappingRule("EMPLOYEE_NAME", "name", "employeename", false),
            createMappingRule("AGE", "age", "age", false),
            createMappingRule("GENDER", "gender", "gender", false),
            createMappingRule("EMP_LOCATION", "address.country", "emplocation", false),
            createMappingRule("OFFICE_LOCATION", "officeDetails.location", "officelocation", false),
            createMappingRule("WORK_EXPERIENCE", "workExperience", "workExperience", true)
        );

        employeeRule.setItems(items);
        rules.setConversionRules(Arrays.asList(employeeRule));
        return rules;
    }

    /**
     * Create a simple mapping rule
     */
    private MappingRules.ConversionRule createMappingRule(String propId, String sourceLocation, String targetLocation, boolean isArray) {
        MappingRules.ConversionRule rule = new MappingRules.ConversionRule();
        rule.setPropID(propId);
        rule.setSourceLocation(sourceLocation);
        rule.setTargetLocation(targetLocation);
        rule.setArray(isArray);
        rule.setItems(null);
        rule.setCustomLogic("Direct mapping");
        return rule;
    }

    /**
     * Create an expression-based mapping rule
     */
    private MappingRules.ConversionRule createExpressionRule(String propId, String targetLocation, String expression) {
        MappingRules.ConversionRule rule = new MappingRules.ConversionRule();
        rule.setPropID(propId);
        rule.setSourceLocation("EXPRESSION: " + expression);
        rule.setTargetLocation(targetLocation);
        rule.setArray(false);
        rule.setItems(null);
        rule.setCustomLogic(expression);
        return rule;
    }

    /**
     * Generate mapping rules as JSON string
     */
    public String generateMappingRulesAsJson(String mapperName) {
        try {
            MappingRules rules = generateMappingRulesFromMapper(mapperName);
            return convertMappingRulesToJson(rules);
        } catch (Exception e) {
            throw new RuntimeException("Error generating JSON rules for mapper " + mapperName + ": " + e.getMessage(), e);
        }
    }

    /**
     * Get list of available mappers
     */
    public List<String> getAvailableMappers() {
        return Arrays.asList("ProductMapper", "OrderMapper", "UserMapper", "EmployeeMapper");
    }

    /**
     * Convert MappingRules object to JSON string
     */
    private String convertMappingRulesToJson(MappingRules rules) {
        try {
            StringBuilder json = new StringBuilder();
            json.append("{\n");
            json.append("  \"sourceContentType\": \"").append(rules.getSourceContentType()).append("\",\n");
            json.append("  \"targetContentType\": \"").append(rules.getTargetContentType()).append("\",\n");
            json.append("  \"conversionRules\": [\n");
            
            if (rules.getConversionRules() != null) {
                for (int i = 0; i < rules.getConversionRules().size(); i++) {
                    MappingRules.ConversionRule rule = rules.getConversionRules().get(i);
                    json.append(convertConversionRuleToJson(rule, "    "));
                    if (i < rules.getConversionRules().size() - 1) {
                        json.append(",");
                    }
                    json.append("\n");
                }
            }
            
            json.append("  ]\n");
            json.append("}");
            
            return json.toString();
        } catch (Exception e) {
            throw new RuntimeException("Error converting mapping rules to JSON: " + e.getMessage(), e);
        }
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
        json.append(indent).append("  \"array\": ").append(rule.isArray()).append(",\n");
        
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