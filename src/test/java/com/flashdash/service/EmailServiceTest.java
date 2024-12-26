package com.flashdash.service;

import com.flashdash.FlashDashApplication;
import com.flashdash.TestUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mail.javamail.JavaMailSender;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.times;

@SpringBootTest(classes = FlashDashApplication.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class EmailServiceTest {

    @Autowired
    private EmailService emailService;

    @MockBean
    private JavaMailSender mailSender;

    @Test
    void testSendActivationEmail() {
        // Arrange
        String to = "test@example.com";
        String activationToken = "activationToken123";

        // Act
        emailService.sendActivationEmail(to, activationToken);

        // Assert
        verify(mailSender, times(1)).send(TestUtils.createSimpleMailMessageForAccountActivation(to, activationToken));
    }

    @Test
    void testSendDailyNotificationEmail() {
        // Arrange
        String to = "test@example.com";

        // Act
        emailService.sendDailyNotificationEmail(to);

        // Assert
        verify(mailSender, times(1)).send(TestUtils.createSimpleMailMessageForDailyNotification(to));
    }
}
