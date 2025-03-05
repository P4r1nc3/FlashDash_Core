package com.flashdash.service;

import com.flashdash.exception.ErrorCode;
import com.flashdash.exception.FlashDashException;
import com.flashdash.model.GameSession;
import com.flashdash.model.GameSessionStatus;
import com.flashdash.model.Question;
import com.flashdash.repository.GameSessionRepository;
import com.p4r1nc3.flashdash.core.model.QuestionRequest;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class GameSessionService {

    private final DeckService deckService;
    private final QuestionService questionService;
    private final GameSessionRepository gameSessionRepository;

    public GameSessionService(DeckService deckService, QuestionService questionService, GameSessionRepository gameSessionRepository) {
        this.deckService = deckService;
        this.questionService = questionService;
        this.gameSessionRepository = gameSessionRepository;
    }

    public List<Question> startGameSession(String deckFrn, String userFrn) {
        Optional<GameSession> existingSessionOptional = gameSessionRepository.findTopByDeckFrnAndUserFrnAndStatus(
                deckFrn,
                userFrn,
                GameSessionStatus.PENDING.toString()
        );

        if (existingSessionOptional.isEmpty()) {
            deckService.getDeckByFrn(deckFrn, userFrn);

            GameSession gameSession = new GameSession();
            gameSession.setGameSessionFrn(generateFrn("game-session"));
            gameSession.setUserFrn(userFrn);
            gameSession.setDeckFrn(deckFrn);
            gameSession.setCreatedAt(LocalDateTime.now());
            gameSession.setUpdatedAt(LocalDateTime.now());
            gameSession.setStatus(GameSessionStatus.PENDING.toString());

            gameSessionRepository.save(gameSession);
        }

        return questionService.getAllQuestionsInDeck(deckFrn, userFrn);
    }

    public GameSession endGameSession(String deckFrn, String userFrn, List<QuestionRequest> userAnswers) {
        Optional<GameSession> gameSessionOptional = gameSessionRepository.findTopByDeckFrnAndUserFrnAndStatus(
                deckFrn,
                userFrn,
                GameSessionStatus.PENDING.toString()
        );

        if (gameSessionOptional.isEmpty()) {
            throw new FlashDashException(ErrorCode.E400003, "No active game session for this deck.");
        }

        List<Question> correctQuestions = questionService.getAllQuestionsInDeck(deckFrn, userFrn);

        int correctCount = 0;
        int wrongCount = 0;

        for (QuestionRequest userQuestion : userAnswers) {
            Question correctQuestion = correctQuestions.stream()
                    .filter(q -> q.getQuestion().equalsIgnoreCase(userQuestion.getQuestion()))
                    .findFirst()
                    .orElseThrow(() -> new FlashDashException(ErrorCode.E404002, "Matching question not found in the provided deck."));

            if (correctQuestion.getCorrectAnswers().equals(userQuestion.getCorrectAnswers())) {
                correctCount++;
            } else {
                wrongCount++;
            }
        }

        int totalQuestions = userAnswers.size();
        int score = (int) (((double) correctCount / totalQuestions) * 100);

        GameSession gameSession = gameSessionOptional.get();
        gameSession.setStatus(GameSessionStatus.FINISHED.toString());
        gameSession.setEndTime(LocalDateTime.now());
        gameSession.setTotalScore(score);
        gameSession.setCorrectAnswersCount(correctCount);
        gameSession.setWrongAnswersCount(wrongCount);
        gameSession.setQuestionCount(totalQuestions);

        gameSessionRepository.save(gameSession);
        return gameSession;
    }

    public List<GameSession> getGameSessions(String deckFrn, String userFrn) {
        return gameSessionRepository.findAllByDeckFrnAndUserFrn(deckFrn, userFrn);
    }

    public GameSession getGameSession(String deckFrn, String gameSessionFrn, String userFrn) {
        return gameSessionRepository.findByDeckFrnAndGameSessionFrnAndUserFrn(deckFrn, gameSessionFrn, userFrn)
                .orElseThrow(() -> new FlashDashException(ErrorCode.E404006, "Game session not found"));
    }

    public void removeAllGameSessionsForUser(String userFrn) {
        List<GameSession> userSessions = gameSessionRepository.findAllByUserFrn(userFrn);
        if (!userSessions.isEmpty()) {
            gameSessionRepository.deleteAll(userSessions);
        }
    }

    private String generateFrn(String resourceType) {
        return "frn:flashdash:" + resourceType + ":" + UUID.randomUUID();
    }
}
