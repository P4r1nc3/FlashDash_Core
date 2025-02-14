package com.flashdash.service;

import com.flashdash.dto.request.ChangePasswordRequest;
import com.flashdash.dto.response.UserResponse;
import com.flashdash.exception.ErrorCode;
import com.flashdash.exception.FlashDashException;
import com.flashdash.model.User;
import com.flashdash.repository.UserRepository;
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

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public UserResponse getCurrentUser(String email) {
        logger.info("Attempting to retrieve user information for email: {}", email);

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> {
                    logger.warn("User with email {} not found", email);
                    return new FlashDashException(
                            ErrorCode.E404001,
                            "User with email " + email + " not found."
                    );
                });

        logger.info("User found with email: {}. Constructing response.", email);

        UserResponse response = new UserResponse();
        response.setFirstName(user.getFirstName());
        response.setLastName(user.getLastName());
        response.setEmail(user.getUsername());
        response.setDailyNotifications(user.isDailyNotifications());
        response.setCreatedAt(user.getCreatedAt());
        response.setUpdatedAt(user.getUpdatedAt());

        logger.info("Response successfully constructed for user with email: {}", email);
        return response;
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

        userRepository.delete(user);
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
