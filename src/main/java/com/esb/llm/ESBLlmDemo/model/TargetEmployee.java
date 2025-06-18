package com.esb.llm.ESBLlmDemo.model;

import java.util.List;

public class TargetEmployee {
    private String employeeId;
    private String employeename;
    private int age;
    private String gender;
    private String emplocation;
    private String officelocation;
    private List<TargetWorkExperience> workExperience;

    // Default constructor
    public TargetEmployee() {}

    // Constructor with all fields
    public TargetEmployee(String employeeId, String employeename, int age, String gender, 
                         String emplocation, String officelocation, List<TargetWorkExperience> workExperience) {
        this.employeeId = employeeId;
        this.employeename = employeename;
        this.age = age;
        this.gender = gender;
        this.emplocation = emplocation;
        this.officelocation = officelocation;
        this.workExperience = workExperience;
    }

    // Getters and Setters
    public String getEmployeeId() { return employeeId; }
    public void setEmployeeId(String employeeId) { this.employeeId = employeeId; }

    public String getEmployeename() { return employeename; }
    public void setEmployeename(String employeename) { this.employeename = employeename; }

    public int getAge() { return age; }
    public void setAge(int age) { this.age = age; }

    public String getGender() { return gender; }
    public void setGender(String gender) { this.gender = gender; }

    public String getEmplocation() { return emplocation; }
    public void setEmplocation(String emplocation) { this.emplocation = emplocation; }

    public String getOfficelocation() { return officelocation; }
    public void setOfficelocation(String officelocation) { this.officelocation = officelocation; }

    public List<TargetWorkExperience> getWorkExperience() { return workExperience; }
    public void setWorkExperience(List<TargetWorkExperience> workExperience) { this.workExperience = workExperience; }

    @Override
    public String toString() {
        return "TargetEmployee{" +
                "employeeId='" + employeeId + '\'' +
                ", employeename='" + employeename + '\'' +
                ", age=" + age +
                ", gender='" + gender + '\'' +
                ", emplocation='" + emplocation + '\'' +
                ", officelocation='" + officelocation + '\'' +
                ", workExperience=" + workExperience +
                '}';
    }
} 