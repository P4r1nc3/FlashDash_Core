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
        gameSession = TestUtils.createGameSession(user, deck, GameSessionStatus.PENDING);
    }

    @Test
    void shouldStartNewGameSessionSuccessfully() {
        // Arrange
        when(gameSessionRepository.findTopByDeckIdAndUserIdAndStatus(deck.getId(), user.getId(), GameSessionStatus.PENDING))
                .thenReturn(null);
        when(deckService.getDeckById(deck.getId(), user)).thenReturn(deck);
        when(questionService.getAllQuestionsInDeck(deck.getId(), user))
                .thenReturn(List.of(TestUtils.createQuestion(deck, "Sample Question")));

        // Act
        List<Question> questions = gameSessionService.startGameSession(deck.getId(), user);

        // Assert
        assertThat(questions).isNotEmpty();
        verify(gameSessionRepository).save(any(GameSession.class));
    }

    @Test
    void shouldResumeExistingGameSession() {
        // Arrange
        when(gameSessionRepository.findTopByDeckIdAndUserIdAndStatus(deck.getId(), user.getId(), GameSessionStatus.PENDING))
                .thenReturn(gameSession);
        when(questionService.getAllQuestionsInDeck(deck.getId(), user))
                .thenReturn(List.of(TestUtils.createQuestion(deck, "Sample Question")));

        // Act
        List<Question> questions = gameSessionService.startGameSession(deck.getId(), user);

        // Assert
        assertThat(questions).isNotEmpty();
        verify(gameSessionRepository, never()).save(gameSession);
    }

    @Test
    void shouldEndGameSessionSuccessfully() {
        // Arrange
        QuestionRequest userAnswer = new QuestionRequest()
                .question("Sample Question")
                .correctAnswers(List.of("Correct Answer"));

        Question correctQuestion = TestUtils.createQuestion(deck, "Sample Question");
        correctQuestion.setCorrectAnswers(List.of("Correct Answer"));

        when(questionService.getAllQuestionsInDeck(deck.getId(), user)).thenReturn(List.of(correctQuestion));
        when(gameSessionRepository.findTopByDeckIdAndUserIdAndStatus(deck.getId(), user.getId(), GameSessionStatus.PENDING))
                .thenReturn(gameSession);

        // Act
        GameSession result = gameSessionService.endGameSession(deck.getId(), user, List.of(userAnswer));

        // Assert
        assertThat(result.getTotalScore()).isEqualTo(100);
        assertThat(result.getCorrectAnswersCount()).isEqualTo(1);
        assertThat(result.getQuestionCount()).isEqualTo(1);
        verify(gameSessionRepository).save(gameSession);
    }

    @Test
    void shouldThrowExceptionWhenEndingSessionWithInvalidQuestion() {
        // Arrange
        QuestionRequest invalidAnswer = new QuestionRequest()
                .question("Invalid Question")
                .correctAnswers(List.of("Wrong Answer"));

        Question correctQuestion = TestUtils.createQuestion(deck, "Sample Question");
        correctQuestion.setCorrectAnswers(List.of("Correct Answer"));

        when(questionService.getAllQuestionsInDeck(deck.getId(), user)).thenReturn(List.of(correctQuestion));

        // Act & Assert
        assertThatThrownBy(() -> gameSessionService.endGameSession(deck.getId(), user, List.of(invalidAnswer)))
                .isInstanceOf(FlashDashException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.E404002)
                .hasMessage("Matching question not found in the provided deck.");
    }

    @Test
    void shouldThrowExceptionWhenEndingSessionWithoutActiveSession() {
        // Arrange
        QuestionRequest userAnswer = new QuestionRequest()
                .question("Sample Question")
                .correctAnswers(List.of("Correct Answer"));

        when(gameSessionRepository.findTopByDeckIdAndUserIdAndStatus(deck.getId(), user.getId(), GameSessionStatus.PENDING))
                .thenReturn(null);

        // Act & Assert
        assertThatThrownBy(() -> gameSessionService.endGameSession(deck.getId(), user, List.of(userAnswer)))
                .isInstanceOf(FlashDashException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.E404002)
                .hasMessage("Matching question not found in the provided deck.");
    }

    @Test
    void shouldGetExistingGameSession() {
        // Arrange
        deck.setId(1L);
        user.setId(1L);
        when(gameSessionRepository.findById(gameSession.getId())).thenReturn(java.util.Optional.of(gameSession));

        // Act
        GameSession retrievedSession = gameSessionService.getGameSession(1L, gameSession.getId(), user);

        // Assert
        assertThat(retrievedSession).isEqualTo(gameSession);
    }

    @Test
    void shouldThrowExceptionWhenGameSessionNotFound() {
        // Arrange
        when(gameSessionRepository.findById(anyLong())).thenReturn(java.util.Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> gameSessionService.getGameSession(deck.getId(), 99L, user))
                .isInstanceOf(FlashDashException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.E404006)
                .hasMessage("Game session not found");
    }

    @Test
    void shouldRemoveAllGameSessionsForUser() {
        // Arrange
        when(gameSessionRepository.findAllByUser(user)).thenReturn(List.of(gameSession));

        // Act
        gameSessionService.removeAllGameSessionsForUser(user);

        // Assert
        verify(gameSessionRepository).deleteAll(anyList());
    }

    @Test
    void shouldNotRemoveSessionsWhenUserHasNone() {
        // Arrange
        when(gameSessionRepository.findAllByUser(user)).thenReturn(List.of());

        // Act
        gameSessionService.removeAllGameSessionsForUser(user);

        // Assert
        verify(gameSessionRepository).deleteAll(List.of());
    }
}
