package com.esb.llm.ESBLlmDemo.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public class TargetDto {
    private String userId;
    private int age;
    private List<String> emails;
    private List<String> phoneNumberList;
    private BigDecimal salary;
    private LocalDate doj;
    private BigDecimal bonus;

    // Constructors
    public TargetDto() {}

    public TargetDto(String userId, int age, List<String> emails, List<String> phoneNumberList, 
                    BigDecimal salary, LocalDate doj) {
        this.userId = userId;
        this.age = age;
        this.emails = emails;
        this.phoneNumberList = phoneNumberList;
        this.salary = salary;
        this.doj = doj;
    }

    // Getters and Setters
    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public List<String> getEmails() {
        return emails;
    }

    public void setEmails(List<String> emails) {
        this.emails = emails;
    }

    public List<String> getPhoneNumberList() {
        return phoneNumberList;
    }

    public void setPhoneNumberList(List<String> phoneNumberList) {
        this.phoneNumberList = phoneNumberList;
    }

    public BigDecimal getSalary() {
        return salary;
    }

    public void setSalary(BigDecimal salary) {
        this.salary = salary;
    }

    public LocalDate getDoj() {
        return doj;
    }

    public void setDoj(LocalDate doj) {
        this.doj = doj;
    }

    public BigDecimal getBonus() {
        return bonus;
    }

    public void setBonus(BigDecimal bonus) {
        this.bonus = bonus;
    }
} 