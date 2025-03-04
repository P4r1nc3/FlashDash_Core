package com.flashdash.controller;

import com.flashdash.model.Question;
import com.flashdash.model.User;
import com.flashdash.service.GameSessionService;
import com.flashdash.utils.EntityToResponseMapper;
import com.p4r1nc3.flashdash.core.model.GameSessionResponse;
import com.p4r1nc3.flashdash.core.model.QuestionRequest;
import com.p4r1nc3.flashdash.core.model.QuestionResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/decks/{deckId}/gameSessions")
public class GameSessionController {

    private final GameSessionService gameSessionService;

    public GameSessionController(GameSessionService gameSessionService) {
        this.gameSessionService = gameSessionService;
    }

    @PostMapping("/start")
    public ResponseEntity<List<QuestionResponse>> startGameSession(@PathVariable Long deckId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User user = (User) authentication.getPrincipal();

        List<Question> questions = gameSessionService.startGameSession(deckId, user);
        return ResponseEntity.ok(EntityToResponseMapper.toQuestionResponseList(questions));
    }

    @PostMapping("/end")
    public ResponseEntity<GameSessionResponse> endGameSession(@PathVariable Long deckId, @RequestBody List<QuestionRequest> userAnswers) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User user = (User) authentication.getPrincipal();

        return ResponseEntity.ok(EntityToResponseMapper.toGameSessionResponse(gameSessionService.endGameSession(deckId, user, userAnswers)));
    }

    @GetMapping
    public ResponseEntity<List<GameSessionResponse>> getGameSessions(@PathVariable Long deckId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User user = (User) authentication.getPrincipal();

        return ResponseEntity.ok(EntityToResponseMapper.toGameSessionResponseList(gameSessionService.getGameSessions(deckId, user)));
    }

    @GetMapping("/{gameSessionId}")
    public ResponseEntity<GameSessionResponse> getGameSession(@PathVariable Long deckId, @PathVariable Long gameSessionId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User user = (User) authentication.getPrincipal();

        return ResponseEntity.ok(EntityToResponseMapper.toGameSessionResponse(gameSessionService.getGameSession(deckId, gameSessionId, user)));
    }
}
