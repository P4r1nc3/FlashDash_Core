package com.flashdash.service;

import com.flashdash.FlashDashApplication;
import com.flashdash.TestUtils;
import com.flashdash.exception.ErrorCode;
import com.flashdash.exception.FlashDashException;
import com.flashdash.model.*;
import com.flashdash.repository.GameSessionRepository;
import com.p4r1nc3.flashdash.core.model.QuestionRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@SpringBootTest(classes = FlashDashApplication.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class GameSessionServiceTest {

    @Autowired
    private GameSessionService gameSessionService;

    @MockitoBean
    private GameSessionRepository gameSessionRepository;

    @MockitoBean
    private DeckService deckService;

    @MockitoBean
    private QuestionService questionService;

    private User user;
    private Deck deck;
    private GameSession gameSession;

    @BeforeEach
    void setUp() {
        user = TestUtils.createUser();
        deck = TestUtils.createDeck(user);
        gameSession = TestUtils.createGameSession(user, deck, GameSessionStatus.PENDING.toString());
    }

    @Test
    void shouldStartNewGameSessionSuccessfully() {
        // Arrange
        when(gameSessionRepository.findTopByDeckFrnAndUserFrnAndStatus(deck.getDeckFrn(), user.getUserFrn(), GameSessionStatus.PENDING.toString()))
                .thenReturn(Optional.empty());
        when(deckService.getDeckByFrn(deck.getDeckFrn(), user.getUserFrn())).thenReturn(deck);
        when(questionService.getAllQuestionsInDeck(deck.getDeckFrn(), user.getUserFrn()))
                .thenReturn(List.of(TestUtils.createQuestion(deck, "Sample Question")));

        // Act
        List<Question> questions = gameSessionService.startGameSession(deck.getDeckFrn(), user.getUserFrn());

        // Assert
        assertThat(questions).isNotEmpty();
        verify(gameSessionRepository).save(any(GameSession.class));
    }

    @Test
    void shouldResumeExistingGameSession() {
        // Arrange
        when(gameSessionRepository.findTopByDeckFrnAndUserFrnAndStatus(deck.getDeckFrn(), user.getUserFrn(), GameSessionStatus.PENDING.toString()))
                .thenReturn(Optional.of(gameSession));
        when(questionService.getAllQuestionsInDeck(deck.getDeckFrn(), user.getUserFrn()))
                .thenReturn(List.of(TestUtils.createQuestion(deck, "Sample Question")));

        // Act
        List<Question> questions = gameSessionService.startGameSession(deck.getDeckFrn(), user.getUserFrn());

        // Assert
        assertThat(questions).isNotEmpty();
        verify(gameSessionRepository, never()).save(any(GameSession.class));
    }

    @Test
    void shouldEndGameSessionSuccessfully() {
        // Arrange
        QuestionRequest userAnswer = new QuestionRequest();
        userAnswer.setQuestion("Sample Question");
        userAnswer.setCorrectAnswers(List.of("Correct Answer"));

        Question correctQuestion = TestUtils.createQuestion(deck, "Sample Question");
        correctQuestion.setCorrectAnswers(List.of("Correct Answer"));

        when(questionService.getAllQuestionsInDeck(deck.getDeckFrn(), user.getUserFrn())).thenReturn(List.of(correctQuestion));
        when(gameSessionRepository.findTopByDeckFrnAndUserFrnAndStatus(deck.getDeckFrn(), user.getUserFrn(), GameSessionStatus.PENDING.toString()))
                .thenReturn(Optional.of(gameSession));

        // Act
        GameSession result = gameSessionService.endGameSession(deck.getDeckFrn(), user.getUserFrn(), List.of(userAnswer));

        // Assert
        assertThat(result.getTotalScore()).isEqualTo(100);
        assertThat(result.getCorrectAnswersCount()).isEqualTo(1);
        assertThat(result.getQuestionCount()).isEqualTo(1);
        verify(gameSessionRepository).save(gameSession);
    }

    @Test
    void shouldThrowExceptionWhenEndingSessionWithInvalidQuestion() {
        // Arrange
        QuestionRequest invalidAnswer = new QuestionRequest();
        invalidAnswer.setQuestion("Invalid Question");
        invalidAnswer.setCorrectAnswers(List.of("Wrong Answer"));

        Question correctQuestion = TestUtils.createQuestion(deck, "Sample Question");
        correctQuestion.setCorrectAnswers(List.of("Correct Answer"));

        GameSession activeSession = TestUtils.createGameSession(user, deck, GameSessionStatus.PENDING.toString());

        when(gameSessionRepository.findTopByDeckFrnAndUserFrnAndStatus(deck.getDeckFrn(), user.getUserFrn(), GameSessionStatus.PENDING.toString()))
                .thenReturn(Optional.of(activeSession));

        when(questionService.getAllQuestionsInDeck(deck.getDeckFrn(), user.getUserFrn())).thenReturn(List.of(correctQuestion));

        // Act & Assert
        assertThatThrownBy(() -> gameSessionService.endGameSession(deck.getDeckFrn(), user.getUserFrn(), List.of(invalidAnswer)))
                .isInstanceOf(FlashDashException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.E404002)
                .hasMessage("Matching question not found in the provided deck.");
    }

    @Test
    void shouldThrowExceptionWhenEndingSessionWithoutActiveSession() {
        // Arrange
        QuestionRequest userAnswer = new QuestionRequest();
        userAnswer.setQuestion("Sample Question");
        userAnswer.setCorrectAnswers(List.of("Correct Answer"));

        when(gameSessionRepository.findTopByDeckFrnAndUserFrnAndStatus(deck.getDeckFrn(), user.getUserFrn(), GameSessionStatus.PENDING.toString()))
                .thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> gameSessionService.endGameSession(deck.getDeckFrn(), user.getUserFrn(), List.of(userAnswer)))
                .isInstanceOf(FlashDashException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.E400003)
                .hasMessage("No active game session for this deck.");
    }

    @Test
    void shouldGetExistingGameSession() {
        // Arrange
        when(gameSessionRepository.findByDeckFrnAndGameSessionFrnAndUserFrn(deck.getDeckFrn(), gameSession.getGameSessionFrn(), user.getUserFrn()))
                .thenReturn(Optional.of(gameSession));

        // Act
        GameSession retrievedSession = gameSessionService.getGameSession(deck.getDeckFrn(), gameSession.getGameSessionFrn(), user.getUserFrn());

        // Assert
        assertThat(retrievedSession).isEqualTo(gameSession);
    }

    @Test
    void shouldThrowExceptionWhenGameSessionNotFound() {
        // Arrange
        when(gameSessionRepository.findByDeckFrnAndGameSessionFrnAndUserFrn(anyString(), anyString(), anyString()))
                .thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> gameSessionService.getGameSession(deck.getDeckFrn(), "invalid-frn", user.getUserFrn()))
                .isInstanceOf(FlashDashException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.E404006)
                .hasMessage("Game session not found");
    }

    @Test
    void shouldRemoveAllGameSessionsForUser() {
        // Arrange
        when(gameSessionRepository.findAllByUserFrn(user.getUserFrn())).thenReturn(List.of(gameSession));

        // Act
        gameSessionService.removeAllGameSessionsForUser(user.getUserFrn());

        // Assert
        verify(gameSessionRepository).deleteAll(anyList());
    }

    @Test
    void shouldNotRemoveSessionsWhenUserHasNone() {
        // Arrange
        when(gameSessionRepository.findAllByUserFrn(user.getUserFrn())).thenReturn(List.of());

        // Act
        gameSessionService.removeAllGameSessionsForUser(user.getUserFrn());

        // Assert
        verify(gameSessionRepository).findAllByUserFrn(user.getUserFrn());
        verify(gameSessionRepository, never()).deleteAll(anyList());
    }
}
