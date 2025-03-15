package com.flashdash.core.controller;

import com.flashdash.core.TestUtils;
import com.flashdash.core.exception.ErrorCode;
import com.flashdash.core.exception.FlashDashException;
import com.flashdash.core.model.Deck;
import com.flashdash.core.model.User;
import com.flashdash.core.service.DeckService;
import com.flashdash.core.utils.EntityToResponseMapper;
import com.p4r1nc3.flashdash.core.model.DeckRequest;
import com.p4r1nc3.flashdash.core.model.DeckResponse;
import org.junit.jupiter.api.*;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.MockedStatic;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@SpringBootTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class DeckControllerTest {

    private DeckController deckController;

    @Mock
    private DeckService deckService;

    @Mock
    private EntityToResponseMapper entityToResponseMapper;

    private User user;
    private String deckFrn;
    private MockedStatic<SecurityContextHolder> mockedSecurityContextHolder;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        deckController = new DeckController(deckService, entityToResponseMapper);

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
        DeckResponse deckResponse = new DeckResponse();

        when(deckService.createDeck(any(DeckRequest.class), eq(user.getUserFrn()))).thenReturn(deck);
        when(entityToResponseMapper.mapToDeckResponse(deck)).thenReturn(deckResponse);

        // Act
        ResponseEntity<DeckResponse> responseEntity = deckController.createDeck(deckRequest);

        // Assert
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertEquals(deckResponse, responseEntity.getBody());
    }

    @Test
    void testGetAllDecksSuccessful() {
        // Arrange
        Deck deck = TestUtils.createDeck(user);
        List<Deck> decks = List.of(deck);
        List<DeckResponse> expectedResponses = List.of(new DeckResponse());

        when(deckService.getAllDecks(eq(user.getUserFrn()))).thenReturn(decks);
        when(entityToResponseMapper.mapToDeckResponse(decks)).thenReturn(expectedResponses);

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
        DeckResponse expectedResponse = new DeckResponse();

        when(deckService.getDeckByFrn(eq(deckFrn), eq(user.getUserFrn()))).thenReturn(deck);
        when(entityToResponseMapper.mapToDeckResponse(deck)).thenReturn(expectedResponse);

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
        DeckResponse expectedResponse = new DeckResponse();

        when(deckService.updateDeck(eq(deckFrn), any(DeckRequest.class), eq(user.getUserFrn()))).thenReturn(deck);
        when(entityToResponseMapper.mapToDeckResponse(deck)).thenReturn(expectedResponse);

        // Act
        ResponseEntity<DeckResponse> responseEntity = deckController.updateDeck(deckFrn, deckRequest);

        // Assert
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertEquals(expectedResponse, responseEntity.getBody());
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
