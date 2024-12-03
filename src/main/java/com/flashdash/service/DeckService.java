package com.flashdash.service;

import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;
import com.flashdash.model.Deck;
import com.flashdash.model.User;
import com.flashdash.model.card.CardDeck;
import com.flashdash.model.question.QuestionDeck;
import com.flashdash.repository.CardRepository;
import com.flashdash.repository.DeckRepository;
import com.flashdash.repository.QuestionRepository;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class DeckService {
    private final DeckRepository deckRepository;
    private final CardRepository cardRepository;
    private final QuestionRepository questionRepository;

    public DeckService(DeckRepository deckRepository, CardRepository cardRepository, QuestionRepository questionRepository) {
        this.deckRepository = deckRepository;
        this.cardRepository = cardRepository;
        this.questionRepository = questionRepository;
    }

    public Deck createDeck(Deck deck, User user) {
        deck.setUser(user);
        deck.setCreatedAt(LocalDateTime.now());
        deck.setUpdatedAt(LocalDateTime.now());

        return deckRepository.save(deck);
    }

    public List<Deck> getAllDecks(User user) {
        return deckRepository.findAllByUser(user);
    }

    public Deck getDeckById(Long deckId, User user) {
        return deckRepository.findByIdAndUser(deckId, user)
                .orElseThrow(() -> new RuntimeException("Deck not found with id: " + deckId));
    }

    public Deck updateDeck(Long deckId, Deck deckDetails, User user) {
        Deck deck = deckRepository.findByIdAndUser(deckId, user)
                .orElseThrow(() -> new RuntimeException("Deck not found with id: " + deckId));

        if (!deck.getClass().equals(deckDetails.getClass())) {
            throw new RuntimeException("Changing deckType is not allowed.");
        }

        deck.setName(deckDetails.getName());
        deck.setDescription(deckDetails.getDescription());
        deck.setUpdatedAt(LocalDateTime.now());

        return deckRepository.save(deck);
    }

    @Transactional
    public void deleteDeck(Long deckId, User user) {
        Deck deck = deckRepository.findByIdAndUser(deckId, user)
                .orElseThrow(() -> new RuntimeException("Deck not found with id: " + deckId));

        if (deck instanceof CardDeck) cardRepository.deleteAllByDeck(deck);
        if (deck instanceof QuestionDeck) questionRepository.deleteAllByDeck(deck);
        deckRepository.delete(deck);
    }
}
