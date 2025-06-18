package com.esb.llm.ESBLlmDemo.service;

import com.esb.llm.ESBLlmDemo.config.MappingRules;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class MappingRulesGeneratorService {

    /**
     * Generate mapping rules from MapStruct annotations
     * @return MappingRules object generated from MapStruct mapper
     */
    public MappingRules generateMappingRulesFromMapStruct() {
        try {
            MappingRules rules = new MappingRules();
            rules.setSourceContentType("JSON");
            rules.setTargetContentType("JSON");

            // Create the main conversion rule for EmployeeList
            MappingRules.ConversionRule employeeRule = new MappingRules.ConversionRule();
            employeeRule.setPropID("EmployeeList");
            employeeRule.setSourceLocation("$");
            employeeRule.setTargetLocation("$");
            employeeRule.setArray(true);

            // Create items based on the @Mapping annotations in EmployeeMapper
            List<MappingRules.ConversionRule> items = Arrays.asList(
                createMappingRule("EMPLOYEE_ID", "employeeId", "employeeId", false),
                createMappingRule("EMPLOYEE_NAME", "name", "employeename", false),
                createMappingRule("AGE", "age", "age", false),
                createMappingRule("GENDER", "gender", "gender", false),
                createMappingRule("EMP_LOCATION", "address.country", "emplocation", false),
                createMappingRule("OFFICE_LOCATION", "officeDetails.location", "officelocation", false),
                createWorkExperienceRule()
            );

            employeeRule.setItems(items);
            rules.setConversionRules(Arrays.asList(employeeRule));
            return rules;

        } catch (Exception e) {
            throw new RuntimeException("Error generating mapping rules from MapStruct: " + e.getMessage(), e);
        }
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
        return rule;
    }

    /**
     * Create work experience mapping rule
     */
    private MappingRules.ConversionRule createWorkExperienceRule() {
        MappingRules.ConversionRule workExpRule = new MappingRules.ConversionRule();
        workExpRule.setPropID("WORK_EXP");
        workExpRule.setSourceLocation("workExperience");
        workExpRule.setTargetLocation("workExperience");
        workExpRule.setArray(true);

        // Create items for work experience
        MappingRules.ConversionRule companyRule = createMappingRule("WORK_EXP_COMPANY", "company", "company", false);
        workExpRule.setItems(Arrays.asList(companyRule));

        return workExpRule;
    }

    /**
     * Generate mapping rules as JSON string
     * @return JSON string representation of mapping rules
     */
    public String generateMappingRulesAsJson() {
        try {
            MappingRules rules = generateMappingRulesFromMapStruct();
            return convertMappingRulesToJson(rules);
        } catch (Exception e) {
            throw new RuntimeException("Error generating JSON rules: " + e.getMessage(), e);
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