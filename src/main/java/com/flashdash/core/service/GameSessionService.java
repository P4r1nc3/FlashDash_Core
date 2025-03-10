package com.flashdash.core.service;

import com.flashdash.core.exception.ErrorCode;
import com.flashdash.core.exception.FlashDashException;
import com.flashdash.core.model.GameSession;
import com.flashdash.core.model.GameSessionStatus;
import com.flashdash.core.model.Question;
import com.flashdash.core.repository.GameSessionRepository;
import com.flashdash.core.utils.FrnGenerator;
import com.flashdash.core.utils.ResourceType;
import com.p4r1nc3.flashdash.activity.model.LogActivityRequest.ActivityTypeEnum;
import com.p4r1nc3.flashdash.core.model.QuestionRequest;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class  GameSessionService {

    private final ActivityService activityService;
    private final QuestionService questionService;
    private final GameSessionRepository gameSessionRepository;

    public GameSessionService(ActivityService activityService,
                              QuestionService questionService,
                              GameSessionRepository gameSessionRepository) {
        this.activityService = activityService;
        this.questionService = questionService;
        this.gameSessionRepository = gameSessionRepository;
    }

    public List<Question> startGameSession(String deckFrn, String userFrn) {
        Optional<GameSession> existingSessionOptional = gameSessionRepository.findTopByDeckFrnAndUserFrnAndStatus(
                deckFrn,
                userFrn,
                GameSessionStatus.PENDING.toString()
        );

        GameSession gameSession;

        if (existingSessionOptional.isPresent()) {
            gameSession = existingSessionOptional.get();
            gameSession.setCreatedAt(LocalDateTime.now());
            gameSession.setUpdatedAt(LocalDateTime.now());
            gameSessionRepository.save(gameSession);
        } else {
            gameSession = new GameSession();
            gameSession.setGameSessionFrn(FrnGenerator.generateFrn(ResourceType.GAME_SESSION));
            gameSession.setUserFrn(userFrn);
            gameSession.setDeckFrn(deckFrn);
            gameSession.setCreatedAt(LocalDateTime.now());
            gameSession.setUpdatedAt(LocalDateTime.now());
            gameSession.setStatus(GameSessionStatus.PENDING.toString());

            gameSessionRepository.save(gameSession);
        }

        activityService.logUserActivity(userFrn, gameSession.getGameSessionFrn(), ActivityTypeEnum.GAME_STARTED);

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
        activityService.logUserActivity(userFrn, gameSession.getGameSessionFrn(), ActivityTypeEnum.GAME_FINISHED);

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
}
