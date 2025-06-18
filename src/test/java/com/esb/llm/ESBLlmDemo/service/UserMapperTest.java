package com.esb.llm.ESBLlmDemo.service;

import com.esb.llm.ESBLlmDemo.mapper.UserMapper;
import com.esb.llm.ESBLlmDemo.model.User;
import com.esb.llm.ESBLlmDemo.model.TargetUser;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class UserMapperTest {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    public void testUserMapping() throws Exception {
        // Create source user
        User.UserAddress address = new User.UserAddress("456 Oak St", "San Francisco", "CA", "94105", "USA");
        User.UserProfile profile = new User.UserProfile("profile.jpg", "Software Engineer", "Developer", "TechCorp");
        User.UserPreference pref1 = new User.UserPreference("Theme", "Dark", true);
        User.UserPreference pref2 = new User.UserPreference("Language", "English", true);
        List<User.UserPreference> preferences = Arrays.asList(pref1, pref2);
        User.UserAccount account = new User.UserAccount("Premium", "Active", LocalDate.now().minusDays(30), "2024-01-15");
        
        User sourceUser = new User("USER001", "Jane", "Smith", "jane@example.com", "+1-555-0123",
                                 LocalDate.of(1990, 5, 15), address, profile, preferences, account);

        // Map to target
        TargetUser targetUser = userMapper.userToTargetUser(sourceUser);

        // Verify mapping
        assertNotNull(targetUser);
        assertEquals("USER001", targetUser.getId());
        assertEquals("Jane Smith", targetUser.getFullName());
        assertEquals("jane@example.com | +1-555-0123", targetUser.getContactInfo());
        assertEquals("1990-05-15", targetUser.getBirthDate());
        assertTrue(targetUser.getAddress().contains("456 Oak St"));
        assertTrue(targetUser.getProfileInfo().contains("Developer"));
        assertEquals(2, targetUser.getPreferences().size());
        assertTrue(targetUser.getAccountInfo().contains("Premium"));
        assertTrue(targetUser.getAge() > 0);

        // Convert to JSON for verification
        String sourceJson = objectMapper.writeValueAsString(sourceUser);
        String targetJson = objectMapper.writeValueAsString(targetUser);

        System.out.println("=== User Mapping Test ===");
        System.out.println("Source JSON:");
        System.out.println(sourceJson);
        System.out.println("\nTarget JSON:");
        System.out.println(targetJson);
        System.out.println("=== Test Complete ===\n");
    }
} 