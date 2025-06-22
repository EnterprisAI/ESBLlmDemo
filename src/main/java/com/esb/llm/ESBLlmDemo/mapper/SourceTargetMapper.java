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
//    @Mapping(target = "salary", expression = "java(calculateSalaryWithAdjustment(sourceDto))")
    TargetDto sourceDtoToTargetDto(SourceDto sourceDto);

    default java.math.BigDecimal calculateSalaryWithAdjustment(SourceDto sourceDto) {
        UserMapperHelp helper = new UserMapperHelp();
        return helper.calculateAdjustedSalary(sourceDto.getSalary(), sourceDto.getAge(), sourceDto.getDoj());
    }
} 