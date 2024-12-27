package com.flashdash.service;

import com.flashdash.exception.ErrorCode;
import com.flashdash.exception.FlashDashException;
import com.flashdash.model.Deck;
import com.flashdash.model.User;
import com.flashdash.model.Question;
import com.flashdash.repository.QuestionRepository;
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

    public Question addQuestionToDeck(Long deckId, Question question, User user) {
        logger.info("Attempting to add question to deck with id: {} for user with email: {}", deckId, user.getUsername());
        Deck deck = deckService.getDeckById(deckId, user);
        logger.info("Deck with id: {} successfully retrieved for adding question", deckId);

        question.setDeck(deck);
        question.setDeleted(false);
        question.setCreatedAt(LocalDateTime.now());
        question.setUpdatedAt(LocalDateTime.now());
        Question savedQuestion = questionRepository.save(question);

        logger.info("Question successfully added to deck with id: {}. Question ID: {}", deckId, savedQuestion.getQuestionId());
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

    public Question updateQuestion(Long deckId, Long questionId, Question questionDetails, User user) {
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

        // Create a new version of the question with the updated details
        Question updatedQuestion = new Question();
        updatedQuestion.setDeck(deck);
        updatedQuestion.setQuestion(questionDetails.getQuestion());
        updatedQuestion.setCorrectAnswers(questionDetails.getCorrectAnswers());
        updatedQuestion.setIncorrectAnswers(questionDetails.getIncorrectAnswers());
        updatedQuestion.setDifficulty(questionDetails.getDifficulty());
        updatedQuestion.setCreatedAt(LocalDateTime.now());
        updatedQuestion.setUpdatedAt(LocalDateTime.now());
        updatedQuestion.setVersion(question.getVersion() + 1);

        // Save the new version of the question
        questionRepository.save(updatedQuestion);

        // Mark the old question as deleted or update its status if needed
        question.setDeleted(true);
        questionRepository.save(question);

        logger.info("Successfully updated question with id: {} in deck with id: {}", questionId, deckId);
        return updatedQuestion;
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

        questionRepository.softDeleteQuestion(question);
        logger.info("Successfully marked question with id: {} as deleted from deck with id: {}", questionId, deckId);
    }
}
