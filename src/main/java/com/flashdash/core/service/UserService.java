package com.flashdash.core.service;

import com.flashdash.core.exception.ErrorCode;
import com.flashdash.core.exception.FlashDashException;
import com.flashdash.core.model.GameSession;
import com.flashdash.core.model.User;
import com.flashdash.core.repository.UserRepository;
import com.flashdash.core.service.api.ActivityService;
import com.flashdash.core.service.api.NotificationService;
import com.flashdash.core.utils.EntityToResponseMapper;
import com.p4r1nc3.flashdash.activity.model.LogActivityRequest.ActivityTypeEnum;
import com.p4r1nc3.flashdash.core.model.ChangePasswordRequest;
import com.p4r1nc3.flashdash.core.model.UserResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.List;

@Service
public class UserService implements UserDetailsService {

    private static final Logger logger = LoggerFactory.getLogger(UserService.class);

    private final PasswordEncoder passwordEncoder;
    private final EntityToResponseMapper mapper;
    private final ActivityService activityService;
    private final NotificationService notificationService;
    private final DeckService deckService;
    private final GameSessionService gameSessionService;
    private final FriendService friendService;
    private final UserRepository userRepository;

    public UserService(PasswordEncoder passwordEncoder,
                       EntityToResponseMapper mapper,
                       ActivityService activityService,
                       NotificationService notificationService,
                       DeckService deckService,
                       GameSessionService gameSessionService,
                       FriendService friendService,
                       UserRepository userRepository) {
        this.passwordEncoder = passwordEncoder;
        this.mapper = mapper;
        this.activityService = activityService;
        this.notificationService = notificationService;
        this.deckService = deckService;
        this.gameSessionService = gameSessionService;
        this.friendService = friendService;
        this.userRepository = userRepository;
    }

    public UserResponse getCurrentUser(String userFrn) {
        logger.info("Attempting to retrieve user information for userFrn: {}", userFrn);

        User user = userRepository.findByUserFrn(userFrn)
                .orElseThrow(() -> {
                    logger.warn("User with userFrn {} not found", userFrn);
                    return new FlashDashException(
                            ErrorCode.E404001,
                            "User with userFrn " + userFrn + " not found."
                    );
                });

        logger.info("User found with userFrn: {}.", userFrn);

        UserResponse userResponse = mapper.mapToUserResponse(user);

        List<GameSession> gameSessions = gameSessionService.getAllGameSessions(user.getUserFrn());

        int totalStudyTimeInMinutes = gameSessions.stream()
                .mapToInt(session -> (int) Duration.between(session.getCreatedAt(), session.getUpdatedAt()).toMinutes())
                .sum();

        userResponse.setStudyTime(totalStudyTimeInMinutes);
        userResponse.setGamesPlayed(gameSessions.size());

        return userResponse;
    }


    public void changePassword(String userFrn, ChangePasswordRequest request) {
        logger.info("Attempting to change password for userFrn: {}", userFrn);

        User user = userRepository.findByUserFrn(userFrn)
                .orElseThrow(() -> {
                    logger.warn("User with userFrn {} not found", userFrn);
                    return new FlashDashException(
                            ErrorCode.E404001,
                            "User with userFrn " + userFrn + " not found."
                    );
                });

        if (!passwordEncoder.matches(request.getOldPassword(), user.getPassword())) {
            throw new FlashDashException(
                    ErrorCode.E401002,
                    "Incorrect old password."
            );
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
        activityService.logUserActivity(user.getUserFrn(), user.getUserFrn(), ActivityTypeEnum.ACCOUNT_UPDATED);
        logger.info("Password successfully changed for userFrn: {}",userFrn);
    }

    public void deleteUser(String userFrn) {
        logger.info("Attempting to delete user with userFrn: {}", userFrn);

        User user = userRepository.findByUserFrn(userFrn)
                .orElseThrow(() -> {
                    logger.warn("User with email {} not found", userFrn);
                    return new FlashDashException(
                            ErrorCode.E404001,
                            "User with email " + userFrn + " not found."
                    );
                });

        gameSessionService.removeAllGameSessionsForUser(user.getUserFrn());
        deckService.deleteAllDecksForUser(user.getUserFrn());
        friendService.removeAllFriends(user.getUserFrn());
        userRepository.delete(user);
        activityService.logUserActivity(user.getUserFrn(), user.getUserFrn(), ActivityTypeEnum.ACCOUNT_DELETED);
        notificationService.unregisterSubscriber(user.getUserFrn());
        logger.info("User with userFrn {} successfully deleted.", userFrn);
    }

    @Override
    public UserDetails loadUserByUsername(String userFrn) throws UsernameNotFoundException {
        logger.info("Loading user details for email: {}", userFrn);

        return userRepository.findByUserFrn(userFrn)
                .orElseThrow(() -> {
                    logger.warn("User with userFrn {} not found during authentication", userFrn);
                    return new FlashDashException(
                            ErrorCode.E404001,
                            "User with email " + userFrn + " not found. Please check the userFrn and try again."
                    );
                });
    }
}
