package com.flashdash.core.service.api;

import com.flashdash.core.config.JwtManager;
import com.flashdash.core.exception.ErrorCode;
import com.flashdash.core.exception.FlashDashException;
import com.flashdash.core.model.User;
import com.flashdash.core.repository.UserRepository;
import com.p4r1nc3.flashdash.notification.ApiClient;
import com.p4r1nc3.flashdash.notification.ApiException;
import com.p4r1nc3.flashdash.notification.api.NotificationsApi;
import com.p4r1nc3.flashdash.notification.api.SubscribersApi;
import com.p4r1nc3.flashdash.notification.model.NotificationSubscriber;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalTime;
import java.util.Optional;

@Service
public class NotificationService {
    private static final Logger logger = LoggerFactory.getLogger(NotificationService.class);

    private final JwtManager jwtManager;
    private final UserRepository userRepository;

    public NotificationService(JwtManager jwtManager, UserRepository userRepository) {
        this.jwtManager = jwtManager;
        this.userRepository = userRepository;
    }

    private NotificationsApi getNotificationsApi(String userFrn) {
        Optional<User> userOptional = userRepository.findByUserFrn(userFrn);
        if (userOptional.isEmpty()) {
            throw new FlashDashException(ErrorCode.E401001, "Invalid user reference.");
        }

        User user = userOptional.get();
        ApiClient apiClient = new ApiClient();
        String token = jwtManager.generateToken(userFrn, user.getEmail());
        apiClient.setBearerToken(token);
        return new NotificationsApi(apiClient);
    }

    private SubscribersApi getSubscribersApi(String userFrn) {
        Optional<User> userOptional = userRepository.findByUserFrn(userFrn);
        if (userOptional.isEmpty()) {
            throw new FlashDashException(ErrorCode.E401001, "Invalid user reference.");
        }

        User user = userOptional.get();
        ApiClient apiClient = new ApiClient();
        String token = jwtManager.generateToken(userFrn, user.getEmail());
        apiClient.setBearerToken(token);
        return new SubscribersApi(apiClient);
    }

    public void registerSubscriber(String userFrn) {
        SubscribersApi subscribersApi = getSubscribersApi(userFrn);
        try {
            subscribersApi.registerSubscriber();
            logger.info("User {} successfully registered for notifications.", userFrn);
        } catch (ApiException e) {
            logger.error("Failed to register user {} for notifications. Error: {}", userFrn, e.getMessage());
            throw new FlashDashException(ErrorCode.E500001, "An error occurred while registering the user. Please try again later.");
        }
    }

    public NotificationSubscriber getSubscriber(String userFrn) {
        SubscribersApi subscribersApi = getSubscribersApi(userFrn);
        try {
            return subscribersApi.getSubscriber();
        } catch (ApiException e) {
            logger.error("Failed to retrieve subscriber {}. Error: {}", userFrn, e.getMessage());
            throw new FlashDashException(ErrorCode.E500001, "An error occurred while retrieving subscriber details. Please try again later.");
        }
    }

    public void unregisterSubscriber(String userFrn) {
        SubscribersApi subscribersApi = getSubscribersApi(userFrn);
        try {
            subscribersApi.unregisterSubscriber();
            logger.info("User {} successfully unregistered from notifications.", userFrn);
        } catch (ApiException e) {
            logger.error("Failed to unregister user {} from notifications. Error: {}", userFrn, e.getMessage());
            throw new FlashDashException(ErrorCode.E500001, "An error occurred while unregistering the user. Please try again later.");
        }
    }

    public void enableDailyNotifications(String userFrn, LocalTime notificationTime) {
        NotificationsApi notificationsApi = getNotificationsApi(userFrn);
        try {
            String notificationTimeString = (notificationTime != null) ? notificationTime.toString() : null;
            notificationsApi.enableDailyNotifications(notificationTimeString);
            logger.info("Daily notifications enabled for user {}.", userFrn);
        } catch (ApiException e) {
            logger.error("Failed to enable daily notifications for user {}. Error: {}", userFrn, e.getMessage());
            throw new FlashDashException(ErrorCode.E500001, "An error occurred while enabling daily notifications. Please try again later.");
        }
    }

    public void disableDailyNotifications(String userFrn) {
        NotificationsApi notificationsApi = getNotificationsApi(userFrn);
        try {
            notificationsApi.disableDailyNotifications();
            logger.info("Daily notifications disabled for user {}.", userFrn);
        } catch (ApiException e) {
            logger.error("Failed to disable daily notifications for user {}. Error: {}", userFrn, e.getMessage());
            throw new FlashDashException(ErrorCode.E500001, "An error occurred while disabling daily notifications. Please try again later.");
        }
    }

    public void sendAccountConfirmationEmail(String userFrn, String token) {
        NotificationsApi notificationsApi = getNotificationsApi(userFrn);
        try {
            notificationsApi.sendAccountConfirmationEmail(token);
            logger.info("Account confirmation email sent to user {}.", userFrn);
        } catch (ApiException e) {
            logger.error("Failed to send account confirmation email to user {}. Error: {}", userFrn, e.getMessage());
            throw new FlashDashException(ErrorCode.E500001, "An error occurred while sending account confirmation email. Please try again later.");
        }
    }

    public void sendFriendInviteEmail(String userFrn) {
        NotificationsApi notificationsApi = getNotificationsApi(userFrn);
        try {
            notificationsApi.sendFriendInviteEmail();
            logger.info("Friend invite email sent to user {}.", userFrn);
        } catch (ApiException e) {
            logger.error("Failed to send friend invite email to user {}. Error: {}", userFrn, e.getMessage());
            throw new FlashDashException(ErrorCode.E500001, "An error occurred while sending friend invite email. Please try again later.");
        }
    }
}
