package com.flashdash.service;

import com.flashdash.dto.response.GameSessionResult;
import com.flashdash.exception.ErrorCode;
import com.flashdash.exception.FlashDashException;
import com.flashdash.model.*;
import com.flashdash.repository.GameSessionRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class GameSessionService {

    private final DeckService deckService;
    private final QuestionService questionService;
    private final GameSessionRepository gameSessionRepository;

    public GameSessionService(DeckService deckService,
                              QuestionService questionService,
                              GameSessionRepository gameSessionRepository) {
        this.deckService = deckService;
        this.questionService = questionService;
        this.gameSessionRepository = gameSessionRepository;
    }

    public List<Question> startGameSession(Long deckId, User user) {
        GameSession existingSession = gameSessionRepository.findTopByDeckIdAndUserIdAndStatus(deckId, user.getId(), GameSessionStatus.PENDING);

        if (existingSession != null) {
            existingSession.setCreatedAt(LocalDateTime.now());
            existingSession.setUpdatedAt(LocalDateTime.now());
            gameSessionRepository.save(existingSession);

            return questionService.getAllQuestionsInDeck(deckId, user);
        }

        Deck deck = deckService.getDeckById(deckId, user);

        GameSession gameSession = new GameSession();
        gameSession.setUser(user);
        gameSession.setDeck(deck);
        gameSession.setCreatedAt(LocalDateTime.now());
        gameSession.setUpdatedAt(LocalDateTime.now());
        gameSession.setStatus(GameSessionStatus.PENDING);

        gameSessionRepository.save(gameSession);

        List<Question> questions = questionService.getAllQuestionsInDeck(deckId, user);

        return questions;
    }

    public GameSessionResult endGameSession(Long deckId, User user, List<Question> userAnswers) {
        List<Question> correctQuestions = questionService.getAllQuestionsInDeck(deckId, user);

        int correctCount = 0;

        for (Question userQuestion : userAnswers) {
            Question correctQuestion = correctQuestions.stream()
                    .filter(q -> q.getQuestionId().equals(userQuestion.getQuestionId()))
                    .findFirst()
                    .orElseThrow(() -> new FlashDashException(ErrorCode.E404003, "Question not found"));

            if (correctQuestion.getCorrectAnswers().equals(userQuestion.getCorrectAnswers())) {
                correctCount++;
            }
        }

        int totalQuestions = userAnswers.size();
        int score = (int) (((double) correctCount / totalQuestions) * 100);

        GameSession gameSession = gameSessionRepository.findTopByDeckIdAndUserIdAndStatus(deckId, user.getId(), GameSessionStatus.PENDING);

        if (gameSession != null) {
            gameSession.setStatus(GameSessionStatus.FINISHED);
            gameSession.setEndTime(LocalDateTime.now());
            gameSession.setTotalScore(score);
            gameSession.setCorrectAnswersCount(correctCount);

            gameSessionRepository.save(gameSession);
        }

        GameSessionResult result = new GameSessionResult(score, correctCount, totalQuestions);
        return result;
    }

    public void removeAllGameSessionsForUser(User user) {
        List<GameSession> userSessions = gameSessionRepository.findAllByUser(user);
        gameSessionRepository.deleteAll(userSessions);
    }
}
