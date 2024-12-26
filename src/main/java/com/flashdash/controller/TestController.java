package com.flashdash.controller;

import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/test")
public class TestController {

    private JavaMailSender mailSender;

    public TestController(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    @GetMapping
    public String test() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        return "This is test. User email: " + email;
    }

    @PostMapping("/send")
    public String sendEmail() {
        String to = "konrad.tupta@student.uj.edu.pl";
        String subject = "Test Email from Flash Dash";
        String body = "Hello! This is a test email sent from Flash Dash.";

        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(to);
            message.setSubject(subject);
            message.setText(body);
            message.setFrom("flashdashservice@gmail.com");
            mailSender.send(message);

            return "Email sent successfully to " + to;
        } catch (Exception e) {
            e.printStackTrace();
            return "Failed to send email: " + e.getMessage();
        }
    }
}
