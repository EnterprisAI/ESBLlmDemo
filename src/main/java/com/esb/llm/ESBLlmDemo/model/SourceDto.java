package com.esb.llm.ESBLlmDemo.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public class SourceDto {
    private String id;
    private int age;
    private List<String> emailList;
    private List<String> addressList;
    private List<String> phoneNumbers;
    private BigDecimal salary;
    private LocalDate doj;

    // Constructors
    public SourceDto() {}

    public SourceDto(String id, int age, List<String> emailList, List<String> addressList, 
                    List<String> phoneNumbers, BigDecimal salary, LocalDate doj) {
        this.id = id;
        this.age = age;
        this.emailList = emailList;
        this.addressList = addressList;
        this.phoneNumbers = phoneNumbers;
        this.salary = salary;
        this.doj = doj;
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public List<String> getEmailList() {
        return emailList;
    }

    public void setEmailList(List<String> emailList) {
        this.emailList = emailList;
    }

    public List<String> getAddressList() {
        return addressList;
    }

    public void setAddressList(List<String> addressList) {
        this.addressList = addressList;
    }

    public List<String> getPhoneNumbers() {
        return phoneNumbers;
    }

    public void setPhoneNumbers(List<String> phoneNumbers) {
        this.phoneNumbers = phoneNumbers;
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
} 