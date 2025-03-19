package com.flashdash.core.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.flashdash.core.model.*;
import com.flashdash.core.repository.UserRepository;
import com.p4r1nc3.flashdash.core.model.*;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class EntityToResponseMapper {

    private final ObjectMapper objectMapper;
    private final UserRepository userRepository;

    public EntityToResponseMapper(ObjectMapper objectMapper,
                                  UserRepository userRepository) {
        this.objectMapper = objectMapper;
        this.userRepository = userRepository;
    }

    public DeckResponse mapToDeckResponse(Deck deck) {
        DeckResponse deckResponse = new DeckResponse();
        deckResponse.setDeckId(extractId(deck.getDeckFrn()));
        deckResponse.setDeckFrn(deck.getDeckFrn());
        deckResponse.setName(deck.getName());
        deckResponse.setDescription(deck.getDescription());
        deckResponse.setCreatedAt(deck.getCreatedAt().atOffset(ZoneOffset.UTC));
        deckResponse.setUpdatedAt(deck.getUpdatedAt().atOffset(ZoneOffset.UTC));
        return deckResponse;
    }

    public List<DeckResponse> mapToDeckResponse(List<Deck> deckList) {
        return deckList.stream()
                .map(this::mapToDeckResponse)
                .collect(Collectors.toList());
    }

    public QuestionResponse mapToQuestionResponse(Question question) {
        QuestionResponse response = new QuestionResponse();
        response.setQuestionId(extractId(question.getQuestionFrn()));
        response.setQuestionFrn(question.getQuestionFrn());
        response.setQuestion(question.getQuestion());
        response.setCorrectAnswers(question.getCorrectAnswers());
        response.setIncorrectAnswers(question.getIncorrectAnswers());

        try {
            response.setDifficulty(QuestionResponse.DifficultyEnum.fromValue(question.getDifficulty()));
        } catch (IllegalArgumentException e) {
            response.setDifficulty(QuestionResponse.DifficultyEnum.EASY);
        }

        response.setCreatedAt(question.getCreatedAt().atOffset(ZoneOffset.UTC));
        response.setUpdatedAt(question.getUpdatedAt().atOffset(ZoneOffset.UTC));
        return response;
    }

    public List<QuestionResponse> mapToQuestionResponse(List<Question> questions) {
        return questions.stream()
                .map(this::mapToQuestionResponse)
                .collect(Collectors.toList());
    }

    public GameSessionResponse mapToGameSessionResponse(GameSession gameSession) {
        GameSessionResponse response = new GameSessionResponse();

        response.setGameSessionId(extractId(gameSession.getGameSessionFrn()));
        response.setGameSessionFrn(gameSession.getGameSessionFrn());
        response.setScore(gameSession.getTotalScore());
        response.setCorrectAnswers(gameSession.getCorrectAnswersCount());
        response.setTotalQuestions(gameSession.getQuestionCount());

        Duration duration = Duration.between(gameSession.getCreatedAt(), gameSession.getUpdatedAt());
        response.setDuration(duration.toMinutes() + " min " + duration.getSeconds() % 60 + " sec");

        response.setAccuracy(gameSession.getQuestionCount() > 0 ? ((float) gameSession.getCorrectAnswersCount() / gameSession.getQuestionCount()) * 100 : 0f);
        response.setStartTime(gameSession.getCreatedAt().atOffset(ZoneOffset.UTC));
        response.setEndTime(gameSession.getUpdatedAt().atOffset(ZoneOffset.UTC));
        response.setSessionDetails(deserializeSessionDetails(gameSession.getSessionDetails()));

        return response;
    }

    public List<GameSessionResponse> mapToGameSessionResponse(List<GameSession> gameSessions) {
        return gameSessions.stream()
                .map(this::mapToGameSessionResponse)
                .collect(Collectors.toList());
    }

    public FriendInvitationResponseReceived mapToReceivedResponse(FriendInvitation invitation) {
        User sender = userRepository.findById(invitation.getSentByFrn()).orElse(null);

        FriendInvitationResponseReceived response = new FriendInvitationResponseReceived();
        response.setInvitationId(extractId(invitation.getInvitationFrn()));
        response.setInvitationFrn(invitation.getInvitationFrn());
        response.setStatus(invitation.getStatus());
        response.setCreatedAt(invitation.getCreatedAt().atOffset(ZoneOffset.UTC));
        response.setUpdatedAt(invitation.getUpdatedAt().atOffset(ZoneOffset.UTC));

        if (sender != null) {
            UserSummary userSummary = new UserSummary();
            userSummary.setUserId(extractId(sender.getUserFrn()));
            userSummary.setUserFrn(sender.getUserFrn());
            userSummary.setFirstName(sender.getFirstName());
            userSummary.setLastName(sender.getLastName());
            userSummary.setEmail(sender.getEmail());

            response.setSender(userSummary);
        }

        return response;
    }

    public List<FriendInvitationResponseReceived> mapToReceivedResponse(List<FriendInvitation> invitations) {
        return invitations.stream()
                .map(this::mapToReceivedResponse)
                .collect(Collectors.toList());
    }

    public FriendInvitationResponseSent mapToSentResponse(FriendInvitation invitation) {
        User recipient = userRepository.findById(invitation.getSentToFrn()).orElse(null);

        FriendInvitationResponseSent response = new FriendInvitationResponseSent();
        response.setInvitationId(extractId(invitation.getInvitationFrn()));
        response.setInvitationFrn(invitation.getInvitationFrn());
        response.setStatus(invitation.getStatus());
        response.setCreatedAt(invitation.getCreatedAt().atOffset(ZoneOffset.UTC));
        response.setUpdatedAt(invitation.getUpdatedAt().atOffset(ZoneOffset.UTC));

        if (recipient != null) {
            UserSummary userSummary = new UserSummary();
            userSummary.setUserId(extractId(recipient.getUserFrn()));
            userSummary.setUserFrn(recipient.getUserFrn());
            userSummary.setFirstName(recipient.getFirstName());
            userSummary.setLastName(recipient.getLastName());
            userSummary.setEmail(recipient.getEmail());

            response.setRecipient(userSummary);
        }

        return response;
    }

    public List<FriendInvitationResponseSent> mapToSentResponse(List<FriendInvitation> invitations) {
        return invitations.stream()
                .map(this::mapToSentResponse)
                .collect(Collectors.toList());
    }

    public UserResponse mapToUserResponse(User user) {
        UserResponse response = new UserResponse();
        response.setUserId(extractId(user.getUserFrn()));
        response.setUserFrn(user.getUserFrn());
        response.setEmail(user.getEmail());
        response.setUsername(user.getUsername());
        response.setFirstName(user.getFirstName());
        response.setLastName(user.getLastName());
        response.setCreatedAt(user.getCreatedAt().atOffset(ZoneOffset.UTC));
        response.setUpdatedAt(user.getUpdatedAt().atOffset(ZoneOffset.UTC));
        response.setStreak(user.getStrike());

        return response;
    }

    public List<UserResponse> mapToUserResponse(List<User> users) {
        return users.stream()
                .map(this::mapToUserResponse)
                .collect(Collectors.toList());
    }

    public String extractId(String frn) {
        if (frn != null && frn.contains(":")) {
            String[] parts = frn.split(":");
            return parts[parts.length - 1];
        }
        return frn;
    }

    private List<GameSessionDetailsResponse> deserializeSessionDetails(String sessionDetailsJson) {
        if (sessionDetailsJson == null || sessionDetailsJson.isBlank()) {
            return new ArrayList<>();
        }
        try {
            return objectMapper.readValue(sessionDetailsJson, new TypeReference<List<GameSessionDetailsResponse>>() {});
        } catch (JsonProcessingException e) {
            return new ArrayList<>();
        }
    }
}
