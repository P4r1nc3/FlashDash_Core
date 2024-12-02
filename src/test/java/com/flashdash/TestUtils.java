package com.flashdash;

import com.flashdash.model.User;

public class TestUtils {
    public static User createDefaultTestUser() {
        User user = new User();
        user.setUsername("test@example.com");
        user.setPassword("password123");
        user.setFirstName("Test");
        user.setLastName("User");
        return user;
    }
}
