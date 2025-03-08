package com.flashdash.core.service;

import com.flashdash.core.model.User;
import com.flashdash.core.repository.UserRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class NotificationScheduler {

    private final EmailService emailService;
    private final UserRepository userRepository;

    public NotificationScheduler(EmailService emailService,
                                 UserRepository userRepository) {
        this.emailService = emailService;
        this.userRepository = userRepository;
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
