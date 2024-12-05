package com.flashdash.service;

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
import org.springframework.stereotype.Service;

@Service
public class UserService implements UserDetailsService {

    private static final Logger logger = LoggerFactory.getLogger(UserService.class);

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
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
        response.setCreatedAt(user.getCreatedAt());
        response.setUpdatedAt(user.getUpdatedAt());

        logger.info("Response successfully constructed for user with email: {}", email);
        return response;
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
