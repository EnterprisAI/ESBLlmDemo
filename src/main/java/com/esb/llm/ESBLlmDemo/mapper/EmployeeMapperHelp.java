package com.esb.llm.ESBLlmDemo.mapper;

import com.esb.llm.ESBLlmDemo.model.Employee;
import com.esb.llm.ESBLlmDemo.model.OfficeDetails;

import java.time.LocalDate;
import java.time.Period;
import java.util.List;

public class EmployeeMapperHelp {
    
    /**
     * Calculate employee salary based on age, experience, and department
     */
    public static double calculateEmployeeSalary(Employee employee) {
        double baseSalary = 50000.0; // Base salary
        int age = employee.getAge();
        int experienceYears = calculateExperienceYears(employee);
        String department = employee.getOfficeDetails().getDepartment();
        
        // Age-based adjustment
        double ageMultiplier = 1.0;
        if (age >= 25 && age <= 35) {
            ageMultiplier = 1.1; // Young professionals get 10% boost
        } else if (age >= 35 && age <= 45) {
            ageMultiplier = 1.2; // Mid-career professionals get 20% boost
        } else if (age > 45) {
            ageMultiplier = 1.3; // Senior professionals get 30% boost
        }
        
        // Experience-based adjustment
        double experienceMultiplier = 1.0 + (experienceYears * 0.05); // 5% per year
        experienceMultiplier = Math.min(experienceMultiplier, 1.5); // Cap at 50%
        
        // Department-based adjustment
        double departmentMultiplier = 1.0;
        if ("Engineering".equals(department)) {
            departmentMultiplier = 1.15; // Engineering gets 15% boost
        } else if ("Marketing".equals(department)) {
            departmentMultiplier = 1.1; // Marketing gets 10% boost
        } else if ("Sales".equals(department)) {
            departmentMultiplier = 1.2; // Sales gets 20% boost
        }
        
        return baseSalary * ageMultiplier * experienceMultiplier * departmentMultiplier;
    }
    
    /**
     * Calculate experience level based on work experience and age
     */
    public static String calculateExperienceLevel(Employee employee) {
        int experienceYears = calculateExperienceYears(employee);
        int age = employee.getAge();
        
        if (experienceYears < 2) {
            return "Junior";
        } else if (experienceYears >= 2 && experienceYears < 5) {
            return "Mid-Level";
        } else if (experienceYears >= 5 && experienceYears < 10) {
            return "Senior";
        } else if (experienceYears >= 10 && age >= 35) {
            return "Expert";
        } else {
            return "Senior";
        }
    }
    
    /**
     * Calculate performance score based on multiple factors
     */
    public static double calculatePerformanceScore(Employee employee) {
        int age = employee.getAge();
        int experienceYears = calculateExperienceYears(employee);
        String employeeType = employee.getOfficeDetails().getEmployeeType();
        String department = employee.getOfficeDetails().getDepartment();
        
        double score = 5.0; // Base score
        
        // Age-based scoring
        if (age >= 25 && age <= 40) {
            score += 1.0; // Prime working age
        } else if (age > 40) {
            score += 0.5; // Experienced but older
        }
        
        // Experience-based scoring
        if (experienceYears >= 3 && experienceYears <= 8) {
            score += 1.5; // Optimal experience range
        } else if (experienceYears > 8) {
            score += 1.0; // Very experienced
        }
        
        // Employee type scoring
        if ("Full-Time".equals(employeeType)) {
            score += 0.5; // Full-time employees get bonus
        }
        
        // Department-based scoring
        if ("Engineering".equals(department)) {
            score += 0.3; // Engineering gets slight bonus
        } else if ("Sales".equals(department)) {
            score += 0.2; // Sales gets slight bonus
        }
        
        // Cap the score at 10.0
        return Math.min(score, 10.0);
    }
    
    /**
     * Calculate total years of experience based on work experience list
     */
    private static int calculateExperienceYears(Employee employee) {
        List<com.esb.llm.ESBLlmDemo.model.WorkExperience> workExp = employee.getWorkExperience();
        if (workExp == null || workExp.isEmpty()) {
            return 0;
        }
        
        // Simple calculation: assume each work experience entry represents 1-3 years
        // In a real scenario, you'd parse the start and end dates
        return Math.min(workExp.size() * 2, 15); // Cap at 15 years
    }
} 