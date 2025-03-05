package com.flashdash.utils;

import com.flashdash.model.Deck;
import com.flashdash.model.GameSession;
import com.flashdash.model.Question;
import com.p4r1nc3.flashdash.core.model.DeckResponse;
import com.p4r1nc3.flashdash.core.model.GameSessionResponse;
import com.p4r1nc3.flashdash.core.model.QuestionResponse;

import java.time.ZoneOffset;
import java.util.List;
import java.util.stream.Collectors;

public class EntityToResponseMapper {

    private EntityToResponseMapper() {
    }

    public static DeckResponse toDeckResponse(Deck deck) {
        DeckResponse deckResponse = new DeckResponse();
        deckResponse.setDeckId(extractId(deck.getDeckFrn()));
        deckResponse.setDeckFrn(deck.getDeckFrn());
        deckResponse.setName(deck.getName());
        deckResponse.setDescription(deck.getDescription());
        deckResponse.setCreatedAt(deck.getCreatedAt().atOffset(ZoneOffset.UTC));
        deckResponse.setUpdatedAt(deck.getUpdatedAt().atOffset(ZoneOffset.UTC));
        return deckResponse;
    }

    public static List<DeckResponse> toDeckResponseList(List<Deck> deckList) {
        return deckList.stream()
                .map(EntityToResponseMapper::toDeckResponse)
                .collect(Collectors.toList());
    }

    public static QuestionResponse toQuestionResponse(Question question) {
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

    public static List<QuestionResponse> toQuestionResponseList(List<Question> questions) {
        return questions.stream()
                .map(EntityToResponseMapper::toQuestionResponse)
                .collect(Collectors.toList());
    }

    public static GameSessionResponse toGameSessionResponse(GameSession gameSession) {
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

    public static List<GameSessionResponse> toGameSessionResponseList(List<GameSession> gameSessions) {
        return gameSessions.stream()
                .map(EntityToResponseMapper::toGameSessionResponse)
                .collect(Collectors.toList());
    }

    public static String extractId(String frn) {
        if (frn != null && frn.contains(":")) {
            String[] parts = frn.split(":");
            return parts[parts.length - 1];
        }
        return frn;
    }
}
