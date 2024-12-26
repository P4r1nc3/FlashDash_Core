package com.flashdash.service;

import com.flashdash.FlashDashApplication;
import com.flashdash.TestUtils;
import com.flashdash.exception.FlashDashException;
import com.flashdash.exception.ErrorCode;
import com.flashdash.model.Deck;
import com.flashdash.model.User;
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
    private QuestionRepository questionRepository;

    @Test
    void shouldCreateDeckSuccessfully() {
        // Arrange
        User user = TestUtils.createUser();
        Deck deck = TestUtils.createDeck(user);
        when(deckRepository.save(any(Deck.class))).thenReturn(deck);

        // Act
        Deck createdDeck = deckService.createDeck(deck, user);

        // Assert
        assertThat(createdDeck).isNotNull();
        assertThat(createdDeck).isEqualTo(deck);
        verify(deckRepository).save(deck);
    }

    @Test
    void shouldGetAllDecksSuccessfully() {
        // Arrange
        User user = TestUtils.createUser();
        List<Deck> decks = List.of(TestUtils.createDeck(user));
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
        Deck deck = TestUtils.createDeck(user);
        when(deckRepository.findByIdAndUser(1L, user)).thenReturn(Optional.of(deck));

        // Act
        Deck retrievedDeck = deckService.getDeckById(1L, user);

        // Assert
        assertThat(retrievedDeck).isEqualTo(deck);
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
        Deck deck = TestUtils.createDeck(user);
        Deck updatedDetails = TestUtils.createDeck(user);
        updatedDetails.setName("Updated Name");
        updatedDetails.setDescription("Updated Description");

        when(deckRepository.findByIdAndUser(1L, user)).thenReturn(Optional.of(deck));
        when(deckRepository.save(any(Deck.class))).thenReturn(deck);

        // Act
        Deck updatedDeck = deckService.updateDeck(1L, updatedDetails, user);

        // Assert
        assertThat(updatedDeck.getName()).isEqualTo("Updated Name");
        assertThat(updatedDeck.getDescription()).isEqualTo("Updated Description");
        verify(deckRepository).findByIdAndUser(1L, user);
        verify(deckRepository).save(deck);
    }

    @Test
    void shouldThrowExceptionWhenUpdatingNonExistentDeck() {
        // Arrange
        User user = TestUtils.createUser();
        Deck updatedDetails = TestUtils.createDeck(user);
        when(deckRepository.findByIdAndUser(1L, user)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> deckService.updateDeck(1L, updatedDetails, user))
                .isInstanceOf(FlashDashException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.E404002);

        verify(deckRepository).findByIdAndUser(1L, user);
    }

    @Test
    void shouldDeleteDeckSuccessfully() {
        // Arrange
        User user = TestUtils.createUser();
        Deck deck = TestUtils.createDeck(user);

        when(deckRepository.findByIdAndUser(1L, user)).thenReturn(Optional.of(deck));
        doNothing().when(questionRepository).deleteAllByDeck(deck);
        doNothing().when(deckRepository).delete(deck);

        // Act
        deckService.deleteDeck(1L, user);

        // Assert
        verify(deckRepository).findByIdAndUser(1L, user);
        verify(questionRepository).deleteAllByDeck(deck);
        verify(deckRepository).delete(deck);
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
