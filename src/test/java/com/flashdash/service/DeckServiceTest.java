package com.flashdash.service;

import com.flashdash.FlashDashApplication;
import com.flashdash.TestUtils;
import com.flashdash.exception.FlashDashException;
import com.flashdash.exception.ErrorCode;
import com.flashdash.model.Deck;
import com.flashdash.model.User;
import com.flashdash.repository.DeckRepository;
import com.flashdash.repository.QuestionRepository;
import com.p4r1nc3.flashdash.core.model.DeckRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.Collections;
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

    @MockitoBean
    private DeckRepository deckRepository;

    @MockitoBean
    private QuestionRepository questionRepository;

    @Test
    void shouldCreateDeckSuccessfully() {
        // Arrange
        User user = TestUtils.createUser();
        DeckRequest deckRequest = TestUtils.createDeckRequest();
        Deck deck = TestUtils.createDeck(user);

        when(deckRepository.save(any(Deck.class))).thenReturn(deck);

        // Act
        Deck createdDeck = deckService.createDeck(deckRequest, user);

        // Assert
        assertThat(createdDeck).isNotNull();
        assertThat(createdDeck.getName()).isEqualTo(deckRequest.getName());
        assertThat(createdDeck.getDescription()).isEqualTo(deckRequest.getDescription());
        verify(deckRepository).save(any(Deck.class));
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
        DeckRequest deckRequest = TestUtils.createDeckRequest();
        deckRequest.setName("Updated Name");
        deckRequest.setDescription("Updated Description");

        when(deckRepository.findByIdAndUser(1L, user)).thenReturn(Optional.of(deck));
        when(deckRepository.save(any(Deck.class))).thenReturn(deck);

        // Act
        Deck updatedDeck = deckService.updateDeck(1L, deckRequest, user);

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
        DeckRequest deckRequest = TestUtils.createDeckRequest();
        when(deckRepository.findByIdAndUser(1L, user)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> deckService.updateDeck(1L, deckRequest, user))
                .isInstanceOf(FlashDashException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.E404002);

        verify(deckRepository).findByIdAndUser(1L, user);
    }

    @Test
    void shouldSoftDeleteDeckSuccessfully() {
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

    @Test
    void shouldDeleteAllDecksForUserSuccessfully() {
        // Arrange
        User user = TestUtils.createUser();
        Deck deck1 = TestUtils.createDeck(user);
        deck1.setId(1L);
        Deck deck2 = TestUtils.createDeck(user);
        deck2.setId(2L);
        List<Deck> userDecks = List.of(deck1, deck2);

        when(deckRepository.findAllByUser(user)).thenReturn(userDecks);
        when(deckRepository.findByIdAndUser(1L, user)).thenReturn(Optional.of(deck1));
        when(deckRepository.findByIdAndUser(2L, user)).thenReturn(Optional.of(deck2));

        doNothing().when(questionRepository).deleteAllByDeck(any(Deck.class));
        doNothing().when(deckRepository).delete(any(Deck.class));

        // Act
        deckService.deleteAllDecksForUser(user);

        // Assert
        verify(deckRepository).findAllByUser(user);
        verify(deckRepository, times(userDecks.size())).delete(any(Deck.class));
        verify(questionRepository, times(userDecks.size())).deleteAllByDeck(any(Deck.class));
    }

    @Test
    void shouldNotDeleteDecksWhenUserHasNone() {
        // Arrange
        User user = TestUtils.createUser();
        when(deckRepository.findAllByUser(user)).thenReturn(List.of());

        // Act
        deckService.deleteAllDecksForUser(user);

        // Assert
        verify(deckRepository).findAllByUser(user);
        verify(deckRepository, never()).delete(any(Deck.class));
        verify(questionRepository, never()).deleteAllByDeck(any(Deck.class));
    }

    @Test
    void shouldNotDeleteAnythingWhenDeletingDecksForNonExistentUser() {
        // Arrange
        User user = TestUtils.createUser();

        when(deckRepository.findAllByUser(user)).thenReturn(Collections.emptyList());

        // Act & Assert
        deckService.deleteAllDecksForUser(user);

        verify(deckRepository).findAllByUser(user);
        verify(deckRepository, never()).delete(any(Deck.class));
        verify(questionRepository, never()).deleteAllByDeck(any(Deck.class));
    }
}
