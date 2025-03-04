package com.flashdash.utils;

import com.flashdash.model.Deck;
import com.flashdash.model.GameSession;
import com.flashdash.model.GameSessionStatus;
import com.flashdash.model.Question;
import com.flashdash.model.User;
import com.p4r1nc3.flashdash.core.model.DeckResponse;
import com.p4r1nc3.flashdash.core.model.GameSessionResponse;
import com.p4r1nc3.flashdash.core.model.QuestionResponse;
import com.flashdash.TestUtils;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class EntityToResponseMapperTest {

    @Test
    void shouldConvertDeckToDeckResponse() {
        // Arrange
        User user = TestUtils.createUser();
        Deck deck = TestUtils.createDeck(user);

        // Act
        DeckResponse deckResponse = EntityToResponseMapper.toDeckResponse(deck);

        // Assert
        assertThat(deckResponse).isNotNull();
        assertThat(deckResponse.getDeckId()).isEqualTo(deck.getId());
        assertThat(deckResponse.getName()).isEqualTo(deck.getName());
        assertThat(deckResponse.getDescription()).isEqualTo(deck.getDescription());
        assertThat(deckResponse.getCreatedAt()).isEqualTo(deck.getCreatedAt().atOffset(java.time.ZoneOffset.UTC));
        assertThat(deckResponse.getUpdatedAt()).isEqualTo(deck.getUpdatedAt().atOffset(java.time.ZoneOffset.UTC));
    }

    @Test
    void shouldConvertDeckListToDeckResponseList() {
        // Arrange
        User user = TestUtils.createUser();
        List<Deck> decks = List.of(TestUtils.createDeck(user));

        // Act
        List<DeckResponse> deckResponses = EntityToResponseMapper.toDeckResponseList(decks);

        // Assert
        assertThat(deckResponses).isNotEmpty();
        assertThat(deckResponses).hasSize(decks.size());
        assertThat(deckResponses.get(0).getDeckId()).isEqualTo(decks.get(0).getId());
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
        QuestionResponse questionResponse = EntityToResponseMapper.toQuestionResponse(question);

        // Assert
        assertThat(questionResponse).isNotNull();
        assertThat(questionResponse.getQuestionId()).isEqualTo(question.getQuestionId());
        assertThat(questionResponse.getQuestion()).isEqualTo(question.getQuestion());
        assertThat(questionResponse.getCorrectAnswers()).isEqualTo(question.getCorrectAnswers());
        assertThat(questionResponse.getIncorrectAnswers()).isEqualTo(question.getIncorrectAnswers());
        assertThat(questionResponse.getDifficulty()).isEqualTo(QuestionResponse.DifficultyEnum.fromValue(question.getDifficulty()));
        assertThat(questionResponse.getCreatedAt()).isEqualTo(question.getCreatedAt().atOffset(java.time.ZoneOffset.UTC));
        assertThat(questionResponse.getUpdatedAt()).isEqualTo(question.getUpdatedAt().atOffset(java.time.ZoneOffset.UTC));
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
        List<QuestionResponse> questionResponses = EntityToResponseMapper.toQuestionResponseList(questions);

        // Assert
        assertThat(questionResponses).isNotEmpty();
        assertThat(questionResponses).hasSize(questions.size());

        for (int i = 0; i < questions.size(); i++) {
            assertThat(questionResponses.get(i).getQuestionId()).isEqualTo(questions.get(i).getQuestionId());
            assertThat(questionResponses.get(i).getQuestion()).isEqualTo(questions.get(i).getQuestion());
            assertThat(questionResponses.get(i).getCorrectAnswers()).isEqualTo(questions.get(i).getCorrectAnswers());
            assertThat(questionResponses.get(i).getIncorrectAnswers()).isEqualTo(questions.get(i).getIncorrectAnswers());
            assertThat(questionResponses.get(i).getDifficulty()).isEqualTo(QuestionResponse.DifficultyEnum.fromValue(questions.get(i).getDifficulty()));
            assertThat(questionResponses.get(i).getCreatedAt()).isEqualTo(questions.get(i).getCreatedAt().atOffset(java.time.ZoneOffset.UTC));
            assertThat(questionResponses.get(i).getUpdatedAt()).isEqualTo(questions.get(i).getUpdatedAt().atOffset(java.time.ZoneOffset.UTC));
        }
    }

    @Test
    void shouldConvertGameSessionToGameSessionResponse() {
        // Arrange
        User user = TestUtils.createUser();
        Deck deck = TestUtils.createDeck(user);
        GameSession gameSession = TestUtils.createGameSession(user, deck, GameSessionStatus.FINISHED);
        gameSession.setCorrectAnswersCount(5);
        gameSession.setQuestionCount(10);
        gameSession.setTotalScore(50);
        gameSession.setCreatedAt(LocalDateTime.now().minusMinutes(30));
        gameSession.setEndTime(LocalDateTime.now());

        // Act
        GameSessionResponse response = EntityToResponseMapper.toGameSessionResponse(gameSession);

        // Assert
        assertThat(response).isNotNull();
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
                TestUtils.createGameSession(user, deck, GameSessionStatus.FINISHED),
                TestUtils.createGameSession(user, deck, GameSessionStatus.FINISHED)
        );

        // Act
        List<GameSessionResponse> responses = EntityToResponseMapper.toGameSessionResponseList(gameSessions);

        // Assert
        assertThat(responses).isNotEmpty();
        assertThat(responses).hasSize(gameSessions.size());
    }

    @Test
    void shouldHandleNullEndTimeInGameSessionResponse() {
        // Arrange
        User user = TestUtils.createUser();
        Deck deck = TestUtils.createDeck(user);
        GameSession gameSession = TestUtils.createGameSession(user, deck, GameSessionStatus.PENDING);
        gameSession.setCreatedAt(LocalDateTime.now());

        // Act
        GameSessionResponse response = EntityToResponseMapper.toGameSessionResponse(gameSession);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.getDuration()).isEqualTo("N/A");
        assertThat(response.getEndTime()).isNull();
    }
}
