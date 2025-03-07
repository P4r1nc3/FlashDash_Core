package com.flashdash.service;

import com.flashdash.exception.ErrorCode;
import com.flashdash.exception.FlashDashException;
import com.flashdash.model.ActivityType;
import com.flashdash.model.User;
import com.flashdash.repository.UserRepository;
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

    private final ActivityService activityService;
    private final DeckService deckService;
    private final GameSessionService gameSessionService;
    private final FriendService friendService;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(ActivityService activityService,
                       DeckService deckService,
                       GameSessionService gameSessionService,
                       FriendService friendService,
                       UserRepository userRepository,
                       PasswordEncoder passwordEncoder) {
        this.activityService = activityService;
        this.deckService = deckService;
        this.gameSessionService = gameSessionService;
        this.friendService = friendService;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
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
        activityService.logActivity(user.getUserFrn(), user.getUserFrn(), ActivityType.ACCOUNT_UPDATED);
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
        activityService.logActivity(user.getUserFrn(), user.getUserFrn(), ActivityType.ACCOUNT_DELETED);
        logger.info("User with email {} successfully deleted.", email);
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        logger.info("Loading user details for email: {}", email);

        return userRepository.findByEmail(email)
                .orElseThrow(() -> {
                    logger.warn("User with email {} not found during authentication", email);
                    return new FlashDashException(
                            ErrorCode.E404001,
                            "User with email " + email + " not found. Please check the email and try again."
                    );
                });
    }
}
