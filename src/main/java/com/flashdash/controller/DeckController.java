package com.flashdash.controller;

import com.flashdash.model.Deck;
import com.flashdash.model.User;
import com.flashdash.service.DeckService;
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
    public ResponseEntity<Deck> createDeck(@RequestBody Deck deck) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User user = (User) authentication.getPrincipal();

        Deck newDeck = deckService.createDeck(deck, user);
        return ResponseEntity.ok(newDeck);
    }

    @GetMapping
    public ResponseEntity<List<Deck>> getAllDecks() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User user = (User) authentication.getPrincipal();

        List<Deck> decks = deckService.getAllDecks(user);
        return ResponseEntity.ok(decks);
    }

    @GetMapping("/{deckId}")
    public ResponseEntity<Deck> getDeck(@PathVariable Long deckId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User user = (User) authentication.getPrincipal();

        Deck deck = deckService.getDeckById(deckId, user);
        return ResponseEntity.ok(deck);
    }

    @PutMapping("/{deckId}")
    public ResponseEntity<Deck> updateDeck(@PathVariable Long deckId, @RequestBody Deck deck) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User user = (User) authentication.getPrincipal();

        Deck newDeck = deckService.updateDeck(deckId, deck, user);
        return ResponseEntity.ok(newDeck);
    }

    @DeleteMapping("/{deckId}")
    public ResponseEntity<Void> deleteDeck(@PathVariable Long deckId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User user = (User) authentication.getPrincipal();

        deckService.deleteDeck(deckId, user);
        return ResponseEntity.noContent().build();
    }
}
