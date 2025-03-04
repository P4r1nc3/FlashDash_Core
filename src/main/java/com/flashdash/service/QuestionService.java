package com.flashdash.service;

import com.flashdash.exception.ErrorCode;
import com.flashdash.exception.FlashDashException;
import com.flashdash.model.Deck;
import com.flashdash.model.User;
import com.flashdash.model.Question;
import com.flashdash.repository.QuestionRepository;
import com.p4r1nc3.flashdash.core.model.QuestionRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class QuestionService {

    private static final Logger logger = LoggerFactory.getLogger(QuestionService.class);

    private final QuestionRepository questionRepository;
    private final DeckService deckService;

    public QuestionService(QuestionRepository questionRepository, DeckService deckService) {
        this.questionRepository = questionRepository;
        this.deckService = deckService;
    }

    public Question addQuestionToDeck(Long deckId, QuestionRequest questionRequest, User user) {
        logger.info("Attempting to add question to deck with id: {} for user with email: {}", deckId, user.getUsername());
        Deck deck = deckService.getDeckById(deckId, user);
        logger.info("Deck with id: {} successfully retrieved for adding question", deckId);

        Question question = new Question();
        question.setQuestion(questionRequest.getQuestion());
        question.setCorrectAnswers(questionRequest.getCorrectAnswers());
        question.setIncorrectAnswers(questionRequest.getIncorrectAnswers());
        question.setDifficulty(questionRequest.getDifficulty().name());
        question.setCreatedAt(LocalDateTime.now());
        question.setUpdatedAt(LocalDateTime.now());
        question.setDeck(deck);

        Question savedQuestion = questionRepository.save(question);

        logger.info("Question successfully added to deck with id: {}. Question id: {}", deckId, savedQuestion.getQuestionId());
        return savedQuestion;
    }

    public List<Question> getAllQuestionsInDeck(Long deckId, User user) {
        logger.info("Fetching all questions for deck with id: {} and user with email: {}", deckId, user.getUsername());
        Deck deck = deckService.getDeckById(deckId, user);
        List<Question> questions = questionRepository.findAllByDeck(deck);

        logger.info("Retrieved {} questions for deck with id: {}", questions.size(), deckId);
        return questions;
    }

    public Question getQuestionById(Long deckId, Long questionId, User user) {
        logger.info("Fetching question with id: {} from deck with id: {} for user with email: {}", questionId, deckId, user.getUsername());
        Deck deck = deckService.getDeckById(deckId, user);
        Question question = questionRepository.findByDeckAndQuestionId(deck, questionId)
                .orElseThrow(() -> {
                    logger.warn("Question with id: {} not found in deck with id: {}", questionId, deckId);
                    return new FlashDashException(
                            ErrorCode.E404003,
                            "Question with id " + questionId + " not found in deck with id " + deckId
                    );
                });

        logger.info("Successfully retrieved question with id: {} from deck with id: {}", questionId, deckId);
        return question;
    }

    public Question updateQuestion(Long deckId, Long questionId, QuestionRequest questionRequest, User user) {
        logger.info("Attempting to update question with id: {} in deck with id: {} for user with email: {}", questionId, deckId, user.getUsername());
        Deck deck = deckService.getDeckById(deckId, user);

        Question question = questionRepository.findByDeckAndQuestionId(deck, questionId)
                .orElseThrow(() -> {
                    logger.warn("Question with id: {} not found in deck with id: {}", questionId, deckId);
                    return new FlashDashException(
                            ErrorCode.E404003,
                            "Question with id " + questionId + " not found in deck with id " + deckId
                    );
                });

        // Update question details
        question.setQuestion(questionRequest.getQuestion());
        question.setCorrectAnswers(questionRequest.getCorrectAnswers());
        question.setIncorrectAnswers(questionRequest.getIncorrectAnswers());
        question.setDifficulty(questionRequest.getDifficulty().name());
        question.setUpdatedAt(LocalDateTime.now());
        question.setVersion(question.getVersion() + 1);
        question.setDeck(deck);

        // Save the new version of the question
        questionRepository.save(question);

        logger.info("Successfully updated question with id: {} in deck with id: {}", questionId, deckId);
        return question;
    }

    public void deleteQuestion(Long deckId, Long questionId, User user) {
        logger.info("Attempting to delete question with id: {} from deck with id: {} for user with email: {}", questionId, deckId, user.getUsername());
        Deck deck = deckService.getDeckById(deckId, user);

        Question question = questionRepository.findByDeckAndQuestionId(deck, questionId)
                .orElseThrow(() -> {
                    logger.warn("Question with id: {} not found in deck with id: {}", questionId, deckId);
                    return new FlashDashException(
                            ErrorCode.E404003,
                            "Question with id " + questionId + " not found in deck with id " + deckId
                    );
                });

        questionRepository.delete(question);
        logger.info("Successfully deleted question with id: {} from deck with id: {}", questionId, deckId);
    }
}
