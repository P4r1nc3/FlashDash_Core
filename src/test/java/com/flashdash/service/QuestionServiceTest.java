package com.flashdash.service;

import com.flashdash.FlashDashApplication;
import com.flashdash.TestUtils;
import com.flashdash.exception.FlashDashException;
import com.flashdash.exception.ErrorCode;
import com.flashdash.model.User;
import com.flashdash.model.question.Question;
import com.flashdash.model.question.QuestionDeck;
import com.flashdash.model.Deck;
import com.flashdash.repository.QuestionRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

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

    @MockBean
    private QuestionRepository questionRepository;

    @MockBean
    private DeckService deckService;

    @Test
    void shouldAddQuestionToDeckSuccessfully() {
        // Arrange
        User user = TestUtils.createUser();
        QuestionDeck questionDeck = TestUtils.createQuestionDeck(user);
        Question question = TestUtils.createQuestion(questionDeck, "What is Java?");

        when(deckService.getDeckById(1L, user)).thenReturn(questionDeck);
        when(questionRepository.save(question)).thenReturn(question);

        // Act
        Question createdQuestion = questionService.addQuestionToDeck(1L, question, user);

        // Assert
        assertThat(createdQuestion).isNotNull();
        assertThat(createdQuestion).isEqualTo(question);
        verify(deckService).getDeckById(1L, user);
        verify(questionRepository).save(question);
    }

    @Test
    void shouldThrowExceptionWhenAddingQuestionToNonQuestionDeck() {
        // Arrange
        User user = TestUtils.createUser();
        Deck nonQuestionDeck = TestUtils.createCardDeck(user);
        Question question = TestUtils.createQuestion(null, "What is Java?");

        when(deckService.getDeckById(1L, user)).thenReturn(nonQuestionDeck);

        // Act & Assert
        assertThatThrownBy(() -> questionService.addQuestionToDeck(1L, question, user))
                .isInstanceOf(FlashDashException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.E400003);

        verify(deckService).getDeckById(1L, user);
        verifyNoInteractions(questionRepository);
    }

    @Test
    void shouldGetAllQuestionsInDeckSuccessfully() {
        // Arrange
        User user = TestUtils.createUser();
        QuestionDeck questionDeck = TestUtils.createQuestionDeck(user);
        List<Question> questions = List.of(
                TestUtils.createQuestion(questionDeck, "What is Java?"),
                TestUtils.createQuestion(questionDeck, "What is Spring?")
        );

        when(deckService.getDeckById(1L, user)).thenReturn(questionDeck);
        when(questionRepository.findAllByDeck(questionDeck)).thenReturn(questions);

        // Act
        List<Question> retrievedQuestions = questionService.getAllQuestionsInDeck(1L, user);

        // Assert
        assertThat(retrievedQuestions).isNotEmpty();
        assertThat(retrievedQuestions).isEqualTo(questions);
        verify(deckService).getDeckById(1L, user);
        verify(questionRepository).findAllByDeck(questionDeck);
    }

    @Test
    void shouldGetQuestionByIdSuccessfully() {
        // Arrange
        User user = TestUtils.createUser();
        QuestionDeck questionDeck = TestUtils.createQuestionDeck(user);
        Question question = TestUtils.createQuestion(questionDeck, "What is Java?");

        when(deckService.getDeckById(1L, user)).thenReturn(questionDeck);
        when(questionRepository.findByDeckAndQuestionId(questionDeck, 1L)).thenReturn(Optional.of(question));

        // Act
        Question retrievedQuestion = questionService.getQuestionById(1L, 1L, user);

        // Assert
        assertThat(retrievedQuestion).isEqualTo(question);
        verify(deckService).getDeckById(1L, user);
        verify(questionRepository).findByDeckAndQuestionId(questionDeck, 1L);
    }

    @Test
    void shouldThrowExceptionWhenQuestionNotFoundById() {
        // Arrange
        User user = TestUtils.createUser();
        QuestionDeck questionDeck = TestUtils.createQuestionDeck(user);

        when(deckService.getDeckById(1L, user)).thenReturn(questionDeck);
        when(questionRepository.findByDeckAndQuestionId(questionDeck, 1L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> questionService.getQuestionById(1L, 1L, user))
                .isInstanceOf(FlashDashException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.E404004);

        verify(deckService).getDeckById(1L, user);
        verify(questionRepository).findByDeckAndQuestionId(questionDeck, 1L);
    }

    @Test
    void shouldUpdateQuestionSuccessfully() {
        // Arrange
        User user = TestUtils.createUser();
        QuestionDeck questionDeck = TestUtils.createQuestionDeck(user);
        Question question = TestUtils.createQuestion(questionDeck, "What is Java?");
        Question updatedDetails = TestUtils.createQuestion(questionDeck, "Updated Question");

        when(deckService.getDeckById(1L, user)).thenReturn(questionDeck);
        when(questionRepository.findByDeckAndQuestionId(questionDeck, 1L)).thenReturn(Optional.of(question));
        when(questionRepository.save(question)).thenReturn(question);

        // Act
        Question updatedQuestion = questionService.updateQuestion(1L, 1L, updatedDetails, user);

        // Assert
        assertThat(updatedQuestion.getQuestion()).isEqualTo("Updated Question");
        verify(deckService).getDeckById(1L, user);
        verify(questionRepository).findByDeckAndQuestionId(questionDeck, 1L);
        verify(questionRepository).save(question);
    }

    @Test
    void shouldThrowExceptionWhenUpdatingNonExistentQuestion() {
        // Arrange
        User user = TestUtils.createUser();
        QuestionDeck questionDeck = TestUtils.createQuestionDeck(user);
        Question updatedDetails = TestUtils.createQuestion(questionDeck, "Updated Question");

        when(deckService.getDeckById(1L, user)).thenReturn(questionDeck);
        when(questionRepository.findByDeckAndQuestionId(questionDeck, 1L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> questionService.updateQuestion(1L, 1L, updatedDetails, user))
                .isInstanceOf(FlashDashException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.E404004);

        verify(deckService).getDeckById(1L, user);
        verify(questionRepository).findByDeckAndQuestionId(questionDeck, 1L);
    }

    @Test
    void shouldDeleteQuestionSuccessfully() {
        // Arrange
        User user = TestUtils.createUser();
        QuestionDeck questionDeck = TestUtils.createQuestionDeck(user);
        Question question = TestUtils.createQuestion(questionDeck, "What is Java?");

        when(deckService.getDeckById(1L, user)).thenReturn(questionDeck);
        when(questionRepository.findByDeckAndQuestionId(questionDeck, 1L)).thenReturn(Optional.of(question));
        doNothing().when(questionRepository).delete(question);

        // Act
        questionService.deleteQuestion(1L, 1L, user);

        // Assert
        verify(deckService).getDeckById(1L, user);
        verify(questionRepository).findByDeckAndQuestionId(questionDeck, 1L);
        verify(questionRepository).delete(question);
    }

    @Test
    void shouldThrowExceptionWhenDeletingNonExistentQuestion() {
        // Arrange
        User user = TestUtils.createUser();
        QuestionDeck questionDeck = TestUtils.createQuestionDeck(user);

        when(deckService.getDeckById(1L, user)).thenReturn(questionDeck);
        when(questionRepository.findByDeckAndQuestionId(questionDeck, 1L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> questionService.deleteQuestion(1L, 1L, user))
                .isInstanceOf(FlashDashException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.E404004);

        verify(deckService).getDeckById(1L, user);
        verify(questionRepository).findByDeckAndQuestionId(questionDeck, 1L);
    }
}
