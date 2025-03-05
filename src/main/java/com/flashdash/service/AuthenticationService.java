package com.flashdash.service;

import com.flashdash.config.JwtManager;
import com.flashdash.exception.FlashDashException;
import com.flashdash.exception.ErrorCode;
import com.flashdash.model.User;
import com.flashdash.repository.UserRepository;
import com.p4r1nc3.flashdash.core.model.AuthenticationResponse;
import com.p4r1nc3.flashdash.core.model.LoginRequest;
import com.p4r1nc3.flashdash.core.model.RegisterRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class AuthenticationService {
    private static final Logger logger = LoggerFactory.getLogger(AuthenticationService.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtManager jwtManager;
    private final EmailService emailService;

    public AuthenticationService(UserRepository userRepository,
                                 PasswordEncoder passwordEncoder,
                                 JwtManager jwtManager,
                                 EmailService emailService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtManager = jwtManager;
        this.emailService = emailService;
    }

    public AuthenticationResponse login(LoginRequest request) {
        logger.info("Login attempt for email: {}", request.getEmail());

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> {
                    logger.warn("Login failed: User with email {} not found", request.getEmail());
                    return new FlashDashException(ErrorCode.E404001, "User not found.");
                });

        if (!user.isEnabled()) {
            logger.warn("Login failed: Account not activated for email {}", request.getEmail());
            throw new FlashDashException(ErrorCode.E403002, "Account not activated.");
        }

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            logger.warn("Login failed: Invalid password for email {}", request.getEmail());
            throw new FlashDashException(ErrorCode.E401002, "Invalid password.");
        }

        String token = jwtManager.generateToken(user.getUsername());
        logger.info("Login successful for email: {}", request.getEmail());

        AuthenticationResponse authenticationResponse = new AuthenticationResponse();
        authenticationResponse.setToken(token);

        return authenticationResponse;
    }

    public AuthenticationResponse register(RegisterRequest request) {
        logger.info("Registration attempt for email: {}", request.getEmail());

        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new FlashDashException(ErrorCode.E409001, "User already exists.");
        }

        String activationToken = UUID.randomUUID().toString();

        User user = new User();
        user.setUserFrn(generateFrn("user"));
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setUsername(request.getEmail().trim().toLowerCase());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setActivationToken(activationToken);
        user.setDailyNotifications(true);
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());
        user.setEnabled(false);

        userRepository.save(user);
        emailService.sendActivationEmail(user.getUsername(), activationToken);

        AuthenticationResponse authenticationResponse = new AuthenticationResponse();
        authenticationResponse.setToken("Account created. Please check your email to activate.");

        return authenticationResponse;
    }

    public void activateAccount(String token) {
        logger.info("Activating account with token: {}", token);

        User user = userRepository.findByActivationToken(token)
                .orElseThrow(() -> new FlashDashException(ErrorCode.E404001, "Invalid activation token."));

        if (user.isEnabled()) {
            logger.warn("Account already activated for email: {}", user.getUsername());
            throw new FlashDashException(ErrorCode.E400001, "Account is already activated.");
        }

        user.setEnabled(true);
        userRepository.save(user);

        logger.info("Account activated successfully for email: {}", user.getUsername());
    }

    private String generateFrn(String resourceType) {
        return "frn:flashdash:" + resourceType + ":" + UUID.randomUUID();
    }
}
