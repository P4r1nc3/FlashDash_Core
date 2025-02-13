package com.flashdash.service;

import com.flashdash.dto.response.GameSessionResult;
import com.flashdash.exception.ErrorCode;
import com.flashdash.exception.FlashDashException;
import com.flashdash.model.Deck;
import com.flashdash.model.GameSession;
import com.flashdash.model.Question;
import com.flashdash.model.User;
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

        Deck deck = deckService.getDeckById(deckId, user);

        // Tworzymy nową sesję gry
        GameSession gameSession = new GameSession();
        gameSession.setUser(user);
        gameSession.setDeck(deck);
        gameSession.setCreatedAt(LocalDateTime.now());
        gameSession.setUpdatedAt(LocalDateTime.now());

        // Zapisujemy sesję gry
        gameSessionRepository.save(gameSession);

        // Pobieramy pytania z decku
        List<Question> questions = questionService.getAllQuestionsInDeck(deckId, user);

        // Zwracamy pytania
        return questions;
    }

    public GameSessionResult endGameSession(Long deckId, User user, List<Question> userAnswers) {
        // Pobieramy pytania z bazy danych
        List<Question> correctQuestions = questionService.getAllQuestionsInDeck(deckId, user);

        // Zmienna do przechowywania liczby poprawnych odpowiedzi
        int correctCount = 0;

        // Iterujemy po odpowiedziach użytkownika
        for (Question userQuestion : userAnswers) {
            // Szukamy pytania w bazie, które ma taki sam identyfikator
            Question correctQuestion = correctQuestions.stream()
                    .filter(q -> q.getQuestionId().equals(userQuestion.getQuestionId()))
                    .findFirst()
                    .orElseThrow(() -> new FlashDashException(ErrorCode.E404003, "Question not found"));

            // Porównujemy odpowiedzi
            if (correctQuestion.getCorrectAnswers().equals(userQuestion.getCorrectAnswers())) {
                correctCount++;
            }
        }

        // Obliczamy wynik
        int totalQuestions = userAnswers.size();
        int score = (int) (((double) correctCount / totalQuestions) * 100);

        // Tworzymy wynik gry
        GameSessionResult result = new GameSessionResult(score, correctCount, totalQuestions);
        return result;
    }
}

