package com.flashdash.controller;

import com.flashdash.dto.response.GameSessionResult;
import com.flashdash.model.Question;
import com.flashdash.model.User;
import com.flashdash.service.GameSessionService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/decks")
public class GameSessionController {

    private final GameSessionService gameSessionService;

    public GameSessionController(GameSessionService gameSessionService) {
        this.gameSessionService = gameSessionService;
    }

    @PostMapping("/{deckId}/gameSessions/start")
    public ResponseEntity<List<Question>> startGameSession(@PathVariable Long deckId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User user = (User) authentication.getPrincipal();

        List<Question> questions = gameSessionService.startGameSession(deckId, user);
        return ResponseEntity.ok(questions);
    }


    @PostMapping("/{deckId}/gameSessions/end")
    public ResponseEntity<GameSessionResult> endGameSession(@PathVariable Long deckId, @RequestBody List<Question> userAnswers) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User user = (User) authentication.getPrincipal();

        GameSessionResult result = gameSessionService.endGameSession(deckId, user, userAnswers);
        return ResponseEntity.ok(result);
    }
}
