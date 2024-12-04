package com.flashdash.controller;

import com.flashdash.FlashDashApplication;
import com.flashdash.TestUtils;
import com.flashdash.dto.response.AuthenticationResponse;
import com.flashdash.dto.request.LoginRequest;
import com.flashdash.dto.request.RegisterRequest;
import com.flashdash.exception.ErrorCode;
import com.flashdash.exception.FlashDashException;
import com.flashdash.service.AuthenticationService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

@SpringBootTest(classes = FlashDashApplication.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class AuthenticationControllerTest {

    @Autowired
    private AuthenticationController authenticationController;

    @MockBean
    private AuthenticationService authenticationService;

    @Test
    public void testLoginSuccessful() {
        // Arrange
        LoginRequest loginRequest = TestUtils.createLoginRequest();
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
        LoginRequest loginRequest = TestUtils.createLoginRequest();
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
        LoginRequest loginRequest = TestUtils.createLoginRequest();
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
        RegisterRequest registerRequest = TestUtils.createRegisterRequest();
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
        RegisterRequest registerRequest = TestUtils.createRegisterRequest();
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
}
