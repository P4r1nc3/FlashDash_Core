package com.flashdash.service;

import com.flashdash.FlashDashApplication;
import com.flashdash.TestUtils;
import com.flashdash.exception.FlashDashException;
import com.flashdash.exception.ErrorCode;
import com.flashdash.model.User;
import com.flashdash.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@SpringBootTest(classes = FlashDashApplication.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class CustomUserDetailsServiceTest {

    @Autowired
    private CustomUserDetailsService customUserDetailsService;

    @MockBean
    private UserRepository userRepository;

    @Test
    void shouldLoadUserByUsernameSuccessfully() {
        // Arrange
        User user = TestUtils.createUser();
        when(userRepository.findByEmail(user.getUsername())).thenReturn(Optional.of(user));

        // Act
        User loadedUser = (User) customUserDetailsService.loadUserByUsername("test@example.com");

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
        assertThatThrownBy(() -> customUserDetailsService.loadUserByUsername("nonexistent@example.com"))
                .isInstanceOf(FlashDashException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.E404001);

        verify(userRepository).findByEmail("nonexistent@example.com");
    }
}
