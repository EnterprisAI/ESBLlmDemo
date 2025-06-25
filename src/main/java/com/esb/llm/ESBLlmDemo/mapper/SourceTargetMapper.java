package com.esb.llm.ESBLlmDemo.mapper;

import com.esb.llm.ESBLlmDemo.model.SourceDto;
import com.esb.llm.ESBLlmDemo.model.TargetDto;
import org.mapstruct.*;

@Mapper(componentModel = "spring", uses = {UserMapperHelp.class})
public interface SourceTargetMapper {

    @Mapping(source = "id", target = "userId")
//    @Mapping(source = "emailList", target = "emails")
    @Mapping(source = "phoneNumbers", target = "phoneNumberList")
    @Mapping(source = "doj", target = "doj")
    @Mapping(target = "salary", expression = "java(calculateSalaryWithAdjustment(sourceDto))")
    @Mapping(target = "age", expression = "java(calculateAdjustedAge(sourceDto.getAge(), sourceDto.getDoj()))")
    @Mapping(target = "bonus", expression = "java(calculateBonusWithHistory(sourceDto))")
    TargetDto sourceDtoToTargetDto(SourceDto sourceDto);

    default java.math.BigDecimal calculateSalaryWithAdjustment(SourceDto sourceDto) {
        UserMapperHelp helper = new UserMapperHelp();
        return helper.calculateAdjustedSalary(sourceDto.getSalary(), sourceDto.getAge(), sourceDto.getDoj());
    }
    
    default int calculateAdjustedAge(int age, java.time.LocalDate doj) {
        // Calculate adjusted age based on experience
        if (doj != null) {
            long yearsOfExperience = java.time.temporal.ChronoUnit.YEARS.between(doj, java.time.LocalDate.now());
            // Add experience bonus to age calculation
            return age + (int)(yearsOfExperience * 0.5);
        }
        return age;
    }

    default java.math.BigDecimal calculateBonusWithHistory(SourceDto sourceDto) {
        UserMapperHelp helper = new UserMapperHelp();
        return helper.calculateBonusHistory(sourceDto.getSalary(), sourceDto.getAge(), sourceDto.getDoj(), sourceDto.getId());
    }
} 