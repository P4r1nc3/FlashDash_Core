package com.flashdash.controller;

import com.flashdash.FlashDashApplication;
import com.flashdash.TestUtils;
import com.flashdash.exception.ErrorCode;
import com.flashdash.exception.FlashDashException;
import com.flashdash.model.Deck;
import com.flashdash.model.User;
import com.flashdash.service.DeckService;
import com.flashdash.utils.EntityToResponseMapper;
import com.p4r1nc3.flashdash.core.model.DeckRequest;
import com.p4r1nc3.flashdash.core.model.DeckResponse;
import org.junit.jupiter.api.*;
import org.mockito.MockedStatic;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@SpringBootTest(classes = FlashDashApplication.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class DeckControllerTest {

    @Autowired
    private DeckController deckController;

    @MockitoBean
    private DeckService deckService;

    private User user;
    private String deckFrn;
    private MockedStatic<SecurityContextHolder> mockedSecurityContextHolder;

    @BeforeEach
    void setUp() {
        user = TestUtils.createUser();
        deckFrn = TestUtils.createDeck(user).getDeckFrn();

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
    void testCreateDeckSuccessful() {
        // Arrange
        DeckRequest deckRequest = TestUtils.createDeckRequest();
        Deck deck = TestUtils.createDeck(user);
        when(deckService.createDeck(any(DeckRequest.class), eq(user.getUserFrn()))).thenReturn(deck);

        // Act
        ResponseEntity<DeckResponse> responseEntity = deckController.createDeck(deckRequest);

        // Assert
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertEquals(EntityToResponseMapper.toDeckResponse(deck), responseEntity.getBody());
    }

    @Test
    void testGetAllDecksSuccessful() {
        // Arrange
        Deck deck = TestUtils.createDeck(user);
        List<Deck> decks = List.of(deck);
        List<DeckResponse> expectedResponses = EntityToResponseMapper.toDeckResponseList(decks);

        when(deckService.getAllDecks(eq(user.getUserFrn()))).thenReturn(decks);

        // Act
        ResponseEntity<List<DeckResponse>> responseEntity = deckController.getAllDecks();

        // Assert
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertEquals(expectedResponses, responseEntity.getBody());
    }

    @Test
    void testGetDeckByFrnSuccessful() {
        // Arrange
        Deck deck = TestUtils.createDeck(user);
        DeckResponse expectedResponse = EntityToResponseMapper.toDeckResponse(deck);

        when(deckService.getDeckByFrn(eq(deckFrn), eq(user.getUserFrn()))).thenReturn(deck);

        // Act
        ResponseEntity<DeckResponse> responseEntity = deckController.getDeck(deckFrn);

        // Assert
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertEquals(expectedResponse, responseEntity.getBody());
    }

    @Test
    void testGetDeckByFrnNotFound() {
        // Arrange
        doThrow(new FlashDashException(ErrorCode.E404001, "Deck not found"))
                .when(deckService).getDeckByFrn(eq(deckFrn), eq(user.getUserFrn()));

        // Act & Assert
        FlashDashException exception = assertThrows(
                FlashDashException.class,
                () -> deckController.getDeck(deckFrn)
        );
        assertEquals(ErrorCode.E404001, exception.getErrorCode());
        assertEquals("Deck not found", exception.getMessage());
    }

    @Test
    void testUpdateDeckSuccessful() {
        // Arrange
        DeckRequest deckRequest = TestUtils.createDeckRequest();
        Deck deck = TestUtils.createDeck(user);
        deck.setName("Updated Name");
        when(deckService.updateDeck(eq(deckFrn), any(DeckRequest.class), eq(user.getUserFrn()))).thenReturn(deck);

        // Act
        ResponseEntity<DeckResponse> responseEntity = deckController.updateDeck(deckFrn, deckRequest);

        // Assert
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertEquals(EntityToResponseMapper.toDeckResponse(deck), responseEntity.getBody());
    }

    @Test
    void testUpdateDeckNotFound() {
        // Arrange
        DeckRequest deckRequest = TestUtils.createDeckRequest();
        doThrow(new FlashDashException(ErrorCode.E404001, "Deck not found"))
                .when(deckService).updateDeck(eq(deckFrn), any(DeckRequest.class), eq(user.getUserFrn()));

        // Act & Assert
        FlashDashException exception = assertThrows(
                FlashDashException.class,
                () -> deckController.updateDeck(deckFrn, deckRequest)
        );
        assertEquals(ErrorCode.E404001, exception.getErrorCode());
        assertEquals("Deck not found", exception.getMessage());
    }

    @Test
    void testDeleteDeckSuccessful() {
        // Arrange
        doNothing().when(deckService).deleteDeck(eq(deckFrn), eq(user.getUserFrn()));

        // Act
        ResponseEntity<Void> responseEntity = deckController.deleteDeck(deckFrn);

        // Assert
        assertEquals(HttpStatus.NO_CONTENT, responseEntity.getStatusCode());
        verify(deckService, times(1)).deleteDeck(eq(deckFrn), eq(user.getUserFrn()));
    }

    @Test
    void testDeleteDeckNotFound() {
        // Arrange
        doThrow(new FlashDashException(ErrorCode.E404001, "Deck not found"))
                .when(deckService).deleteDeck(eq(deckFrn), eq(user.getUserFrn()));

        // Act & Assert
        FlashDashException exception = assertThrows(
                FlashDashException.class,
                () -> deckController.deleteDeck(deckFrn)
        );

        assertEquals(ErrorCode.E404001, exception.getErrorCode());
        assertEquals("Deck not found", exception.getMessage());
    }
}
