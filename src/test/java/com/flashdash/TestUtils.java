package com.flashdash;

import com.flashdash.dto.response.AuthenticationResponse;
import com.flashdash.dto.request.LoginRequest;
import com.flashdash.dto.request.RegisterRequest;
import com.flashdash.model.User;
import com.flashdash.model.Question;
import com.flashdash.model.Deck;
import com.flashdash.model.FriendInvitation;
import org.springframework.mail.SimpleMailMessage;

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

    public static User createFriendUser() {
        User user = new User();
        user.setUsername("friend@example.com");
        user.setPassword("password123");
        user.setFirstName("Friend");
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

    public static Deck createDeck(User user) {
        Deck deck = new Deck();
        deck.setName("Sample Deck");
        deck.setDescription("This is a sample deck.");
        deck.setCreatedAt(LocalDateTime.now());
        deck.setUpdatedAt(LocalDateTime.now());
        deck.setUser(user);
        return deck;
    }

    public static Question createQuestion(Deck deck, String content) {
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

    public static FriendInvitation createFriendInvitation(User sender, User recipient) {
        FriendInvitation invitation = new FriendInvitation();
        invitation.setSentBy(sender);
        invitation.setSentTo(recipient);
        return invitation;
    }

    public static SimpleMailMessage createSimpleMailMessageForAccountActivation(String to, String activationToken) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject("Account Activation");
        message.setText("Click the link to activate your account: http://localhost:8080/auth/activate?token=" + activationToken);
        message.setFrom("flashdashservice@gmail.com");
        return message;
    }

    public static SimpleMailMessage createSimpleMailMessageForDailyNotification(String to) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject("Reminder: Time to Learn!");
        message.setText("Hi there! Don't forget to continue your learning journey with FlashDash. Let's make today productive!");
        message.setFrom("flashdashservice@gmail.com");
        return message;
    }

    public static SimpleMailMessage createSimpleMailMessageForFriendInvitation(String to, String senderFirstName, String senderLastName) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject("You have a new Friend Invitation!");
        message.setText("Hi there! You've received a new friend invitation from " + senderFirstName + " " + senderLastName + ". Visit the FlashDash app to accept or decline.");
        message.setFrom("flashdashservice@gmail.com");
        return message;
    }
}
