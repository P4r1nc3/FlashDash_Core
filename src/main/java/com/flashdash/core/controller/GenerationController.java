package com.flashdash.core.controller;

import com.flashdash.core.model.Question;
import com.flashdash.core.service.GenerationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/generate")
public class GenerationController {
    private final GenerationService generationService;

    public GenerationController(GenerationService generationService) {
        this.generationService = generationService;
    }

    @PostMapping("/end")
    public ResponseEntity<List<Question>> generate(@RequestParam int count, @RequestParam String topic) {
        return ResponseEntity.ok(generationService.generateQuestions(count, topic));
    }
}
