package com.flashdash.controller;

import com.flashdash.FlashDashApplication;
import com.flashdash.TestUtils;
import com.flashdash.exception.ErrorCode;
import com.flashdash.exception.FlashDashException;
import com.flashdash.model.User;
import com.flashdash.model.Deck;
import com.flashdash.model.Question;
import com.flashdash.service.QuestionService;
import com.flashdash.utils.EntityToResponseMapper;
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
class QuestionControllerTest {

    @Autowired
    private QuestionController questionController;

    @MockitoBean
    private QuestionService questionService;

    private User user;
    private MockedStatic<SecurityContextHolder> mockedSecurityContextHolder;

    @BeforeEach
    void setUp() {
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
    @Order(1)
    void testAddQuestionToDeckSuccessful() {
        // Arrange
        Deck deck = TestUtils.createDeck(user);
        QuestionRequest questionRequest = TestUtils.createQuestionRequest();
        Question question = TestUtils.createQuestion(deck, questionRequest.getQuestion());
        QuestionResponse expectedResponse = EntityToResponseMapper.toQuestionResponse(question);

        when(questionService.addQuestionToDeck(eq(1L), any(QuestionRequest.class), eq(user))).thenReturn(question);

        // Act
        ResponseEntity<QuestionResponse> responseEntity = questionController.addQuestionToDeck(1L, questionRequest);

        // Assert
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertEquals(expectedResponse, responseEntity.getBody());
    }

    @Test
    @Order(2)
    void testGetAllQuestionsInDeckSuccessful() {
        // Arrange
        Deck deck = TestUtils.createDeck(user);
        List<Question> questions = List.of(
                TestUtils.createQuestion(deck, "What is Java?"),
                TestUtils.createQuestion(deck, "What is Spring?")
        );
        List<QuestionResponse> expectedResponses = EntityToResponseMapper.toQuestionResponseList(questions);

        when(questionService.getAllQuestionsInDeck(eq(1L), eq(user))).thenReturn(questions);

        // Act
        ResponseEntity<List<QuestionResponse>> responseEntity = questionController.getAllQuestionsInDeck(1L);

        // Assert
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertEquals(expectedResponses, responseEntity.getBody());
    }

    @Test
    @Order(3)
    void testGetQuestionSuccessful() {
        // Arrange
        Deck deck = TestUtils.createDeck(user);
        Question question = TestUtils.createQuestion(deck, "What is Java?");
        QuestionResponse expectedResponse = EntityToResponseMapper.toQuestionResponse(question);

        when(questionService.getQuestionById(eq(1L), eq(1L), eq(user))).thenReturn(question);

        // Act
        ResponseEntity<QuestionResponse> responseEntity = questionController.getQuestion(1L, 1L);

        // Assert
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertEquals(expectedResponse, responseEntity.getBody());
    }

    @Test
    @Order(4)
    void testGetQuestionNotFound() {
        // Arrange
        doThrow(new FlashDashException(ErrorCode.E404004, "Question not found"))
                .when(questionService).getQuestionById(eq(1L), eq(1L), eq(user));

        // Act & Assert
        FlashDashException exception = assertThrows(
                FlashDashException.class,
                () -> questionController.getQuestion(1L, 1L)
        );
        assertEquals(ErrorCode.E404004, exception.getErrorCode());
        assertEquals("Question not found", exception.getMessage());
    }

    @Test
    @Order(5)
    void testUpdateQuestionSuccessful() {
        // Arrange
        Deck deck = TestUtils.createDeck(user);
        QuestionRequest questionRequest = TestUtils.createQuestionRequest();
        Question updatedQuestion = TestUtils.createQuestion(deck, "Updated Question?");
        QuestionResponse expectedResponse = EntityToResponseMapper.toQuestionResponse(updatedQuestion);

        when(questionService.updateQuestion(eq(1L), eq(1L), any(QuestionRequest.class), eq(user))).thenReturn(updatedQuestion);

        // Act
        ResponseEntity<QuestionResponse> responseEntity = questionController.updateQuestion(1L, 1L, questionRequest);

        // Assert
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertEquals(expectedResponse, responseEntity.getBody());
    }

    @Test
    @Order(6)
    void testUpdateQuestionNotFound() {
        // Arrange
        QuestionRequest questionRequest = TestUtils.createQuestionRequest();
        doThrow(new FlashDashException(ErrorCode.E404004, "Question not found"))
                .when(questionService).updateQuestion(eq(1L), eq(1L), any(QuestionRequest.class), eq(user));

        // Act & Assert
        FlashDashException exception = assertThrows(
                FlashDashException.class,
                () -> questionController.updateQuestion(1L, 1L, questionRequest)
        );
        assertEquals(ErrorCode.E404004, exception.getErrorCode());
        assertEquals("Question not found", exception.getMessage());
    }

    @Test
    @Order(7)
    void testDeleteQuestionSuccessful() {
        // Arrange
        doNothing().when(questionService).deleteQuestion(eq(1L), eq(1L), eq(user));

        // Act
        ResponseEntity<Void> responseEntity = questionController.deleteQuestion(1L, 1L);

        // Assert
        assertEquals(HttpStatus.NO_CONTENT, responseEntity.getStatusCode());
        verify(questionService, times(1)).deleteQuestion(eq(1L), eq(1L), eq(user));
    }

    @Test
    @Order(8)
    void testDeleteQuestionNotFound() {
        // Arrange
        doThrow(new FlashDashException(ErrorCode.E404004, "Question not found"))
                .when(questionService).deleteQuestion(eq(1L), eq(1L), eq(user));

        // Act & Assert
        FlashDashException exception = assertThrows(
                FlashDashException.class,
                () -> questionController.deleteQuestion(1L, 1L)
        );
        assertEquals(ErrorCode.E404004, exception.getErrorCode());
        assertEquals("Question not found", exception.getMessage());
    }
}
