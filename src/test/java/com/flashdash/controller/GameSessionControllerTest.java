package com.flashdash.controller;

import com.flashdash.FlashDashApplication;
import com.flashdash.TestUtils;
import com.flashdash.exception.ErrorCode;
import com.flashdash.exception.FlashDashException;
import com.flashdash.model.*;
import com.flashdash.service.GameSessionService;
import com.flashdash.utils.EntityToResponseMapper;
import com.p4r1nc3.flashdash.core.model.GameSessionResponse;
import com.p4r1nc3.flashdash.core.model.QuestionRequest;
import com.p4r1nc3.flashdash.core.model.QuestionResponse;
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
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class GameSessionControllerTest {

    @Autowired
    private GameSessionController gameSessionController;

    @MockitoBean
    private GameSessionService gameSessionService;

    private User user;
    private Deck deck;
    private GameSession gameSession;
    private MockedStatic<SecurityContextHolder> mockedSecurityContextHolder;

    @BeforeEach
    void setUp() {
        user = TestUtils.createUser();
        deck = TestUtils.createDeck(user);
        gameSession = TestUtils.createGameSession(user, deck, GameSessionStatus.PENDING);

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
    void testStartGameSessionSuccessful() {
        // Arrange
        Question question = TestUtils.createQuestion(deck, "What is Java?");
        List<Question> questions = List.of(question);
        List<QuestionResponse> expectedResponses = EntityToResponseMapper.toQuestionResponseList(questions);

        when(gameSessionService.startGameSession(eq(deck.getId()), eq(user))).thenReturn(questions);

        // Act
        ResponseEntity<List<QuestionResponse>> responseEntity = gameSessionController.startGameSession(deck.getId());

        // Assert
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertEquals(expectedResponses, responseEntity.getBody());
    }

    @Test
    @Order(2)
    void testEndGameSessionSuccessful() {
        // Arrange
        QuestionRequest userAnswer = new QuestionRequest()
                .question("What is Java?")
                .correctAnswers(List.of("A programming language"));

        when(gameSessionService.endGameSession(eq(deck.getId()), eq(user), anyList()))
                .thenReturn(gameSession);

        GameSessionResponse expectedResponse = EntityToResponseMapper.toGameSessionResponse(gameSession);

        // Act
        ResponseEntity<GameSessionResponse> responseEntity = gameSessionController.endGameSession(deck.getId(), List.of(userAnswer));

        // Assert
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertEquals(expectedResponse, responseEntity.getBody());
    }

    @Test
    @Order(3)
    void testEndGameSessionNoActiveSession() {
        // Arrange
        QuestionRequest userAnswer = new QuestionRequest().question("What is Java?").correctAnswers(List.of("A programming language"));

        doThrow(new FlashDashException(ErrorCode.E400003, "No active game session for this deck."))
                .when(gameSessionService).endGameSession(eq(deck.getId()), eq(user), anyList());

        // Act & Assert
        FlashDashException exception = assertThrows(
                FlashDashException.class,
                () -> gameSessionController.endGameSession(deck.getId(), List.of(userAnswer))
        );
        assertEquals(ErrorCode.E400003, exception.getErrorCode());
        assertEquals("No active game session for this deck.", exception.getMessage());
    }

    @Test
    @Order(4)
    void testGetAllGameSessionsSuccessful() {
        // Arrange
        List<GameSession> gameSessions = List.of(gameSession);
        List<GameSessionResponse> expectedResponses = EntityToResponseMapper.toGameSessionResponseList(gameSessions);

        when(gameSessionService.getGameSessions(eq(deck.getId()), eq(user))).thenReturn(gameSessions);

        // Act
        ResponseEntity<List<GameSessionResponse>> responseEntity = gameSessionController.getGameSessions(deck.getId());

        // Assert
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertEquals(expectedResponses, responseEntity.getBody());
    }

    @Test
    @Order(5)
    void testGetSingleGameSessionSuccessful() {
        // Arrange
        GameSessionResponse expectedResponse = EntityToResponseMapper.toGameSessionResponse(gameSession);

        when(gameSessionService.getGameSession(eq(deck.getId()), eq(gameSession.getId()), eq(user)))
                .thenReturn(gameSession);

        // Act
        ResponseEntity<GameSessionResponse> responseEntity = gameSessionController.getGameSession(deck.getId(), gameSession.getId());

        // Assert
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertEquals(expectedResponse, responseEntity.getBody());
    }

    @Test
    @Order(6)
    void testGetSingleGameSessionNotFound() {
        // Arrange
        doThrow(new FlashDashException(ErrorCode.E404006, "Game session not found"))
                .when(gameSessionService).getGameSession(eq(deck.getId()), eq(99L), eq(user));

        // Act & Assert
        FlashDashException exception = assertThrows(
                FlashDashException.class,
                () -> gameSessionController.getGameSession(deck.getId(), 99L)
        );
        assertEquals(ErrorCode.E404006, exception.getErrorCode());
        assertEquals("Game session not found", exception.getMessage());
    }
}
