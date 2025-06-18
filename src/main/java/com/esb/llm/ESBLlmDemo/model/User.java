package com.esb.llm.ESBLlmDemo.model;

import java.time.LocalDate;
import java.util.List;

public class User {
    private String userId;
    private String firstName;
    private String lastName;
    private String emailAddress;
    private String phoneNumber;
    private LocalDate dateOfBirth;
    private UserAddress residentialAddress;
    private UserProfile userProfile;
    private List<UserPreference> userPreferences;
    private UserAccount accountDetails;

    // Constructors
    public User() {}

    public User(String userId, String firstName, String lastName, String emailAddress, String phoneNumber,
                LocalDate dateOfBirth, UserAddress residentialAddress, UserProfile userProfile,
                List<UserPreference> userPreferences, UserAccount accountDetails) {
        this.userId = userId;
        this.firstName = firstName;
        this.lastName = lastName;
        this.emailAddress = emailAddress;
        this.phoneNumber = phoneNumber;
        this.dateOfBirth = dateOfBirth;
        this.residentialAddress = residentialAddress;
        this.userProfile = userProfile;
        this.userPreferences = userPreferences;
        this.accountDetails = accountDetails;
    }

    // Getters and Setters
    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getEmailAddress() {
        return emailAddress;
    }

    public void setEmailAddress(String emailAddress) {
        this.emailAddress = emailAddress;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public LocalDate getDateOfBirth() {
        return dateOfBirth;
    }

    public void setDateOfBirth(LocalDate dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    public UserAddress getResidentialAddress() {
        return residentialAddress;
    }

    public void setResidentialAddress(UserAddress residentialAddress) {
        this.residentialAddress = residentialAddress;
    }

    public UserProfile getUserProfile() {
        return userProfile;
    }

    public void setUserProfile(UserProfile userProfile) {
        this.userProfile = userProfile;
    }

    public List<UserPreference> getUserPreferences() {
        return userPreferences;
    }

    public void setUserPreferences(List<UserPreference> userPreferences) {
        this.userPreferences = userPreferences;
    }

    public UserAccount getAccountDetails() {
        return accountDetails;
    }

    public void setAccountDetails(UserAccount accountDetails) {
        this.accountDetails = accountDetails;
    }

    // Inner classes
    public static class UserAddress {
        private String streetAddress;
        private String cityName;
        private String stateCode;
        private String postalCode;
        private String countryName;

        public UserAddress() {}

        public UserAddress(String streetAddress, String cityName, String stateCode, String postalCode, String countryName) {
            this.streetAddress = streetAddress;
            this.cityName = cityName;
            this.stateCode = stateCode;
            this.postalCode = postalCode;
            this.countryName = countryName;
        }

        public String getStreetAddress() {
            return streetAddress;
        }

        public void setStreetAddress(String streetAddress) {
            this.streetAddress = streetAddress;
        }

        public String getCityName() {
            return cityName;
        }

        public void setCityName(String cityName) {
            this.cityName = cityName;
        }

        public String getStateCode() {
            return stateCode;
        }

        public void setStateCode(String stateCode) {
            this.stateCode = stateCode;
        }

        public String getPostalCode() {
            return postalCode;
        }

        public void setPostalCode(String postalCode) {
            this.postalCode = postalCode;
        }

        public String getCountryName() {
            return countryName;
        }

        public void setCountryName(String countryName) {
            this.countryName = countryName;
        }
    }

    public static class UserProfile {
        private String profilePicture;
        private String bio;
        private String occupation;
        private String company;

        public UserProfile() {}

        public UserProfile(String profilePicture, String bio, String occupation, String company) {
            this.profilePicture = profilePicture;
            this.bio = bio;
            this.occupation = occupation;
            this.company = company;
        }

        public String getProfilePicture() {
            return profilePicture;
        }

        public void setProfilePicture(String profilePicture) {
            this.profilePicture = profilePicture;
        }

        public String getBio() {
            return bio;
        }

        public void setBio(String bio) {
            this.bio = bio;
        }

        public String getOccupation() {
            return occupation;
        }

        public void setOccupation(String occupation) {
            this.occupation = occupation;
        }

        public String getCompany() {
            return company;
        }

        public void setCompany(String company) {
            this.company = company;
        }
    }

    public static class UserPreference {
        private String preferenceType;
        private String preferenceValue;
        private boolean isActive;

        public UserPreference() {}

        public UserPreference(String preferenceType, String preferenceValue, boolean isActive) {
            this.preferenceType = preferenceType;
            this.preferenceValue = preferenceValue;
            this.isActive = isActive;
        }

        public String getPreferenceType() {
            return preferenceType;
        }

        public void setPreferenceType(String preferenceType) {
            this.preferenceType = preferenceType;
        }

        public String getPreferenceValue() {
            return preferenceValue;
        }

        public void setPreferenceValue(String preferenceValue) {
            this.preferenceValue = preferenceValue;
        }

        public boolean isActive() {
            return isActive;
        }

        public void setActive(boolean active) {
            isActive = active;
        }
    }

    public static class UserAccount {
        private String accountType;
        private String accountStatus;
        private LocalDate accountCreatedDate;
        private String lastLoginDate;

        public UserAccount() {}

        public UserAccount(String accountType, String accountStatus, LocalDate accountCreatedDate, String lastLoginDate) {
            this.accountType = accountType;
            this.accountStatus = accountStatus;
            this.accountCreatedDate = accountCreatedDate;
            this.lastLoginDate = lastLoginDate;
        }

        public String getAccountType() {
            return accountType;
        }

        public void setAccountType(String accountType) {
            this.accountType = accountType;
        }

        public String getAccountStatus() {
            return accountStatus;
        }

        public void setAccountStatus(String accountStatus) {
            this.accountStatus = accountStatus;
        }

        public LocalDate getAccountCreatedDate() {
            return accountCreatedDate;
        }

        public void setAccountCreatedDate(LocalDate accountCreatedDate) {
            this.accountCreatedDate = accountCreatedDate;
        }

        public String getLastLoginDate() {
            return lastLoginDate;
        }

        public void setLastLoginDate(String lastLoginDate) {
            this.lastLoginDate = lastLoginDate;
        }
    }
} 