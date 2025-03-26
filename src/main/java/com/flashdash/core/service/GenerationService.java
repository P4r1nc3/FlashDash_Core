package com.flashdash.core.service;

import com.flashdash.core.model.Question;
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

    public List<Question> generateQuestions(String topic, String language, int count) {
        String promptText = """
                Generate a list of {count} questions about the topic: {topic}.
                Both questions and answers should be in {language} language.
                """;

        PromptTemplate promptTemplate = new PromptTemplate(promptText,
                Map.of(
                        "count", String.valueOf(count),
                        "topic", topic,
                        "language", language
                ));

        return chatClient.prompt(promptTemplate.create())
                .call()
                .entity(new ParameterizedTypeReference<>() {});
    }
}
