package com.flashdash.service;

import com.flashdash.exception.ErrorCode;
import com.flashdash.exception.FlashDashException;
import com.flashdash.model.*;
import com.flashdash.repository.GameSessionRepository;
import com.p4r1nc3.flashdash.core.model.QuestionRequest;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

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

    public List<Question> startGameSession(Long deckId, User user) {
        GameSession existingSession = gameSessionRepository.findTopByDeckIdAndUserIdAndStatus(deckId, user.getId(), GameSessionStatus.PENDING);

        if (existingSession == null) {
            Deck deck = deckService.getDeckById(deckId, user);

            GameSession gameSession = new GameSession();
            gameSession.setUser(user);
            gameSession.setDeck(deck);
            gameSession.setCreatedAt(LocalDateTime.now());
            gameSession.setUpdatedAt(LocalDateTime.now());
            gameSession.setStatus(GameSessionStatus.PENDING);

            gameSessionRepository.save(gameSession);
        }

        return questionService.getAllQuestionsInDeck(deckId, user);
    }

    public GameSession endGameSession(Long deckId, User user, List<QuestionRequest> userAnswers) {
        List<Question> correctQuestions = questionService.getAllQuestionsInDeck(deckId, user);

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

        GameSession gameSession = gameSessionRepository.findTopByDeckIdAndUserIdAndStatus(deckId, user.getId(), GameSessionStatus.PENDING);

        if (gameSession == null) {
            throw new FlashDashException(ErrorCode.E400003, "No active game session for this deck.");
        }

        gameSession.setStatus(GameSessionStatus.FINISHED);
        gameSession.setEndTime(LocalDateTime.now());
        gameSession.setTotalScore(score);
        gameSession.setCorrectAnswersCount(correctCount);
        gameSession.setWrongAnswersCount(wrongCount);
        gameSession.setQuestionCount(totalQuestions);

        gameSessionRepository.save(gameSession);
        return gameSession;
    }

    public List<GameSession> getGameSessions(Long deckId, User user) {
        return gameSessionRepository.findAllByDeckIdAndUserId(deckId, user.getId());
    }

    public GameSession getGameSession(Long deckId, Long gameSessionId, User user) {
        return gameSessionRepository.findById(gameSessionId)
                .filter(session -> {
                    Long sessionDeckId = session.getDeck() != null ? session.getDeck().getId() : null;
                    return sessionDeckId != null && sessionDeckId.equals(deckId) && session.getUser().getId().equals(user.getId());
                })
                .orElseThrow(() -> new FlashDashException(ErrorCode.E404006, "Game session not found"));
    }

    public void removeAllGameSessionsForUser(User user) {
        List<GameSession> userSessions = gameSessionRepository.findAllByUser(user);
        gameSessionRepository.deleteAll(userSessions);
    }
}
