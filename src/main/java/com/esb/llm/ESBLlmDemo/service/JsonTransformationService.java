package com.esb.llm.ESBLlmDemo.service;

import com.esb.llm.ESBLlmDemo.mapper.EmployeeMapper;
import com.esb.llm.ESBLlmDemo.model.Employee;
import com.esb.llm.ESBLlmDemo.model.TargetEmployee;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class JsonTransformationService {

    @Autowired
    private EmployeeMapper employeeMapper;

    @Autowired
    private ObjectMapper objectMapper;

    /**
     * Transform source JSON to target JSON using MapStruct
     * @param sourceJson The source JSON string
     * @return The transformed JSON string
     */
    public String transformJson(String sourceJson) {
        try {
            // Parse source JSON to List<Employee>
            List<Employee> employees = objectMapper.readValue(sourceJson, new TypeReference<List<Employee>>() {});
            // Map to List<TargetEmployee>
            List<TargetEmployee> targetEmployees = employeeMapper.employeesToTargetEmployees(employees);
            // Convert back to JSON string
            return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(targetEmployees);
        } catch (Exception e) {
            throw new RuntimeException("Error transforming JSON: " + e.getMessage(), e);
        }
    }

    /**
     * Validate if the source JSON matches the expected structure
     * @param sourceJson The source JSON string
     * @return true if valid, false otherwise
     */
    public boolean validateSourceJson(String sourceJson) {
        try {
            objectMapper.readValue(sourceJson, new TypeReference<List<Employee>>() {});
            return true;
        } catch (Exception e) {
            return false;
        }
    }
} 