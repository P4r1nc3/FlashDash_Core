package com.flashdash.service;

import com.flashdash.FlashDashApplication;
import com.flashdash.TestUtils;
import com.flashdash.exception.ErrorCode;
import com.flashdash.exception.FlashDashException;
import com.flashdash.model.User;
import com.flashdash.model.Question;
import com.flashdash.model.Deck;
import com.flashdash.repository.QuestionRepository;
import com.flashdash.utils.EntityToResponseMapper;
import com.p4r1nc3.flashdash.core.model.QuestionRequest;
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
class QuestionServiceTest {

    @Autowired
    private QuestionService questionService;

    @MockitoBean
    private QuestionRepository questionRepository;

    @MockitoBean
    private DeckService deckService;

    @Test
    void shouldAddQuestionToDeckSuccessfully() {
        // Arrange
        User user = TestUtils.createUser();
        Deck deck = TestUtils.createDeck(user);
        QuestionRequest questionRequest = TestUtils.createQuestionRequest();
        Question question = TestUtils.createQuestion(deck, questionRequest.getQuestion());

        when(deckService.getDeckById(1L, user)).thenReturn(deck);
        when(questionRepository.save(any(Question.class))).thenReturn(question);

        // Act
        Question createdQuestion = questionService.addQuestionToDeck(1L, questionRequest, user);

        // Assert
        assertThat(createdQuestion).isNotNull();
        assertThat(createdQuestion.getQuestion()).isEqualTo(questionRequest.getQuestion());
        verify(deckService).getDeckById(1L, user);
        verify(questionRepository).save(any(Question.class));
    }

    @Test
    void shouldGetAllQuestionsInDeckSuccessfully() {
        // Arrange
        User user = TestUtils.createUser();
        Deck deck = TestUtils.createDeck(user);
        List<Question> questions = List.of(
                TestUtils.createQuestion(deck, "What is Java?"),
                TestUtils.createQuestion(deck, "What is Spring?")
        );

        when(deckService.getDeckById(1L, user)).thenReturn(deck);
        when(questionRepository.findAllByDeck(deck)).thenReturn(questions);

        // Act
        List<Question> retrievedQuestions = questionService.getAllQuestionsInDeck(1L, user);

        // Assert
        assertThat(retrievedQuestions).isNotEmpty();
        assertThat(retrievedQuestions).isEqualTo(questions);
        verify(deckService).getDeckById(1L, user);
        verify(questionRepository).findAllByDeck(deck);
    }

    @Test
    void shouldGetQuestionByIdSuccessfully() {
        // Arrange
        User user = TestUtils.createUser();
        Deck deck = TestUtils.createDeck(user);
        Question question = TestUtils.createQuestion(deck, "What is Java?");

        when(deckService.getDeckById(1L, user)).thenReturn(deck);
        when(questionRepository.findByDeckAndQuestionId(deck, 1L)).thenReturn(Optional.of(question));

        // Act
        Question retrievedQuestion = questionService.getQuestionById(1L, 1L, user);

        // Assert
        assertThat(retrievedQuestion).isEqualTo(question);
        verify(deckService).getDeckById(1L, user);
        verify(questionRepository).findByDeckAndQuestionId(deck, 1L);
    }

    @Test
    void shouldThrowExceptionWhenQuestionNotFoundById() {
        // Arrange
        User user = TestUtils.createUser();
        Deck deck = TestUtils.createDeck(user);

        when(deckService.getDeckById(1L, user)).thenReturn(deck);
        when(questionRepository.findByDeckAndQuestionId(deck, 1L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> questionService.getQuestionById(1L, 1L, user))
                .isInstanceOf(FlashDashException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.E404003);

        verify(deckService).getDeckById(1L, user);
        verify(questionRepository).findByDeckAndQuestionId(deck, 1L);
    }

    @Test
    void shouldUpdateQuestionSuccessfully() {
        // Arrange
        User user = TestUtils.createUser();
        Deck deck = TestUtils.createDeck(user);
        QuestionRequest questionRequest = TestUtils.createQuestionRequest();
        Question existingQuestion = TestUtils.createQuestion(deck, "Old Question");

        when(deckService.getDeckById(1L, user)).thenReturn(deck);
        when(questionRepository.findByDeckAndQuestionId(deck, 1L)).thenReturn(Optional.of(existingQuestion));
        when(questionRepository.save(existingQuestion)).thenReturn(existingQuestion);

        // Act
        Question updatedQuestion = questionService.updateQuestion(1L, 1L, questionRequest, user);

        // Assert
        assertThat(updatedQuestion.getQuestion()).isEqualTo(questionRequest.getQuestion());
        verify(deckService).getDeckById(1L, user);
        verify(questionRepository).findByDeckAndQuestionId(deck, 1L);
        verify(questionRepository).save(existingQuestion);
    }

    @Test
    void shouldThrowExceptionWhenUpdatingNonExistentQuestion() {
        // Arrange
        User user = TestUtils.createUser();
        Deck deck = TestUtils.createDeck(user);
        QuestionRequest questionRequest = TestUtils.createQuestionRequest();

        when(deckService.getDeckById(1L, user)).thenReturn(deck);
        when(questionRepository.findByDeckAndQuestionId(deck, 1L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> questionService.updateQuestion(1L, 1L, questionRequest, user))
                .isInstanceOf(FlashDashException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.E404003);

        verify(deckService).getDeckById(1L, user);
        verify(questionRepository).findByDeckAndQuestionId(deck, 1L);
    }

    @Test
    void shouldDeleteQuestionSuccessfully() {
        // Arrange
        User user = TestUtils.createUser();
        Deck deck = TestUtils.createDeck(user);
        Question question = TestUtils.createQuestion(deck, "What is Java?");

        when(deckService.getDeckById(1L, user)).thenReturn(deck);
        when(questionRepository.findByDeckAndQuestionId(deck, 1L)).thenReturn(Optional.of(question));

        // Act
        questionService.deleteQuestion(1L, 1L, user);

        // Assert
        verify(deckService).getDeckById(1L, user);
        verify(questionRepository).findByDeckAndQuestionId(deck, 1L);
        verify(questionRepository).delete(question);
    }

    @Test
    void shouldThrowExceptionWhenDeletingNonExistentQuestion() {
        // Arrange
        User user = TestUtils.createUser();
        Deck deck = TestUtils.createDeck(user);
        when(deckService.getDeckById(1L, user)).thenReturn(deck);
        when(questionRepository.findByDeckAndQuestionId(deck, 1L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> questionService.deleteQuestion(1L, 1L, user))
                .isInstanceOf(FlashDashException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.E404003);

        verify(deckService).getDeckById(1L, user);
        verify(questionRepository).findByDeckAndQuestionId(deck, 1L);
    }
}
