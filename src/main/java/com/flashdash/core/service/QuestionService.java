package com.flashdash.core.service;

import com.flashdash.core.exception.ErrorCode;
import com.flashdash.core.exception.FlashDashException;
import com.flashdash.core.model.Question;
import com.flashdash.core.repository.QuestionRepository;
import com.flashdash.core.service.api.ActivityService;
import com.flashdash.core.utils.FrnGenerator;
import com.flashdash.core.utils.ResourceType;
import com.p4r1nc3.flashdash.activity.model.LogActivityRequest.ActivityTypeEnum;
import com.p4r1nc3.flashdash.core.model.GenerateQuestionsRequest;
import com.p4r1nc3.flashdash.core.model.QuestionRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class QuestionService {

    private static final Logger logger = LoggerFactory.getLogger(QuestionService.class);

    private final ActivityService activityService;
    private final GenerationService generationService;
    private final DeckService deckService;
    private final QuestionRepository questionRepository;

    public QuestionService(ActivityService activityService,
                           GenerationService generationService,
                           DeckService deckService,
                           QuestionRepository questionRepository) {
        this.activityService = activityService;
        this.generationService = generationService;
        this.deckService = deckService;
        this.questionRepository = questionRepository;
    }

    public Question addQuestionToDeck(String deckFrn, QuestionRequest questionRequest, String userFrn) {
        logger.info("Adding question to deck with FRN: {} for user FRN: {}", deckFrn, userFrn);

        deckService.getDeckByFrn(deckFrn, userFrn);

        List<Question> existingQuestions = questionRepository.findAllByDeckFrn(deckFrn);
        for (Question existingQuestion : existingQuestions) {
            if (existingQuestion.getQuestion().equalsIgnoreCase(questionRequest.getQuestion().trim())) {
                logger.warn("Question with the same text already exists in deck FRN: {}", deckFrn);
                throw new FlashDashException(ErrorCode.E400004, "Question with the same text already exists in this deck.");
            }
        }

        Set<String> allAnswers = new HashSet<>();

        for (String correctAnswer : questionRequest.getCorrectAnswers()) {
            String trimmedAnswer = correctAnswer.trim();
            if (!allAnswers.add(trimmedAnswer)) {
                logger.warn("Duplicate answer found in question request: {}", trimmedAnswer);
                throw new FlashDashException(ErrorCode.E400005, "Duplicate answer found. Each answer must be unique.");
            }
        }

        for (String incorrectAnswer : questionRequest.getIncorrectAnswers()) {
            String trimmedAnswer = incorrectAnswer.trim();
            if (!allAnswers.add(trimmedAnswer)) {
                logger.warn("Duplicate answer found in question request: {}", trimmedAnswer);
                throw new FlashDashException(ErrorCode.E400005, "Duplicate answer found. Each answer must be unique.");
            }
        }

        Question question = new Question();
        question.setQuestionFrn(FrnGenerator.generateFrn(ResourceType.QUESTION));
        question.setDeckFrn(deckFrn);
        question.setQuestion(questionRequest.getQuestion().trim());
        question.setCorrectAnswers(questionRequest.getCorrectAnswers());
        question.setIncorrectAnswers(questionRequest.getIncorrectAnswers());
        question.setDifficulty(questionRequest.getDifficulty());
        question.setCreatedAt(LocalDateTime.now());
        question.setUpdatedAt(LocalDateTime.now());

        Question savedQuestion = questionRepository.save(question);
        activityService.logUserActivity(userFrn, question.getQuestion(), ActivityTypeEnum.QUESTION_CREATED);
        logger.info("Added question with FRN: {} to deck with FRN: {}", savedQuestion.getQuestionFrn(), deckFrn);

        return savedQuestion;
    }

    public List<Question> generateQuestions(String deckFrn, GenerateQuestionsRequest requestBody, String userFrn) {
        if (requestBody.getCount() < 0 || requestBody.getCount() > 10) {
            logger.error("Invalid count value: {}. Count must be between 0 and 10. deckFrn={}, language={}, difficulty={}, prompt='{}'",
                    requestBody.getCount(),
                    deckFrn,
                    requestBody.getLanguage(),
                    requestBody.getDifficulty(),
                    requestBody.getPrompt()
            );
            throw new FlashDashException(ErrorCode.E400006, "Count must be between 0 and 10");
        }

        logger.info("Starting question generation: count={}, language={}, difficulty={}, prompt='{}', deckFrn={}",
                requestBody.getCount(),
                requestBody.getLanguage(),
                requestBody.getDifficulty(),
                requestBody.getPrompt(),
                deckFrn
        );

        List<Question> generatedQuestions = generationService.generateQuestions(requestBody);

        for (Question question : generatedQuestions) {
            question.setQuestionFrn(FrnGenerator.generateFrn(ResourceType.QUESTION));
            question.setDeckFrn(deckFrn);
            question.setCreatedAt(LocalDateTime.now());
            question.setUpdatedAt(LocalDateTime.now());
        }

        List<Question> savedQuestions = questionRepository.saveAll(generatedQuestions);

        logger.info("Successfully generated and saved {} questions for deckFrn={}", savedQuestions.size(), deckFrn);
        activityService.logUserActivity(userFrn, deckFrn, ActivityTypeEnum.QUESTIONS_GENERATED);

        return savedQuestions;
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

        String newQuestionText = questionRequest.getQuestion().trim();
        if (!newQuestionText.equalsIgnoreCase(question.getQuestion())) {
            List<Question> existingQuestions = questionRepository.findAllByDeckFrn(deckFrn);
            for (Question existingQuestion : existingQuestions) {
                if (!existingQuestion.getQuestionFrn().equals(questionFrn) &&
                        existingQuestion.getQuestion().equalsIgnoreCase(newQuestionText)) {
                    logger.warn("Cannot update question. A question with the same text already exists in deck FRN: {}", deckFrn);
                    throw new FlashDashException(ErrorCode.E400004, "Question with the same text already exists in this deck.");
                }
            }
        }

        Set<String> allAnswers = new HashSet<>();

        for (String correctAnswer : questionRequest.getCorrectAnswers()) {
            String trimmedAnswer = correctAnswer.trim();
            if (!allAnswers.add(trimmedAnswer)) {
                logger.warn("Duplicate answer found in question update: {}", trimmedAnswer);
                throw new FlashDashException(ErrorCode.E400005, "Duplicate answer found. Each answer must be unique.");
            }
        }

        for (String incorrectAnswer : questionRequest.getIncorrectAnswers()) {
            String trimmedAnswer = incorrectAnswer.trim();
            if (!allAnswers.add(trimmedAnswer)) {
                logger.warn("Duplicate answer found in question update: {}", trimmedAnswer);
                throw new FlashDashException(ErrorCode.E400005, "Duplicate answer found. Each answer must be unique.");
            }
        }

        question.setQuestion(newQuestionText);
        question.setCorrectAnswers(questionRequest.getCorrectAnswers());
        question.setIncorrectAnswers(questionRequest.getIncorrectAnswers());
        question.setDifficulty(questionRequest.getDifficulty());
        question.setUpdatedAt(LocalDateTime.now());

        Question updatedQuestion = questionRepository.save(question);
        activityService.logUserActivity(userFrn, question.getQuestion(), ActivityTypeEnum.QUESTION_UPDATED);
        logger.info("Successfully updated question FRN: {} in deck FRN: {}", questionFrn, deckFrn);

        return updatedQuestion;
    }

    public void deleteQuestion(String deckFrn, String questionFrn, String userFrn) {
        logger.info("Deleting question FRN: {} from deck FRN: {} for user FRN: {}", questionFrn, deckFrn, userFrn);

        Question question = getQuestionByFrn(deckFrn, questionFrn, userFrn);
        questionRepository.delete(question);
        activityService.logUserActivity(userFrn, question.getQuestion(), ActivityTypeEnum.QUESTION_DELETED);

        logger.info("Successfully deleted question FRN: {} from deck FRN: {}", questionFrn, deckFrn);
    }
}
