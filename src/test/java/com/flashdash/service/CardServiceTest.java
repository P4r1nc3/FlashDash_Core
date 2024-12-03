package com.flashdash.service;

import com.flashdash.FlashDashApplication;
import com.flashdash.TestUtils;
import com.flashdash.exception.FlashDashException;
import com.flashdash.exception.ErrorCode;
import com.flashdash.model.User;
import com.flashdash.model.card.Card;
import com.flashdash.model.card.CardDeck;
import com.flashdash.model.Deck;
import com.flashdash.repository.CardRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@SpringBootTest(classes = FlashDashApplication.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class CardServiceTest {

    @Autowired
    private CardService cardService;

    @MockBean
    private CardRepository cardRepository;

    @MockBean
    private DeckService deckService;

    @Test
    void shouldAddCardToDeckSuccessfully() {
        // Arrange
        User user = TestUtils.createUser();
        CardDeck cardDeck = TestUtils.createCardDeck(user);
        Card card = TestUtils.createCard(cardDeck, "What is Java?", "A programming language");

        when(deckService.getDeckById(1L, user)).thenReturn(cardDeck);
        when(cardRepository.save(card)).thenReturn(card);

        // Act
        Card createdCard = cardService.addCardToDeck(1L, card, user);

        // Assert
        assertThat(createdCard).isNotNull();
        assertThat(createdCard).isEqualTo(card);
        verify(deckService).getDeckById(1L, user);
        verify(cardRepository).save(card);
    }

    @Test
    void shouldThrowExceptionWhenAddingCardToNonCardDeck() {
        // Arrange
        User user = TestUtils.createUser();
        Deck nonCardDeck = TestUtils.createQuestionDeck(user);
        Card card = TestUtils.createCard(null, "What is Java?", "A programming language");

        when(deckService.getDeckById(1L, user)).thenReturn(nonCardDeck);

        // Act & Assert
        assertThatThrownBy(() -> cardService.addCardToDeck(1L, card, user))
                .isInstanceOf(FlashDashException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.E400002);

        verify(deckService).getDeckById(1L, user);
        verifyNoInteractions(cardRepository);
    }

    @Test
    void shouldGetAllCardsInDeckSuccessfully() {
        // Arrange
        User user = TestUtils.createUser();
        CardDeck cardDeck = TestUtils.createCardDeck(user);
        List<Card> cards = List.of(
                TestUtils.createCard(cardDeck, "What is Java?", "A programming language"),
                TestUtils.createCard(cardDeck, "What is Spring?", "A framework")
        );

        when(deckService.getDeckById(1L, user)).thenReturn(cardDeck);
        when(cardRepository.findAllByDeck(cardDeck)).thenReturn(cards);

        // Act
        List<Card> retrievedCards = cardService.getAllCardsInDeck(1L, user);

        // Assert
        assertThat(retrievedCards).isNotEmpty();
        assertThat(retrievedCards).isEqualTo(cards);
        verify(deckService).getDeckById(1L, user);
        verify(cardRepository).findAllByDeck(cardDeck);
    }

    @Test
    void shouldGetCardByIdSuccessfully() {
        // Arrange
        User user = TestUtils.createUser();
        CardDeck cardDeck = TestUtils.createCardDeck(user);
        Card card = TestUtils.createCard(cardDeck, "What is Java?", "A programming language");

        when(deckService.getDeckById(1L, user)).thenReturn(cardDeck);
        when(cardRepository.findByDeckAndCardId(cardDeck, 1L)).thenReturn(Optional.of(card));

        // Act
        Card retrievedCard = cardService.getCardById(1L, 1L, user);

        // Assert
        assertThat(retrievedCard).isEqualTo(card);
        verify(deckService).getDeckById(1L, user);
        verify(cardRepository).findByDeckAndCardId(cardDeck, 1L);
    }

    @Test
    void shouldThrowExceptionWhenCardNotFoundById() {
        // Arrange
        User user = TestUtils.createUser();
        CardDeck cardDeck = TestUtils.createCardDeck(user);

        when(deckService.getDeckById(1L, user)).thenReturn(cardDeck);
        when(cardRepository.findByDeckAndCardId(cardDeck, 1L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> cardService.getCardById(1L, 1L, user))
                .isInstanceOf(FlashDashException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.E404003);

        verify(deckService).getDeckById(1L, user);
        verify(cardRepository).findByDeckAndCardId(cardDeck, 1L);
    }

    @Test
    void shouldUpdateCardSuccessfully() {
        // Arrange
        User user = TestUtils.createUser();
        CardDeck cardDeck = TestUtils.createCardDeck(user);
        Card card = TestUtils.createCard(cardDeck, "What is Java?", "A programming language");
        Card updatedDetails = TestUtils.createCard(cardDeck, "Updated Question", "Updated Answer");

        when(deckService.getDeckById(1L, user)).thenReturn(cardDeck);
        when(cardRepository.findByDeckAndCardId(cardDeck, 1L)).thenReturn(Optional.of(card));
        when(cardRepository.save(card)).thenReturn(card);

        // Act
        Card updatedCard = cardService.updateCard(1L, 1L, updatedDetails, user);

        // Assert
        assertThat(updatedCard.getQuestion()).isEqualTo("Updated Question");
        assertThat(updatedCard.getAnswer()).isEqualTo("Updated Answer");
        verify(deckService).getDeckById(1L, user);
        verify(cardRepository).findByDeckAndCardId(cardDeck, 1L);
        verify(cardRepository).save(card);
    }

    @Test
    void shouldThrowExceptionWhenUpdatingNonExistentCard() {
        // Arrange
        User user = TestUtils.createUser();
        CardDeck cardDeck = TestUtils.createCardDeck(user);
        Card updatedDetails = TestUtils.createCard(cardDeck, "Updated Question", "Updated Answer");

        when(deckService.getDeckById(1L, user)).thenReturn(cardDeck);
        when(cardRepository.findByDeckAndCardId(cardDeck, 1L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> cardService.updateCard(1L, 1L, updatedDetails, user))
                .isInstanceOf(FlashDashException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.E404003);

        verify(deckService).getDeckById(1L, user);
        verify(cardRepository).findByDeckAndCardId(cardDeck, 1L);
    }

    @Test
    void shouldDeleteCardSuccessfully() {
        // Arrange
        User user = TestUtils.createUser();
        CardDeck cardDeck = TestUtils.createCardDeck(user);
        Card card = TestUtils.createCard(cardDeck, "What is Java?", "A programming language");

        when(deckService.getDeckById(1L, user)).thenReturn(cardDeck);
        when(cardRepository.findByDeckAndCardId(cardDeck, 1L)).thenReturn(Optional.of(card));
        doNothing().when(cardRepository).delete(card);

        // Act
        cardService.deleteCard(1L, 1L, user);

        // Assert
        verify(deckService).getDeckById(1L, user);
        verify(cardRepository).findByDeckAndCardId(cardDeck, 1L);
        verify(cardRepository).delete(card);
    }

    @Test
    void shouldThrowExceptionWhenDeletingNonExistentCard() {
        // Arrange
        User user = TestUtils.createUser();
        CardDeck cardDeck = TestUtils.createCardDeck(user);

        when(deckService.getDeckById(1L, user)).thenReturn(cardDeck);
        when(cardRepository.findByDeckAndCardId(cardDeck, 1L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> cardService.deleteCard(1L, 1L, user))
                .isInstanceOf(FlashDashException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.E404003);

        verify(deckService).getDeckById(1L, user);
        verify(cardRepository).findByDeckAndCardId(cardDeck, 1L);
    }
}
