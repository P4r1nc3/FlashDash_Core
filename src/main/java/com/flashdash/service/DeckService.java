package com.flashdash.service;

import com.p4r1nc3.flashdash.core.model.DeckRequest;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import com.flashdash.exception.ErrorCode;
import com.flashdash.exception.FlashDashException;
import com.flashdash.model.Deck;
import com.flashdash.model.User;
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

    public Deck createDeck(DeckRequest deckRequest, User user) {
        logger.info("Attempting to create a new deck for user with email: {}", user.getUsername());

        Deck deck = new Deck();
        deck.setUser(user);
        deck.setName(deckRequest.getName());
        deck.setDescription(deckRequest.getDescription());
        deck.setCreatedAt(LocalDateTime.now());
        deck.setUpdatedAt(LocalDateTime.now());
        Deck savedDeck = deckRepository.save(deck);

        logger.info("Successfully created deck with id: {} for user with email: {}", savedDeck.getId(), user.getUsername());
        return savedDeck;
    }

    public List<Deck> getAllDecks(User user) {
        logger.info("Fetching all decks for user with email: {}", user.getUsername());
        List<Deck> decks = deckRepository.findAllByUser(user);

        logger.info("Retrieved {} decks for user with email: {}", decks.size(), user.getUsername());
        return decks;
    }

    public Deck getDeckById(Long deckId, User user) {
        logger.info("Fetching deck with id: {} for user with email: {}", deckId, user.getUsername());

        return deckRepository.findByIdAndUser(deckId, user)
                .orElseThrow(() -> {
                    logger.warn("Deck with id: {} not found for user with email: {}", deckId, user.getUsername());
                    return new FlashDashException(
                            ErrorCode.E404002,
                            "Deck with id " + deckId + " not found for the user."
                    );
                });
    }

    public Deck updateDeck(Long deckId, DeckRequest deckRequest, User user) {
        logger.info("Attempting to update deck with id: {} for user with email: {}", deckId, user.getUsername());

        Deck deck = deckRepository.findByIdAndUser(deckId, user)
                .orElseThrow(() -> {
                    logger.warn("Deck with id: {} not found for user with email: {}", deckId, user.getUsername());
                    return new FlashDashException(
                            ErrorCode.E404002,
                            "Deck with id " + deckId + " not found for the user."
                    );
                });

        deck.setName(deckRequest.getName());
        deck.setDescription(deckRequest.getDescription());
        deck.setUpdatedAt(LocalDateTime.now());

        Deck updatedDeck = deckRepository.save(deck);
        logger.info("Successfully updated deck with id: {} for user with email: {}", deckId, user.getUsername());
        return updatedDeck;
    }

    @Transactional
    public void deleteDeck(Long deckId, User user) {
        logger.info("Attempting to delete deck with id: {} for user with email: {}", deckId, user.getUsername());

        Deck deck = deckRepository.findByIdAndUser(deckId, user)
                .orElseThrow(() -> {
                    logger.warn("Deck with id: {} not found for user with email: {}", deckId, user.getUsername());
                    return new FlashDashException(
                            ErrorCode.E404002,
                            "Deck with id " + deckId + " not found for the user."
                    );
                });

        questionRepository.deleteAllByDeck(deck);
        deckRepository.delete(deck);

        logger.info("Successfully deleted deck with id: {} for user with email: {}", deckId, user.getUsername());
    }

    @Transactional
    public void deleteAllDecksForUser(User user) {
        logger.info("Attempting to delete all decks for user with email: {}", user.getUsername());

        List<Deck> decks = getAllDecks(user);

        if (decks.isEmpty()) {
            logger.info("No decks found for user with email: {}", user.getUsername());
            return;
        }

        decks.forEach(deck -> deleteDeck(deck.getId(), user));

        logger.info("Successfully deleted all decks for user with email: {}", user.getUsername());
    }
}
