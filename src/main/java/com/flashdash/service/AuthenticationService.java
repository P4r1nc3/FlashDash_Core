package com.flashdash.service;

import com.flashdash.config.JwtManager;
import com.flashdash.dto.AuthenticationResponse;
import com.flashdash.dto.LoginRequest;
import com.flashdash.dto.RegisterRequest;
import com.flashdash.config.error.FlashDashException;
import com.flashdash.config.error.ErrorCode;
import com.flashdash.model.User;
import com.flashdash.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class AuthenticationService {
    private static final Logger logger = LoggerFactory.getLogger(AuthenticationService.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtManager jwtManager;

    public AuthenticationService(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtManager jwtManager) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtManager = jwtManager;
    }

    public AuthenticationResponse login(LoginRequest request) {
        logger.info("Login attempt for email: {}", request.getEmail());

        Optional<User> userOptional = userRepository.findByEmail(request.getEmail());

        if (userOptional.isEmpty()) {
            logger.warn("Login failed: User with email {} not found", request.getEmail());
            throw new FlashDashException(ErrorCode.E404001);
        }

        User user = userOptional.get();
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            logger.warn("Login failed: Invalid password for email {}", request.getEmail());
            throw new FlashDashException(ErrorCode.E401001);
        }

        String token = jwtManager.generateToken(user.getUsername());
        logger.info("Login successful for email: {}", request.getEmail());
        return new AuthenticationResponse(token);
    }

    public AuthenticationResponse register(RegisterRequest request) {
        logger.info("Registration attempt for email: {}", request.getEmail());

        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            logger.warn("Registration failed: User with email {} already exists", request.getEmail());
            throw new FlashDashException(ErrorCode.E409001);
        }

        User user = new User(
                request.getFirstName(),
                request.getLastName(),
                request.getEmail().trim().toLowerCase(),
                passwordEncoder.encode(request.getPassword())
        );

        userRepository.save(user);
        logger.info("User registered successfully: {}", request.getEmail());

        String token = jwtManager.generateToken(user.getUsername());
        logger.info("Token generated for registered user: {}", request.getEmail());
        return new AuthenticationResponse(token);
    }
}
