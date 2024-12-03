package com.flashdash.repository;

import com.flashdash.FlashDashApplication;
import com.flashdash.TestUtils;
import com.flashdash.model.Deck;
import com.flashdash.model.User;
import com.flashdash.model.card.CardDeck;
import com.flashdash.model.question.QuestionDeck;
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

        CardDeck cardDeck = TestUtils.createCardDeck(user);
        deckRepository.save(cardDeck);

        QuestionDeck questionDeck = TestUtils.createQuestionDeck(user);
        deckRepository.save(questionDeck);

        // Act
        List<Deck> decks = deckRepository.findAllByUser(user);

        // Assert
        assertThat(decks).hasSize(2);
        assertThat(decks).extracting(Deck::getName)
                .containsExactlyInAnyOrder(cardDeck.getName(), questionDeck.getName());
    }

    @Test
    void shouldFindSpecificDeckByIdAndUser() {
        // Arrange
        User user = TestUtils.createUser();
        userRepository.save(user);

        QuestionDeck questionDeck = TestUtils.createQuestionDeck(user);
        deckRepository.save(questionDeck);

        // Act
        Optional<Deck> foundDeck = deckRepository.findByIdAndUser(questionDeck.getId(), user);

        // Assert
        assertThat(foundDeck).isPresent();
        assertThat(foundDeck.get().getName()).isEqualTo(questionDeck.getName());
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

        CardDeck cardDeck = TestUtils.createCardDeck(user1);
        deckRepository.save(cardDeck);

        // Act
        Optional<Deck> foundDeck = deckRepository.findByIdAndUser(cardDeck.getId(), user2);

        // Assert
        assertThat(foundDeck).isNotPresent();
    }

    @Test
    void shouldDeleteAllDecksByUser() {
        // Arrange
        User user = TestUtils.createUser();
        userRepository.save(user);

        CardDeck cardDeck = TestUtils.createCardDeck(user);
        deckRepository.save(cardDeck);

        QuestionDeck questionDeck = TestUtils.createQuestionDeck(user);
        deckRepository.save(questionDeck);

        // Act
        deckRepository.deleteAll(deckRepository.findAllByUser(user));
        List<Deck> decks = deckRepository.findAllByUser(user);

        // Assert
        assertThat(decks).isEmpty();
    }
}
