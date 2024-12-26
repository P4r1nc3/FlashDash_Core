package com.flashdash.service;

import com.flashdash.FlashDashApplication;
import com.flashdash.TestUtils;
import com.flashdash.model.User;
import com.flashdash.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.util.Arrays;
import java.util.List;

import static org.mockito.Mockito.*;

@SpringBootTest(classes = FlashDashApplication.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class NotificationSchedulerTest {
    @Autowired
    private NotificationScheduler notificationScheduler;

    @MockBean
    private UserRepository userRepository;

    @MockBean
    private EmailService emailService;

    @Test
    void shouldSendDailyNotificationsToUsersWithNotificationsEnabled() {
        // Arrange
        User user1 = TestUtils.createUser();
        user1.setDailyNotifications(true);

        User user2 = TestUtils.createFriendUser();
        user2.setDailyNotifications(true);

        // Only return users with dailyNotifications set to true
        List<User> users = Arrays.asList(user1, user2);

        when(userRepository.findByDailyNotificationsTrue()).thenReturn(users);

        // Act
        notificationScheduler.sendDailyNotifications();

        // Assert
        verify(emailService, times(1)).sendDailyNotificationEmail(user1.getUsername());
        verify(emailService, times(1)).sendDailyNotificationEmail(user2.getUsername());
    }

    @Test
    void shouldNotSendNotificationsToUsersWithoutNotificationsEnabled() {
        // Arrange
        User user1 = TestUtils.createUser();
        user1.setDailyNotifications(true);

        User user2 = TestUtils.createFriendUser();
        user2.setDailyNotifications(false);

        List<User> users = Arrays.asList(user1);

        when(userRepository.findByDailyNotificationsTrue()).thenReturn(users);

        // Act
        notificationScheduler.sendDailyNotifications();

        // Assert
        verify(emailService, times(1)).sendDailyNotificationEmail(user1.getUsername());
        verify(emailService, times(0)).sendDailyNotificationEmail(user2.getUsername());
    }

    @Test
    void shouldNotSendNotificationsIfNoUsersHaveDailyNotificationsEnabled() {
        // Arrange
        User user1 = TestUtils.createUser();
        user1.setDailyNotifications(false);

        User user2 = TestUtils.createFriendUser();
        user2.setDailyNotifications(false);

        List<User> users = Arrays.asList();

        when(userRepository.findByDailyNotificationsTrue()).thenReturn(users);

        // Act
        notificationScheduler.sendDailyNotifications();

        // Assert
        verify(emailService, times(0)).sendDailyNotificationEmail(user1.getUsername());
        verify(emailService, times(0)).sendDailyNotificationEmail(user2.getUsername());
    }

}
