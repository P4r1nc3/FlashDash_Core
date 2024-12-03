package com.flashdash.repository;

import com.flashdash.FlashDashApplication;
import com.flashdash.TestUtils;
import com.flashdash.model.User;
import com.flashdash.model.card.Card;
import com.flashdash.model.card.CardDeck;
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
class CardRepositoryTest {

    @Autowired
    private CardRepository cardRepository;

    @Autowired
    private DeckRepository deckRepository;

    @Autowired
    private UserRepository userRepository;

    @BeforeEach
    void setUp() {
        cardRepository.deleteAll();
        deckRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    void shouldFindAllCardsByDeck() {
        // Arrange
        User user = TestUtils.createUser();
        userRepository.save(user);

        CardDeck deck = TestUtils.createCardDeck(user);
        deckRepository.save(deck);

        Card card1 = TestUtils.createCard(deck, "What is Java?", "A programming language.");
        Card card2 = TestUtils.createCard(deck, "What is Spring?", "A Java framework.");
        cardRepository.save(card1);
        cardRepository.save(card2);

        // Act
        List<Card> cards = cardRepository.findAllByDeck(deck);

        // Assert
        assertThat(cards).hasSize(2);
        assertThat(cards).extracting(Card::getQuestion)
                .containsExactlyInAnyOrder("What is Java?", "What is Spring?");
    }

    @Test
    void shouldFindSpecificCardByDeckAndId() {
        // Arrange
        User user = TestUtils.createUser();
        userRepository.save(user);

        CardDeck deck = TestUtils.createCardDeck(user);
        deckRepository.save(deck);

        Card card = TestUtils.createCard(deck, "What is Java?", "A programming language.");
        card = cardRepository.save(card);

        // Act
        Optional<Card> foundCard = cardRepository.findByDeckAndCardId(deck, card.getCardId());

        // Assert
        assertThat(foundCard).isPresent();
        assertThat(foundCard.get().getQuestion()).isEqualTo("What is Java?");
    }

    @Test
    void shouldReturnEmptyWhenCardNotInDeck() {
        // Arrange
        User user = TestUtils.createUser();
        userRepository.save(user);

        CardDeck deck = TestUtils.createCardDeck(user);
        deckRepository.save(deck);

        // Act
        Optional<Card> foundCard = cardRepository.findByDeckAndCardId(deck, 999L);

        // Assert
        assertThat(foundCard).isNotPresent();
    }

    @Test
    void shouldDeleteAllCardsByDeck() {
        // Arrange
        User user = TestUtils.createUser();
        userRepository.save(user);

        CardDeck deck = TestUtils.createCardDeck(user);
        deckRepository.save(deck);

        Card card1 = TestUtils.createCard(deck, "What is Java?", "A programming language.");
        Card card2 = TestUtils.createCard(deck, "What is Spring?", "A Java framework.");
        cardRepository.save(card1);
        cardRepository.save(card2);

        // Act
        cardRepository.deleteAllByDeck(deck);
        List<Card> cards = cardRepository.findAllByDeck(deck);

        // Assert
        assertThat(cards).isEmpty();
    }
}
