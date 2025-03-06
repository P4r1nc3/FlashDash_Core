package com.flashdash.utils;

import com.flashdash.model.*;
import com.flashdash.repository.UserRepository;
import com.p4r1nc3.flashdash.core.model.*;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class EntityToResponseMapper {

    private final UserRepository userRepository;

    public EntityToResponseMapper(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public DeckResponse toDeckResponse(Deck deck) {
        DeckResponse deckResponse = new DeckResponse();
        deckResponse.setDeckId(extractId(deck.getDeckFrn()));
        deckResponse.setDeckFrn(deck.getDeckFrn());
        deckResponse.setName(deck.getName());
        deckResponse.setDescription(deck.getDescription());
        deckResponse.setCreatedAt(deck.getCreatedAt().atOffset(ZoneOffset.UTC));
        deckResponse.setUpdatedAt(deck.getUpdatedAt().atOffset(ZoneOffset.UTC));
        return deckResponse;
    }

    public List<DeckResponse> toDeckResponseList(List<Deck> deckList) {
        return deckList.stream()
                .map(this::toDeckResponse)
                .collect(Collectors.toList());
    }

    public QuestionResponse toQuestionResponse(Question question) {
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

    public List<QuestionResponse> toQuestionResponseList(List<Question> questions) {
        return questions.stream()
                .map(this::toQuestionResponse)
                .collect(Collectors.toList());
    }

    public GameSessionResponse toGameSessionResponse(GameSession gameSession) {
        GameSessionResponse response = new GameSessionResponse();
        response.setGameSessionId(extractId(gameSession.getGameSessionFrn()));
        response.setGameSessionFrn(gameSession.getGameSessionFrn());
        response.setScore(gameSession.getTotalScore());
        response.setCorrectAnswers(gameSession.getCorrectAnswersCount());
        response.setTotalQuestions(gameSession.getQuestionCount());

        if (gameSession.getEndTime() != null) {
            long durationInMinutes = java.time.Duration.between(gameSession.getCreatedAt(), gameSession.getEndTime()).toMinutes();
            response.setDuration(durationInMinutes + " min");
        } else {
            response.setDuration("N/A");
        }

        if (gameSession.getQuestionCount() > 0) {
            response.setAccuracy(((float) gameSession.getCorrectAnswersCount() / gameSession.getQuestionCount()) * 100);
        } else {
            response.setAccuracy(0f);
        }

        response.setStartTime(gameSession.getCreatedAt().atOffset(ZoneOffset.UTC));
        response.setEndTime(gameSession.getEndTime() != null ? gameSession.getEndTime().atOffset(ZoneOffset.UTC) : null);

        return response;
    }

    public List<GameSessionResponse> toGameSessionResponseList(List<GameSession> gameSessions) {
        return gameSessions.stream()
                .map(this::toGameSessionResponse)
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
            FriendInvitationResponseReceivedSender senderResponse = new FriendInvitationResponseReceivedSender();
            senderResponse.setUserId(extractId(sender.getUserFrn()));
            senderResponse.setUserFrn(sender.getUserFrn());
            senderResponse.setFirstName(sender.getFirstName());
            senderResponse.setLastName(sender.getLastName());
            senderResponse.setEmail(sender.getEmail());

            response.setSender(senderResponse);
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
            FriendInvitationResponseSentRecipient recipientResponse = new FriendInvitationResponseSentRecipient();
            recipientResponse.setUserId(extractId(recipient.getUserFrn()));
            recipientResponse.setUserFrn(recipient.getUserFrn());
            recipientResponse.setFirstName(recipient.getFirstName());
            recipientResponse.setLastName(recipient.getLastName());
            recipientResponse.setEmail(recipient.getEmail());

            response.setRecipient(recipientResponse);
        }

        return response;
    }

    public List<FriendInvitationResponseSent> mapToSentResponse(List<FriendInvitation> invitations) {
        return invitations.stream()
                .map(this::mapToSentResponse)
                .collect(Collectors.toList());
    }

    public String extractId(String frn) {
        if (frn != null && frn.contains(":")) {
            String[] parts = frn.split(":");
            return parts[parts.length - 1];
        }
        return frn;
    }
}
