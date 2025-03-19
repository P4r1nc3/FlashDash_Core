package com.flashdash.core.service;

import com.flashdash.core.config.JwtManager;
import com.flashdash.core.exception.FlashDashException;
import com.flashdash.core.exception.ErrorCode;
import com.flashdash.core.model.User;
import com.flashdash.core.repository.UserRepository;
import com.flashdash.core.service.api.ActivityService;
import com.flashdash.core.service.api.NotificationService;
import com.flashdash.core.utils.FrnGenerator;
import com.flashdash.core.utils.ResourceType;
import com.p4r1nc3.flashdash.activity.model.ActivityStatisticsResponse;
import com.p4r1nc3.flashdash.activity.model.LogActivityRequest.ActivityTypeEnum;
import com.p4r1nc3.flashdash.core.model.AuthenticationResponse;
import com.p4r1nc3.flashdash.core.model.LoginRequest;
import com.p4r1nc3.flashdash.core.model.RegisterRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class AuthenticationService {

    private static final Logger logger = LoggerFactory.getLogger(AuthenticationService.class);

    private final PasswordEncoder passwordEncoder;
    private final JwtManager jwtManager;
    private final ActivityService activityService;
    private final NotificationService notificationService;
    private final UserRepository userRepository;


    public AuthenticationService(PasswordEncoder passwordEncoder,
                                 JwtManager jwtManager,
                                 ActivityService activityService,
                                 NotificationService notificationService,
                                 UserRepository userRepository) {
        this.passwordEncoder = passwordEncoder;
        this.jwtManager = jwtManager;
        this.activityService = activityService;
        this.notificationService = notificationService;
        this.userRepository = userRepository;
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

        String token = jwtManager.generateToken(user.getUserFrn(), request.getEmail());
        logger.info("Login successful for email: {}", request.getEmail());

        activityService.logUserActivity(user.getUserFrn(), user.getUserFrn(), ActivityTypeEnum.ACCOUNT_LOGIN);
        ActivityStatisticsResponse activityStatistics = activityService.getActivityStatistics(user.getUserFrn());
        user.setStrike(activityStatistics.getCurrentStreak());
        user.setUpdatedAt(LocalDateTime.now());
        userRepository.save(user);

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
        user.setUserFrn(FrnGenerator.generateFrn(ResourceType.USER));
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail().trim().toLowerCase());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setActivationToken(activationToken);
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());
        user.setEnabled(false);
        user.setStudyTime(Duration.ZERO);
        user.setGamesPlayed(0);
        user.setPoints(0);
        user.setStrike(1);
        userRepository.save(user);

        activityService.logUserActivity(user.getUserFrn(), user.getUserFrn(), ActivityTypeEnum.ACCOUNT_REGISTRATION);
        notificationService.registerSubscriber(user.getUserFrn());
        notificationService.sendAccountConfirmationEmail(user.getUserFrn(), activationToken);

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
        activityService.logUserActivity(user.getUserFrn(), user.getUserFrn(), ActivityTypeEnum.ACCOUNT_CONFIRMATION);

        logger.info("Account activated successfully for email: {}", user.getUsername());
    }
}
