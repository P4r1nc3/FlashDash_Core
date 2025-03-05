package com.flashdash.service;

import com.flashdash.model.User;
import com.p4r1nc3.flashdash.core.model.DeckRequest;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import com.flashdash.exception.ErrorCode;
import com.flashdash.exception.FlashDashException;
import com.flashdash.model.Deck;
import com.flashdash.repository.DeckRepository;
import com.flashdash.repository.QuestionRepository;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class DeckService {

    private static final Logger logger = LoggerFactory.getLogger(DeckService.class);

    private final DeckRepository deckRepository;
    private final QuestionRepository questionRepository;

    public DeckService(DeckRepository deckRepository, QuestionRepository questionRepository) {
        this.deckRepository = deckRepository;
        this.questionRepository = questionRepository;
    }

    public Deck createDeck(DeckRequest deckRequest, String userFrn) {
        logger.info("Creating new deck for user FRN: {}", userFrn);

        Deck deck = new Deck();
        deck.setDeckFrn(generateFrn("deck"));
        deck.setUserFrn(userFrn);
        deck.setName(deckRequest.getName());
        deck.setDescription(deckRequest.getDescription());
        deck.setCreatedAt(LocalDateTime.now());
        deck.setUpdatedAt(LocalDateTime.now());
        Deck savedDeck = deckRepository.save(deck);

        logger.info("Created deck with FRN: {} for user FRN: {}", savedDeck.getDeckFrn(), userFrn);
        return savedDeck;
    }

    public List<Deck> getAllDecks(String userFrn) {
        logger.info("Fetching all decks for user FRN: {}", userFrn);
        List<Deck> decks = deckRepository.findAllByUserFrn(userFrn);
        logger.info("Retrieved {} decks for user FRN: {}", decks.size(), userFrn);
        return decks;
    }

    public Deck getDeckByFrn(String deckFrn, String userFrn) {
        logger.info("Fetching deck with FRN: {} for user FRN: {}", deckFrn, userFrn);
        return deckRepository.findByDeckFrnAndUserFrn(deckFrn, userFrn)
                .orElseThrow(() -> {
                    logger.warn("Deck with FRN: {} not found for user FRN: {}", deckFrn, userFrn);
                    return new FlashDashException(ErrorCode.E404002, "Deck not found.");
                });
    }

    public Deck updateDeck(String deckFrn, DeckRequest deckRequest, String userFrn) {
        logger.info("Updating deck with FRN: {} for user FRN: {}", deckFrn, userFrn);

        Deck deck = getDeckByFrn(deckFrn, userFrn);
        deck.setName(deckRequest.getName());
        deck.setDescription(deckRequest.getDescription());
        deck.setUpdatedAt(LocalDateTime.now());

        Deck updatedDeck = deckRepository.save(deck);
        logger.info("Successfully updated deck with FRN: {} for user FRN: {}", deckFrn, userFrn);
        return updatedDeck;
    }

    @Transactional
    public void deleteDeck(String deckFrn, String userFrn) {
        logger.info("Deleting deck with FRN: {} for user FRN: {}", deckFrn, userFrn);

        Deck deck = getDeckByFrn(deckFrn, userFrn);
        questionRepository.deleteAllByDeckFrn(deckFrn);
        deckRepository.delete(deck);

        logger.info("Deleted deck with FRN: {} for user FRN: {}", deckFrn, userFrn);
    }

    @Transactional
    public void deleteAllDecksForUser(String userFrn) {
        logger.info("Attempting to delete all decks for user FRN: {}", userFrn);

        List<Deck> decks = getAllDecks(userFrn);

        if (decks.isEmpty()) {
            logger.info("No decks found for user FRN: {}", userFrn);
            return;
        }

        decks.forEach(deck -> deleteDeck(deck.getDeckFrn(), userFrn));

        logger.info("Successfully deleted all decks for userFRN: {}", userFrn);
    }

    private String generateFrn(String resourceType) {
        return "frn:flashdash:" + resourceType + ":" + java.util.UUID.randomUUID();
    }
}
