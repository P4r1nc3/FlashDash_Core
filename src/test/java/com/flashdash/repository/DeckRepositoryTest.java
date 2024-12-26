package com.flashdash.repository;

import com.flashdash.FlashDashApplication;
import com.flashdash.TestUtils;
import com.flashdash.model.Deck;
import com.flashdash.model.User;
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
@SpringBootTest(classes = FlashDashApplication.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class DeckRepositoryTest {

    @Autowired
    private DeckRepository deckRepository;

    @Autowired
    private UserRepository userRepository;

    @BeforeEach
    void setUp() {
        deckRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    void shouldFindAllDecksByUser() {
        // Arrange
        User user = TestUtils.createUser();
        userRepository.save(user);

        Deck deck1 = TestUtils.createDeck(user);
        deck1.setName("Deck 1");
        deckRepository.save(deck1);

        Deck deck2 = TestUtils.createDeck(user);
        deck2.setName("Deck 2");
        deckRepository.save(deck2);

        // Act
        List<Deck> decks = deckRepository.findAllByUser(user);

        // Assert
        assertThat(decks).hasSize(2);
        assertThat(decks).extracting(Deck::getName)
                .containsExactlyInAnyOrder(deck1.getName(), deck2.getName());
    }

    @Test
    void shouldFindSpecificDeckByIdAndUser() {
        // Arrange
        User user = TestUtils.createUser();
        userRepository.save(user);

        Deck deck = TestUtils.createDeck(user);
        deckRepository.save(deck);

        // Act
        Optional<Deck> foundDeck = deckRepository.findByIdAndUser(deck.getId(), user);

        // Assert
        assertThat(foundDeck).isPresent();
        assertThat(foundDeck.get().getName()).isEqualTo(deck.getName());
    }

    @Test
    void shouldReturnEmptyWhenDeckBelongsToAnotherUser() {
        // Arrange
        User user1 = TestUtils.createUser();
        userRepository.save(user1);

        User user2 = new User();
        user2.setUsername("anotheruser@example.com");
        user2.setPassword("password456");
        user2.setFirstName("Another");
        user2.setLastName("User");
        userRepository.save(user2);

        Deck deck = TestUtils.createDeck(user1);
        deckRepository.save(deck);

        // Act
        Optional<Deck> foundDeck = deckRepository.findByIdAndUser(deck.getId(), user2);

        // Assert
        assertThat(foundDeck).isNotPresent();
    }

    @Test
    void shouldDeleteAllDecksByUser() {
        // Arrange
        User user = TestUtils.createUser();
        userRepository.save(user);

        Deck deck1 = TestUtils.createDeck(user);
        deckRepository.save(deck1);

        Deck deck2 = TestUtils.createDeck(user);
        deckRepository.save(deck2);

        // Act
        deckRepository.deleteAll(deckRepository.findAllByUser(user));
        List<Deck> decks = deckRepository.findAllByUser(user);

        // Assert
        assertThat(decks).isEmpty();
    }
}
