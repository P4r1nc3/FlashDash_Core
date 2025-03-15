package com.flashdash.core.service;

import com.flashdash.core.TestUtils;
import com.flashdash.core.exception.ErrorCode;
import com.flashdash.core.exception.FlashDashException;
import com.flashdash.core.model.User;
import com.flashdash.core.repository.UserRepository;
import com.p4r1nc3.flashdash.core.model.ChangePasswordRequest;
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
        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));

        // Act
        User returnedUser = userService.getCurrentUser(user.getEmail());

        // Assert
        assertThat(returnedUser).isNotNull();
        assertThat(returnedUser.getUserFrn()).isEqualTo(user.getUserFrn());
        assertThat(returnedUser.getEmail()).isEqualTo(user.getEmail());

        verify(userRepository).findByEmail(user.getEmail());
    }

    @Test
    void shouldThrowExceptionWhenCurrentUserNotFound() {
        // Arrange
        when(userRepository.findByEmail("nonexistent@example.com")).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> userService.getCurrentUser("nonexistent@example.com"))
                .isInstanceOf(FlashDashException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.E404001);

        verify(userRepository).findByEmail("nonexistent@example.com");
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
        when(userRepository.findByUserFrn("nonexistent-frn")).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> userService.loadUserByUsername("nonexistent-frn"))
                .isInstanceOf(FlashDashException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.E404001)
                .hasMessage("User with email nonexistent-frn not found. Please check the userFrn and try again.");

        verify(userRepository).findByUserFrn("nonexistent-frn");
    }

    @Test
    void shouldThrowExceptionWhenOldPasswordIsIncorrect() {
        // Arrange
        ChangePasswordRequest request = new ChangePasswordRequest();
        request.setOldPassword("wrongPassword");
        request.setNewPassword("newPassword");

        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(request.getOldPassword(), user.getPassword())).thenReturn(false);

        // Act & Assert
        assertThatThrownBy(() -> userService.changePassword(user.getEmail(), request))
                .isInstanceOf(FlashDashException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.E401002)
                .hasMessage("Incorrect old password.");

        verify(userRepository).findByEmail(user.getEmail());
        verify(passwordEncoder).matches(request.getOldPassword(), user.getPassword());
        verify(userRepository, never()).save(user);
    }

    @Test
    void shouldThrowExceptionWhenChangingPasswordForNonExistentUser() {
        // Arrange
        ChangePasswordRequest request = new ChangePasswordRequest();
        request.setOldPassword("oldPassword");
        request.setNewPassword("newPassword");

        when(userRepository.findByEmail("nonexistent@example.com")).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> userService.changePassword("nonexistent@example.com", request))
                .isInstanceOf(FlashDashException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.E404001)
                .hasMessage("User with email nonexistent@example.com not found.");

        verify(userRepository).findByEmail("nonexistent@example.com");
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void shouldDeleteUserSuccessfully() {
        // Arrange
        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
        doNothing().when(deckService).deleteAllDecksForUser(user.getUserFrn());
        doNothing().when(gameSessionService).removeAllGameSessionsForUser(user.getUserFrn());
        doNothing().when(friendService).removeAllFriends(user.getUserFrn());
        doNothing().when(userRepository).delete(user);

        // Act
        userService.deleteUser(user.getEmail());

        // Assert
        verify(userRepository).findByEmail(user.getEmail());
        verify(deckService).deleteAllDecksForUser(user.getUserFrn());
        verify(gameSessionService).removeAllGameSessionsForUser(user.getUserFrn());
        verify(friendService).removeAllFriends(user.getUserFrn());
        verify(userRepository).delete(user);
    }

    @Test
    void shouldThrowExceptionWhenDeletingNonExistentUser() {
        // Arrange
        when(userRepository.findByEmail("nonexistent@example.com")).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> userService.deleteUser("nonexistent@example.com"))
                .isInstanceOf(FlashDashException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.E404001)
                .hasMessage("User with email nonexistent@example.com not found.");

        verify(userRepository).findByEmail("nonexistent@example.com");
        verify(deckService, never()).deleteAllDecksForUser(anyString());
        verify(gameSessionService, never()).removeAllGameSessionsForUser(anyString());
        verify(friendService, never()).removeAllFriends(anyString());
        verify(userRepository, never()).delete(any(User.class));
    }
}
