package com.flashdash.controller;

import com.flashdash.model.User;
import com.flashdash.model.Question;
import com.flashdash.service.QuestionService;
import com.flashdash.utils.EntityToResponseMapper;
import com.p4r1nc3.flashdash.core.model.QuestionRequest;
import com.p4r1nc3.flashdash.core.model.QuestionResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/decks/{deckId}/questions")
public class QuestionController {
    private final QuestionService questionService;

    public QuestionController(QuestionService questionService) {
        this.questionService = questionService;
    }

    @PostMapping
    public ResponseEntity<QuestionResponse> addQuestionToDeck(@PathVariable Long deckId, @RequestBody QuestionRequest questionRequest) {
        User user = getAuthenticatedUser();
        Question newQuestion = questionService.addQuestionToDeck(deckId, questionRequest, user);
        return ResponseEntity.ok(EntityToResponseMapper.toQuestionResponse(newQuestion));
    }

    @GetMapping
    public ResponseEntity<List<QuestionResponse>> getAllQuestionsInDeck(@PathVariable Long deckId) {
        User user = getAuthenticatedUser();
        List<Question> questions = questionService.getAllQuestionsInDeck(deckId, user);
        return ResponseEntity.ok(EntityToResponseMapper.toQuestionResponseList(questions));
    }

    @GetMapping("/{questionId}")
    public ResponseEntity<QuestionResponse> getQuestion(@PathVariable Long deckId, @PathVariable Long questionId) {
        User user = getAuthenticatedUser();
        Question question = questionService.getQuestionById(deckId, questionId, user);
        return ResponseEntity.ok(EntityToResponseMapper.toQuestionResponse(question));
    }

    @PutMapping("/{questionId}")
    public ResponseEntity<QuestionResponse> updateQuestion(@PathVariable Long deckId, @PathVariable Long questionId, @RequestBody QuestionRequest questionRequest) {
        User user = getAuthenticatedUser();
        Question updatedQuestion = questionService.updateQuestion(deckId, questionId, questionRequest, user);
        return ResponseEntity.ok(EntityToResponseMapper.toQuestionResponse(updatedQuestion));
    }

    @DeleteMapping("/{questionId}")
    public ResponseEntity<Void> deleteQuestion(@PathVariable Long deckId, @PathVariable Long questionId) {
        User user = getAuthenticatedUser();
        questionService.deleteQuestion(deckId, questionId, user);
        return ResponseEntity.noContent().build();
    }

    private User getAuthenticatedUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return (User) authentication.getPrincipal();
    }
}
