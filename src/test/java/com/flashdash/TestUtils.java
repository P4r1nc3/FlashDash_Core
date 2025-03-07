package com.flashdash;

import com.flashdash.model.*;
import com.flashdash.utils.FrnGenerator;
import com.flashdash.utils.ResourceType;
import com.p4r1nc3.flashdash.core.model.*;
import org.springframework.mail.SimpleMailMessage;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public class TestUtils {

    public static RegisterRequest createRegisterRequest(User user) {
        RegisterRequest registerRequest = new RegisterRequest();
        registerRequest.setFirstName(user.getFirstName());
        registerRequest.setLastName(user.getLastName());
        registerRequest.setEmail(user.getEmail());
        registerRequest.setPassword(user.getPassword());
        return registerRequest;
    }

    public static LoginRequest createLoginRequest(User user) {
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setEmail(user.getUsername());
        loginRequest.setPassword(user.getPassword());
        return loginRequest;
    }

    public static AuthenticationResponse createAuthenticationResponse() {
        AuthenticationResponse response = new AuthenticationResponse();
        response.setToken("mockToken");
        return response;
    }

    public static User createUser() {
        User user = new User();
        user.setUserFrn(FrnGenerator.generateFrn(ResourceType.USER));
        user.setEmail("user" + UUID.randomUUID() + "@example.com");
        user.setPassword("password123");
        user.setFirstName("Test");
        user.setLastName("User");
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());
        user.setEnabled(true);
        user.setDailyNotifications(true);
        user.setFriendsFrnList(List.of());
        user.setGamesPlayed(10);
        user.setPoints(140);
        user.setStreak(4);
        return user;
    }

    public static Activity createActivity(String userFrn, String targetFrn, ActivityType activityType) {
        Activity activity = new Activity();
        activity.setActivityFrn(FrnGenerator.generateFrn(ResourceType.ACTIVITY));
        activity.setUserFrn(userFrn);
        activity.setTargetFrn(targetFrn);
        activity.setActivity(activityType);
        activity.setCreatedAt(LocalDateTime.now());
        activity.setUpdatedAt(LocalDateTime.now());
        return activity;
    }

    public static FriendInvitation createFriendInvitation(User sender, User recipient) {
        FriendInvitation invitation = new FriendInvitation();
        invitation.setInvitationFrn(FrnGenerator.generateFrn(ResourceType.INVITATION));
        invitation.setSentByFrn(sender.getUserFrn());
        invitation.setSentToFrn(recipient.getUserFrn());
        invitation.setStatus("PENDING");
        invitation.setCreatedAt(LocalDateTime.now());
        invitation.setUpdatedAt(LocalDateTime.now());
        return invitation;
    }

    public static DeckRequest createDeckRequest() {
        DeckRequest deckRequest = new DeckRequest();
        deckRequest.setName("Sample Deck");
        deckRequest.setDescription("This is a sample deck.");
        return deckRequest;
    }

    public static Deck createDeck(User user) {
        Deck deck = new Deck();
        deck.setDeckFrn(FrnGenerator.generateFrn(ResourceType.DECK));
        deck.setUserFrn(user.getUserFrn());
        deck.setName("Sample Deck");
        deck.setDescription("This is a sample deck.");
        deck.setCreatedAt(LocalDateTime.now());
        deck.setUpdatedAt(LocalDateTime.now());
        return deck;
    }

    public static QuestionRequest createQuestionRequest() {
        QuestionRequest questionRequest = new QuestionRequest();
        questionRequest.setQuestion("What is Java?");
        questionRequest.setCorrectAnswers(List.of("A programming language", "A platform"));
        questionRequest.setIncorrectAnswers(List.of("A drink", "A city"));
        questionRequest.setDifficulty(QuestionRequest.DifficultyEnum.MEDIUM);
        return questionRequest;
    }

    public static Question createQuestion(Deck deck, String content) {
        Question question = new Question();
        question.setQuestionFrn(FrnGenerator.generateFrn(ResourceType.QUESTION));
        question.setDeckFrn(deck.getDeckFrn());
        question.setQuestion(content);
        question.setCorrectAnswers(List.of("Correct Answer 1", "Correct Answer 2"));
        question.setIncorrectAnswers(List.of("Incorrect Answer 1", "Incorrect Answer 2"));
        question.setDifficulty("medium");
        question.setCreatedAt(LocalDateTime.now());
        question.setUpdatedAt(LocalDateTime.now());
        return question;
    }

    public static GameSession createGameSession(User user, Deck deck, String status) {
        GameSession session = new GameSession();
        session.setGameSessionFrn(FrnGenerator.generateFrn(ResourceType.GAME_SESSION));
        session.setUserFrn(user.getUserFrn());
        session.setDeckFrn(deck.getDeckFrn());
        session.setStatus(status);
        session.setTotalScore(0);
        session.setCorrectAnswersCount(0);
        session.setWrongAnswersCount(0);
        session.setQuestionCount(0);
        session.setStartTime(LocalDateTime.now());
        session.setEndTime(null);
        session.setCreatedAt(LocalDateTime.now());
        session.setUpdatedAt(LocalDateTime.now());
        return session;
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
