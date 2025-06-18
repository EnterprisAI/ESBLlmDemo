package com.esb.llm.ESBLlmDemo.model;

public class TargetWorkExperience {
    private String company;

    // Default constructor
    public TargetWorkExperience() {}

    // Constructor with company field
    public TargetWorkExperience(String company) {
        this.company = company;
    }

    // Getters and Setters
    public String getCompany() { return company; }
    public void setCompany(String company) { this.company = company; }

    @Override
    public String toString() {
        return "TargetWorkExperience{" +
                "company='" + company + '\'' +
                '}';
    }
} 