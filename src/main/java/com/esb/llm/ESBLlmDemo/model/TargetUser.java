package com.esb.llm.ESBLlmDemo.model;

import java.time.LocalDate;
import java.util.List;

public class TargetUser {
    private String id;
    private String fullName;
    private String contactInfo;
    private String birthDate;
    private String address;
    private String profileInfo;
    private List<TargetUserPreference> preferences;
    private String accountInfo;
    private int age;

    // Constructors
    public TargetUser() {}

    public TargetUser(String id, String fullName, String contactInfo, String birthDate,
                     String address, String profileInfo, List<TargetUserPreference> preferences,
                     String accountInfo, int age) {
        this.id = id;
        this.fullName = fullName;
        this.contactInfo = contactInfo;
        this.birthDate = birthDate;
        this.address = address;
        this.profileInfo = profileInfo;
        this.preferences = preferences;
        this.accountInfo = accountInfo;
        this.age = age;
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getContactInfo() {
        return contactInfo;
    }

    public void setContactInfo(String contactInfo) {
        this.contactInfo = contactInfo;
    }

    public String getBirthDate() {
        return birthDate;
    }

    public void setBirthDate(String birthDate) {
        this.birthDate = birthDate;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getProfileInfo() {
        return profileInfo;
    }

    public void setProfileInfo(String profileInfo) {
        this.profileInfo = profileInfo;
    }

    public List<TargetUserPreference> getPreferences() {
        return preferences;
    }

    public void setPreferences(List<TargetUserPreference> preferences) {
        this.preferences = preferences;
    }

    public String getAccountInfo() {
        return accountInfo;
    }

    public void setAccountInfo(String accountInfo) {
        this.accountInfo = accountInfo;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    // Inner class
    public static class TargetUserPreference {
        private String type;
        private String value;
        private boolean enabled;

        public TargetUserPreference() {}

        public TargetUserPreference(String type, String value, boolean enabled) {
            this.type = type;
            this.value = value;
            this.enabled = enabled;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }
    }
} 