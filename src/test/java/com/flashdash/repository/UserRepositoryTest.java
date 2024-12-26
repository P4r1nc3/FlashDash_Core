package com.flashdash.repository;

import com.flashdash.FlashDashApplication;
import com.flashdash.model.User;
import com.flashdash.TestUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(classes = FlashDashApplication.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
    }

    @Test
    void shouldFindUserByEmail() {
        // Arrange
        User user = TestUtils.createUser();
        userRepository.save(user);

        // Act
        Optional<User> foundUser = userRepository.findByEmail("test@example.com");

        // Assert
        assertThat(foundUser).isPresent();
        assertThat(foundUser.get().getUsername()).isEqualTo("test@example.com");
        assertThat(foundUser.get().getFirstName()).isEqualTo("Test");
        assertThat(foundUser.get().getLastName()).isEqualTo("User");
    }

    @Test
    void shouldReturnEmptyWhenEmailDoesNotExist() {
        // Act
        Optional<User> foundUser = userRepository.findByEmail("nonexistent@example.com");

        // Assert
        assertThat(foundUser).isNotPresent();
    }

    @Test
    void shouldFindUserByActivationToken() {
        // Arrange
        User user = TestUtils.createUser();
        String activationToken = "some-activation-token";
        user.setActivationToken(activationToken);
        userRepository.save(user);

        // Act
        Optional<User> foundUser = userRepository.findByActivationToken(activationToken);

        // Assert
        assertThat(foundUser).isPresent();
        assertThat(foundUser.get().getActivationToken()).isEqualTo(activationToken);
        assertThat(foundUser.get().getUsername()).isEqualTo("test@example.com");
    }

    @Test
    void shouldReturnEmptyWhenActivationTokenDoesNotExist() {
        // Act
        Optional<User> foundUser = userRepository.findByActivationToken("nonexistent-token");

        // Assert
        assertThat(foundUser).isNotPresent();
    }
}
