package com.esb.llm.ESBLlmDemo.model;

import java.util.List;

public class Employee {
    private String employeeId;
    private String name;
    private String gender;
    private int age;
    private Address address;
    private OfficeDetails officeDetails;
    private List<WorkExperience> workExperience;

    // Default constructor
    public Employee() {}

    // Constructor with all fields
    public Employee(String employeeId, String name, String gender, int age, 
                   Address address, OfficeDetails officeDetails, List<WorkExperience> workExperience) {
        this.employeeId = employeeId;
        this.name = name;
        this.gender = gender;
        this.age = age;
        this.address = address;
        this.officeDetails = officeDetails;
        this.workExperience = workExperience;
    }

    // Getters and Setters
    public String getEmployeeId() { return employeeId; }
    public void setEmployeeId(String employeeId) { this.employeeId = employeeId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getGender() { return gender; }
    public void setGender(String gender) { this.gender = gender; }

    public int getAge() { return age; }
    public void setAge(int age) { this.age = age; }

    public Address getAddress() { return address; }
    public void setAddress(Address address) { this.address = address; }

    public OfficeDetails getOfficeDetails() { return officeDetails; }
    public void setOfficeDetails(OfficeDetails officeDetails) { this.officeDetails = officeDetails; }

    public List<WorkExperience> getWorkExperience() { return workExperience; }
    public void setWorkExperience(List<WorkExperience> workExperience) { this.workExperience = workExperience; }

    @Override
    public String toString() {
        return "Employee{" +
                "employeeId='" + employeeId + '\'' +
                ", name='" + name + '\'' +
                ", gender='" + gender + '\'' +
                ", age=" + age +
                ", address=" + address +
                ", officeDetails=" + officeDetails +
                ", workExperience=" + workExperience +
                '}';
    }
} 