package com.esb.llm.ESBLlmDemo.controller;

import com.esb.llm.ESBLlmDemo.mapper.SourceTargetMapper;
import com.esb.llm.ESBLlmDemo.model.SourceDto;
import com.esb.llm.ESBLlmDemo.model.TargetDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

@RestController
@RequestMapping("/api/mapper")
public class SourceTargetController {

    @Autowired
    private SourceTargetMapper sourceTargetMapper;

    @PostMapping("/transform")
    public ResponseEntity<TargetDto> transformSourceToTarget(@RequestBody SourceDto sourceDto) {
        TargetDto targetDto = sourceTargetMapper.sourceDtoToTargetDto(sourceDto);
        return ResponseEntity.ok(targetDto);
    }

    @GetMapping("/demo")
    public ResponseEntity<TargetDto> getDemoTransformation() {
        // Create a sample SourceDto for demonstration
        SourceDto sourceDto = new SourceDto();
        sourceDto.setId("DEMO001");
        sourceDto.setAge(32);
        sourceDto.setEmailList(Arrays.asList("demo@company.com", "demo.personal@gmail.com"));
        sourceDto.setAddressList(Arrays.asList("789 Demo St, Demo City", "321 Test Ave, Test Town"));
        sourceDto.setPhoneNumbers(Arrays.asList("+1-555-111-2222", "+1-555-333-4444"));
        sourceDto.setSalary(new BigDecimal("85000.00"));
        sourceDto.setDoj(LocalDate.of(2019, 7, 1));

        TargetDto targetDto = sourceTargetMapper.sourceDtoToTargetDto(sourceDto);
        return ResponseEntity.ok(targetDto);
    }

    @GetMapping("/salary-calculation-demo")
    public ResponseEntity<List<TargetDto>> getSalaryCalculationDemo() {
        // Create multiple scenarios to demonstrate salary calculation
        SourceDto[] scenarios = {
            createSourceDto("SCENARIO1", 25, new BigDecimal("50000.00"), LocalDate.of(2022, 1, 1)),
            createSourceDto("SCENARIO2", 35, new BigDecimal("80000.00"), LocalDate.of(2018, 3, 15)),
            createSourceDto("SCENARIO3", 50, new BigDecimal("120000.00"), LocalDate.of(2010, 6, 10))
        };

        List<TargetDto> results = Arrays.stream(scenarios)
            .map(sourceTargetMapper::sourceDtoToTargetDto)
            .toList();

        return ResponseEntity.ok(results);
    }

    private SourceDto createSourceDto(String id, int age, BigDecimal salary, LocalDate doj) {
        SourceDto dto = new SourceDto();
        dto.setId(id);
        dto.setAge(age);
        dto.setEmailList(Arrays.asList(id.toLowerCase() + "@company.com"));
        dto.setAddressList(Arrays.asList("Address for " + id));
        dto.setPhoneNumbers(Arrays.asList("+1-555-" + id.substring(7) + "-0000"));
        dto.setSalary(salary);
        dto.setDoj(doj);
        return dto;
    }
} 