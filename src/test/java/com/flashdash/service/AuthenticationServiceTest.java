package com.flashdash.service;

import com.flashdash.FlashDashApplication;
import com.flashdash.TestUtils;
import com.flashdash.dto.response.AuthenticationResponse;
import com.flashdash.dto.request.LoginRequest;
import com.flashdash.dto.request.RegisterRequest;
import com.flashdash.exception.FlashDashException;
import com.flashdash.exception.ErrorCode;
import com.flashdash.model.User;
import com.flashdash.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@SpringBootTest(classes = FlashDashApplication.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class AuthenticationServiceTest {

    @Autowired
    private AuthenticationService authenticationService;

    @MockBean
    private UserRepository userRepository;

    @MockBean
    private PasswordEncoder passwordEncoder;

    @Test
    void shouldLoginSuccessfully() {
        // Arrange
        User user = TestUtils.createUser();
        when(userRepository.findByEmail(user.getUsername())).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(true);

        LoginRequest loginRequest = TestUtils.createLoginRequest();

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
        LoginRequest loginRequest = TestUtils.createLoginRequest();
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
        User user = TestUtils.createUser();
        when(userRepository.findByEmail(user.getUsername())).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(false);

        LoginRequest loginRequest = TestUtils.createLoginRequest();

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
        RegisterRequest registerRequest = TestUtils.createRegisterRequest();
        when(userRepository.findByEmail(registerRequest.getEmail())).thenReturn(Optional.empty());
        when(passwordEncoder.encode(registerRequest.getPassword())).thenReturn("encodedPassword");

        // Act
        AuthenticationResponse response = authenticationService.register(registerRequest);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.getToken()).isNotBlank();
        verify(userRepository).findByEmail(registerRequest.getEmail());
        verify(userRepository).save(any(User.class));
    }

    @Test
    void shouldThrowExceptionWhenUserAlreadyExistsDuringRegistration() {
        // Arrange
        User existingUser = TestUtils.createUser();
        when(userRepository.findByEmail(existingUser.getUsername())).thenReturn(Optional.of(existingUser));

        RegisterRequest registerRequest = TestUtils.createRegisterRequest();

        // Act & Assert
        assertThatThrownBy(() -> authenticationService.register(registerRequest))
                .isInstanceOf(FlashDashException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.E409001);

        verify(userRepository).findByEmail(registerRequest.getEmail());
        verifyNoMoreInteractions(userRepository);
        verifyNoInteractions(passwordEncoder);
    }
}
