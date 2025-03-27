package com.flashdash.core.service;

import com.flashdash.core.exception.FlashDashException;
import com.flashdash.core.model.Question;
import com.p4r1nc3.flashdash.core.model.GenerateQuestionsRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

import static com.flashdash.core.exception.ErrorCode.E500001;

@Service
public class GenerationService {

    private static final Logger logger = LoggerFactory.getLogger(GenerationService.class);
    private final ChatClient chatClient;

    public GenerationService(ChatClient.Builder chatClientBuilder) {
        this.chatClient = chatClientBuilder.build();
    }

    public List<Question> generateQuestions(GenerateQuestionsRequest requestBody) {
        String promptText = """
                Generate a list of {count} questions with the difficulty {difficulty} about the topic: {topic}.
                Both questions and answers should be in {language} language.
                """;

        PromptTemplate promptTemplate = new PromptTemplate(promptText,
                Map.of(
                        "count", String.valueOf(requestBody.getCount()),
                        "difficulty", String.valueOf(requestBody.getDifficulty()),
                        "topic", requestBody.getPrompt(),
                        "language", requestBody.getLanguage()
                ));

        try {
            return chatClient.prompt(promptTemplate.create())
                    .call()
                    .entity(new ParameterizedTypeReference<>() {
                    });
        } catch (Exception e) {
            logger.error("Failed to generate questions: " + e.getMessage(), e);
            throw new FlashDashException(E500001, "Failed to generate questions: " + e.getMessage() );
        }
    }
}
