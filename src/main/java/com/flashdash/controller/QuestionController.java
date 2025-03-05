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
@RequestMapping("/decks/{deckFrn}/questions")
public class QuestionController {
    private final QuestionService questionService;

    public QuestionController(QuestionService questionService) {
        this.questionService = questionService;
    }

    @PostMapping
    public ResponseEntity<QuestionResponse> addQuestionToDeck(@PathVariable String deckFrn, @RequestBody QuestionRequest questionRequest) {
        String userFrn = getAuthenticatedUser();
        Question newQuestion = questionService.addQuestionToDeck(deckFrn, questionRequest, userFrn);
        return ResponseEntity.ok(EntityToResponseMapper.toQuestionResponse(newQuestion));
    }

    @GetMapping
    public ResponseEntity<List<QuestionResponse>> getAllQuestionsInDeck(@PathVariable String deckFrn) {
        String userFrn = getAuthenticatedUser();
        List<Question> questions = questionService.getAllQuestionsInDeck(deckFrn, userFrn);
        return ResponseEntity.ok(EntityToResponseMapper.toQuestionResponseList(questions));
    }

    @GetMapping("/{questionFrn}")
    public ResponseEntity<QuestionResponse> getQuestion(@PathVariable String deckFrn, @PathVariable String questionFrn) {
        String userFrn = getAuthenticatedUser();
        Question question = questionService.getQuestionByFrn(deckFrn, questionFrn, userFrn);
        return ResponseEntity.ok(EntityToResponseMapper.toQuestionResponse(question));
    }

    @PutMapping("/{questionFrn}")
    public ResponseEntity<QuestionResponse> updateQuestion(
            @PathVariable String deckFrn,
            @PathVariable String questionFrn,
            @RequestBody QuestionRequest questionRequest
    ) {
        String userFrn = getAuthenticatedUser();
        Question updatedQuestion = questionService.updateQuestion(deckFrn, questionFrn, questionRequest, userFrn);
        return ResponseEntity.ok(EntityToResponseMapper.toQuestionResponse(updatedQuestion));
    }

    @DeleteMapping("/{questionFrn}")
    public ResponseEntity<Void> deleteQuestion(@PathVariable String deckFrn, @PathVariable String questionFrn) {
        String userFrn = getAuthenticatedUser();
        questionService.deleteQuestion(deckFrn, questionFrn, userFrn);
        return ResponseEntity.noContent().build();
    }

    private String getAuthenticatedUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User user = (User) authentication.getPrincipal();
        return user.getUserFrn();
    }
}
