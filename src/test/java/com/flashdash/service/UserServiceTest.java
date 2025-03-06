package com.flashdash.service;

import com.flashdash.FlashDashApplication;
import com.flashdash.TestUtils;
import com.flashdash.dto.response.UserResponse;
import com.flashdash.exception.ErrorCode;
import com.flashdash.exception.FlashDashException;
import com.flashdash.model.User;
import com.flashdash.repository.UserRepository;
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

@SpringBootTest(classes = FlashDashApplication.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class UserServiceTest {

    @Autowired
    private UserService userService;

    @MockitoBean
    private GameSessionService gameSessionService;

    @MockitoBean
    private DeckService deckService;

    @MockitoBean
    private FriendService friendService;

    @MockitoBean
    private UserRepository userRepository;

    @MockitoBean
    private PasswordEncoder passwordEncoder;

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
        UserResponse userResponse = userService.getCurrentUser(user.getEmail());

        // Assert
        assertThat(userResponse).isNotNull();
        assertThat(userResponse.getFirstName()).isEqualTo(user.getFirstName());
        assertThat(userResponse.getLastName()).isEqualTo(user.getLastName());
        assertThat(userResponse.getEmail()).isEqualTo(user.getEmail());
        assertThat(userResponse.getCreatedAt()).isEqualTo(user.getCreatedAt());
        assertThat(userResponse.getUpdatedAt()).isEqualTo(user.getUpdatedAt());

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
        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));

        // Act
        User loadedUser = (User) userService.loadUserByUsername(user.getEmail());

        // Assert
        assertThat(loadedUser).isNotNull();
        assertThat(loadedUser.getUsername()).isEqualTo(user.getUsername());
        assertThat(loadedUser.getPassword()).isEqualTo(user.getPassword());

        verify(userRepository).findByEmail(user.getEmail());
    }

    @Test
    void shouldThrowExceptionWhenUserNotFound() {
        // Arrange
        when(userRepository.findByEmail("nonexistent@example.com")).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> userService.loadUserByUsername("nonexistent@example.com"))
                .isInstanceOf(FlashDashException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.E404001);

        verify(userRepository).findByEmail("nonexistent@example.com");
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

        // Verify that the repository was queried but user was not saved
        verify(userRepository, times(1)).findByEmail("nonexistent@example.com");
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
        verify(userRepository, times(1)).findByEmail(user.getEmail());
        verify(deckService, times(1)).deleteAllDecksForUser(user.getUserFrn());
        verify(gameSessionService, times(1)).removeAllGameSessionsForUser(user.getUserFrn());
        verify(friendService, times(1)).removeAllFriends(user.getUserFrn());
        verify(userRepository, times(1)).delete(user);
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

        verify(userRepository, times(1)).findByEmail("nonexistent@example.com");
        verify(deckService, never()).deleteAllDecksForUser(anyString());
        verify(gameSessionService, never()).removeAllGameSessionsForUser(anyString());
        verify(friendService, never()).removeAllFriends(anyString());
        verify(userRepository, never()).delete(any(User.class));
    }
}
