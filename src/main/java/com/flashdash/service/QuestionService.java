package com.flashdash.service;

import com.flashdash.model.Deck;
import com.flashdash.model.User;
import com.flashdash.model.question.Question;
import com.flashdash.model.question.QuestionDeck;
import com.flashdash.repository.QuestionRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class QuestionService {

    private final QuestionRepository questionRepository;
    private final DeckService deckService;

    public QuestionService(QuestionRepository questionRepository, DeckService deckService) {
        this.questionRepository = questionRepository;
        this.deckService = deckService;
    }

    public Question addQuestionToDeck(Long deckId, Question question, User user) {
        Deck deck = deckService.getDeckById(deckId, user);
        question.setDeck(validateDeckIsQuestionDeck(deck));
        question.setCreatedAt(LocalDateTime.now());
        question.setUpdatedAt(LocalDateTime.now());

        return questionRepository.save(question);
    }

    public List<Question> getAllQuestionsInDeck(Long deckId, User user) {
        Deck deck = deckService.getDeckById(deckId, user);
        return questionRepository.findAllByDeck(validateDeckIsQuestionDeck(deck));
    }

    public Question getQuestionById(Long deckId, Long questionId, User user) {
        Deck deck = deckService.getDeckById(deckId, user);
        return questionRepository.findByDeckAndQuestionId(validateDeckIsQuestionDeck(deck), questionId)
                .orElseThrow(() -> new RuntimeException("Question not found with id: " + questionId));
    }

    public Question updateQuestion(Long deckId, Long questionId, Question questionDetails, User user) {
        Deck deck = deckService.getDeckById(deckId, user);
        Question question = questionRepository.findByDeckAndQuestionId(validateDeckIsQuestionDeck(deck), questionId)
                .orElseThrow(() -> new RuntimeException("Question not found with id: " + questionId));

        question.setQuestion(questionDetails.getQuestion());
        question.setCorrectAnswers(questionDetails.getCorrectAnswers());
        question.setIncorrectAnswers(questionDetails.getIncorrectAnswers());
        question.setDifficulty(questionDetails.getDifficulty());
        question.setUpdatedAt(LocalDateTime.now());

        return questionRepository.save(question);
    }

    public void deleteQuestion(Long deckId, Long questionId, User user) {
        Deck deck = deckService.getDeckById(deckId, user);
        Question question = questionRepository.findByDeckAndQuestionId(validateDeckIsQuestionDeck(deck), questionId)
                .orElseThrow(() -> new RuntimeException("Question not found with id: " + questionId));

        questionRepository.delete(question);
    }

    private QuestionDeck validateDeckIsQuestionDeck(Deck deck) {
        if (!(deck instanceof QuestionDeck)) {
            throw new RuntimeException("Provided deck is not a QUESTION deck.");
        }
        return (QuestionDeck) deck;
    }
}
