package com.flashdash.controller;

import com.flashdash.model.User;
import com.flashdash.model.question.Question;
import com.flashdash.service.QuestionService;
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
    public ResponseEntity<Question> addQuestionToDeck(@PathVariable Long deckId, @RequestBody Question question) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User user = (User) authentication.getPrincipal();

        Question newQuestion = questionService.addQuestionToDeck(deckId, question, user);
        return ResponseEntity.ok(newQuestion);
    }

    @GetMapping
    public ResponseEntity<List<Question>> getAllQuestionsInDeck(@PathVariable Long deckId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User user = (User) authentication.getPrincipal();

        List<Question> questions = questionService.getAllQuestionsInDeck(deckId, user);
        return ResponseEntity.ok(questions);
    }

    @GetMapping("/{questionId}")
    public ResponseEntity<Question> getQuestion(@PathVariable Long deckId, @PathVariable Long questionId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User user = (User) authentication.getPrincipal();

        Question question = questionService.getQuestionById(deckId, questionId, user);
        return ResponseEntity.ok(question);
    }

    @PutMapping("/{questionId}")
    public ResponseEntity<Question> updateQuestion(@PathVariable Long deckId, @PathVariable Long questionId, @RequestBody Question question) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User user = (User) authentication.getPrincipal();

        Question newQuestion = questionService.updateQuestion(deckId, questionId, question, user);
        return ResponseEntity.ok(newQuestion);
    }

    @DeleteMapping("/{questionId}")
    public ResponseEntity<Void> deleteQuestion(@PathVariable Long deckId, @PathVariable Long questionId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User user = (User) authentication.getPrincipal();

        questionService.deleteQuestion(deckId, questionId, user);
        return ResponseEntity.noContent().build();
    }
}
