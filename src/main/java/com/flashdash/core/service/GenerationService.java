package com.flashdash.core.service;

import com.flashdash.core.model.Question;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class GenerationService {

    private final ChatClient chatClient;

    public GenerationService(ChatClient.Builder chatClientBuilder) {
        this.chatClient = chatClientBuilder.build();
    }

    public List<Question> generateQuestions(int count, String topic) {
        PromptTemplate promptTemplate = new PromptTemplate("Return a list of " + count + " questions, associated with the following topic: " + topic);

        return chatClient.prompt(promptTemplate.create())
                .call()
                .entity(new ParameterizedTypeReference<>() {});
    }
}
