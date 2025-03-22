package com.flashdash.core.service;

import com.flashdash.core.TestUtils;
import com.flashdash.core.exception.ErrorCode;
import com.flashdash.core.exception.FlashDashException;
import com.flashdash.core.model.User;
import com.flashdash.core.model.Question;
import com.flashdash.core.model.Deck;
import com.flashdash.core.repository.QuestionRepository;
import com.flashdash.core.service.api.ActivityService;
import com.p4r1nc3.flashdash.core.model.QuestionRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.Arrays;
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
        when(questionRepository.findAllByDeckFrn(deck.getDeckFrn())).thenReturn(List.of());
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
    void shouldThrowExceptionWhenAddingDuplicateQuestion() {
        // Arrange
        Question existingQuestion = TestUtils.createQuestion(deck, questionRequest.getQuestion());

        when(deckService.getDeckByFrn(deck.getDeckFrn(), user.getUserFrn())).thenReturn(deck);
        when(questionRepository.findAllByDeckFrn(deck.getDeckFrn())).thenReturn(List.of(existingQuestion));

        // Act & Assert
        assertThatThrownBy(() -> questionService.addQuestionToDeck(deck.getDeckFrn(), questionRequest, user.getUserFrn()))
                .isInstanceOf(FlashDashException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.E400004);

        verify(deckService).getDeckByFrn(deck.getDeckFrn(), user.getUserFrn());
        verify(questionRepository).findAllByDeckFrn(deck.getDeckFrn());
        verify(questionRepository, never()).save(any(Question.class));
    }

    @Test
    void shouldThrowExceptionWhenAddingQuestionWithDuplicateCorrectAnswers() {
        // Arrange
        QuestionRequest requestWithDuplicates = new QuestionRequest();
        requestWithDuplicates.setQuestion("What is Java?");
        requestWithDuplicates.setCorrectAnswers(Arrays.asList("A programming language", "A programming language"));
        requestWithDuplicates.setIncorrectAnswers(Arrays.asList("A type of coffee", "An island"));
        requestWithDuplicates.setDifficulty(QuestionRequest.DifficultyEnum.MEDIUM);

        when(deckService.getDeckByFrn(deck.getDeckFrn(), user.getUserFrn())).thenReturn(deck);
        when(questionRepository.findAllByDeckFrn(deck.getDeckFrn())).thenReturn(List.of());

        // Act & Assert
        assertThatThrownBy(() -> questionService.addQuestionToDeck(deck.getDeckFrn(), requestWithDuplicates, user.getUserFrn()))
                .isInstanceOf(FlashDashException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.E400005);

        verify(deckService).getDeckByFrn(deck.getDeckFrn(), user.getUserFrn());
        verify(questionRepository, never()).save(any(Question.class));
    }

    @Test
    void shouldThrowExceptionWhenAddingQuestionWithDuplicateIncorrectAnswers() {
        // Arrange
        QuestionRequest requestWithDuplicates = new QuestionRequest();
        requestWithDuplicates.setQuestion("What is Java?");
        requestWithDuplicates.setCorrectAnswers(Arrays.asList("A programming language"));
        requestWithDuplicates.setIncorrectAnswers(Arrays.asList("A type of coffee", "A type of coffee"));
        requestWithDuplicates.setDifficulty(QuestionRequest.DifficultyEnum.MEDIUM);

        when(deckService.getDeckByFrn(deck.getDeckFrn(), user.getUserFrn())).thenReturn(deck);
        when(questionRepository.findAllByDeckFrn(deck.getDeckFrn())).thenReturn(List.of());

        // Act & Assert
        assertThatThrownBy(() -> questionService.addQuestionToDeck(deck.getDeckFrn(), requestWithDuplicates, user.getUserFrn()))
                .isInstanceOf(FlashDashException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.E400005);

        verify(deckService).getDeckByFrn(deck.getDeckFrn(), user.getUserFrn());
        verify(questionRepository, never()).save(any(Question.class));
    }

    @Test
    void shouldThrowExceptionWhenAddingQuestionWithOverlappingAnswers() {
        // Arrange
        QuestionRequest requestWithDuplicates = new QuestionRequest();
        requestWithDuplicates.setQuestion("What is Java?");
        requestWithDuplicates.setCorrectAnswers(Arrays.asList("A programming language"));
        requestWithDuplicates.setIncorrectAnswers(Arrays.asList("A type of coffee", "A programming language"));
        requestWithDuplicates.setDifficulty(QuestionRequest.DifficultyEnum.MEDIUM);

        when(deckService.getDeckByFrn(deck.getDeckFrn(), user.getUserFrn())).thenReturn(deck);
        when(questionRepository.findAllByDeckFrn(deck.getDeckFrn())).thenReturn(List.of());

        // Act & Assert
        assertThatThrownBy(() -> questionService.addQuestionToDeck(deck.getDeckFrn(), requestWithDuplicates, user.getUserFrn()))
                .isInstanceOf(FlashDashException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.E400005);

        verify(deckService).getDeckByFrn(deck.getDeckFrn(), user.getUserFrn());
        verify(questionRepository, never()).save(any(Question.class));
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
        when(questionRepository.findAllByDeckFrn(deck.getDeckFrn())).thenReturn(List.of(question));
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
    void shouldThrowExceptionWhenUpdatingToDuplicateQuestion() {
        // Arrange
        Question existingQuestion = TestUtils.createQuestion(deck, "What is Spring?");
        Question questionToUpdate = TestUtils.createQuestion(deck, "What is Java?");

        QuestionRequest updateRequest = new QuestionRequest();
        updateRequest.setQuestion("What is Spring?"); // Trying to update to a question that already exists
        updateRequest.setCorrectAnswers(Arrays.asList("A framework"));
        updateRequest.setIncorrectAnswers(Arrays.asList("A season"));
        updateRequest.setDifficulty(QuestionRequest.DifficultyEnum.MEDIUM);

        when(deckService.getDeckByFrn(deck.getDeckFrn(), user.getUserFrn())).thenReturn(deck);
        when(questionRepository.findByDeckFrnAndQuestionFrn(deck.getDeckFrn(), questionToUpdate.getQuestionFrn()))
                .thenReturn(Optional.of(questionToUpdate));
        when(questionRepository.findAllByDeckFrn(deck.getDeckFrn())).thenReturn(List.of(existingQuestion, questionToUpdate));

        // Act & Assert
        assertThatThrownBy(() -> questionService.updateQuestion(deck.getDeckFrn(), questionToUpdate.getQuestionFrn(), updateRequest, user.getUserFrn()))
                .isInstanceOf(FlashDashException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.E400004);

        verify(deckService).getDeckByFrn(deck.getDeckFrn(), user.getUserFrn());
        verify(questionRepository).findByDeckFrnAndQuestionFrn(deck.getDeckFrn(), questionToUpdate.getQuestionFrn());
        verify(questionRepository, never()).save(any(Question.class));
    }

    @Test
    void shouldThrowExceptionWhenUpdatingWithDuplicateAnswers() {
        // Arrange
        QuestionRequest updateRequest = new QuestionRequest();
        updateRequest.setQuestion("Updated question");
        updateRequest.setCorrectAnswers(Arrays.asList("Answer 1", "Answer 1")); // Duplicate answers
        updateRequest.setIncorrectAnswers(Arrays.asList("Wrong 1", "Wrong 2"));
        updateRequest.setDifficulty(QuestionRequest.DifficultyEnum.MEDIUM);

        when(deckService.getDeckByFrn(deck.getDeckFrn(), user.getUserFrn())).thenReturn(deck);
        when(questionRepository.findByDeckFrnAndQuestionFrn(deck.getDeckFrn(), question.getQuestionFrn()))
                .thenReturn(Optional.of(question));
        when(questionRepository.findAllByDeckFrn(deck.getDeckFrn())).thenReturn(List.of(question));

        // Act & Assert
        assertThatThrownBy(() -> questionService.updateQuestion(deck.getDeckFrn(), question.getQuestionFrn(), updateRequest, user.getUserFrn()))
                .isInstanceOf(FlashDashException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.E400005);

        verify(deckService).getDeckByFrn(deck.getDeckFrn(), user.getUserFrn());
        verify(questionRepository).findByDeckFrnAndQuestionFrn(deck.getDeckFrn(), question.getQuestionFrn());
        verify(questionRepository, never()).save(any(Question.class));
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
