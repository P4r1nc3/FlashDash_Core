package com.flashdash.core.controller;

import com.flashdash.core.TestUtils;
import com.flashdash.core.exception.ErrorCode;
import com.flashdash.core.exception.FlashDashException;
import com.flashdash.core.model.Deck;
import com.flashdash.core.model.Question;
import com.flashdash.core.model.User;
import com.flashdash.core.service.QuestionService;
import com.flashdash.core.utils.EntityToResponseMapper;
import com.p4r1nc3.flashdash.core.model.QuestionRequest;
import com.p4r1nc3.flashdash.core.model.QuestionResponse;
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
class QuestionControllerTest {

    private QuestionController questionController;

    @Mock
    private QuestionService questionService;

    @Mock
    private EntityToResponseMapper entityToResponseMapper;

    private User user;
    private MockedStatic<SecurityContextHolder> mockedSecurityContextHolder;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        questionController = new QuestionController(questionService, entityToResponseMapper);

        user = TestUtils.createUser();

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
    void shouldAddQuestionToDeckSuccessfully() {
        // Arrange
        Deck deck = TestUtils.createDeck(user);
        QuestionRequest questionRequest = TestUtils.createQuestionRequest();
        Question question = TestUtils.createQuestion(deck, questionRequest.getQuestion());
        QuestionResponse expectedResponse = new QuestionResponse();

        when(questionService.addQuestionToDeck(eq(deck.getDeckFrn()), any(QuestionRequest.class), eq(user.getUserFrn())))
                .thenReturn(question);
        when(entityToResponseMapper.mapToQuestionResponse(question)).thenReturn(expectedResponse);

        // Act
        ResponseEntity<QuestionResponse> responseEntity = questionController.addQuestionToDeck(deck.getDeckFrn(), questionRequest);

        // Assert
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertEquals(expectedResponse, responseEntity.getBody());
        verify(questionService, times(1)).addQuestionToDeck(eq(deck.getDeckFrn()), any(QuestionRequest.class), eq(user.getUserFrn()));
    }

    @Test
    void shouldGetAllQuestionsInDeckSuccessfully() {
        // Arrange
        Deck deck = TestUtils.createDeck(user);
        List<Question> questions = List.of(
                TestUtils.createQuestion(deck, "What is Java?"),
                TestUtils.createQuestion(deck, "What is Spring?")
        );
        List<QuestionResponse> expectedResponses = List.of(new QuestionResponse(), new QuestionResponse());

        when(questionService.getAllQuestionsInDeck(eq(deck.getDeckFrn()), eq(user.getUserFrn())))
                .thenReturn(questions);
        when(entityToResponseMapper.mapToQuestionResponse(questions)).thenReturn(expectedResponses);

        // Act
        ResponseEntity<List<QuestionResponse>> responseEntity = questionController.getAllQuestionsInDeck(deck.getDeckFrn());

        // Assert
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertEquals(expectedResponses, responseEntity.getBody());
        verify(questionService, times(1)).getAllQuestionsInDeck(eq(deck.getDeckFrn()), eq(user.getUserFrn()));
    }

    @Test
    void shouldGetQuestionSuccessfully() {
        // Arrange
        Deck deck = TestUtils.createDeck(user);
        String deckFrn = deck.getDeckFrn();
        Question question = TestUtils.createQuestion(deck, "What is Java?");
        String questionFrn = question.getQuestionFrn();
        QuestionResponse expectedResponse = new QuestionResponse();

        when(questionService.getQuestionByFrn(eq(deckFrn), eq(questionFrn), eq(user.getUserFrn())))
                .thenReturn(question);
        when(entityToResponseMapper.mapToQuestionResponse(question)).thenReturn(expectedResponse);

        // Act
        ResponseEntity<QuestionResponse> responseEntity = questionController.getQuestion(deckFrn, questionFrn);

        // Assert
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertEquals(expectedResponse, responseEntity.getBody());
        verify(questionService, times(1)).getQuestionByFrn(eq(deckFrn), eq(questionFrn), eq(user.getUserFrn()));
    }

    @Test
    void shouldThrowExceptionWhenGettingNonExistentQuestion() {
        // Arrange
        String deckFrn = "deck-123";
        String questionFrn = "question-456";

        doThrow(new FlashDashException(ErrorCode.E404004, "Question not found"))
                .when(questionService).getQuestionByFrn(eq(deckFrn), eq(questionFrn), eq(user.getUserFrn()));

        // Act & Assert
        FlashDashException exception = assertThrows(
                FlashDashException.class,
                () -> questionController.getQuestion(deckFrn, questionFrn)
        );

        assertEquals(ErrorCode.E404004, exception.getErrorCode());
        assertEquals("Question not found", exception.getMessage());

        verify(questionService, times(1)).getQuestionByFrn(eq(deckFrn), eq(questionFrn), eq(user.getUserFrn()));
    }

    @Test
    void shouldUpdateQuestionSuccessfully() {
        // Arrange
        Deck deck = TestUtils.createDeck(user);
        QuestionRequest questionRequest = TestUtils.createQuestionRequest();
        Question updatedQuestion = TestUtils.createQuestion(deck, "Updated Question?");
        QuestionResponse expectedResponse = new QuestionResponse();

        when(questionService.updateQuestion(eq(deck.getDeckFrn()), eq(updatedQuestion.getQuestionFrn()), any(QuestionRequest.class), eq(user.getUserFrn())))
                .thenReturn(updatedQuestion);
        when(entityToResponseMapper.mapToQuestionResponse(updatedQuestion)).thenReturn(expectedResponse);

        // Act
        ResponseEntity<QuestionResponse> responseEntity = questionController.updateQuestion(deck.getDeckFrn(), updatedQuestion.getQuestionFrn(), questionRequest);

        // Assert
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertEquals(expectedResponse, responseEntity.getBody());
        verify(questionService, times(1)).updateQuestion(eq(deck.getDeckFrn()), eq(updatedQuestion.getQuestionFrn()), any(QuestionRequest.class), eq(user.getUserFrn()));
    }

    @Test
    void shouldThrowExceptionWhenUpdatingNonExistentQuestion() {
        // Arrange
        String deckFrn = "deck-123";
        String questionFrn = "question-456";
        QuestionRequest questionRequest = TestUtils.createQuestionRequest();

        doThrow(new FlashDashException(ErrorCode.E404004, "Question not found"))
                .when(questionService).updateQuestion(eq(deckFrn), eq(questionFrn), any(QuestionRequest.class), eq(user.getUserFrn()));

        // Act & Assert
        FlashDashException exception = assertThrows(
                FlashDashException.class,
                () -> questionController.updateQuestion(deckFrn, questionFrn, questionRequest)
        );

        assertEquals(ErrorCode.E404004, exception.getErrorCode());
        assertEquals("Question not found", exception.getMessage());

        verify(questionService, times(1)).updateQuestion(eq(deckFrn), eq(questionFrn), any(QuestionRequest.class), eq(user.getUserFrn()));
    }

    @Test
    void shouldDeleteQuestionSuccessfully() {
        // Arrange
        String deckFrn = "deck-123";
        String questionFrn = "question-456";

        doNothing().when(questionService).deleteQuestion(eq(deckFrn), eq(questionFrn), eq(user.getUserFrn()));

        // Act
        ResponseEntity<Void> responseEntity = questionController.deleteQuestion(deckFrn, questionFrn);

        // Assert
        assertEquals(HttpStatus.NO_CONTENT, responseEntity.getStatusCode());
        verify(questionService, times(1)).deleteQuestion(eq(deckFrn), eq(questionFrn), eq(user.getUserFrn()));
    }

    @Test
    void shouldThrowExceptionWhenDeletingNonExistentQuestion() {
        // Arrange
        String deckFrn = "deck-123";
        String questionFrn = "question-456";

        doThrow(new FlashDashException(ErrorCode.E404004, "Question not found"))
                .when(questionService).deleteQuestion(eq(deckFrn), eq(questionFrn), eq(user.getUserFrn()));

        // Act & Assert
        FlashDashException exception = assertThrows(
                FlashDashException.class,
                () -> questionController.deleteQuestion(deckFrn, questionFrn)
        );

        assertEquals(ErrorCode.E404004, exception.getErrorCode());
        assertEquals("Question not found", exception.getMessage());

        verify(questionService, times(1)).deleteQuestion(eq(deckFrn), eq(questionFrn), eq(user.getUserFrn()));
    }
}
