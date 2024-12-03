package com.flashdash;

import com.flashdash.dto.AuthenticationResponse;
import com.flashdash.dto.LoginRequest;
import com.flashdash.dto.RegisterRequest;
import com.flashdash.model.User;
import com.flashdash.model.question.Question;
import com.flashdash.model.question.QuestionDeck;

import java.time.LocalDateTime;
import java.util.List;

public class TestUtils {

    public static User createUser() {
        User user = new User();
        user.setUsername("test@example.com");
        user.setPassword("password123");
        user.setFirstName("Test");
        user.setLastName("User");
        return user;
    }

    public static LoginRequest createLoginRequest() {
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setEmail("test@example.com");
        loginRequest.setPassword("password123");
        return loginRequest;
    }

    public static RegisterRequest createRegisterRequest() {
        RegisterRequest registerRequest = new RegisterRequest();
        registerRequest.setFirstName("Test");
        registerRequest.setLastName("User");
        registerRequest.setEmail("test@example.com");
        registerRequest.setPassword("password123");
        return registerRequest;
    }

    public static AuthenticationResponse createAuthenticationResponse() {
        AuthenticationResponse response = new AuthenticationResponse();
        response.setToken("mockToken");
        return response;
    }

    public static QuestionDeck createQuestionDeck(User user) {
        QuestionDeck deck = new QuestionDeck();
        deck.setName("Sample Question Deck");
        deck.setDescription("This is a sample question deck.");
        deck.setCreatedAt(LocalDateTime.now());
        deck.setUpdatedAt(LocalDateTime.now());
        deck.setUser(user);
        deck.setQuestions(new java.util.HashSet<>());
        return deck;
    }

    public static Question createQuestion(QuestionDeck deck, String content) {
        Question question = new Question();
        question.setQuestion(content);
        question.setCorrectAnswers(List.of("Correct Answer 1", "Correct Answer 2"));
        question.setIncorrectAnswers(List.of("Incorrect Answer 1", "Incorrect Answer 2"));
        question.setDifficulty("Medium");
        question.setCreatedAt(LocalDateTime.now());
        question.setUpdatedAt(LocalDateTime.now());
        question.setDeck(deck);
        return question;
    }
}
