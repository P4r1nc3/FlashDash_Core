package com.flashdash.core.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.flashdash.core.exception.ErrorCode;
import com.flashdash.core.exception.FlashDashException;
import com.flashdash.core.model.GameSession;
import com.flashdash.core.model.GameSessionStatus;
import com.flashdash.core.model.Question;
import com.flashdash.core.model.User;
import com.flashdash.core.repository.GameSessionRepository;
import com.flashdash.core.repository.UserRepository;
import com.flashdash.core.service.api.ActivityService;
import com.flashdash.core.utils.FrnGenerator;
import com.flashdash.core.utils.ResourceType;
import com.p4r1nc3.flashdash.activity.model.LogActivityRequest.ActivityTypeEnum;
import com.p4r1nc3.flashdash.core.model.GameSessionDetailsResponse;
import com.p4r1nc3.flashdash.core.model.QuestionRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;

@Service
public class  GameSessionService {

    private static final Logger logger = LoggerFactory.getLogger(GameSessionService.class);

    private final ObjectMapper objectMapper;
    private final ActivityService activityService;
    private final QuestionService questionService;
    private final GameSessionRepository gameSessionRepository;
    private final UserRepository userRepository;

    public GameSessionService(ObjectMapper objectMapper,
                              ActivityService activityService,
                              QuestionService questionService,
                              GameSessionRepository gameSessionRepository,
                              UserRepository userRepository) {
        this.objectMapper = objectMapper;
        this.activityService = activityService;
        this.questionService = questionService;
        this.gameSessionRepository = gameSessionRepository;
        this.userRepository = userRepository;
    }

    public List<Question> startGameSession(String deckFrn, String userFrn) {
        Optional<GameSession> existingSessionOptional = gameSessionRepository.findTopByDeckFrnAndUserFrnAndStatus(
                deckFrn,
                userFrn,
                GameSessionStatus.PENDING.toString()
        );

        existingSessionOptional.ifPresent(gameSessionRepository::delete);

        GameSession newGameSession = new GameSession();
        newGameSession.setGameSessionFrn(FrnGenerator.generateFrn(ResourceType.GAME_SESSION));
        newGameSession.setUserFrn(userFrn);
        newGameSession.setDeckFrn(deckFrn);
        newGameSession.setStatus(GameSessionStatus.PENDING.toString());
        newGameSession.setTotalScore(0);
        newGameSession.setCorrectAnswersCount(0);
        newGameSession.setWrongAnswersCount(0);
        newGameSession.setQuestionCount(0);
        newGameSession.setCreatedAt(LocalDateTime.now());
        newGameSession.setUpdatedAt(LocalDateTime.now());
        newGameSession.setSessionDetails("");

        gameSessionRepository.save(newGameSession);

        activityService.logUserActivity(userFrn, newGameSession.getGameSessionFrn(), ActivityTypeEnum.GAME_STARTED);

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
        List<GameSessionDetailsResponse> sessionDetailsList = new ArrayList<>();

        int correctCount = 0;
        int wrongCount = 0;

        for (QuestionRequest userQuestion : userAnswers) {
            Question correctQuestion = correctQuestions.stream()
                    .filter(q -> q.getQuestion().equalsIgnoreCase(userQuestion.getQuestion()))
                    .findFirst()
                    .orElseThrow(() -> new FlashDashException(ErrorCode.E404002, "Matching question not found in the provided deck."));

            boolean wasCorrect = new HashSet<>(correctQuestion.getCorrectAnswers())
                    .equals(new HashSet<>(userQuestion.getCorrectAnswers()));

            if (wasCorrect) {
                correctCount++;
            } else {
                wrongCount++;
            }

            GameSessionDetailsResponse sessionDetailsResponse = new GameSessionDetailsResponse();
            sessionDetailsResponse.setQuestionText(correctQuestion.getQuestion());
            sessionDetailsResponse.setCorrectAnswers(correctQuestion.getCorrectAnswers());
            sessionDetailsResponse.setIncorrectAnswers(correctQuestion.getIncorrectAnswers());
            sessionDetailsResponse.setUserAnswers(userQuestion.getCorrectAnswers());
            sessionDetailsResponse.setWasCorrect(wasCorrect);

            sessionDetailsList.add(sessionDetailsResponse);
        }

        int totalQuestions = userAnswers.size();
        int score = correctCount*10 - wrongCount*4;

        GameSession gameSession = gameSessionOptional.get();
        gameSession.setStatus(GameSessionStatus.FINISHED.toString());
        gameSession.setUpdatedAt(LocalDateTime.now());
        gameSession.setTotalScore(score);
        gameSession.setCorrectAnswersCount(correctCount);
        gameSession.setWrongAnswersCount(wrongCount);
        gameSession.setQuestionCount(totalQuestions);

        try {
            gameSession.setSessionDetails(objectMapper.writeValueAsString(sessionDetailsList));
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Error serializing session details", e);
        }

        Duration duration = Duration.between(gameSession.getCreatedAt(), gameSession.getUpdatedAt());

        gameSessionRepository.save(gameSession);

        User user = userRepository.findByUserFrn(userFrn)
                .orElseThrow(() -> {
                    logger.warn("User with userFrn {} not found", userFrn);
                    return new FlashDashException(
                            ErrorCode.E404001,
                            "User with userFrn " + userFrn + " not found."
                    );
                });

        logger.info("User found with userFrn: {}.", userFrn);

        user.setGamesPlayed(user.getGamesPlayed() + 1);
        user.setPoints(user.getPoints() + score);
        user.setStudyTime(user.getStudyTime().plus(duration));
        userRepository.save(user);

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

    public List<GameSession> getAllGameSessions(String userFrn) {
        return gameSessionRepository.findAllByUserFrn(userFrn);
    }

    public void removeAllGameSessionsForUser(String userFrn) {
        List<GameSession> userSessions = gameSessionRepository.findAllByUserFrn(userFrn);
        if (!userSessions.isEmpty()) {
            gameSessionRepository.deleteAll(userSessions);
        }
    }
}
