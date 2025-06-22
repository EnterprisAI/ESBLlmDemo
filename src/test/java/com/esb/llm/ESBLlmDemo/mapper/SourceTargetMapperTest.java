package com.esb.llm.ESBLlmDemo.mapper;

import com.esb.llm.ESBLlmDemo.model.SourceDto;
import com.esb.llm.ESBLlmDemo.model.TargetDto;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class SourceTargetMapperTest {

    @Autowired
    private SourceTargetMapper sourceTargetMapper;

    @Test
    public void testSourceDtoToTargetDtoMapping() {
        // Create sample source data
        SourceDto sourceDto = new SourceDto();
        sourceDto.setId("EMP001");
        sourceDto.setAge(30);
        sourceDto.setEmailList(Arrays.asList("john.doe@company.com", "john.doe.personal@gmail.com"));
        sourceDto.setAddressList(Arrays.asList("123 Main St, City", "456 Oak Ave, Town"));
        sourceDto.setPhoneNumbers(Arrays.asList("+1-555-123-4567", "+1-555-987-6543"));
        sourceDto.setSalary(new BigDecimal("75000.00"));
        sourceDto.setDoj(LocalDate.of(2020, 3, 15));

        // Perform mapping
        TargetDto targetDto = sourceTargetMapper.sourceDtoToTargetDto(sourceDto);

        // Verify basic field mappings
        assertNotNull(targetDto);
        assertEquals("EMP001", targetDto.getUserId());
        assertEquals(30, targetDto.getAge());
        assertEquals(Arrays.asList("john.doe@company.com", "john.doe.personal@gmail.com"), targetDto.getEmails());
        assertEquals(Arrays.asList("+1-555-123-4567", "+1-555-987-6543"), targetDto.getPhoneNumberList());
        assertEquals(LocalDate.of(2020, 3, 15), targetDto.getDoj());

        // Verify salary calculation (should be different from base salary due to adjustments)
        assertNotNull(targetDto.getSalary());
        assertNotEquals(new BigDecimal("75000.00"), targetDto.getSalary());
        assertTrue(targetDto.getSalary().compareTo(BigDecimal.ZERO) > 0);

        System.out.println("Original Salary: " + sourceDto.getSalary());
        System.out.println("Adjusted Salary: " + targetDto.getSalary());
        System.out.println("Salary Difference: " + targetDto.getSalary().subtract(sourceDto.getSalary()));
    }

    @Test
    public void testSalaryCalculationWithDifferentScenarios() {
        // Test scenario 1: Young employee with moderate experience
        SourceDto youngEmployee = createSourceDto("EMP002", 28, new BigDecimal("60000.00"), 
            LocalDate.of(2021, 6, 1));
        TargetDto result1 = sourceTargetMapper.sourceDtoToTargetDto(youngEmployee);
        
        // Test scenario 2: Senior employee with long experience
        SourceDto seniorEmployee = createSourceDto("EMP003", 45, new BigDecimal("100000.00"), 
            LocalDate.of(2010, 1, 15));
        TargetDto result2 = sourceTargetMapper.sourceDtoToTargetDto(seniorEmployee);

        // Test scenario 3: Very senior employee
        SourceDto verySeniorEmployee = createSourceDto("EMP004", 55, new BigDecimal("120000.00"), 
            LocalDate.of(2005, 8, 20));
        TargetDto result3 = sourceTargetMapper.sourceDtoToTargetDto(verySeniorEmployee);

        // Print results for comparison
        System.out.println("\n=== Salary Calculation Results ===");
        System.out.println("Young Employee (28y, 2y exp): " + youngEmployee.getSalary() + " -> " + result1.getSalary());
        System.out.println("Senior Employee (45y, 13y exp): " + seniorEmployee.getSalary() + " -> " + result2.getSalary());
        System.out.println("Very Senior Employee (55y, 18y exp): " + verySeniorEmployee.getSalary() + " -> " + result3.getSalary());

        // Verify all salaries are calculated and different from base
        assertNotNull(result1.getSalary());
        assertNotNull(result2.getSalary());
        assertNotNull(result3.getSalary());
        assertNotEquals(youngEmployee.getSalary(), result1.getSalary());
        assertNotEquals(seniorEmployee.getSalary(), result2.getSalary());
        assertNotEquals(verySeniorEmployee.getSalary(), result3.getSalary());
    }

    private SourceDto createSourceDto(String id, int age, BigDecimal salary, LocalDate doj) {
        SourceDto dto = new SourceDto();
        dto.setId(id);
        dto.setAge(age);
        dto.setEmailList(Arrays.asList("test@company.com"));
        dto.setAddressList(Arrays.asList("Test Address"));
        dto.setPhoneNumbers(Arrays.asList("+1-555-000-0000"));
        dto.setSalary(salary);
        dto.setDoj(doj);
        return dto;
    }
} 