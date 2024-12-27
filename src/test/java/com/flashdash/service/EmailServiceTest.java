package com.flashdash.service;

import com.flashdash.FlashDashApplication;
import com.flashdash.TestUtils;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

import static org.mockito.Mockito.*;

@SpringBootTest(classes = FlashDashApplication.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class EmailServiceTest {

    @Autowired
    private EmailService emailService;

    @MockBean
    private JavaMailSender mailSender;

    @Test
    @SneakyThrows
    void testSendActivationEmail() {
        // Arrange
        String to = "test@example.com";
        String activationToken = "activationToken123";
        SimpleMailMessage expectedMessage = TestUtils.createSimpleMailMessageForAccountActivation(to, activationToken);
        doNothing().when(mailSender).send(expectedMessage);

        // Act
        emailService.sendActivationEmail(to, activationToken);

        // Assert
        Thread.sleep(10);
        verify(mailSender, times(1)).send(expectedMessage);
    }

    @Test
    @SneakyThrows
    void testSendDailyNotificationEmail() {
        // Arrange
        String to = "test@example.com";
        SimpleMailMessage expectedMessage = TestUtils.createSimpleMailMessageForDailyNotification(to);
        doNothing().when(mailSender).send(expectedMessage);

        // Act
        emailService.sendDailyNotificationEmail(to);

        // Assert
        Thread.sleep(10);
        verify(mailSender, times(1)).send(expectedMessage);
    }

    @Test
    @SneakyThrows
    void testSendFriendInvitationEmail() {
        // Arrange
        String to = "test@example.com";
        String senderFirstName = "Konrad";
        String senderLastName = "Tupta";
        SimpleMailMessage expectedMessage = TestUtils.createSimpleMailMessageForFriendInvitation(to, senderFirstName, senderLastName);
        doNothing().when(mailSender).send(expectedMessage);

        // Act
        emailService.sendFriendInvitationEmail(to, senderFirstName, senderLastName);

        // Assert
        Thread.sleep(10);
        verify(mailSender, times(1)).send(expectedMessage);
    }
}
