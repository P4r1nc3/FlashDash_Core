package com.flashdash.core.controller;

import com.flashdash.core.TestUtils;
import com.flashdash.core.exception.ErrorCode;
import com.flashdash.core.exception.FlashDashException;
import com.flashdash.core.model.User;
import com.flashdash.core.service.AuthenticationService;
import com.p4r1nc3.flashdash.core.model.AuthenticationResponse;
import com.p4r1nc3.flashdash.core.model.LoginRequest;
import com.p4r1nc3.flashdash.core.model.RegisterRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

@SpringBootTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class AuthenticationControllerTest {

    @Autowired
    private AuthenticationController authenticationController;

    @MockitoBean
    private AuthenticationService authenticationService;

    private User user;

    @BeforeEach
    void setUp() {
        user = TestUtils.createUser();
    }

    @Test
    public void testLoginSuccessful() {
        // Arrange
        LoginRequest loginRequest = TestUtils.createLoginRequest(user);
        AuthenticationResponse authenticationResponse = TestUtils.createAuthenticationResponse();
        when(authenticationService.login(loginRequest)).thenReturn(authenticationResponse);

        // Act
        ResponseEntity<AuthenticationResponse> responseEntity = authenticationController.login(loginRequest);

        // Assert
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertEquals(authenticationResponse, responseEntity.getBody());
    }

    @Test
    public void testLoginUserNotFound() {
        // Arrange
        LoginRequest loginRequest = TestUtils.createLoginRequest(user);
        String expectedMessage = "User with email not found. Please check the email and try again.";
        doThrow(new FlashDashException(ErrorCode.E404001, expectedMessage))
                .when(authenticationService)
                .login(loginRequest);

        // Act & Assert
        FlashDashException exception = assertThrows(
                FlashDashException.class,
                () -> authenticationController.login(loginRequest)
        );
        assertEquals(ErrorCode.E404001, exception.getErrorCode());
        assertEquals(expectedMessage, exception.getMessage());
    }

    @Test
    public void testLoginInvalidPassword() {
        // Arrange
        LoginRequest loginRequest = TestUtils.createLoginRequest(user);
        String expectedMessage = "Invalid password for email. Please check your credentials and try again.";
        doThrow(new FlashDashException(ErrorCode.E401002, expectedMessage))
                .when(authenticationService)
                .login(loginRequest);

        // Act & Assert
        FlashDashException exception = assertThrows(
                FlashDashException.class,
                () -> authenticationController.login(loginRequest)
        );
        assertEquals(ErrorCode.E401002, exception.getErrorCode());
        assertEquals(expectedMessage, exception.getMessage());
    }

    @Test
    public void testRegisterSuccessful() {
        // Arrange
        RegisterRequest registerRequest = TestUtils.createRegisterRequest(user);
        AuthenticationResponse authenticationResponse = TestUtils.createAuthenticationResponse();
        when(authenticationService.register(registerRequest)).thenReturn(authenticationResponse);

        // Act
        ResponseEntity<AuthenticationResponse> responseEntity = authenticationController.register(registerRequest);
        HttpStatusCode httpStatus = responseEntity.getStatusCode();
        AuthenticationResponse responseBody = responseEntity.getBody();

        // Assert
        assertEquals(HttpStatus.OK, httpStatus);
        assertEquals(authenticationResponse, responseBody);
    }

    @Test
    public void testRegisterUserAlreadyExists() {
        // Arrange
        RegisterRequest registerRequest = TestUtils.createRegisterRequest(user);
        String expectedMessage = "User with email already exists. Please use a different email to register.";
        doThrow(new FlashDashException(ErrorCode.E409001, expectedMessage))
                .when(authenticationService)
                .register(registerRequest);

        // Act & Assert
        FlashDashException exception = assertThrows(
                FlashDashException.class,
                () -> authenticationController.register(registerRequest)
        );
        assertEquals(ErrorCode.E409001, exception.getErrorCode());
        assertEquals(expectedMessage, exception.getMessage());
    }

    @Test
    public void testActivateAccountSuccessful() {
        // Arrange
        String activationToken = "validActivationToken";

        // Act
        ResponseEntity<String> responseEntity = authenticationController.activateAccount(activationToken);

        // Assert
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertEquals("Account activated successfully!", responseEntity.getBody());
    }

    @Test
    public void testActivateAccountInvalidToken() {
        // Arrange
        String invalidToken = "invalidActivationToken";
        String expectedMessage = "Invalid activation token.";
        doThrow(new FlashDashException(ErrorCode.E404001, expectedMessage))
                .when(authenticationService)
                .activateAccount(invalidToken);

        // Act & Assert
        FlashDashException exception = assertThrows(
                FlashDashException.class,
                () -> authenticationController.activateAccount(invalidToken)
        );

        assertEquals(ErrorCode.E404001, exception.getErrorCode());
        assertEquals(expectedMessage, exception.getMessage());
    }

    @Test
    public void testActivateAccountAlreadyActivated() {
        // Arrange
        String alreadyActivatedToken = "alreadyActivatedToken";
        String expectedMessage = "Account is already activated.";
        doThrow(new FlashDashException(ErrorCode.E400001, expectedMessage))
                .when(authenticationService)
                .activateAccount(alreadyActivatedToken);

        // Act & Assert
        FlashDashException exception = assertThrows(
                FlashDashException.class,
                () -> authenticationController.activateAccount(alreadyActivatedToken)
        );

        assertEquals(ErrorCode.E400001, exception.getErrorCode());
        assertEquals(expectedMessage, exception.getMessage());
    }

}
