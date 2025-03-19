package com.flashdash.core.controller;

import com.flashdash.core.TestUtils;
import com.flashdash.core.exception.ErrorCode;
import com.flashdash.core.exception.FlashDashException;
import com.flashdash.core.model.User;
import com.flashdash.core.service.UserService;
import com.flashdash.core.service.api.NotificationService;
import com.flashdash.core.utils.EntityToResponseMapper;
import com.p4r1nc3.flashdash.core.model.ChangePasswordRequest;
import com.p4r1nc3.flashdash.core.model.UserResponse;
import com.p4r1nc3.flashdash.notification.model.NotificationSubscriber;
import org.junit.jupiter.api.*;
import org.mockito.MockedStatic;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.time.LocalTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@SpringBootTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class UserControllerTest {

    @Autowired
    private UserController userController;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private NotificationService notificationService;

    @MockitoBean
    private EntityToResponseMapper entityToResponseMapper;

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
    void shouldReturnCurrentUserSuccessfully() {
        // Arrange
        UserResponse userResponse = new UserResponse();
        userResponse.setUserFrn(user.getUserFrn());
        userResponse.setFirstName(user.getFirstName());
        userResponse.setLastName(user.getLastName());
        userResponse.setEmail(user.getEmail());

        when(userService.getCurrentUser(user.getUserFrn())).thenReturn(user);
        when(entityToResponseMapper.mapToUserResponse(user)).thenReturn(userResponse);

        // Act
        ResponseEntity<UserResponse> response = userController.getUser();

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.getStatusCodeValue()).isEqualTo(200);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getUserFrn()).isEqualTo(user.getUserFrn());
        assertThat(response.getBody().getEmail()).isEqualTo(user.getEmail());

        verify(userService).getCurrentUser(user.getUserFrn());
        verify(entityToResponseMapper).mapToUserResponse(user);
    }

    @Test
    void shouldHandleUserNotFound() {
        // Arrange
        when(userService.getCurrentUser(user.getUserFrn())).thenThrow(new FlashDashException(
                ErrorCode.E404001,
                "User not found."
        ));

        // Act & Assert
        FlashDashException exception = assertThrows(
                FlashDashException.class,
                () -> userController.getUser()
        );
        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.E404001);
        verify(userService).getCurrentUser(user.getUserFrn());
    }

    @Test
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

        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.E401002);
        assertThat(exception.getMessage()).isEqualTo("Incorrect old password.");
        verify(userService, times(1)).changePassword(user.getUsername(), request);
    }

    @Test
    void shouldDeleteUserSuccessfully() {
        // Arrange
        doNothing().when(userService).deleteUser(user.getUserFrn());

        // Act
        ResponseEntity<Void> response = userController.deleteUser();

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.getStatusCodeValue()).isEqualTo(204);
        verify(userService, times(1)).deleteUser(user.getUserFrn());
    }

    @Test
    void shouldThrowExceptionWhenDeletingNonExistentUser() {
        // Arrange
        doThrow(new FlashDashException(ErrorCode.E404001, "User not found."))
                .when(userService).deleteUser(user.getUserFrn());

        // Act & Assert
        FlashDashException exception = assertThrows(
                FlashDashException.class,
                () -> userController.deleteUser()
        );

        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.E404001);
        assertThat(exception.getMessage()).isEqualTo("User not found.");
        verify(userService, times(1)).deleteUser(user.getUserFrn());
    }

    @Test
    void shouldEnableNotificationsSuccessfully() {
        // Arrange
        LocalTime notificationTime = LocalTime.of(12, 30);
        doNothing().when(notificationService).enableDailyNotifications(user.getUserFrn(), notificationTime);

        // Act
        ResponseEntity<Void> response = userController.enableNotifications(notificationTime);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.getStatusCodeValue()).isEqualTo(200);
        verify(notificationService, times(1)).enableDailyNotifications(user.getUserFrn(), notificationTime);
    }

    @Test
    void shouldEnableNotificationsWithNullTimeSuccessfully() {
        // Arrange
        doNothing().when(notificationService).enableDailyNotifications(user.getUserFrn(), null);

        // Act
        ResponseEntity<Void> response = userController.enableNotifications(null);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.getStatusCodeValue()).isEqualTo(200);
        verify(notificationService, times(1)).enableDailyNotifications(user.getUserFrn(), null);
    }

    @Test
    void shouldReturnNotificationDetailsSuccessfully() {
        // Arrange
        NotificationSubscriber mockSubscriber = new NotificationSubscriber();
        mockSubscriber.setUserFrn(user.getUserFrn());
        mockSubscriber.setDailyNotifications(true);

        when(notificationService.getSubscriber(user.getUserFrn())).thenReturn(mockSubscriber);

        // Act
        ResponseEntity<NotificationSubscriber> response = userController.getNotificationsDetails();

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.getStatusCodeValue()).isEqualTo(200);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getUserFrn()).isEqualTo(user.getUserFrn());
        assertThat(response.getBody().getDailyNotifications()).isTrue();

        verify(notificationService, times(1)).getSubscriber(user.getUserFrn());
    }

    @Test
    void shouldDisableNotificationsSuccessfully() {
        // Arrange
        doNothing().when(notificationService).disableDailyNotifications(user.getUserFrn());

        // Act
        ResponseEntity<Void> response = userController.disableNotifications();

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.getStatusCodeValue()).isEqualTo(200);
        verify(notificationService, times(1)).disableDailyNotifications(user.getUserFrn());
    }
}