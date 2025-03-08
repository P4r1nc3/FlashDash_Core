package com.flashdash.core.service;

import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private final JavaMailSender mailSender;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    @Async
    public void sendActivationEmail(String to, String activationToken) {
        String activationLink = "http://localhost:8080/auth/activate?token=" + activationToken;
        String subject = "Account Activation";
        String content = "Click the link to activate your account: " + activationLink;
        sendEmail(to, subject, content);
    }

    @Async
    public void sendDailyNotificationEmail(String to) {
        String subject = "Reminder: Time to Learn!";
        String content = "Hi there! Don't forget to continue your learning journey with FlashDash. Let's make today productive!";
        sendEmail(to, subject, content);
    }

    @Async
    public void sendFriendInvitationEmail(String recipientEmail, String senderFirstName, String senderLastName) {
        String subject = "You have a new Friend Invitation!";
        String content = "Hi there! You've received a new friend invitation from " + senderFirstName + " " + senderLastName
                         + ". Visit the FlashDash app to accept or decline.";
        sendEmail(recipientEmail, subject, content);
    }

    private void sendEmail(String to, String subject, String content) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(to);
            message.setSubject(subject);
            message.setText(content);
            message.setFrom("flashdashservice@gmail.com");
            mailSender.send(message);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
