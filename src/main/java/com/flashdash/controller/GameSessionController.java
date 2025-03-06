package com.flashdash.controller;

import com.flashdash.model.GameSession;
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
@RequestMapping("/decks/{deckFrn}/gameSessions")
public class GameSessionController {

    private final GameSessionService gameSessionService;
    private final EntityToResponseMapper entityToResponseMapper;

    public GameSessionController(GameSessionService gameSessionService, EntityToResponseMapper entityToResponseMapper) {
        this.gameSessionService = gameSessionService;
        this.entityToResponseMapper = entityToResponseMapper;
    }

    @PostMapping("/start")
    public ResponseEntity<List<QuestionResponse>> startGameSession(@PathVariable String deckFrn) {
        String userFrn = getAuthenticatedUser();
        List<Question> questions = gameSessionService.startGameSession(deckFrn, userFrn);
        return ResponseEntity.ok(entityToResponseMapper.mapToQuestionResponse(questions));
    }

    @PostMapping("/end")
    public ResponseEntity<GameSessionResponse> endGameSession(@PathVariable String deckFrn, @RequestBody List<QuestionRequest> userAnswers) {
        String userFrn = getAuthenticatedUser();
        GameSession gameSession = gameSessionService.endGameSession(deckFrn, userFrn, userAnswers);
        return ResponseEntity.ok(entityToResponseMapper.mapToGameSessionResponse(gameSession));
    }

    @GetMapping
    public ResponseEntity<List<GameSessionResponse>> getGameSessions(@PathVariable String deckFrn) {
        String userFrn = getAuthenticatedUser();
        List<GameSession> gameSessionList = gameSessionService.getGameSessions(deckFrn, userFrn);
        return ResponseEntity.ok(entityToResponseMapper.mapToGameSessionResponse(gameSessionList));
    }

    @GetMapping("/{gameSessionFrn}")
    public ResponseEntity<GameSessionResponse> getGameSession(@PathVariable String deckFrn, @PathVariable String gameSessionFrn) {
        String userFrn = getAuthenticatedUser();
        GameSession gameSession = gameSessionService.getGameSession(deckFrn, gameSessionFrn, userFrn);
        return ResponseEntity.ok(entityToResponseMapper.mapToGameSessionResponse(gameSession));
    }

    private String getAuthenticatedUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User user = (User) authentication.getPrincipal();
        return user.getUserFrn();
    }
}
