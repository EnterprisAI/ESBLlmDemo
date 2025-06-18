package com.esb.llm.ESBLlmDemo.mapper;

import com.esb.llm.ESBLlmDemo.model.*;
import org.mapstruct.*;
import org.mapstruct.factory.Mappers;

import java.util.List;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public interface EmployeeMapper {
    EmployeeMapper INSTANCE = Mappers.getMapper(EmployeeMapper.class);

    @Mappings({
        @Mapping(source = "employeeId", target = "employeeId"),
        @Mapping(source = "name", target = "employeename"),
        @Mapping(source = "age", target = "age"),
        @Mapping(source = "gender", target = "gender"),
        @Mapping(source = "address.country", target = "emplocation"),
        @Mapping(source = "officeDetails.location", target = "officelocation"),
        @Mapping(source = "workExperience", target = "workExperience")
    })
    TargetEmployee employeeToTargetEmployee(Employee employee);

    List<TargetEmployee> employeesToTargetEmployees(List<Employee> employees);

    @Mapping(source = "company", target = "company")
    TargetWorkExperience workExperienceToTargetWorkExperience(WorkExperience workExperience);

    default List<TargetWorkExperience> workExperienceListToTargetList(List<WorkExperience> workExperience) {
        if (workExperience == null) return null;
        return workExperience.stream()
                .map(this::workExperienceToTargetWorkExperience)
                .collect(Collectors.toList());
    }

    @AfterMapping
    default void mapWorkExperience(Employee employee, @MappingTarget TargetEmployee targetEmployee) {
        targetEmployee.setWorkExperience(workExperienceListToTargetList(employee.getWorkExperience()));
    }
} 