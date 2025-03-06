package com.flashdash.utils;

import com.flashdash.FlashDashApplication;
import com.flashdash.model.*;
import com.flashdash.repository.UserRepository;
import com.p4r1nc3.flashdash.core.model.*;
import com.flashdash.TestUtils;
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

@SpringBootTest(classes = FlashDashApplication.class)
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
        DeckResponse deckResponse = mapper.toDeckResponse(deck);

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
        List<DeckResponse> deckResponses = mapper.toDeckResponseList(decks);

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
        QuestionResponse questionResponse = mapper.toQuestionResponse(question);

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
        List<QuestionResponse> questionResponses = mapper.toQuestionResponseList(questions);

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
        GameSession gameSession = TestUtils.createGameSession(user, deck, "FINISHED");
        gameSession.setCorrectAnswersCount(5);
        gameSession.setQuestionCount(10);
        gameSession.setTotalScore(50);
        gameSession.setCreatedAt(LocalDateTime.now().minusMinutes(30));
        gameSession.setEndTime(LocalDateTime.now());

        // Act
        GameSessionResponse response = mapper.toGameSessionResponse(gameSession);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.getGameSessionId()).isEqualTo(mapper.extractId(gameSession.getGameSessionFrn()));
        assertThat(response.getGameSessionFrn()).isEqualTo(gameSession.getGameSessionFrn());
        assertThat(response.getScore()).isEqualTo(50);
        assertThat(response.getCorrectAnswers()).isEqualTo(5);
        assertThat(response.getTotalQuestions()).isEqualTo(10);
        assertThat(response.getAccuracy()).isEqualTo(50.0f);
        assertThat(response.getDuration()).isEqualTo("30 min");
        assertThat(response.getStartTime()).isEqualTo(gameSession.getCreatedAt().atOffset(ZoneOffset.UTC));
        assertThat(response.getEndTime()).isEqualTo(gameSession.getEndTime().atOffset(ZoneOffset.UTC));
    }

    @Test
    void shouldConvertGameSessionListToGameSessionResponseList() {
        // Arrange
        User user = TestUtils.createUser();
        Deck deck = TestUtils.createDeck(user);
        List<GameSession> gameSessions = List.of(
                TestUtils.createGameSession(user, deck, "FINISHED"),
                TestUtils.createGameSession(user, deck, "FINISHED")
        );

        // Act
        List<GameSessionResponse> responses = mapper.toGameSessionResponseList(gameSessions);

        // Assert
        assertThat(responses).isNotEmpty();
        assertThat(responses).hasSize(gameSessions.size());
    }

    @Test
    void shouldHandleNullEndTimeInGameSessionResponse() {
        // Arrange
        User user = TestUtils.createUser();
        Deck deck = TestUtils.createDeck(user);
        GameSession gameSession = TestUtils.createGameSession(user, deck, "PENDING");
        gameSession.setCreatedAt(LocalDateTime.now());

        // Act
        GameSessionResponse response = mapper.toGameSessionResponse(gameSession);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.getDuration()).isEqualTo("N/A");
        assertThat(response.getEndTime()).isNull();
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
        UserResponse response = mapper.toUserResponse(user);

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
}
