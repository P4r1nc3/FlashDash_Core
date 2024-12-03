package com.flashdash.controller;

import com.flashdash.FlashDashApplication;
import com.flashdash.TestUtils;
import com.flashdash.exception.ErrorCode;
import com.flashdash.exception.FlashDashException;
import com.flashdash.model.Deck;
import com.flashdash.model.User;
import com.flashdash.model.card.CardDeck;
import com.flashdash.service.DeckService;
import org.junit.jupiter.api.*;
import org.mockito.MockedStatic;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@SpringBootTest(classes = FlashDashApplication.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class DeckControllerTest {

    @Autowired
    private DeckController deckController;

    @MockBean
    private DeckService deckService;

    private User user;
    private MockedStatic<SecurityContextHolder> mockedSecurityContextHolder;

    @BeforeEach
    void setUp() {
        user = TestUtils.createUser();

        Authentication authentication = mock(Authentication.class);
        when(authentication.getPrincipal()).thenReturn(user);

        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);

        mockedSecurityContextHolder = mockStatic(SecurityContextHolder.class);
        mockedSecurityContextHolder.when(SecurityContextHolder::getContext).thenReturn(securityContext);
    }

    @AfterEach
    void tearDown() {
        mockedSecurityContextHolder.close();
    }

    @Test
    @Order(1)
    void testCreateDeckSuccessful() {
        // Arrange
        CardDeck cardDeck = TestUtils.createCardDeck(user);
        when(deckService.createDeck(any(Deck.class), eq(user))).thenReturn(cardDeck);

        // Act
        ResponseEntity<Deck> responseEntity = deckController.createDeck(cardDeck);

        // Assert
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertEquals(cardDeck, responseEntity.getBody());
    }

    @Test
    @Order(2)
    void testGetAllDecksSuccessful() {
        // Arrange
        CardDeck cardDeck = TestUtils.createCardDeck(user);
        List<Deck> decks = List.of(cardDeck);
        when(deckService.getAllDecks(eq(user))).thenReturn(decks);

        // Act
        ResponseEntity<List<Deck>> responseEntity = deckController.getAllDecks();

        // Assert
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertEquals(decks, responseEntity.getBody());
    }

    @Test
    @Order(3)
    void testGetDeckByIdSuccessful() {
        // Arrange
        CardDeck cardDeck = TestUtils.createCardDeck(user);
        when(deckService.getDeckById(eq(1L), eq(user))).thenReturn(cardDeck);

        // Act
        ResponseEntity<Deck> responseEntity = deckController.getDeck(1L);

        // Assert
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertEquals(cardDeck, responseEntity.getBody());
    }

    @Test
    @Order(4)
    void testGetDeckByIdNotFound() {
        // Arrange
        doThrow(new FlashDashException(ErrorCode.E404001, "Deck not found"))
                .when(deckService).getDeckById(eq(1L), eq(user));

        // Act & Assert
        FlashDashException exception = assertThrows(
                FlashDashException.class,
                () -> deckController.getDeck(1L)
        );
        assertEquals(ErrorCode.E404001, exception.getErrorCode());
        assertEquals("Deck not found", exception.getMessage());
    }

    @Test
    @Order(5)
    void testUpdateDeckSuccessful() {
        // Arrange
        CardDeck cardDeck = TestUtils.createCardDeck(user);
        cardDeck.setName("Updated Name");
        when(deckService.updateDeck(eq(1L), any(Deck.class), eq(user))).thenReturn(cardDeck);

        // Act
        ResponseEntity<Deck> responseEntity = deckController.updateDeck(1L, cardDeck);

        // Assert
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertEquals("Updated Name", responseEntity.getBody().getName());
    }

    @Test
    @Order(6)
    void testUpdateDeckNotFound() {
        // Arrange
        CardDeck cardDeck = TestUtils.createCardDeck(user);
        doThrow(new FlashDashException(ErrorCode.E404001, "Deck not found"))
                .when(deckService).updateDeck(eq(1L), any(Deck.class), eq(user));

        // Act & Assert
        FlashDashException exception = assertThrows(
                FlashDashException.class,
                () -> deckController.updateDeck(1L, cardDeck)
        );
        assertEquals(ErrorCode.E404001, exception.getErrorCode());
        assertEquals("Deck not found", exception.getMessage());
    }

    @Test
    @Order(7)
    void testDeleteDeckSuccessful() {
        // Arrange
        doNothing().when(deckService).deleteDeck(eq(1L), eq(user));

        // Act
        ResponseEntity<Void> responseEntity = deckController.deleteDeck(1L);

        // Assert
        assertEquals(HttpStatus.NO_CONTENT, responseEntity.getStatusCode());
        verify(deckService, times(1)).deleteDeck(eq(1L), eq(user));
    }

    @Test
    @Order(8)
    void testDeleteDeckNotFound() {
        // Arrange
        doThrow(new FlashDashException(ErrorCode.E404001, "Deck not found"))
                .when(deckService).deleteDeck(eq(1L), eq(user));

        // Act & Assert
        FlashDashException exception = assertThrows(
                FlashDashException.class,
                () -> deckController.deleteDeck(1L)
        );
        assertEquals(ErrorCode.E404001, exception.getErrorCode());
        assertEquals("Deck not found", exception.getMessage());
    }
}
