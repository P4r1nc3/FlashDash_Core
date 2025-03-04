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

    public DeckController(DeckService deckService) {
        this.deckService = deckService;
    }

    @PostMapping
    public ResponseEntity<DeckResponse> createDeck(@RequestBody DeckRequest deckRequest) {
        User user = getAuthenticatedUser();
        Deck deck = deckService.createDeck(deckRequest, user);
        return ResponseEntity.ok(EntityToResponseMapper.toDeckResponse(deck));
    }

    @GetMapping
    public ResponseEntity<List<DeckResponse>> getAllDecks() {
        User user = getAuthenticatedUser();
        List<Deck> deckList = deckService.getAllDecks(user);
        return ResponseEntity.ok(EntityToResponseMapper.toDeckResponseList(deckList));
    }

    @GetMapping("/{deckId}")
    public ResponseEntity<DeckResponse> getDeck(@PathVariable Long deckId) {
        User user = getAuthenticatedUser();
        Deck deck = deckService.getDeckById(deckId, user);
        return ResponseEntity.ok(EntityToResponseMapper.toDeckResponse(deck));
    }

    @PutMapping("/{deckId}")
    public ResponseEntity<DeckResponse> updateDeck(@PathVariable Long deckId, @RequestBody DeckRequest deckRequest) {
        User user = getAuthenticatedUser();
        Deck deck = deckService.updateDeck(deckId, deckRequest, user);
        return ResponseEntity.ok(EntityToResponseMapper.toDeckResponse(deck));
    }

    @DeleteMapping("/{deckId}")
    public ResponseEntity<Void> deleteDeck(@PathVariable Long deckId) {
        User user = getAuthenticatedUser();
        deckService.deleteDeck(deckId, user);
        return ResponseEntity.noContent().build();
    }

    private User getAuthenticatedUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return (User) authentication.getPrincipal();
    }
}
