package com.flashdash.core.repository;

import com.flashdash.core.FlashDashCoreApplication;
import com.flashdash.core.TestUtils;
import com.flashdash.core.model.Deck;
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
@SpringBootTest(classes = FlashDashCoreApplication.class)
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
    void shouldFindAllDecksByUserFrn() {
        // Arrange
        User user = TestUtils.createUser();
        userRepository.save(user);

        Deck deck1 = TestUtils.createDeck(user);
        deckRepository.save(deck1);

        Deck deck2 = TestUtils.createDeck(user);
        deckRepository.save(deck2);

        // Act
        List<Deck> decks = deckRepository.findAllByUserFrn(user.getUserFrn());

        // Assert
        assertThat(decks).hasSize(2);
        assertThat(decks).extracting(Deck::getDeckFrn)
                .containsExactlyInAnyOrder(deck1.getDeckFrn(), deck2.getDeckFrn());
    }

    @Test
    void shouldFindSpecificDeckByDeckFrnAndUserFrn() {
        // Arrange
        User user = TestUtils.createUser();
        userRepository.save(user);

        Deck deck = TestUtils.createDeck(user);
        deckRepository.save(deck);

        // Act
        Optional<Deck> foundDeck = deckRepository.findByDeckFrnAndUserFrn(deck.getDeckFrn(), user.getUserFrn());

        // Assert
        assertThat(foundDeck).isPresent();
        assertThat(foundDeck.get().getDeckFrn()).isEqualTo(deck.getDeckFrn());
    }

    @Test
    void shouldReturnEmptyWhenDeckBelongsToAnotherUser() {
        // Arrange
        User user1 = TestUtils.createUser();
        userRepository.save(user1);

        User user2 = TestUtils.createUser();
        userRepository.save(user2);

        Deck deck = TestUtils.createDeck(user1);
        deckRepository.save(deck);

        // Act
        Optional<Deck> foundDeck = deckRepository.findByDeckFrnAndUserFrn(deck.getDeckFrn(), user2.getUserFrn());

        // Assert
        assertThat(foundDeck).isNotPresent();
    }
}
