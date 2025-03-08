package com.flashdash.core.service;

import com.flashdash.core.FlashDashCoreApplication;
import com.flashdash.core.TestUtils;
import com.flashdash.core.exception.FlashDashException;
import com.flashdash.core.exception.ErrorCode;
import com.flashdash.core.model.Deck;
import com.flashdash.core.model.User;
import com.flashdash.core.repository.DeckRepository;
import com.flashdash.core.repository.QuestionRepository;
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

@SpringBootTest(classes = FlashDashCoreApplication.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class DeckServiceTest {

    @Autowired
    private DeckService deckService;

    @MockitoBean
    private ActivityService activityService;

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
        Deck createdDeck = deckService.createDeck(deckRequest, user.getUserFrn());

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
        when(deckRepository.findAllByUserFrn(user.getUserFrn())).thenReturn(decks);

        // Act
        List<Deck> retrievedDecks = deckService.getAllDecks(user.getUserFrn());

        // Assert
        assertThat(retrievedDecks).isNotEmpty();
        assertThat(retrievedDecks).isEqualTo(decks);
        verify(deckRepository).findAllByUserFrn(user.getUserFrn());
    }

    @Test
    void shouldGetDeckByFrnSuccessfully() {
        // Arrange
        User user = TestUtils.createUser();
        Deck deck = TestUtils.createDeck(user);
        when(deckRepository.findByDeckFrnAndUserFrn(deck.getDeckFrn(), user.getUserFrn())).thenReturn(Optional.of(deck));

        // Act
        Deck retrievedDeck = deckService.getDeckByFrn(deck.getDeckFrn(), user.getUserFrn());

        // Assert
        assertThat(retrievedDeck).isEqualTo(deck);
        verify(deckRepository).findByDeckFrnAndUserFrn(deck.getDeckFrn(), user.getUserFrn());
    }

    @Test
    void shouldThrowExceptionWhenDeckNotFoundByFrn() {
        // Arrange
        User user = TestUtils.createUser();
        String deckFrn = "frn:flashdash:deck:nonexistent";
        when(deckRepository.findByDeckFrnAndUserFrn(deckFrn, user.getUserFrn())).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> deckService.getDeckByFrn(deckFrn, user.getUserFrn()))
                .isInstanceOf(FlashDashException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.E404002);

        verify(deckRepository).findByDeckFrnAndUserFrn(deckFrn, user.getUserFrn());
    }

    @Test
    void shouldUpdateDeckSuccessfully() {
        // Arrange
        User user = TestUtils.createUser();
        Deck deck = TestUtils.createDeck(user);
        DeckRequest deckRequest = TestUtils.createDeckRequest();
        deckRequest.setName("Updated Name");
        deckRequest.setDescription("Updated Description");

        when(deckRepository.findByDeckFrnAndUserFrn(deck.getDeckFrn(), user.getUserFrn())).thenReturn(Optional.of(deck));
        when(deckRepository.save(any(Deck.class))).thenReturn(deck);

        // Act
        Deck updatedDeck = deckService.updateDeck(deck.getDeckFrn(), deckRequest, user.getUserFrn());

        // Assert
        assertThat(updatedDeck.getName()).isEqualTo("Updated Name");
        assertThat(updatedDeck.getDescription()).isEqualTo("Updated Description");
        verify(deckRepository).findByDeckFrnAndUserFrn(deck.getDeckFrn(), user.getUserFrn());
        verify(deckRepository).save(deck);
    }

    @Test
    void shouldThrowExceptionWhenUpdatingNonExistentDeck() {
        // Arrange
        User user = TestUtils.createUser();
        DeckRequest deckRequest = TestUtils.createDeckRequest();
        String deckFrn = "frn:flashdash:deck:nonexistent";

        when(deckRepository.findByDeckFrnAndUserFrn(deckFrn, user.getUserFrn())).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> deckService.updateDeck(deckFrn, deckRequest, user.getUserFrn()))
                .isInstanceOf(FlashDashException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.E404002);

        verify(deckRepository).findByDeckFrnAndUserFrn(deckFrn, user.getUserFrn());
    }

    @Test
    void shouldDeleteDeckSuccessfully() {
        // Arrange
        User user = TestUtils.createUser();
        Deck deck = TestUtils.createDeck(user);

        when(deckRepository.findByDeckFrnAndUserFrn(deck.getDeckFrn(), user.getUserFrn())).thenReturn(Optional.of(deck));
        doNothing().when(questionRepository).deleteAllByDeckFrn(deck.getDeckFrn());
        doNothing().when(deckRepository).delete(deck);

        // Act
        deckService.deleteDeck(deck.getDeckFrn(), user.getUserFrn());

        // Assert
        verify(deckRepository).findByDeckFrnAndUserFrn(deck.getDeckFrn(), user.getUserFrn());
        verify(questionRepository).deleteAllByDeckFrn(deck.getDeckFrn());
        verify(deckRepository).delete(deck);
    }

    @Test
    void shouldThrowExceptionWhenDeletingNonExistentDeck() {
        // Arrange
        User user = TestUtils.createUser();
        String deckFrn = "frn:flashdash:deck:nonexistent";

        when(deckRepository.findByDeckFrnAndUserFrn(deckFrn, user.getUserFrn())).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> deckService.deleteDeck(deckFrn, user.getUserFrn()))
                .isInstanceOf(FlashDashException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.E404002);

        verify(deckRepository).findByDeckFrnAndUserFrn(deckFrn, user.getUserFrn());
    }

    @Test
    void shouldDeleteAllDecksForUserSuccessfully() {
        // Arrange
        User user = TestUtils.createUser();
        Deck deck1 = TestUtils.createDeck(user);
        Deck deck2 = TestUtils.createDeck(user);
        List<Deck> userDecks = List.of(deck1, deck2);

        when(deckRepository.findAllByUserFrn(user.getUserFrn())).thenReturn(userDecks);

        when(deckRepository.findByDeckFrnAndUserFrn(deck1.getDeckFrn(), user.getUserFrn())).thenReturn(Optional.of(deck1));
        when(deckRepository.findByDeckFrnAndUserFrn(deck2.getDeckFrn(), user.getUserFrn())).thenReturn(Optional.of(deck2));

        doNothing().when(questionRepository).deleteAllByDeckFrn(anyString());
        doNothing().when(deckRepository).delete(any(Deck.class));

        // Act
        deckService.deleteAllDecksForUser(user.getUserFrn());

        // Assert
        verify(deckRepository).findAllByUserFrn(user.getUserFrn());
        verify(deckRepository, times(userDecks.size())).findByDeckFrnAndUserFrn(anyString(), anyString());
        verify(deckRepository, times(userDecks.size())).delete(any(Deck.class));
        verify(questionRepository, times(userDecks.size())).deleteAllByDeckFrn(anyString());
    }


    @Test
    void shouldNotDeleteDecksWhenUserHasNone() {
        // Arrange
        User user = TestUtils.createUser();
        when(deckRepository.findAllByUserFrn(user.getUserFrn())).thenReturn(Collections.emptyList());

        // Act
        deckService.deleteAllDecksForUser(user.getUserFrn());

        // Assert
        verify(deckRepository).findAllByUserFrn(user.getUserFrn());
        verify(deckRepository, never()).delete(any(Deck.class));
        verify(questionRepository, never()).deleteAllByDeckFrn(anyString());
    }

    @Test
    void shouldNotDeleteAnythingWhenDeletingDecksForNonExistentUser() {
        // Arrange
        User user = TestUtils.createUser();

        when(deckRepository.findAllByUserFrn(user.getUserFrn())).thenReturn(Collections.emptyList());

        // Act
        deckService.deleteAllDecksForUser(user.getUserFrn());

        // Assert
        verify(deckRepository).findAllByUserFrn(user.getUserFrn());
        verify(deckRepository, never()).delete(any(Deck.class));
        verify(questionRepository, never()).deleteAllByDeckFrn(anyString());
    }
}
