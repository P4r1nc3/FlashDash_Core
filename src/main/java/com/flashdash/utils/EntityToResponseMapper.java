package com.flashdash.utils;

import com.flashdash.model.Deck;
import com.flashdash.model.Question;
import com.p4r1nc3.flashdash.core.model.DeckResponse;
import com.p4r1nc3.flashdash.core.model.QuestionResponse;

import java.time.ZoneOffset;
import java.util.List;
import java.util.stream.Collectors;

public class EntityToResponseMapper {

    private EntityToResponseMapper() {
    }

    public static DeckResponse toDeckResponse(Deck deck) {
        DeckResponse deckResponse = new DeckResponse();
        deckResponse.setDeckId(deck.getId());
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
        response.setQuestionId(question.getQuestionId());
        response.setQuestion(question.getQuestion());
        response.setCorrectAnswers(question.getCorrectAnswers());
        response.setIncorrectAnswers(question.getIncorrectAnswers());
        response.setDifficulty(QuestionResponse.DifficultyEnum.fromValue(question.getDifficulty().toLowerCase()));
        response.setCreatedAt(question.getCreatedAt().atOffset(ZoneOffset.UTC));
        response.setUpdatedAt(question.getUpdatedAt().atOffset(ZoneOffset.UTC));
        return response;
    }

    public static List<QuestionResponse> toQuestionResponseList(List<Question> questions) {
        return questions.stream()
                .map(EntityToResponseMapper::toQuestionResponse)
                .collect(Collectors.toList());
    }
}
