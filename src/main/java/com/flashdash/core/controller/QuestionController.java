package com.flashdash.core.controller;

import com.flashdash.core.model.User;
import com.flashdash.core.model.Question;
import com.flashdash.core.service.QuestionService;
import com.flashdash.core.utils.EntityToResponseMapper;
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
    private final EntityToResponseMapper entityToResponseMapper;

    public QuestionController(QuestionService questionService, EntityToResponseMapper entityToResponseMapper) {
        this.questionService = questionService;
        this.entityToResponseMapper = entityToResponseMapper;
    }

    @PostMapping
    public ResponseEntity<QuestionResponse> addQuestionToDeck(@PathVariable String deckFrn, @RequestBody QuestionRequest questionRequest) {
        String userFrn = getAuthenticatedUser();
        Question newQuestion = questionService.addQuestionToDeck(deckFrn, questionRequest, userFrn);
        return ResponseEntity.ok(entityToResponseMapper.mapToQuestionResponse(newQuestion));
    }

    @PostMapping("/generate")
    public ResponseEntity<List<QuestionResponse>> generateQuestions(
            @PathVariable String deckFrn,
            @RequestParam String prompt,
            @RequestParam(defaultValue = "english") String language,
            @RequestParam(defaultValue = "5") int count) {
        List<Question> questions = questionService.generateQuestions(deckFrn, prompt, language, count);
        return ResponseEntity.ok(entityToResponseMapper.mapToQuestionResponse(questions));
    }

    @GetMapping
    public ResponseEntity<List<QuestionResponse>> getAllQuestionsInDeck(@PathVariable String deckFrn) {
        String userFrn = getAuthenticatedUser();
        List<Question> questions = questionService.getAllQuestionsInDeck(deckFrn, userFrn);
        return ResponseEntity.ok(entityToResponseMapper.mapToQuestionResponse(questions));
    }

    @GetMapping("/{questionFrn}")
    public ResponseEntity<QuestionResponse> getQuestion(@PathVariable String deckFrn, @PathVariable String questionFrn) {
        String userFrn = getAuthenticatedUser();
        Question question = questionService.getQuestionByFrn(deckFrn, questionFrn, userFrn);
        return ResponseEntity.ok(entityToResponseMapper.mapToQuestionResponse(question));
    }

    @PutMapping("/{questionFrn}")
    public ResponseEntity<QuestionResponse> updateQuestion(
            @PathVariable String deckFrn,
            @PathVariable String questionFrn,
            @RequestBody QuestionRequest questionRequest
    ) {
        String userFrn = getAuthenticatedUser();
        Question updatedQuestion = questionService.updateQuestion(deckFrn, questionFrn, questionRequest, userFrn);
        return ResponseEntity.ok(entityToResponseMapper.mapToQuestionResponse(updatedQuestion));
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
