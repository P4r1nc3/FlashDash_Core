package com.flashdash.service;

import com.flashdash.FlashDashApplication;
import com.flashdash.TestUtils;
import com.flashdash.dto.request.ChangePasswordRequest;
import com.flashdash.dto.response.UserResponse;
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
class UserServiceTest {

    @Autowired
    private UserService userService;

    @MockBean
    private GameSessionService gameSessionService;

    @MockBean
    private DeckService deckService;

    @MockBean
    private FriendService friendService;

    @MockBean
    private UserRepository userRepository;

    @MockBean
    private PasswordEncoder passwordEncoder;

    @Test
    void shouldReturnCurrentUserSuccessfully() {
        // Arrange
        User user = TestUtils.createUser();
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));

        // Act
        UserResponse userResponse = userService.getCurrentUser("test@example.com");

        // Assert
        assertThat(userResponse).isNotNull();
        assertThat(userResponse.getFirstName()).isEqualTo(user.getFirstName());
        assertThat(userResponse.getLastName()).isEqualTo(user.getLastName());
        assertThat(userResponse.getEmail()).isEqualTo(user.getUsername());
        assertThat(userResponse.getCreatedAt()).isEqualTo(user.getCreatedAt());
        assertThat(userResponse.getUpdatedAt()).isEqualTo(user.getUpdatedAt());

        verify(userRepository).findByEmail("test@example.com");
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
        User user = TestUtils.createUser();
        when(userRepository.findByEmail(user.getUsername())).thenReturn(Optional.of(user));

        // Act
        User loadedUser = (User) userService.loadUserByUsername("test@example.com");

        // Assert
        assertThat(loadedUser).isNotNull();
        assertThat(loadedUser.getUsername()).isEqualTo(user.getUsername());
        assertThat(loadedUser.getPassword()).isEqualTo(user.getPassword());

        verify(userRepository).findByEmail("test@example.com");
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
        User user = TestUtils.createUser();
        ChangePasswordRequest request = new ChangePasswordRequest();
        request.setOldPassword("wrongPassword");
        request.setNewPassword("newPassword");

        when(userRepository.findByEmail(user.getUsername())).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(request.getOldPassword(), user.getPassword())).thenReturn(false);

        // Act & Assert
        assertThatThrownBy(() -> userService.changePassword(user.getUsername(), request))
                .isInstanceOf(FlashDashException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.E401002)
                .hasMessage("Incorrect old password.");

        verify(userRepository).findByEmail(user.getUsername());
        verify(passwordEncoder).matches(request.getOldPassword(), user.getPassword());
        verify(userRepository, times(0)).save(user);
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

        verify(userRepository, times(0)).save(any(User.class));
    }

    @Test
    void shouldDeleteUserSuccessfully() {
        // Arrange
        User user = TestUtils.createUser();

        when(userRepository.findByEmail(user.getUsername())).thenReturn(Optional.of(user));
        doNothing().when(deckService).deleteAllDecksForUser(user);
        doNothing().when(gameSessionService).removeAllGameSessionsForUser(user);
        doNothing().when(friendService).removeAllFriends(user.getUsername());
        doNothing().when(userRepository).delete(user);

        // Act
        userService.deleteUser(user.getUsername());

        // Assert
        verify(userRepository, times(1)).findByEmail(user.getUsername());
        verify(deckService, times(1)).deleteAllDecksForUser(user);
        verify(gameSessionService, times(1)).removeAllGameSessionsForUser(user);
        verify(friendService, times(1)).removeAllFriends(user.getUsername());
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
        verify(deckService, never()).deleteAllDecksForUser(any(User.class));
        verify(gameSessionService, never()).removeAllGameSessionsForUser(any(User.class));
        verify(friendService, never()).removeAllFriends(anyString());
        verify(userRepository, never()).delete(any(User.class));
    }
}
