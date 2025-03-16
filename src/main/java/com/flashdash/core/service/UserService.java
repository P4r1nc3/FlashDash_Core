package com.flashdash.core.service;

import com.flashdash.core.exception.ErrorCode;
import com.flashdash.core.exception.FlashDashException;
import com.flashdash.core.model.User;
import com.flashdash.core.repository.UserRepository;
import com.flashdash.core.service.api.ActivityService;
import com.p4r1nc3.flashdash.activity.model.LogActivityRequest.ActivityTypeEnum;
import com.p4r1nc3.flashdash.core.model.ChangePasswordRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserService implements UserDetailsService {

    private static final Logger logger = LoggerFactory.getLogger(UserService.class);

    private final PasswordEncoder passwordEncoder;
    private final ActivityService activityService;
    private final DeckService deckService;
    private final GameSessionService gameSessionService;
    private final FriendService friendService;
    private final UserRepository userRepository;

    public UserService(PasswordEncoder passwordEncoder,
                       ActivityService activityService,
                       DeckService deckService,
                       GameSessionService gameSessionService,
                       FriendService friendService,
                       UserRepository userRepository) {
        this.passwordEncoder = passwordEncoder;
        this.activityService = activityService;
        this.deckService = deckService;
        this.gameSessionService = gameSessionService;
        this.friendService = friendService;
        this.userRepository = userRepository;
    }

    public User getCurrentUser(String email) {
        logger.info("Attempting to retrieve user information for email: {}", email);

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> {
                    logger.warn("User with email {} not found", email);
                    return new FlashDashException(
                            ErrorCode.E404001,
                            "User with email " + email + " not found."
                    );
                });

        logger.info("User found with email: {}.", email);

        return user;
    }

    public void changePassword(String email, ChangePasswordRequest request) {
        logger.info("Attempting to change password for user: {}", email);

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> {
                    logger.warn("User with email {} not found", email);
                    return new FlashDashException(
                            ErrorCode.E404001,
                            "User with email " + email + " not found."
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
        logger.info("Password successfully changed for user: {}", email);
    }

    public void deleteUser(String email) {
        logger.info("Attempting to delete user with email: {}", email);

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> {
                    logger.warn("User with email {} not found", email);
                    return new FlashDashException(
                            ErrorCode.E404001,
                            "User with email " + email + " not found."
                    );
                });

        gameSessionService.removeAllGameSessionsForUser(user.getUserFrn());
        deckService.deleteAllDecksForUser(user.getUserFrn());
        friendService.removeAllFriends(user.getUserFrn());
        userRepository.delete(user);
        activityService.logUserActivity(user.getUserFrn(), user.getUserFrn(), ActivityTypeEnum.ACCOUNT_DELETED);
        logger.info("User with email {} successfully deleted.", email);
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
