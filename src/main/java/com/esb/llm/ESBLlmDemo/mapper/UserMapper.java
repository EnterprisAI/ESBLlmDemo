package com.esb.llm.ESBLlmDemo.mapper;

import com.esb.llm.ESBLlmDemo.model.User;
import com.esb.llm.ESBLlmDemo.model.TargetUser;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface UserMapper {

    @Mapping(source = "userId", target = "id")
    @Mapping(target = "fullName", expression = "java(user.getFirstName() + \" \" + user.getLastName())")
    @Mapping(target = "contactInfo", expression = "java(user.getEmailAddress() + \" | \" + user.getPhoneNumber())")
    @Mapping(source = "dateOfBirth", target = "birthDate")
    @Mapping(target = "address", expression = "java(user.getResidentialAddress().getStreetAddress() + \", \" + user.getResidentialAddress().getCityName() + \", \" + user.getResidentialAddress().getStateCode() + \" \" + user.getResidentialAddress().getPostalCode())")
    @Mapping(target = "profileInfo", expression = "java(user.getUserProfile().getOccupation() + \" at \" + user.getUserProfile().getCompany())")
    @Mapping(source = "userPreferences", target = "preferences")
    @Mapping(target = "accountInfo", expression = "java(user.getAccountDetails().getAccountType() + \" - \" + user.getAccountDetails().getAccountStatus())")
    @Mapping(target = "age", expression = "java(java.time.LocalDate.now().getYear() - user.getDateOfBirth().getYear())")
    TargetUser userToTargetUser(User user);

    @Mapping(source = "preferenceType", target = "type")
    @Mapping(source = "preferenceValue", target = "value")
    @Mapping(source = "active", target = "enabled")
    TargetUser.TargetUserPreference userPreferenceToTargetUserPreference(User.UserPreference pref);
} 