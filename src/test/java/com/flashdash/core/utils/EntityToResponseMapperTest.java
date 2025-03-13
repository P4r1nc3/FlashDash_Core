package com.flashdash.core.utils;

import com.flashdash.core.FlashDashCoreApplication;
import com.flashdash.core.model.*;
import com.flashdash.core.repository.UserRepository;
import com.p4r1nc3.flashdash.core.model.*;
import com.flashdash.core.TestUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@SpringBootTest(classes = FlashDashCoreApplication.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class EntityToResponseMapperTest {

    @Autowired
    private EntityToResponseMapper mapper;
    @MockitoBean
    private UserRepository userRepository;


    @Test
    void shouldConvertDeckToDeckResponse() {
        // Arrange
        User user = TestUtils.createUser();
        Deck deck = TestUtils.createDeck(user);

        // Act
        DeckResponse deckResponse = mapper.mapToDeckResponse(deck);

        // Assert
        assertThat(deckResponse).isNotNull();
        assertThat(deckResponse.getDeckId()).isEqualTo(mapper.extractId(deck.getDeckFrn()));
        assertThat(deckResponse.getDeckFrn()).isEqualTo(deck.getDeckFrn());
        assertThat(deckResponse.getName()).isEqualTo(deck.getName());
        assertThat(deckResponse.getDescription()).isEqualTo(deck.getDescription());
        assertThat(deckResponse.getCreatedAt()).isEqualTo(deck.getCreatedAt().atOffset(ZoneOffset.UTC));
        assertThat(deckResponse.getUpdatedAt()).isEqualTo(deck.getUpdatedAt().atOffset(ZoneOffset.UTC));
    }

    @Test
    void shouldConvertDeckListToDeckResponseList() {
        // Arrange
        User user = TestUtils.createUser();
        List<Deck> decks = List.of(TestUtils.createDeck(user));

        // Act
        List<DeckResponse> deckResponses = mapper.mapToDeckResponse(decks);

        // Assert
        assertThat(deckResponses).isNotEmpty();
        assertThat(deckResponses).hasSize(decks.size());
        assertThat(deckResponses.get(0).getDeckId()).isEqualTo(mapper.extractId(decks.get(0).getDeckFrn()));
        assertThat(deckResponses.get(0).getName()).isEqualTo(decks.get(0).getName());
        assertThat(deckResponses.get(0).getDescription()).isEqualTo(decks.get(0).getDescription());
    }

    @Test
    void shouldConvertQuestionToQuestionResponse() {
        // Arrange
        User user = TestUtils.createUser();
        Deck deck = TestUtils.createDeck(user);
        Question question = TestUtils.createQuestion(deck, "What is Java?");

        // Act
        QuestionResponse questionResponse = mapper.mapToQuestionResponse(question);

        // Assert
        assertThat(questionResponse).isNotNull();
        assertThat(questionResponse.getQuestionId()).isEqualTo(mapper.extractId(question.getQuestionFrn()));
        assertThat(questionResponse.getQuestionFrn()).isEqualTo(question.getQuestionFrn());
        assertThat(questionResponse.getQuestion()).isEqualTo(question.getQuestion());
        assertThat(questionResponse.getCorrectAnswers()).isEqualTo(question.getCorrectAnswers());
        assertThat(questionResponse.getIncorrectAnswers()).isEqualTo(question.getIncorrectAnswers());
        assertThat(questionResponse.getDifficulty()).isEqualTo(QuestionResponse.DifficultyEnum.fromValue(question.getDifficulty()));
        assertThat(questionResponse.getCreatedAt()).isEqualTo(question.getCreatedAt().atOffset(ZoneOffset.UTC));
        assertThat(questionResponse.getUpdatedAt()).isEqualTo(question.getUpdatedAt().atOffset(ZoneOffset.UTC));
    }

    @Test
    void shouldConvertQuestionListToQuestionResponseList() {
        // Arrange
        User user = TestUtils.createUser();
        Deck deck = TestUtils.createDeck(user);
        List<Question> questions = List.of(
                TestUtils.createQuestion(deck, "What is Java?"),
                TestUtils.createQuestion(deck, "What is Spring Boot?")
        );

        // Act
        List<QuestionResponse> questionResponses = mapper.mapToQuestionResponse(questions);

        // Assert
        assertThat(questionResponses).isNotEmpty();
        assertThat(questionResponses).hasSize(questions.size());

        for (int i = 0; i < questions.size(); i++) {
            assertThat(questionResponses.get(i).getQuestionId()).isEqualTo(mapper.extractId(questions.get(i).getQuestionFrn()));
            assertThat(questionResponses.get(i).getQuestion()).isEqualTo(questions.get(i).getQuestion());
            assertThat(questionResponses.get(i).getCorrectAnswers()).isEqualTo(questions.get(i).getCorrectAnswers());
            assertThat(questionResponses.get(i).getIncorrectAnswers()).isEqualTo(questions.get(i).getIncorrectAnswers());
            assertThat(questionResponses.get(i).getDifficulty()).isEqualTo(QuestionResponse.DifficultyEnum.fromValue(questions.get(i).getDifficulty()));
            assertThat(questionResponses.get(i).getCreatedAt()).isEqualTo(questions.get(i).getCreatedAt().atOffset(ZoneOffset.UTC));
            assertThat(questionResponses.get(i).getUpdatedAt()).isEqualTo(questions.get(i).getUpdatedAt().atOffset(ZoneOffset.UTC));
        }
    }

    @Test
    void shouldConvertGameSessionToGameSessionResponse() {
        // Arrange
        User user = TestUtils.createUser();
        Deck deck = TestUtils.createDeck(user);

        LocalDateTime startTime = LocalDateTime.now().minusMinutes(30).minusSeconds(10);
        LocalDateTime endTime = LocalDateTime.now();

        GameSession gameSession = TestUtils.createGameSession(user, deck, "FINISHED");
        gameSession.setCorrectAnswersCount(5);
        gameSession.setQuestionCount(10);
        gameSession.setTotalScore(50);
        gameSession.setStartTime(startTime);
        gameSession.setEndTime(endTime);
        gameSession.setCreatedAt(startTime);

        // Act
        GameSessionResponse response = mapper.mapToGameSessionResponse(gameSession);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.getGameSessionId()).isEqualTo(mapper.extractId(gameSession.getGameSessionFrn()));
        assertThat(response.getGameSessionFrn()).isEqualTo(gameSession.getGameSessionFrn());
        assertThat(response.getScore()).isEqualTo(50);
        assertThat(response.getCorrectAnswers()).isEqualTo(5);
        assertThat(response.getTotalQuestions()).isEqualTo(10);
        assertThat(response.getAccuracy()).isEqualTo(50.0f);
        assertThat(response.getDuration()).isEqualTo("30 min 10 sec");
        assertThat(response.getStartTime()).isEqualTo(startTime.atOffset(ZoneOffset.UTC));
        assertThat(response.getEndTime()).isEqualTo(endTime.atOffset(ZoneOffset.UTC));
    }


    @Test
    void shouldConvertGameSessionListToGameSessionResponseList() {
        // Arrange
        User user = TestUtils.createUser();
        Deck deck = TestUtils.createDeck(user);

        LocalDateTime startTime1 = LocalDateTime.now().minusMinutes(25).minusSeconds(15);
        LocalDateTime endTime1 = LocalDateTime.now().minusMinutes(5);

        LocalDateTime startTime2 = LocalDateTime.now().minusMinutes(40).minusSeconds(30);
        LocalDateTime endTime2 = LocalDateTime.now().minusMinutes(10);

        GameSession gameSession1 = TestUtils.createGameSession(user, deck, "FINISHED");
        gameSession1.setStartTime(startTime1);
        gameSession1.setEndTime(endTime1);
        gameSession1.setCreatedAt(startTime1);
        gameSession1.setCorrectAnswersCount(6);
        gameSession1.setQuestionCount(12);
        gameSession1.setTotalScore(60);

        GameSession gameSession2 = TestUtils.createGameSession(user, deck, "FINISHED");
        gameSession2.setStartTime(startTime2);
        gameSession2.setEndTime(endTime2);
        gameSession2.setCreatedAt(startTime2);
        gameSession2.setCorrectAnswersCount(8);
        gameSession2.setQuestionCount(16);
        gameSession2.setTotalScore(80);

        List<GameSession> gameSessions = List.of(gameSession1, gameSession2);

        // Act
        List<GameSessionResponse> responses = mapper.mapToGameSessionResponse(gameSessions);

        // Assert
        assertThat(responses).isNotEmpty();
        assertThat(responses).hasSize(gameSessions.size());

        GameSessionResponse response1 = responses.get(0);
        GameSessionResponse response2 = responses.get(1);

        assertThat(response1.getGameSessionId()).isEqualTo(mapper.extractId(gameSession1.getGameSessionFrn()));
        assertThat(response1.getGameSessionFrn()).isEqualTo(gameSession1.getGameSessionFrn());
        assertThat(response1.getScore()).isEqualTo(60);
        assertThat(response1.getCorrectAnswers()).isEqualTo(6);
        assertThat(response1.getTotalQuestions()).isEqualTo(12);
        assertThat(response1.getAccuracy()).isEqualTo(50.0f);
        assertThat(response1.getDuration()).isEqualTo("20 min 15 sec");
        assertThat(response1.getStartTime()).isEqualTo(startTime1.atOffset(ZoneOffset.UTC));
        assertThat(response1.getEndTime()).isEqualTo(endTime1.atOffset(ZoneOffset.UTC));

        assertThat(response2.getGameSessionId()).isEqualTo(mapper.extractId(gameSession2.getGameSessionFrn()));
        assertThat(response2.getGameSessionFrn()).isEqualTo(gameSession2.getGameSessionFrn());
        assertThat(response2.getScore()).isEqualTo(80);
        assertThat(response2.getCorrectAnswers()).isEqualTo(8);
        assertThat(response2.getTotalQuestions()).isEqualTo(16);
        assertThat(response2.getAccuracy()).isEqualTo(50.0f);
        assertThat(response2.getDuration()).isEqualTo("30 min 30 sec");
        assertThat(response2.getStartTime()).isEqualTo(startTime2.atOffset(ZoneOffset.UTC));
        assertThat(response2.getEndTime()).isEqualTo(endTime2.atOffset(ZoneOffset.UTC));
    }

    @Test
    void shouldConvertFriendInvitationToReceivedResponse() {
        User sender = TestUtils.createUser();
        User recipient = TestUtils.createUser();
        FriendInvitation invitation = TestUtils.createFriendInvitation(sender, recipient);

        when(userRepository.findById(invitation.getSentByFrn())).thenReturn(Optional.of(sender));

        FriendInvitationResponseReceived response = mapper.mapToReceivedResponse(invitation);

        assertThat(response).isNotNull();
        assertThat(response.getSender()).isNotNull();
        assertThat(response.getSender().getFirstName()).isEqualTo(sender.getFirstName());
    }

    @Test
    void shouldConvertFriendInvitationListToReceivedResponseList() {
        User sender = TestUtils.createUser();
        User recipient = TestUtils.createUser();
        List<FriendInvitation> invitations = List.of(TestUtils.createFriendInvitation(sender, recipient));

        when(userRepository.findById(any())).thenReturn(Optional.of(sender));

        List<FriendInvitationResponseReceived> responses = mapper.mapToReceivedResponse(invitations);

        assertThat(responses).hasSize(invitations.size());
    }

    @Test
    void shouldConvertFriendInvitationToSentResponse() {
        User sender = TestUtils.createUser();
        User recipient = TestUtils.createUser();
        FriendInvitation invitation = TestUtils.createFriendInvitation(sender, recipient);

        when(userRepository.findById(invitation.getSentToFrn())).thenReturn(Optional.of(recipient));

        FriendInvitationResponseSent response = mapper.mapToSentResponse(invitation);

        assertThat(response).isNotNull();
        assertThat(response.getRecipient()).isNotNull();
        assertThat(response.getRecipient().getFirstName()).isEqualTo(recipient.getFirstName());
    }

    @Test
    void shouldConvertFriendInvitationListToSentResponseList() {
        User sender = TestUtils.createUser();
        User recipient = TestUtils.createUser();
        List<FriendInvitation> invitations = List.of(TestUtils.createFriendInvitation(sender, recipient));

        when(userRepository.findById(any())).thenReturn(Optional.of(recipient));

        List<FriendInvitationResponseSent> responses = mapper.mapToSentResponse(invitations);

        assertThat(responses).hasSize(invitations.size());
    }

    @Test
    void shouldConvertUserToUserResponse() {
        // Arrange
        User user = TestUtils.createUser();

        // Act
        UserResponse response = mapper.mapToUserResponse(user);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.getUserId()).isEqualTo(mapper.extractId(user.getUserFrn()));
        assertThat(response.getUserFrn()).isEqualTo(user.getUserFrn());
        assertThat(response.getEmail()).isEqualTo(user.getEmail());
        assertThat(response.getUsername()).isEqualTo(user.getUsername());
        assertThat(response.getFirstName()).isEqualTo(user.getFirstName());
        assertThat(response.getLastName()).isEqualTo(user.getLastName());
        assertThat(response.getCreatedAt()).isEqualTo(user.getCreatedAt().atOffset(ZoneOffset.UTC));
        assertThat(response.getUpdatedAt()).isEqualTo(user.getUpdatedAt().atOffset(ZoneOffset.UTC));
        assertThat(response.getDailyNotifications()).isEqualTo(user.isDailyNotifications());
        assertThat(response.getGamesPlayed()).isEqualTo(user.getGamesPlayed());
        assertThat(response.getPoints()).isEqualTo(user.getPoints());
        assertThat(response.getStreak()).isEqualTo(user.getStreak());
    }

    @Test
    void shouldConvertUserToFriendResponse() {
        // Arrange
        User user = TestUtils.createUser();

        // Act
        FriendResponse response = mapper.mapToFriendResponse(user);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.getUserId()).isEqualTo(mapper.extractId(user.getUserFrn()));
        assertThat(response.getUserFrn()).isEqualTo(user.getUserFrn());
        assertThat(response.getUsername()).isEqualTo(user.getUsername());
        assertThat(response.getFirstName()).isEqualTo(user.getFirstName());
        assertThat(response.getLastName()).isEqualTo(user.getLastName());
        assertThat(response.getGamesPlayed()).isEqualTo(user.getGamesPlayed());
        assertThat(response.getPoints()).isEqualTo(user.getPoints());
        assertThat(response.getStreak()).isEqualTo(user.getStreak());
    }

    @Test
    void shouldConvertUserListToFriendResponseList() {
        // Arrange
        User user1 = TestUtils.createUser();
        User user2 = TestUtils.createUser();
        List<User> users = List.of(user1, user2);

        // Act
        List<FriendResponse> friendResponses = mapper.mapToFriendResponse(users);

        // Assert
        assertThat(friendResponses).isNotEmpty();
        assertThat(friendResponses).hasSize(users.size());

        for (int i = 0; i < users.size(); i++) {
            assertThat(friendResponses.get(i).getUserFrn()).isEqualTo(users.get(i).getUserFrn());
            assertThat(friendResponses.get(i).getUsername()).isEqualTo(users.get(i).getUsername());
            assertThat(friendResponses.get(i).getFirstName()).isEqualTo(users.get(i).getFirstName());
            assertThat(friendResponses.get(i).getLastName()).isEqualTo(users.get(i).getLastName());
            assertThat(friendResponses.get(i).getGamesPlayed()).isEqualTo(users.get(i).getGamesPlayed());
            assertThat(friendResponses.get(i).getPoints()).isEqualTo(users.get(i).getPoints());
            assertThat(friendResponses.get(i).getStreak()).isEqualTo(users.get(i).getStreak());
        }
    }
}
