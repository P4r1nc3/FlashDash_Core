package com.flashdash.controller;

import com.flashdash.FlashDashApplication;
import com.flashdash.TestUtils;
import com.flashdash.dto.response.UserResponse;
import com.flashdash.exception.ErrorCode;
import com.flashdash.exception.FlashDashException;
import com.flashdash.model.User;
import com.flashdash.service.UserService;
import com.p4r1nc3.flashdash.core.model.ChangePasswordRequest;
import org.junit.jupiter.api.*;
import org.mockito.MockedStatic;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@SpringBootTest(classes = FlashDashApplication.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class UserControllerTest {

    @Autowired
    private UserController userController;

    @MockitoBean
    private UserService userService;

    private User user;
    private MockedStatic<SecurityContextHolder> mockedSecurityContextHolder;

    @BeforeEach
    void setUp() {
        user = TestUtils.createUser();

        Authentication authentication = mock(Authentication.class);
        when(authentication.getPrincipal()).thenReturn(user);
        when(authentication.getName()).thenReturn(user.getUsername());

        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);

        mockedSecurityContextHolder = mockStatic(SecurityContextHolder.class);
        mockedSecurityContextHolder.when(SecurityContextHolder::getContext).thenReturn(securityContext);
    }

    @AfterEach
    void tearDown() {
        mockedSecurityContextHolder.close();
    }

    @Test
    @Order(1)
    void shouldReturnCurrentUserSuccessfully() {
        // Arrange
        UserResponse userResponse = new UserResponse();
        userResponse.setFirstName(user.getFirstName());
        userResponse.setLastName(user.getLastName());
        userResponse.setEmail(user.getUsername());
        userResponse.setDailyNotifications(user.isDailyNotifications());
        userResponse.setCreatedAt(user.getCreatedAt());
        userResponse.setUpdatedAt(user.getUpdatedAt());

        when(userService.getCurrentUser(user.getUsername())).thenReturn(userResponse);

        // Act
        ResponseEntity<UserResponse> response = userController.getUser();

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.getStatusCodeValue()).isEqualTo(200);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getEmail()).isEqualTo(user.getUsername());

        verify(userService).getCurrentUser(user.getUsername());
    }

    @Test
    @Order(2)
    void shouldHandleUserNotFound() {
        // Arrange
        when(userService.getCurrentUser(user.getUsername())).thenThrow(new FlashDashException(
                ErrorCode.E404001,
                "User not found."
        ));

        // Act & Assert
        FlashDashException exception = assertThrows(
                FlashDashException.class,
                () -> userController.getUser()
        );
        assertEquals(ErrorCode.E404001, exception.getErrorCode());
        verify(userService).getCurrentUser(user.getUsername());
    }

    @Test
    @Order(3)
    void shouldChangePasswordSuccessfully() {
        // Arrange
        ChangePasswordRequest request = new ChangePasswordRequest();
        request.setOldPassword("oldPassword");
        request.setNewPassword("newPassword");

        doNothing().when(userService).changePassword(user.getUsername(), request);

        // Act
        ResponseEntity<Void> response = userController.changePassword(request);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.getStatusCodeValue()).isEqualTo(200);
        verify(userService, times(1)).changePassword(user.getUsername(), request);
    }

    @Test
    @Order(4)
    void shouldThrowExceptionWhenChangingPasswordWithIncorrectOldPassword() {
        // Arrange
        ChangePasswordRequest request = new ChangePasswordRequest();
        request.setOldPassword("wrongOldPassword");
        request.setNewPassword("newPassword");

        doThrow(new FlashDashException(ErrorCode.E401002, "Incorrect old password."))
                .when(userService).changePassword(user.getUsername(), request);

        // Act & Assert
        FlashDashException exception = assertThrows(
                FlashDashException.class,
                () -> userController.changePassword(request)
        );

        assertEquals(ErrorCode.E401002, exception.getErrorCode());
        assertEquals("Incorrect old password.", exception.getMessage());
        verify(userService, times(1)).changePassword(user.getUsername(), request);
    }

    @Test
    @Order(5)
    void shouldThrowExceptionWhenChangingPasswordNonExistentUser() {
        // Arrange
        ChangePasswordRequest request = new ChangePasswordRequest();
        request.setOldPassword("wrongOldPassword");
        request.setNewPassword("newPassword");

        doThrow(new FlashDashException(ErrorCode.E404001, "User not found."))
                .when(userService).changePassword(user.getUsername(), request);

        // Act & Assert
        FlashDashException exception = assertThrows(
                FlashDashException.class,
                () -> userController.changePassword(request)
        );

        assertEquals(ErrorCode.E404001, exception.getErrorCode());
        assertEquals("User not found.", exception.getMessage());
        verify(userService, times(1)).changePassword(user.getUsername(), request);
    }

    @Test
    @Order(6)
    void shouldDeleteUserSuccessfully() {
        // Arrange
        doNothing().when(userService).deleteUser(user.getUsername());

        // Act
        ResponseEntity<Void> response = userController.deleteUser();

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.getStatusCodeValue()).isEqualTo(204);
        verify(userService, times(1)).deleteUser(user.getUsername());
    }

    @Test
    @Order(7)
    void shouldThrowExceptionWhenDeletingNonExistentUser() {
        // Arrange
        doThrow(new FlashDashException(ErrorCode.E404001, "User not found."))
                .when(userService).deleteUser(user.getUsername());

        // Act & Assert
        FlashDashException exception = assertThrows(
                FlashDashException.class,
                () -> userController.deleteUser()
        );

        assertEquals(ErrorCode.E404001, exception.getErrorCode());
        assertEquals("User not found.", exception.getMessage());
        verify(userService, times(1)).deleteUser(user.getUsername());
    }

}
