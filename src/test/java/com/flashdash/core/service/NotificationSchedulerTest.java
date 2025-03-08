package com.flashdash.core.service;

import com.flashdash.core.FlashDashCoreApplication;
import com.flashdash.core.TestUtils;
import com.flashdash.core.model.User;
import com.flashdash.core.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.List;

import static org.mockito.Mockito.*;

@SpringBootTest(classes = FlashDashCoreApplication.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class NotificationSchedulerTest {

    @Autowired
    private NotificationScheduler notificationScheduler;

    @MockitoBean
    private UserRepository userRepository;

    @MockitoBean
    private EmailService emailService;

    private User user1;
    private User user2;

    @BeforeEach
    void setUp() {
        user1 = TestUtils.createUser();
        user1.setDailyNotifications(true);

        user2 = TestUtils.createUser();
        user2.setDailyNotifications(true);
    }

    @Test
    void shouldSendDailyNotificationsToUsersWithNotificationsEnabled() {
        // Arrange
        List<User> users = List.of(user1, user2);
        when(userRepository.findByDailyNotificationsTrue()).thenReturn(users);

        // Act
        notificationScheduler.sendDailyNotifications();

        // Assert
        verify(emailService, times(1)).sendDailyNotificationEmail(user1.getEmail());
        verify(emailService, times(1)).sendDailyNotificationEmail(user2.getEmail());
    }

    @Test
    void shouldNotSendNotificationsToUsersWithoutNotificationsEnabled() {
        // Arrange
        user1.setDailyNotifications(false);
        user2.setDailyNotifications(false);
        when(userRepository.findByDailyNotificationsTrue()).thenReturn(List.of());

        // Act
        notificationScheduler.sendDailyNotifications();

        // Assert
        verify(emailService, never()).sendDailyNotificationEmail(anyString());
    }

    @Test
    void shouldSendNotificationOnlyToUsersWithEnabledNotifications() {
        // Arrange
        user1.setDailyNotifications(true);
        user2.setDailyNotifications(false);
        when(userRepository.findByDailyNotificationsTrue()).thenReturn(List.of(user1));

        // Act
        notificationScheduler.sendDailyNotifications();

        // Assert
        verify(emailService, times(1)).sendDailyNotificationEmail(user1.getEmail());
        verify(emailService, never()).sendDailyNotificationEmail(user2.getEmail());
    }

    @Test
    void shouldNotSendNotificationsIfNoUsersHaveDailyNotificationsEnabled() {
        // Arrange
        when(userRepository.findByDailyNotificationsTrue()).thenReturn(List.of());

        // Act
        notificationScheduler.sendDailyNotifications();

        // Assert
        verify(emailService, never()).sendDailyNotificationEmail(anyString());
    }
}
