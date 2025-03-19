package com.flashdash.core.service;

import com.flashdash.core.TestUtils;
import com.flashdash.core.exception.ErrorCode;
import com.flashdash.core.exception.FlashDashException;
import com.flashdash.core.model.User;
import com.flashdash.core.repository.UserRepository;
import com.flashdash.core.service.api.ActivityService;
import com.flashdash.core.service.api.NotificationService;
import com.p4r1nc3.flashdash.core.model.ChangePasswordRequest;
import com.p4r1nc3.flashdash.core.model.UserResponse;
import com.p4r1nc3.flashdash.notification.model.NotificationSubscriber;
import com.p4r1nc3.flashdash.activity.model.ActivityStatisticsResponse;
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
class UserServiceTest {

    @Autowired
    private UserService userService;

    @MockitoBean
    private PasswordEncoder passwordEncoder;

    @MockitoBean
    private ActivityService activityService;

    @MockitoBean
    private NotificationService notificationService;

    @MockitoBean
    private DeckService deckService;

    @MockitoBean
    private GameSessionService gameSessionService;

    @MockitoBean
    private FriendService friendService;

    @MockitoBean
    private UserRepository userRepository;

    private User user;

    @BeforeEach
    void setUp() {
        user = TestUtils.createUser();
    }

    @Test
    void shouldReturnCurrentUserSuccessfully() {
        // Arrange
        when(userRepository.findByUserFrn(user.getUserFrn())).thenReturn(Optional.of(user));

        // Act
        User returnedUser = userService.getCurrentUser(user.getUserFrn());

        // Assert
        assertThat(returnedUser).isNotNull();
        assertThat(returnedUser.getUserFrn()).isEqualTo(user.getUserFrn());
        assertThat(returnedUser.getEmail()).isEqualTo(user.getEmail());

        verify(userRepository).findByUserFrn(user.getUserFrn());
    }

    @Test
    void shouldThrowExceptionWhenCurrentUserNotFound() {
        // Arrange
        String nonExistentUserFrn = "nonexistent-frn";
        when(userRepository.findByUserFrn(nonExistentUserFrn)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> userService.getCurrentUser(nonExistentUserFrn))
                .isInstanceOf(FlashDashException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.E404001)
                .hasMessage("User with userFrn " + nonExistentUserFrn + " not found.");

        verify(userRepository).findByUserFrn(nonExistentUserFrn);
    }

    @Test
    void shouldSaveUserSuccessfully() {
        // Arrange
        doNothing().when(userRepository).save(user);

        // Act
        userService.saveUser(user);

        // Assert
        verify(userRepository).save(user);
    }

    @Test
    void shouldLoadUserByUsernameSuccessfully() {
        // Arrange
        when(userRepository.findByUserFrn(user.getUserFrn())).thenReturn(Optional.of(user));

        // Act
        User loadedUser = (User) userService.loadUserByUsername(user.getUserFrn());

        // Assert
        assertThat(loadedUser).isNotNull();
        assertThat(loadedUser.getUserFrn()).isEqualTo(user.getUserFrn());
        assertThat(loadedUser.getEmail()).isEqualTo(user.getEmail());

        verify(userRepository).findByUserFrn(user.getUserFrn());
    }

    @Test
    void shouldThrowExceptionWhenUserNotFound() {
        // Arrange
        String nonExistentUserFrn = "nonexistent-frn";
        when(userRepository.findByUserFrn(nonExistentUserFrn)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> userService.loadUserByUsername(nonExistentUserFrn))
                .isInstanceOf(FlashDashException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.E404001)
                .hasMessage("User with email " + nonExistentUserFrn + " not found. Please check the userFrn and try again.");

        verify(userRepository).findByUserFrn(nonExistentUserFrn);
    }

    @Test
    void shouldChangePasswordSuccessfully() {
        // Arrange
        ChangePasswordRequest request = new ChangePasswordRequest();
        request.setOldPassword("oldPassword");
        request.setNewPassword("newPassword");

        user.setPassword("encodedOldPassword");
        when(userRepository.findByUserFrn(user.getUserFrn())).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("oldPassword", "encodedOldPassword")).thenReturn(true);
        when(passwordEncoder.encode("newPassword")).thenReturn("encodedNewPassword");

        // Act
        userService.changePassword(user.getUserFrn(), request);

        // Assert
        verify(userRepository).findByUserFrn(user.getUserFrn());
        verify(passwordEncoder).matches("oldPassword", "encodedOldPassword");
        verify(passwordEncoder).encode("newPassword");
        verify(userRepository).save(user);
        verify(activityService).logUserActivity(
                eq(user.getUserFrn()),
                eq(user.getUserFrn()),
                eq(com.p4r1nc3.flashdash.activity.model.LogActivityRequest.ActivityTypeEnum.ACCOUNT_UPDATED)
        );
    }

    @Test
    void shouldThrowExceptionWhenChangingPasswordWithIncorrectOldPassword() {
        // Arrange
        ChangePasswordRequest request = new ChangePasswordRequest();
        request.setOldPassword("wrongOldPassword");
        request.setNewPassword("newPassword");

        user.setPassword("encodedOldPassword");
        when(userRepository.findByUserFrn(user.getUserFrn())).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrongOldPassword", "encodedOldPassword")).thenReturn(false);

        // Act & Assert
        assertThatThrownBy(() -> userService.changePassword(user.getUserFrn(), request))
                .isInstanceOf(FlashDashException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.E401002)
                .hasMessage("Incorrect old password.");

        verify(userRepository).findByUserFrn(user.getUserFrn());
        verify(passwordEncoder).matches("wrongOldPassword", "encodedOldPassword");
        verify(passwordEncoder, never()).encode(anyString());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void shouldDeleteUserSuccessfully() {
        // Arrange
        when(userRepository.findByUserFrn(user.getUserFrn())).thenReturn(Optional.of(user));
        doNothing().when(deckService).deleteAllDecksForUser(user.getUserFrn());
        doNothing().when(gameSessionService).removeAllGameSessionsForUser(user.getUserFrn());
        doNothing().when(friendService).removeAllFriends(user.getUserFrn());
        doNothing().when(notificationService).unregisterSubscriber(user.getUserFrn());

        // Act
        userService.deleteUser(user.getUserFrn());

        // Assert
        verify(userRepository).findByUserFrn(user.getUserFrn());
        verify(deckService).deleteAllDecksForUser(user.getUserFrn());
        verify(gameSessionService).removeAllGameSessionsForUser(user.getUserFrn());
        verify(friendService).removeAllFriends(user.getUserFrn());
        verify(userRepository).delete(user);
        verify(notificationService).unregisterSubscriber(user.getUserFrn());
        verify(activityService).logUserActivity(user.getUserFrn(), user.getUserFrn(),
                com.p4r1nc3.flashdash.activity.model.LogActivityRequest.ActivityTypeEnum.ACCOUNT_DELETED);
    }

    @Test
    void shouldThrowExceptionWhenDeletingNonExistentUser() {
        // Arrange
        String nonExistentUserFrn = "nonexistent-frn";
        when(userRepository.findByUserFrn(nonExistentUserFrn)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> userService.deleteUser(nonExistentUserFrn))
                .isInstanceOf(FlashDashException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.E404001)
                .hasMessage("User with email " + nonExistentUserFrn + " not found.");

        verify(userRepository).findByUserFrn(nonExistentUserFrn);
        verify(deckService, never()).deleteAllDecksForUser(anyString());
        verify(gameSessionService, never()).removeAllGameSessionsForUser(anyString());
        verify(friendService, never()).removeAllFriends(anyString());
        verify(userRepository, never()).delete(any(User.class));
        verify(notificationService, never()).unregisterSubscriber(anyString());
    }
}