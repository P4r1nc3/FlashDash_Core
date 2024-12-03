package com.flashdash.controller;

import com.flashdash.FlashDashApplication;
import com.flashdash.TestUtils;
import com.flashdash.exception.ErrorCode;
import com.flashdash.exception.FlashDashException;
import com.flashdash.model.User;
import com.flashdash.model.card.Card;
import com.flashdash.model.card.CardDeck;
import com.flashdash.service.CardService;
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
class CardControllerTest {

    @Autowired
    private CardController cardController;

    @MockBean
    private CardService cardService;

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
    void testAddCardToDeckSuccessful() {
        // Arrange
        CardDeck cardDeck = TestUtils.createCardDeck(user);
        Card card = TestUtils.createCard(cardDeck, "Question", "Answer");
        when(cardService.addCardToDeck(eq(1L), any(Card.class), eq(user))).thenReturn(card);

        // Act
        ResponseEntity<Card> responseEntity = cardController.addCardToDeck(1L, card);

        // Assert
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertEquals(card, responseEntity.getBody());
    }

    @Test
    @Order(2)
    void testGetAllCardsInDeckSuccessful() {
        // Arrange
        CardDeck cardDeck = TestUtils.createCardDeck(user);
        List<Card> cards = List.of(
                TestUtils.createCard(cardDeck, "Question1", "Answer1"),
                TestUtils.createCard(cardDeck, "Question2", "Answer2")
        );
        when(cardService.getAllCardsInDeck(eq(1L), eq(user))).thenReturn(cards);

        // Act
        ResponseEntity<List<Card>> responseEntity = cardController.getAllCardsInDeck(1L);

        // Assert
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertEquals(cards, responseEntity.getBody());
    }

    @Test
    @Order(3)
    void testGetCardSuccessful() {
        // Arrange
        CardDeck cardDeck = TestUtils.createCardDeck(user);
        Card card = TestUtils.createCard(cardDeck, "Question", "Answer");
        when(cardService.getCardById(eq(1L), eq(1L), eq(user))).thenReturn(card);

        // Act
        ResponseEntity<Card> responseEntity = cardController.getCard(1L, 1L);

        // Assert
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertEquals(card, responseEntity.getBody());
    }

    @Test
    @Order(4)
    void testGetCardNotFound() {
        // Arrange
        doThrow(new FlashDashException(ErrorCode.E404003, "Card not found"))
                .when(cardService).getCardById(eq(1L), eq(1L), eq(user));

        // Act & Assert
        FlashDashException exception = assertThrows(
                FlashDashException.class,
                () -> cardController.getCard(1L, 1L)
        );
        assertEquals(ErrorCode.E404003, exception.getErrorCode());
        assertEquals("Card not found", exception.getMessage());
    }

    @Test
    @Order(5)
    void testUpdateCardSuccessful() {
        // Arrange
        CardDeck cardDeck = TestUtils.createCardDeck(user);
        Card updatedCard = TestUtils.createCard(cardDeck, "Updated Question", "Updated Answer");
        when(cardService.updateCard(eq(1L), eq(1L), any(Card.class), eq(user))).thenReturn(updatedCard);

        // Act
        ResponseEntity<Card> responseEntity = cardController.updateCard(1L, 1L, updatedCard);

        // Assert
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertEquals(updatedCard, responseEntity.getBody());
    }

    @Test
    @Order(6)
    void testUpdateCardNotFound() {
        // Arrange
        CardDeck cardDeck = TestUtils.createCardDeck(user);
        Card updatedCard = TestUtils.createCard(cardDeck, "Updated Question", "Updated Answer");
        doThrow(new FlashDashException(ErrorCode.E404003, "Card not found"))
                .when(cardService).updateCard(eq(1L), eq(1L), any(Card.class), eq(user));

        // Act & Assert
        FlashDashException exception = assertThrows(
                FlashDashException.class,
                () -> cardController.updateCard(1L, 1L, updatedCard)
        );
        assertEquals(ErrorCode.E404003, exception.getErrorCode());
        assertEquals("Card not found", exception.getMessage());
    }

    @Test
    @Order(7)
    void testDeleteCardSuccessful() {
        // Arrange
        doNothing().when(cardService).deleteCard(eq(1L), eq(1L), eq(user));

        // Act
        ResponseEntity<Void> responseEntity = cardController.deleteCard(1L, 1L);

        // Assert
        assertEquals(HttpStatus.NO_CONTENT, responseEntity.getStatusCode());
        verify(cardService, times(1)).deleteCard(eq(1L), eq(1L), eq(user));
    }

    @Test
    @Order(8)
    void testDeleteCardNotFound() {
        // Arrange
        doThrow(new FlashDashException(ErrorCode.E404003, "Card not found"))
                .when(cardService).deleteCard(eq(1L), eq(1L), eq(user));

        // Act & Assert
        FlashDashException exception = assertThrows(
                FlashDashException.class,
                () -> cardController.deleteCard(1L, 1L)
        );
        assertEquals(ErrorCode.E404003, exception.getErrorCode());
        assertEquals("Card not found", exception.getMessage());
    }
}
