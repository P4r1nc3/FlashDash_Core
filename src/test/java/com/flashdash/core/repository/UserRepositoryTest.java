package com.flashdash.core.repository;

import com.flashdash.core.TestUtils;
import com.flashdash.core.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@Transactional
@SpringBootTest
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
        Optional<User> foundUser = userRepository.findByEmail(user.getEmail());

        // Assert
        assertThat(foundUser).isPresent();
        assertThat(foundUser.get().getUserFrn()).isEqualTo(user.getUserFrn());
        assertThat(foundUser.get().getEmail()).isEqualTo(user.getEmail());
        assertThat(foundUser.get().getFirstName()).isEqualTo(user.getFirstName());
        assertThat(foundUser.get().getLastName()).isEqualTo(user.getLastName());
    }

    @Test
    void shouldReturnEmptyWhenEmailDoesNotExist() {
        // Act
        Optional<User> foundUser = userRepository.findByEmail("nonexistent@example.com");

        // Assert
        assertThat(foundUser).isNotPresent();
    }

    @Test
    void shouldFindUserByUserFrn() {
        // Arrange
        User user = TestUtils.createUser();
        userRepository.save(user);

        // Act
        Optional<User> foundUser = userRepository.findByUserFrn(user.getUserFrn());

        // Assert
        assertThat(foundUser).isPresent();
        assertThat(foundUser.get().getUserFrn()).isEqualTo(user.getUserFrn());
        assertThat(foundUser.get().getEmail()).isEqualTo(user.getEmail());
        assertThat(foundUser.get().getFirstName()).isEqualTo(user.getFirstName());
        assertThat(foundUser.get().getLastName()).isEqualTo(user.getLastName());
    }

    @Test
    void shouldReturnEmptyWhenUserFrnDoesNotExist() {
        // Act
        Optional<User> foundUser = userRepository.findByUserFrn("nonexistent-userFrn");

        // Assert
        assertThat(foundUser).isNotPresent();
    }


    @Test
    void shouldFindUserByActivationToken() {
        // Arrange
        User user = TestUtils.createUser();
        String activationToken = "test-activation-token";
        user.setActivationToken(activationToken);
        userRepository.save(user);

        // Act
        Optional<User> foundUser = userRepository.findByActivationToken(activationToken);

        // Assert
        assertThat(foundUser).isPresent();
        assertThat(foundUser.get().getActivationToken()).isEqualTo(activationToken);
        assertThat(foundUser.get().getUserFrn()).isEqualTo(user.getUserFrn());
    }

    @Test
    void shouldReturnEmptyWhenActivationTokenDoesNotExist() {
        // Act
        Optional<User> foundUser = userRepository.findByActivationToken("nonexistent-token");

        // Assert
        assertThat(foundUser).isNotPresent();
    }

    @Test
    void shouldFindUsersByUserFrnList() {
        // Arrange
        User user1 = TestUtils.createUser();
        userRepository.save(user1);

        User user2 = TestUtils.createUser();
        userRepository.save(user2);

        List<String> userFrns = List.of(user1.getUserFrn(), user2.getUserFrn());

        // Act
        List<User> foundUsers = userRepository.findByUserFrnIn(userFrns);

        // Assert
        assertThat(foundUsers).hasSize(2);
        assertThat(foundUsers).extracting(User::getUserFrn).containsExactlyInAnyOrder(user1.getUserFrn(), user2.getUserFrn());
    }

}
