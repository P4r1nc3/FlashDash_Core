package com.flashdash.service;

import com.flashdash.FlashDashApplication;
import com.flashdash.TestUtils;
import com.flashdash.dto.response.GameSessionResult;
import com.flashdash.exception.ErrorCode;
import com.flashdash.exception.FlashDashException;
import com.flashdash.model.*;
import com.flashdash.repository.GameSessionRepository;
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
        when(questionService.getAllQuestionsInDeck(deck.getId(), user)).thenReturn(List.of(TestUtils.createQuestion(deck, "Sample Question")));

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
        when(questionService.getAllQuestionsInDeck(deck.getId(), user)).thenReturn(List.of(TestUtils.createQuestion(deck, "Sample Question")));

        // Act
        List<Question> questions = gameSessionService.startGameSession(deck.getId(), user);

        // Assert
        assertThat(questions).isNotEmpty();
        verify(gameSessionRepository).save(gameSession);
    }

    @Test
    void shouldEndGameSessionSuccessfully() {
        // Arrange
        Question question = TestUtils.createQuestion(deck, "Sample Question");
        question.setQuestionId(1L); // Upewnij się, że questionId nie jest null
        List<Question> userAnswers = List.of(question);

        when(questionService.getAllQuestionsInDeck(deck.getId(), user)).thenReturn(userAnswers);
        when(gameSessionRepository.findTopByDeckIdAndUserIdAndStatus(deck.getId(), user.getId(), GameSessionStatus.PENDING))
                .thenReturn(gameSession);

        // Act
        GameSessionResult result = gameSessionService.endGameSession(deck.getId(), user, userAnswers);

        // Assert
        assertThat(result.getScore()).isEqualTo(100);
        assertThat(result.getCorrectAnswers()).isEqualTo(1);
        assertThat(result.getTotalQuestions()).isEqualTo(1);
        verify(gameSessionRepository).save(gameSession);
    }

    @Test
    void shouldThrowExceptionWhenEndingSessionWithInvalidQuestion() {
        // Arrange
        Question invalidQuestion = TestUtils.createQuestion(deck, "Invalid Question");
        invalidQuestion.setQuestionId(99L); // Nadaj mu ID, żeby uniknąć NullPointerException

        List<Question> userAnswers = List.of(invalidQuestion);

        Question validQuestion = TestUtils.createQuestion(deck, "Sample Question");
        validQuestion.setQuestionId(1L);

        when(questionService.getAllQuestionsInDeck(deck.getId(), user)).thenReturn(List.of(validQuestion));

        // Act & Assert
        assertThatThrownBy(() -> gameSessionService.endGameSession(deck.getId(), user, userAnswers))
                .isInstanceOf(FlashDashException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.E404003)
                .hasMessage("Question not found");
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
