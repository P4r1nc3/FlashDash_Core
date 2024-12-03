package com.flashdash.service;

import com.flashdash.FlashDashApplication;
import com.flashdash.TestUtils;
import com.flashdash.exception.FlashDashException;
import com.flashdash.exception.ErrorCode;
import com.flashdash.model.Deck;
import com.flashdash.model.User;
import com.flashdash.model.card.CardDeck;
import com.flashdash.model.question.QuestionDeck;
import com.flashdash.repository.CardRepository;
import com.flashdash.repository.DeckRepository;
import com.flashdash.repository.QuestionRepository;
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
class DeckServiceTest {

    @Autowired
    private DeckService deckService;

    @MockBean
    private DeckRepository deckRepository;

    @MockBean
    private CardRepository cardRepository;

    @MockBean
    private QuestionRepository questionRepository;

    @Test
    void shouldCreateDeckSuccessfully() {
        // Arrange
        User user = TestUtils.createUser();
        CardDeck cardDeck = TestUtils.createCardDeck(user);
        when(deckRepository.save(any(Deck.class))).thenReturn(cardDeck);

        // Act
        Deck createdDeck = deckService.createDeck(cardDeck, user);

        // Assert
        assertThat(createdDeck).isNotNull();
        assertThat(createdDeck).isEqualTo(cardDeck);
        verify(deckRepository).save(cardDeck);
    }

    @Test
    void shouldGetAllDecksSuccessfully() {
        // Arrange
        User user = TestUtils.createUser();
        List<Deck> decks = List.of(TestUtils.createCardDeck(user), TestUtils.createQuestionDeck(user));
        when(deckRepository.findAllByUser(user)).thenReturn(decks);

        // Act
        List<Deck> retrievedDecks = deckService.getAllDecks(user);

        // Assert
        assertThat(retrievedDecks).isNotEmpty();
        assertThat(retrievedDecks).isEqualTo(decks);
        verify(deckRepository).findAllByUser(user);
    }

    @Test
    void shouldGetDeckByIdSuccessfully() {
        // Arrange
        User user = TestUtils.createUser();
        CardDeck cardDeck = TestUtils.createCardDeck(user);
        when(deckRepository.findByIdAndUser(1L, user)).thenReturn(Optional.of(cardDeck));

        // Act
        Deck retrievedDeck = deckService.getDeckById(1L, user);

        // Assert
        assertThat(retrievedDeck).isEqualTo(cardDeck);
        verify(deckRepository).findByIdAndUser(1L, user);
    }

    @Test
    void shouldThrowExceptionWhenDeckNotFoundById() {
        // Arrange
        User user = TestUtils.createUser();
        when(deckRepository.findByIdAndUser(1L, user)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> deckService.getDeckById(1L, user))
                .isInstanceOf(FlashDashException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.E404002);

        verify(deckRepository).findByIdAndUser(1L, user);
    }

    @Test
    void shouldUpdateDeckSuccessfully() {
        // Arrange
        User user = TestUtils.createUser();
        CardDeck cardDeck = TestUtils.createCardDeck(user);
        CardDeck updatedDetails = TestUtils.createCardDeck(user);
        updatedDetails.setName("Updated Name");
        updatedDetails.setDescription("Updated Description");

        when(deckRepository.findByIdAndUser(1L, user)).thenReturn(Optional.of(cardDeck));
        when(deckRepository.save(any(Deck.class))).thenReturn(cardDeck);

        // Act
        Deck updatedDeck = deckService.updateDeck(1L, updatedDetails, user);

        // Assert
        assertThat(updatedDeck.getName()).isEqualTo("Updated Name");
        assertThat(updatedDeck.getDescription()).isEqualTo("Updated Description");
        verify(deckRepository).findByIdAndUser(1L, user);
        verify(deckRepository).save(cardDeck);
    }

    @Test
    void shouldThrowExceptionWhenUpdatingNonExistentDeck() {
        // Arrange
        User user = TestUtils.createUser();
        CardDeck updatedDetails = TestUtils.createCardDeck(user);
        when(deckRepository.findByIdAndUser(1L, user)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> deckService.updateDeck(1L, updatedDetails, user))
                .isInstanceOf(FlashDashException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.E404002);

        verify(deckRepository).findByIdAndUser(1L, user);
    }

    @Test
    void shouldThrowExceptionWhenChangingDeckTypeDuringUpdate() {
        // Arrange
        User user = TestUtils.createUser();
        CardDeck cardDeck = TestUtils.createCardDeck(user);
        QuestionDeck questionDeck = TestUtils.createQuestionDeck(user);

        when(deckRepository.findByIdAndUser(1L, user)).thenReturn(Optional.of(cardDeck));

        // Act & Assert
        assertThatThrownBy(() -> deckService.updateDeck(1L, questionDeck, user))
                .isInstanceOf(FlashDashException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.E400001);

        verify(deckRepository).findByIdAndUser(1L, user);
    }

    @Test
    void shouldDeleteDeckSuccessfully() {
        // Arrange
        User user = TestUtils.createUser();
        CardDeck cardDeck = TestUtils.createCardDeck(user);

        when(deckRepository.findByIdAndUser(1L, user)).thenReturn(Optional.of(cardDeck));
        doNothing().when(cardRepository).deleteAllByDeck(cardDeck);
        doNothing().when(deckRepository).delete(cardDeck);

        // Act
        deckService.deleteDeck(1L, user);

        // Assert
        verify(deckRepository).findByIdAndUser(1L, user);
        verify(cardRepository).deleteAllByDeck(cardDeck);
        verify(deckRepository).delete(cardDeck);
    }

    @Test
    void shouldThrowExceptionWhenDeletingNonExistentDeck() {
        // Arrange
        User user = TestUtils.createUser();
        when(deckRepository.findByIdAndUser(1L, user)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> deckService.deleteDeck(1L, user))
                .isInstanceOf(FlashDashException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.E404002);

        verify(deckRepository).findByIdAndUser(1L, user);
    }
}
