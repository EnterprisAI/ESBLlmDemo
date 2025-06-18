package com.esb.llm.ESBLlmDemo.config;

import java.util.List;
import java.util.Map;

public class MappingRules {
    private String sourceContentType;
    private String targetContentType;
    private List<ConversionRule> conversionRules;

    // Default constructor
    public MappingRules() {}

    // Constructor with all fields
    public MappingRules(String sourceContentType, String targetContentType, List<ConversionRule> conversionRules) {
        this.sourceContentType = sourceContentType;
        this.targetContentType = targetContentType;
        this.conversionRules = conversionRules;
    }

    // Getters and Setters
    public String getSourceContentType() { return sourceContentType; }
    public void setSourceContentType(String sourceContentType) { this.sourceContentType = sourceContentType; }

    public String getTargetContentType() { return targetContentType; }
    public void setTargetContentType(String targetContentType) { this.targetContentType = targetContentType; }

    public List<ConversionRule> getConversionRules() { return conversionRules; }
    public void setConversionRules(List<ConversionRule> conversionRules) { this.conversionRules = conversionRules; }

    public static class ConversionRule {
        private String propID;
        private String sourceLocation;
        private String targetLocation;
        private boolean isArray;
        private List<ConversionRule> items;
        private String customLogic;

        // Default constructor
        public ConversionRule() {}

        // Constructor with all fields
        public ConversionRule(String propID, String sourceLocation, String targetLocation, 
                            boolean isArray, List<ConversionRule> items) {
            this.propID = propID;
            this.sourceLocation = sourceLocation;
            this.targetLocation = targetLocation;
            this.isArray = isArray;
            this.items = items;
        }

        // Getters and Setters
        public String getPropID() { return propID; }
        public void setPropID(String propID) { this.propID = propID; }

        public String getSourceLocation() { return sourceLocation; }
        public void setSourceLocation(String sourceLocation) { this.sourceLocation = sourceLocation; }

        public String getTargetLocation() { return targetLocation; }
        public void setTargetLocation(String targetLocation) { this.targetLocation = targetLocation; }

        public boolean isArray() { return isArray; }
        public void setArray(boolean array) { isArray = array; }

        public List<ConversionRule> getItems() { return items; }
        public void setItems(List<ConversionRule> items) { this.items = items; }

        public String getCustomLogic() { return customLogic; }
        public void setCustomLogic(String customLogic) { this.customLogic = customLogic; }
    }

    // Static method to get the default mapping rules
    public static MappingRules getDefaultMappingRules() {
        MappingRules rules = new MappingRules();
        rules.setSourceContentType("JSON");
        rules.setTargetContentType("JSON");
        
        // Create the main conversion rule for EmployeeList
        ConversionRule employeeListRule = new ConversionRule();
        employeeListRule.setPropID("EmployeeList");
        employeeListRule.setSourceLocation("$");
        employeeListRule.setTargetLocation("$");
        employeeListRule.setArray(true);
        
        // Create items for EmployeeList
        List<ConversionRule> employeeItems = List.of(
            new ConversionRule("EMPLOYEE_ID", "employeeId", "employeeId", false, null),
            new ConversionRule("EMPLOYEE_NAME", "name", "employeename", false, null),
            new ConversionRule("AGE", "age", "age", false, null),
            new ConversionRule("GENDER", "gender", "gender", false, null),
            new ConversionRule("EMP_LOCATION", "address.country", "emplocation", false, null),
            new ConversionRule("OFFICE_LOCATION", "officeDetails.location", "officelocation", false, null),
            new ConversionRule("WORK_EXP", "workExperience", "workExperience", true, 
                List.of(new ConversionRule("WORK_EXP_COMPANY", "company", "company", false, null)))
        );
        
        employeeListRule.setItems(employeeItems);
        rules.setConversionRules(List.of(employeeListRule));
        
        return rules;
    }
} 