package com.flashdash.core.service;

import com.flashdash.core.model.Question;
import com.p4r1nc3.flashdash.core.model.GenerateQuestionsRequest;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class GenerationService {

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

        return chatClient.prompt(promptTemplate.create())
                .call()
                .entity(new ParameterizedTypeReference<>() {});
    }
}
