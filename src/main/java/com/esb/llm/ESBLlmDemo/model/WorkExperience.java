package com.esb.llm.ESBLlmDemo.model;

public class WorkExperience {
    private String company;
    private String role;
    private String startDate;
    private String endDate;
    private String location;

    // Default constructor
    public WorkExperience() {}

    // Constructor with all fields
    public WorkExperience(String company, String role, String startDate, String endDate, String location) {
        this.company = company;
        this.role = role;
        this.startDate = startDate;
        this.endDate = endDate;
        this.location = location;
    }

    // Getters and Setters
    public String getCompany() { return company; }
    public void setCompany(String company) { this.company = company; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public String getStartDate() { return startDate; }
    public void setStartDate(String startDate) { this.startDate = startDate; }

    public String getEndDate() { return endDate; }
    public void setEndDate(String endDate) { this.endDate = endDate; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    @Override
    public String toString() {
        return "WorkExperience{" +
                "company='" + company + '\'' +
                ", role='" + role + '\'' +
                ", startDate='" + startDate + '\'' +
                ", endDate='" + endDate + '\'' +
                ", location='" + location + '\'' +
                '}';
    }
} 