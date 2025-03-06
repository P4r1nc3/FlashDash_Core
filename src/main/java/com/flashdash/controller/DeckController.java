package com.flashdash.controller;

import com.flashdash.model.Deck;
import com.flashdash.model.User;
import com.flashdash.service.DeckService;
import com.flashdash.utils.EntityToResponseMapper;
import com.p4r1nc3.flashdash.core.model.DeckRequest;
import com.p4r1nc3.flashdash.core.model.DeckResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;

@RestController
@RequestMapping("/decks")
public class DeckController {
    private final DeckService deckService;
    private final EntityToResponseMapper entityToResponseMapper;

    public DeckController(DeckService deckService, EntityToResponseMapper entityToResponseMapper) {
        this.deckService = deckService;
        this.entityToResponseMapper = entityToResponseMapper;
    }

    @PostMapping
    public ResponseEntity<DeckResponse> createDeck(@RequestBody DeckRequest deckRequest) {
        String userFrn = getAuthenticatedUser();
        Deck deck = deckService.createDeck(deckRequest, userFrn);
        return ResponseEntity.ok(entityToResponseMapper.mapToDeckResponse(deck));
    }

    @GetMapping
    public ResponseEntity<List<DeckResponse>> getAllDecks() {
        String userFrn = getAuthenticatedUser();
        List<Deck> deckList = deckService.getAllDecks(userFrn);
        return ResponseEntity.ok(entityToResponseMapper.mapToDeckResponse(deckList));
    }

    @GetMapping("/{deckFrn}")
    public ResponseEntity<DeckResponse> getDeck(@PathVariable String deckFrn) {
        String userFrn = getAuthenticatedUser();
        Deck deck = deckService.getDeckByFrn(deckFrn, userFrn);
        return ResponseEntity.ok(entityToResponseMapper.mapToDeckResponse(deck));
    }

    @PutMapping("/{deckFrn}")
    public ResponseEntity<DeckResponse> updateDeck(@PathVariable String deckFrn, @RequestBody DeckRequest deckRequest) {
        String userFrn = getAuthenticatedUser();
        Deck deck = deckService.updateDeck(deckFrn, deckRequest, userFrn);
        return ResponseEntity.ok(entityToResponseMapper.mapToDeckResponse(deck));
    }

    @DeleteMapping("/{deckFrn}")
    public ResponseEntity<Void> deleteDeck(@PathVariable String deckFrn) {
        String userFrn = getAuthenticatedUser();
        deckService.deleteDeck(deckFrn, userFrn);
        return ResponseEntity.noContent().build();
    }

    private String getAuthenticatedUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User user = (User) authentication.getPrincipal();
        return user.getUserFrn();
    }
}
