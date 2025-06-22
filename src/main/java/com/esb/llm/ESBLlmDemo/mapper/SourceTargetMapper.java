package com.esb.llm.ESBLlmDemo.mapper;

import com.esb.llm.ESBLlmDemo.model.SourceDto;
import com.esb.llm.ESBLlmDemo.model.TargetDto;
import org.mapstruct.*;

@Mapper(componentModel = "spring", uses = {UserMapperHelp.class})
public interface SourceTargetMapper {

    @Mapping(source = "id", target = "userId")
    @Mapping(source = "emailList", target = "emails")
    @Mapping(source = "phoneNumbers", target = "phoneNumberList")
    TargetDto sourceDtoToTargetDto(SourceDto sourceDto);

    @AfterMapping
    default void setAdjustedSalary(SourceDto sourceDto, @MappingTarget TargetDto targetDto) {
        UserMapperHelp helper = new UserMapperHelp();
        targetDto.setSalary(helper.calculateAdjustedSalary(sourceDto.getSalary(), sourceDto.getAge(), sourceDto.getDoj()));
    }
} 