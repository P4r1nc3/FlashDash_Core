package com.flashdash.service;

import com.flashdash.model.User;
import com.flashdash.repository.UserRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class NotificationScheduler {

    private final UserRepository userRepository;
    private final EmailService emailService;

    public NotificationScheduler(UserRepository userRepository, EmailService emailService) {
        this.userRepository = userRepository;
        this.emailService = emailService;
    }

    // Every day at 8 am
    @Scheduled(cron = "0 0 8 * * ?")
    public void sendDailyNotifications() {
        List<User> users = userRepository.findByDailyNotificationsTrue();

        for (User user : users) {
            emailService.sendDailyNotificationEmail(user.getUsername());
        }
    }
}
