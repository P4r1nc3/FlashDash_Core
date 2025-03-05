package com.flashdash.service;

import com.flashdash.exception.ErrorCode;
import com.flashdash.exception.FlashDashException;
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

    public Question addQuestionToDeck(String deckFrn, QuestionRequest questionRequest, String userFrn) {
        logger.info("Adding question to deck with FRN: {} for user FRN: {}", deckFrn, userFrn);

        deckService.getDeckByFrn(deckFrn, userFrn);

        Question question = new Question();
        question.setQuestionFrn(generateFrn("question"));
        question.setDeckFrn(deckFrn);
        question.setQuestion(questionRequest.getQuestion());
        question.setCorrectAnswers(questionRequest.getCorrectAnswers());
        question.setIncorrectAnswers(questionRequest.getIncorrectAnswers());
        question.setDifficulty(questionRequest.getDifficulty().name());
        question.setCreatedAt(LocalDateTime.now());
        question.setUpdatedAt(LocalDateTime.now());

        Question savedQuestion = questionRepository.save(question);
        logger.info("Added question with FRN: {} to deck with FRN: {}", savedQuestion.getQuestionFrn(), deckFrn);
        return savedQuestion;
    }

    public List<Question> getAllQuestionsInDeck(String deckFrn, String userFrn) {
        logger.info("Fetching all questions for deck FRN: {} and user FRN: {}", deckFrn, userFrn);
        deckService.getDeckByFrn(deckFrn, userFrn);
        List<Question> questions = questionRepository.findAllByDeckFrn(deckFrn);
        logger.info("Retrieved {} questions for deck FRN: {}", questions.size(), deckFrn);
        return questions;
    }

    public Question getQuestionByFrn(String deckFrn, String questionFrn, String userFrn) {
        logger.info("Fetching question FRN: {} from deck FRN: {} for user FRN: {}", questionFrn, deckFrn, userFrn);
        deckService.getDeckByFrn(deckFrn, userFrn);

        return questionRepository.findByDeckFrnAndQuestionFrn(deckFrn, questionFrn)
                .orElseThrow(() -> {
                    logger.warn("Question with FRN: {} not found in deck FRN: {}", questionFrn, deckFrn);
                    return new FlashDashException(ErrorCode.E404003, "Question not found.");
                });
    }

    public Question updateQuestion(String deckFrn, String questionFrn, QuestionRequest questionRequest, String userFrn) {
        logger.info("Updating question FRN: {} in deck FRN: {} for user FRN: {}", questionFrn, deckFrn, userFrn);

        Question question = getQuestionByFrn(deckFrn, questionFrn, userFrn);
        question.setQuestion(questionRequest.getQuestion());
        question.setCorrectAnswers(questionRequest.getCorrectAnswers());
        question.setIncorrectAnswers(questionRequest.getIncorrectAnswers());
        question.setDifficulty(questionRequest.getDifficulty().name());
        question.setUpdatedAt(LocalDateTime.now());

        Question updatedQuestion = questionRepository.save(question);
        logger.info("Successfully updated question FRN: {} in deck FRN: {}", questionFrn, deckFrn);
        return updatedQuestion;
    }

    public void deleteQuestion(String deckFrn, String questionFrn, String userFrn) {
        logger.info("Deleting question FRN: {} from deck FRN: {} for user FRN: {}", questionFrn, deckFrn, userFrn);

        Question question = getQuestionByFrn(deckFrn, questionFrn, userFrn);
        questionRepository.delete(question);

        logger.info("Successfully deleted question FRN: {} from deck FRN: {}", questionFrn, deckFrn);
    }

    private String generateFrn(String resourceType) {
        return "frn:flashdash:" + resourceType + ":" + java.util.UUID.randomUUID();
    }
}
