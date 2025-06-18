package com.esb.llm.ESBLlmDemo.model;

public class OfficeDetails {
    private String department;
    private String designation;
    private String location;
    private String employeeType;

    // Default constructor
    public OfficeDetails() {}

    // Constructor with all fields
    public OfficeDetails(String department, String designation, String location, String employeeType) {
        this.department = department;
        this.designation = designation;
        this.location = location;
        this.employeeType = employeeType;
    }

    // Getters and Setters
    public String getDepartment() { return department; }
    public void setDepartment(String department) { this.department = department; }

    public String getDesignation() { return designation; }
    public void setDesignation(String designation) { this.designation = designation; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public String getEmployeeType() { return employeeType; }
    public void setEmployeeType(String employeeType) { this.employeeType = employeeType; }

    @Override
    public String toString() {
        return "OfficeDetails{" +
                "department='" + department + '\'' +
                ", designation='" + designation + '\'' +
                ", location='" + location + '\'' +
                ", employeeType='" + employeeType + '\'' +
                '}';
    }
} 