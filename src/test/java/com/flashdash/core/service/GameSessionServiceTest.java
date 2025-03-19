package com.flashdash.core.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.flashdash.core.TestUtils;
import com.flashdash.core.exception.ErrorCode;
import com.flashdash.core.exception.FlashDashException;
import com.flashdash.core.model.*;
import com.flashdash.core.repository.GameSessionRepository;
import com.flashdash.core.repository.UserRepository;
import com.flashdash.core.service.api.ActivityService;
import com.p4r1nc3.flashdash.activity.model.LogActivityRequest.ActivityTypeEnum;
import com.p4r1nc3.flashdash.core.model.QuestionRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.time.Duration;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@SpringBootTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class GameSessionServiceTest {

    @Autowired
    private GameSessionService gameSessionService;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ActivityService activityService;

    @MockitoBean
    private QuestionService questionService;

    @MockitoBean
    private GameSessionRepository gameSessionRepository;

    @MockitoBean
    private UserRepository userRepository;

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
        when(questionService.getAllQuestionsInDeck(deck.getDeckFrn(), user.getUserFrn()))
                .thenReturn(List.of(TestUtils.createQuestion(deck, "Sample Question")));

        // Act
        List<Question> questions = gameSessionService.startGameSession(deck.getDeckFrn(), user.getUserFrn());

        // Assert
        assertThat(questions).isNotEmpty();
        verify(gameSessionRepository, times(1)).save(any(GameSession.class));
        verify(activityService).logUserActivity(eq(user.getUserFrn()), anyString(), eq(ActivityTypeEnum.GAME_STARTED));
    }

    @Test
    void shouldDeleteExistingGameSessionAndStartNew() {
        // Arrange
        when(gameSessionRepository.findTopByDeckFrnAndUserFrnAndStatus(deck.getDeckFrn(), user.getUserFrn(), GameSessionStatus.PENDING.toString()))
                .thenReturn(Optional.of(gameSession));
        when(questionService.getAllQuestionsInDeck(deck.getDeckFrn(), user.getUserFrn()))
                .thenReturn(List.of(TestUtils.createQuestion(deck, "Sample Question")));

        // Act
        List<Question> questions = gameSessionService.startGameSession(deck.getDeckFrn(), user.getUserFrn());

        // Assert
        assertThat(questions).isNotEmpty();
        verify(gameSessionRepository, times(1)).delete(gameSession);
        verify(gameSessionRepository, times(1)).save(any(GameSession.class));
        verify(activityService).logUserActivity(eq(user.getUserFrn()), anyString(), eq(ActivityTypeEnum.GAME_STARTED));
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

        // Mock UserService behavior
        when(userRepository.findByUserFrn(user.getUserFrn())).thenReturn(Optional.of(user));
        doNothing().when(userRepository).save(user);

        // Act
        GameSession result = gameSessionService.endGameSession(deck.getDeckFrn(), user.getUserFrn(), List.of(userAnswer));

        // Assert
        assertThat(result.getStatus()).isEqualTo(GameSessionStatus.FINISHED.toString());
        assertThat(result.getTotalScore()).isEqualTo(10);
        assertThat(result.getCorrectAnswersCount()).isEqualTo(1);
        assertThat(result.getWrongAnswersCount()).isEqualTo(0);
        assertThat(result.getQuestionCount()).isEqualTo(1);
        assertThat(result.getSessionDetails()).isNotBlank();

        verify(gameSessionRepository).save(gameSession);
        verify(userRepository).findByUserFrn(user.getUserFrn());
        verify(userRepository).save(user);
        verify(activityService).logUserActivity(eq(user.getUserFrn()), eq(gameSession.getGameSessionFrn()), eq(ActivityTypeEnum.GAME_FINISHED));

        // Verify user stats were updated
        assertThat(user.getGamesPlayed()).isEqualTo(1);
        assertThat(user.getPoints()).isEqualTo(10);
        assertThat(user.getStudyTime()).isNotNull();
    }

    @Test
    void shouldHandleIncorrectAnswers() {
        // Arrange
        QuestionRequest userAnswer = new QuestionRequest();
        userAnswer.setQuestion("Sample Question");
        userAnswer.setCorrectAnswers(List.of("Wrong Answer"));  // User provides wrong answer

        Question correctQuestion = TestUtils.createQuestion(deck, "Sample Question");
        correctQuestion.setCorrectAnswers(List.of("Correct Answer"));

        when(questionService.getAllQuestionsInDeck(deck.getDeckFrn(), user.getUserFrn())).thenReturn(List.of(correctQuestion));
        when(gameSessionRepository.findTopByDeckFrnAndUserFrnAndStatus(deck.getDeckFrn(), user.getUserFrn(), GameSessionStatus.PENDING.toString()))
                .thenReturn(Optional.of(gameSession));

        // Setup user with initial stats
        user.setGamesPlayed(0);
        user.setPoints(0);
        user.setStudyTime(Duration.ZERO);

        // Mock UserService behavior
        when(userRepository.findByUserFrn(user.getUserFrn())).thenReturn(Optional.of(user));
        doNothing().when(userRepository).save(user);

        // Act
        GameSession result = gameSessionService.endGameSession(deck.getDeckFrn(), user.getUserFrn(), List.of(userAnswer));

        // Assert
        assertThat(result.getStatus()).isEqualTo(GameSessionStatus.FINISHED.toString());
        assertThat(result.getTotalScore()).isEqualTo(-4);  // Score should be negative (wrong answer penalty)
        assertThat(result.getCorrectAnswersCount()).isEqualTo(0);
        assertThat(result.getWrongAnswersCount()).isEqualTo(1);
        assertThat(result.getQuestionCount()).isEqualTo(1);

        verify(gameSessionRepository).save(gameSession);
        verify(userRepository).findByUserFrn(user.getUserFrn());
        verify(userRepository).save(user);

        // Verify user stats were updated with negative points
        assertThat(user.getGamesPlayed()).isEqualTo(1);
        assertThat(user.getPoints()).isEqualTo(-4);
        assertThat(user.getStudyTime()).isNotNull();
    }

    @Test
    void shouldThrowExceptionWhenEndingSessionWithInvalidQuestion() {
        // Arrange
        QuestionRequest invalidAnswer = new QuestionRequest();
        invalidAnswer.setQuestion("Invalid Question");
        invalidAnswer.setCorrectAnswers(List.of("Wrong Answer"));

        Question correctQuestion = TestUtils.createQuestion(deck, "Sample Question");
        correctQuestion.setCorrectAnswers(List.of("Correct Answer"));

        when(gameSessionRepository.findTopByDeckFrnAndUserFrnAndStatus(deck.getDeckFrn(), user.getUserFrn(), GameSessionStatus.PENDING.toString()))
                .thenReturn(Optional.of(gameSession));

        when(questionService.getAllQuestionsInDeck(deck.getDeckFrn(), user.getUserFrn())).thenReturn(List.of(correctQuestion));

        // Act & Assert
        assertThatThrownBy(() -> gameSessionService.endGameSession(deck.getDeckFrn(), user.getUserFrn(), List.of(invalidAnswer)))
                .isInstanceOf(FlashDashException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.E404002)
                .hasMessage("Matching question not found in the provided deck.");

        verify(userRepository, never()).findByUserFrn(anyString());
        verify(userRepository, never()).save(any(User.class));
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

        verify(userRepository, never()).findByUserFrn(anyString());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void shouldRetrieveExistingGameSession() {
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
    void shouldGetAllGameSessionsForUser() {
        // Arrange
        when(gameSessionRepository.findAllByUserFrn(user.getUserFrn())).thenReturn(List.of(gameSession));

        // Act
        List<GameSession> sessions = gameSessionService.getAllGameSessions(user.getUserFrn());

        // Assert
        assertThat(sessions).hasSize(1);
        assertThat(sessions.get(0)).isEqualTo(gameSession);
        verify(gameSessionRepository).findAllByUserFrn(user.getUserFrn());
    }

    @Test
    void shouldGetGameSessionsForDeck() {
        // Arrange
        when(gameSessionRepository.findAllByDeckFrnAndUserFrn(deck.getDeckFrn(), user.getUserFrn())).thenReturn(List.of(gameSession));

        // Act
        List<GameSession> sessions = gameSessionService.getGameSessions(deck.getDeckFrn(), user.getUserFrn());

        // Assert
        assertThat(sessions).hasSize(1);
        assertThat(sessions.get(0)).isEqualTo(gameSession);
        verify(gameSessionRepository).findAllByDeckFrnAndUserFrn(deck.getDeckFrn(), user.getUserFrn());
    }

    @Test
    void shouldRemoveAllGameSessionsForUser() {
        // Arrange
        when(gameSessionRepository.findAllByUserFrn(user.getUserFrn())).thenReturn(List.of(gameSession));

        // Act
        gameSessionService.removeAllGameSessionsForUser(user.getUserFrn());

        // Assert
        verify(gameSessionRepository).findAllByUserFrn(user.getUserFrn());
        verify(gameSessionRepository).deleteAll(List.of(gameSession));
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