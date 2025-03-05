package com.flashdash.service;

import com.flashdash.FlashDashApplication;
import com.flashdash.TestUtils;
import com.flashdash.exception.FlashDashException;
import com.flashdash.exception.ErrorCode;
import com.flashdash.model.User;
import com.flashdash.repository.UserRepository;
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

@SpringBootTest(classes = FlashDashApplication.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class AuthenticationServiceTest {

    @Autowired
    private AuthenticationService authenticationService;

    @MockitoBean
    private UserRepository userRepository;

    @MockitoBean
    private PasswordEncoder passwordEncoder;

    @MockitoBean
    private EmailService emailService;

    private User user;

    @BeforeEach
    void setUp() {
        user = TestUtils.createUser();
    }

    @Test
    void shouldLoginSuccessfully() {
        // Arrange
        user.setEnabled(true);
        when(userRepository.findByEmail(user.getUsername())).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(true);

        LoginRequest loginRequest = TestUtils.createLoginRequest(user);

        // Act
        AuthenticationResponse response = authenticationService.login(loginRequest);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.getToken()).isNotBlank();

        verify(userRepository).findByEmail(user.getUsername());
        verify(passwordEncoder).matches(loginRequest.getPassword(), user.getPassword());
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
        when(userRepository.findByEmail(user.getUsername())).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(false);

        LoginRequest loginRequest = TestUtils.createLoginRequest(user);

        // Act & Assert
        assertThatThrownBy(() -> authenticationService.login(loginRequest))
                .isInstanceOf(FlashDashException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.E401002);

        verify(userRepository).findByEmail(loginRequest.getEmail());
        verify(passwordEncoder).matches(loginRequest.getPassword(), user.getPassword());
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
        assertThat(response.getToken()).isNotBlank();

        verify(userRepository).findByEmail(registerRequest.getEmail());
        verify(userRepository).save(any(User.class));
        verify(emailService).sendActivationEmail(eq(registerRequest.getEmail()), anyString());
    }
    @Test
    void shouldNotSendActivationEmailWhenUserAlreadyExists() {
        // Arrange
        when(userRepository.findByEmail(user.getUsername())).thenReturn(Optional.of(user));

        RegisterRequest registerRequest = TestUtils.createRegisterRequest(user);

        // Act & Assert
        assertThatThrownBy(() -> authenticationService.register(registerRequest))
                .isInstanceOf(FlashDashException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.E409001);

        verify(userRepository).findByEmail(registerRequest.getEmail());
        verifyNoMoreInteractions(userRepository);
        verifyNoInteractions(emailService);
    }

    @Test
    void shouldActivateAccountSuccessfully() {
        // Arrange
        User user = TestUtils.createUser();
        user.setEnabled(false);
        String activationToken = "validToken";
        user.setActivationToken(activationToken);
        when(userRepository.findByActivationToken(activationToken)).thenReturn(Optional.of(user));

        // Act
        authenticationService.activateAccount(activationToken);

        // Assert
        assertThat(user.isEnabled()).isTrue();

        verify(userRepository).findByActivationToken(activationToken);
        verify(userRepository).save(user);
    }

    @Test
    void shouldThrowExceptionWhenActivationTokenIsInvalid() {
        // Arrange
        String invalidToken = "invalidToken";
        when(userRepository.findByActivationToken(invalidToken)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> authenticationService.activateAccount(invalidToken))
                .isInstanceOf(FlashDashException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.E404001);

        verify(userRepository).findByActivationToken(invalidToken);
        verifyNoMoreInteractions(userRepository);
    }

    @Test
    void shouldNotAllowActivationIfUserIsAlreadyActivated() {
        // Arrange
        User user = TestUtils.createUser();
        user.setEnabled(true);  // User is already activated
        user.setActivationToken("activated");
        when(userRepository.findByActivationToken("alreadyActivatedToken")).thenReturn(Optional.of(user));

        // Act & Assert
        assertThatThrownBy(() -> authenticationService.activateAccount("alreadyActivatedToken"))
                .isInstanceOf(FlashDashException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.E400001)
                .hasMessage("Account is already activated.");

        verify(userRepository).findByActivationToken("alreadyActivatedToken");
        verifyNoMoreInteractions(userRepository);
    }

    @Test
    void shouldThrowExceptionWhenAccountIsNotActivated() {
        // Arrange
        user.setEnabled(false);
        when(userRepository.findByEmail(user.getUsername())).thenReturn(Optional.of(user));

        LoginRequest loginRequest = TestUtils.createLoginRequest(user);

        // Act & Assert
        assertThatThrownBy(() -> authenticationService.login(loginRequest))
                .isInstanceOf(FlashDashException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.E403002)
                .hasMessage("Account not activated.");

        verify(userRepository).findByEmail(user.getUsername());
        verifyNoInteractions(passwordEncoder);
    }
}
