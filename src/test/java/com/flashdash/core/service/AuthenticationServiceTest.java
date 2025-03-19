package com.flashdash.core.service;

import com.flashdash.core.TestUtils;
import com.flashdash.core.config.JwtManager;
import com.flashdash.core.exception.FlashDashException;
import com.flashdash.core.exception.ErrorCode;
import com.flashdash.core.model.User;
import com.flashdash.core.repository.UserRepository;
import com.flashdash.core.service.api.ActivityService;
import com.flashdash.core.service.api.NotificationService;
import com.p4r1nc3.flashdash.activity.model.ActivityStatisticsResponse;
import com.p4r1nc3.flashdash.activity.model.LogActivityRequest.ActivityTypeEnum;
import com.p4r1nc3.flashdash.core.model.AuthenticationResponse;
import com.p4r1nc3.flashdash.core.model.LoginRequest;
import com.p4r1nc3.flashdash.core.model.RegisterRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@SpringBootTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class AuthenticationServiceTest {

    @Autowired
    private AuthenticationService authenticationService;

    @MockitoBean
    private PasswordEncoder passwordEncoder;

    @MockitoBean
    private JwtManager jwtManager;

    @MockitoBean
    private ActivityService activityService;

    @MockitoBean
    private NotificationService notificationService;

    @MockitoBean
    private UserRepository userRepository;

    private User user;

    @BeforeEach
    void setUp() {
        user = TestUtils.createUser();

        when(jwtManager.generateToken(user.getUsername(), user.getEmail())).thenReturn("mocked-jwt-token");
    }

    @Test
    void shouldLoginSuccessfully() {
        // Arrange
        user.setEnabled(true);
        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(true);
        when(jwtManager.generateToken(user.getUserFrn(), user.getEmail())).thenReturn("mocked-jwt-token");

        ActivityStatisticsResponse activityStats = new ActivityStatisticsResponse();
        activityStats.setCurrentStreak(5);
        when(activityService.getActivityStatistics(user.getUserFrn())).thenReturn(activityStats);

        LoginRequest loginRequest = TestUtils.createLoginRequest(user);

        // Act
        AuthenticationResponse response = authenticationService.login(loginRequest);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.getToken()).isEqualTo("mocked-jwt-token");

        verify(userRepository).findByEmail(user.getEmail());
        verify(passwordEncoder).matches(loginRequest.getPassword(), user.getPassword());
        verify(jwtManager).generateToken(user.getUserFrn(), user.getEmail());
        verify(activityService).logUserActivity(user.getUserFrn(), user.getUserFrn(), ActivityTypeEnum.ACCOUNT_LOGIN);
        verify(userRepository).save(user);
    }

    @Test
    void shouldThrowExceptionWhenUserNotFoundDuringLogin() {
        // Arrange
        LoginRequest loginRequest = TestUtils.createLoginRequest(user);
        when(userRepository.findByEmail(loginRequest.getEmail())).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> authenticationService.login(loginRequest))
                .isInstanceOf(FlashDashException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.E404001);

        verify(userRepository).findByEmail(loginRequest.getEmail());
        verifyNoInteractions(passwordEncoder);
    }

    @Test
    void shouldThrowExceptionWhenPasswordDoesNotMatch() {
        // Arrange
        user.setEnabled(true);
        LoginRequest loginRequest = TestUtils.createLoginRequest(user);
        when(userRepository.findByEmail(loginRequest.getEmail())).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(loginRequest.getPassword(), user.getPassword())).thenReturn(false);

        // Act & Assert
        assertThatThrownBy(() -> authenticationService.login(loginRequest))
                .isInstanceOf(FlashDashException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.E401002);

        verify(userRepository).findByEmail(loginRequest.getEmail());
        verify(passwordEncoder).matches(loginRequest.getPassword(), user.getPassword());
    }

    @Test
    void shouldThrowExceptionWhenAccountNotActivated() {
        // Arrange
        user.setEnabled(false);
        LoginRequest loginRequest = TestUtils.createLoginRequest(user);
        when(userRepository.findByEmail(loginRequest.getEmail())).thenReturn(Optional.of(user));

        // Act & Assert
        assertThatThrownBy(() -> authenticationService.login(loginRequest))
                .isInstanceOf(FlashDashException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.E403002);

        verify(userRepository).findByEmail(loginRequest.getEmail());
        verifyNoInteractions(passwordEncoder);
    }

    @Test
    void shouldRegisterSuccessfully() {
        // Arrange
        RegisterRequest registerRequest = TestUtils.createRegisterRequest(user);
        when(userRepository.findByEmail(registerRequest.getEmail())).thenReturn(Optional.empty());
        when(passwordEncoder.encode(registerRequest.getPassword())).thenReturn("encodedPassword");

        // Act
        AuthenticationResponse response = authenticationService.register(registerRequest);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.getToken()).contains("Account created");

        verify(userRepository).findByEmail(registerRequest.getEmail());
        verify(userRepository).save(any(User.class));
        verify(notificationService).sendAccountConfirmationEmail(anyString(), anyString());
    }

    @Test
    void shouldThrowExceptionWhenUserAlreadyExistsDuringRegistration() {
        // Arrange
        RegisterRequest registerRequest = TestUtils.createRegisterRequest(user);
        when(userRepository.findByEmail(registerRequest.getEmail())).thenReturn(Optional.of(user));

        // Act & Assert
        assertThatThrownBy(() -> authenticationService.register(registerRequest))
                .isInstanceOf(FlashDashException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.E409001);

        verify(userRepository).findByEmail(registerRequest.getEmail());
        verifyNoMoreInteractions(userRepository);
        verifyNoInteractions(notificationService);
    }

    @Test
    void shouldActivateAccountSuccessfully() {
        // Arrange
        user.setEnabled(false);
        user.setActivationToken("validToken");
        when(userRepository.findByActivationToken("validToken")).thenReturn(Optional.of(user));

        // Act
        authenticationService.activateAccount("validToken");

        // Assert
        assertThat(user.isEnabled()).isTrue();

        verify(userRepository).findByActivationToken("validToken");
        verify(userRepository).save(user);
    }

    @Test
    void shouldThrowExceptionWhenActivationTokenIsInvalid() {
        // Arrange
        when(userRepository.findByActivationToken("invalidToken")).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> authenticationService.activateAccount("invalidToken"))
                .isInstanceOf(FlashDashException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.E404001);

        verify(userRepository).findByActivationToken("invalidToken");
        verifyNoMoreInteractions(userRepository);
    }

    @Test
    void shouldNotAllowActivationIfUserIsAlreadyActivated() {
        // Arrange
        user.setEnabled(true);
        user.setActivationToken("alreadyActivated");
        when(userRepository.findByActivationToken("alreadyActivated")).thenReturn(Optional.of(user));

        // Act & Assert
        assertThatThrownBy(() -> authenticationService.activateAccount("alreadyActivated"))
                .isInstanceOf(FlashDashException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.E400001)
                .hasMessage("Account is already activated.");

        verify(userRepository).findByActivationToken("alreadyActivated");
        verifyNoMoreInteractions(userRepository);
    }

    @Test
    void shouldThrowExceptionWhenAccountIsNotActivated() {
        // Arrange
        user.setEnabled(false);
        LoginRequest loginRequest = TestUtils.createLoginRequest(user);
        when(userRepository.findByEmail(loginRequest.getEmail())).thenReturn(Optional.of(user));

        // Act & Assert
        assertThatThrownBy(() -> authenticationService.login(loginRequest))
                .isInstanceOf(FlashDashException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.E403002)
                .hasMessage("Account not activated.");

        verify(userRepository).findByEmail(loginRequest.getEmail());
        verifyNoInteractions(passwordEncoder);
    }
}