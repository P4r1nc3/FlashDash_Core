package com.flashdash.core.service;

import com.flashdash.core.TestUtils;
import com.flashdash.core.exception.ErrorCode;
import com.flashdash.core.exception.FlashDashException;
import com.flashdash.core.model.User;
import com.flashdash.core.model.Question;
import com.flashdash.core.model.Deck;
import com.flashdash.core.repository.QuestionRepository;
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

@SpringBootTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class QuestionServiceTest {

    @Autowired
    private QuestionService questionService;

    @MockitoBean
    private ActivityService activityService;

    @MockitoBean
    private DeckService deckService;

    @MockitoBean
    private QuestionRepository questionRepository;

    private User user;
    private Deck deck;
    private QuestionRequest questionRequest;
    private Question question;

    @BeforeEach
    void setUp() {
        user = TestUtils.createUser();
        deck = TestUtils.createDeck(user);
        questionRequest = TestUtils.createQuestionRequest();
        question = TestUtils.createQuestion(deck, questionRequest.getQuestion());
    }

    @Test
    void shouldAddQuestionToDeckSuccessfully() {
        // Arrange
        when(deckService.getDeckByFrn(deck.getDeckFrn(), user.getUserFrn())).thenReturn(deck);
        when(questionRepository.save(any(Question.class))).thenReturn(question);

        // Act
        Question createdQuestion = questionService.addQuestionToDeck(deck.getDeckFrn(), questionRequest, user.getUserFrn());

        // Assert
        assertThat(createdQuestion).isNotNull();
        assertThat(createdQuestion.getQuestion()).isEqualTo(questionRequest.getQuestion());
        verify(deckService).getDeckByFrn(deck.getDeckFrn(), user.getUserFrn());
        verify(questionRepository).save(any(Question.class));
    }

    @Test
    void shouldGetAllQuestionsInDeckSuccessfully() {
        // Arrange
        List<Question> questions = List.of(
                TestUtils.createQuestion(deck, "What is Java?"),
                TestUtils.createQuestion(deck, "What is Spring?")
        );

        when(deckService.getDeckByFrn(deck.getDeckFrn(), user.getUserFrn())).thenReturn(deck);
        when(questionRepository.findAllByDeckFrn(deck.getDeckFrn())).thenReturn(questions);

        // Act
        List<Question> retrievedQuestions = questionService.getAllQuestionsInDeck(deck.getDeckFrn(), user.getUserFrn());

        // Assert
        assertThat(retrievedQuestions).isNotEmpty();
        assertThat(retrievedQuestions).isEqualTo(questions);
        verify(deckService).getDeckByFrn(deck.getDeckFrn(), user.getUserFrn());
        verify(questionRepository).findAllByDeckFrn(deck.getDeckFrn());
    }

    @Test
    void shouldGetQuestionByFrnSuccessfully() {
        // Arrange
        when(deckService.getDeckByFrn(deck.getDeckFrn(), user.getUserFrn())).thenReturn(deck);
        when(questionRepository.findByDeckFrnAndQuestionFrn(deck.getDeckFrn(), question.getQuestionFrn()))
                .thenReturn(Optional.of(question));

        // Act
        Question retrievedQuestion = questionService.getQuestionByFrn(deck.getDeckFrn(), question.getQuestionFrn(), user.getUserFrn());

        // Assert
        assertThat(retrievedQuestion).isEqualTo(question);
        verify(deckService).getDeckByFrn(deck.getDeckFrn(), user.getUserFrn());
        verify(questionRepository).findByDeckFrnAndQuestionFrn(deck.getDeckFrn(), question.getQuestionFrn());
    }

    @Test
    void shouldThrowExceptionWhenQuestionNotFoundByFrn() {
        // Arrange
        when(deckService.getDeckByFrn(deck.getDeckFrn(), user.getUserFrn())).thenReturn(deck);
        when(questionRepository.findByDeckFrnAndQuestionFrn(deck.getDeckFrn(), "nonexistent-frn")).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> questionService.getQuestionByFrn(deck.getDeckFrn(), "nonexistent-frn", user.getUserFrn()))
                .isInstanceOf(FlashDashException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.E404003);

        verify(deckService).getDeckByFrn(deck.getDeckFrn(), user.getUserFrn());
        verify(questionRepository).findByDeckFrnAndQuestionFrn(deck.getDeckFrn(), "nonexistent-frn");
    }

    @Test
    void shouldUpdateQuestionSuccessfully() {
        // Arrange
        when(deckService.getDeckByFrn(deck.getDeckFrn(), user.getUserFrn())).thenReturn(deck);
        when(questionRepository.findByDeckFrnAndQuestionFrn(deck.getDeckFrn(), question.getQuestionFrn()))
                .thenReturn(Optional.of(question));
        when(questionRepository.save(question)).thenReturn(question);

        // Act
        Question updatedQuestion = questionService.updateQuestion(deck.getDeckFrn(), question.getQuestionFrn(), questionRequest, user.getUserFrn());

        // Assert
        assertThat(updatedQuestion.getQuestion()).isEqualTo(questionRequest.getQuestion());
        verify(deckService).getDeckByFrn(deck.getDeckFrn(), user.getUserFrn());
        verify(questionRepository).findByDeckFrnAndQuestionFrn(deck.getDeckFrn(), question.getQuestionFrn());
        verify(questionRepository).save(question);
    }

    @Test
    void shouldThrowExceptionWhenUpdatingNonExistentQuestion() {
        // Arrange
        when(deckService.getDeckByFrn(deck.getDeckFrn(), user.getUserFrn())).thenReturn(deck);
        when(questionRepository.findByDeckFrnAndQuestionFrn(deck.getDeckFrn(), "nonexistent-frn")).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> questionService.updateQuestion(deck.getDeckFrn(), "nonexistent-frn", questionRequest, user.getUserFrn()))
                .isInstanceOf(FlashDashException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.E404003);

        verify(deckService).getDeckByFrn(deck.getDeckFrn(), user.getUserFrn());
        verify(questionRepository).findByDeckFrnAndQuestionFrn(deck.getDeckFrn(), "nonexistent-frn");
    }

    @Test
    void shouldDeleteQuestionSuccessfully() {
        // Arrange
        when(deckService.getDeckByFrn(deck.getDeckFrn(), user.getUserFrn())).thenReturn(deck);
        when(questionRepository.findByDeckFrnAndQuestionFrn(deck.getDeckFrn(), question.getQuestionFrn()))
                .thenReturn(Optional.of(question));

        // Act
        questionService.deleteQuestion(deck.getDeckFrn(), question.getQuestionFrn(), user.getUserFrn());

        // Assert
        verify(deckService).getDeckByFrn(deck.getDeckFrn(), user.getUserFrn());
        verify(questionRepository).findByDeckFrnAndQuestionFrn(deck.getDeckFrn(), question.getQuestionFrn());
        verify(questionRepository).delete(question);
    }

    @Test
    void shouldThrowExceptionWhenDeletingNonExistentQuestion() {
        // Arrange
        when(deckService.getDeckByFrn(deck.getDeckFrn(), user.getUserFrn())).thenReturn(deck);
        when(questionRepository.findByDeckFrnAndQuestionFrn(deck.getDeckFrn(), "nonexistent-frn")).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> questionService.deleteQuestion(deck.getDeckFrn(), "nonexistent-frn", user.getUserFrn()))
                .isInstanceOf(FlashDashException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.E404003);

        verify(deckService).getDeckByFrn(deck.getDeckFrn(), user.getUserFrn());
        verify(questionRepository).findByDeckFrnAndQuestionFrn(deck.getDeckFrn(), "nonexistent-frn");
    }
}
