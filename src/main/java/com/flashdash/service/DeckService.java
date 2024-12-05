package com.flashdash.service;

import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import com.flashdash.exception.ErrorCode;
import com.flashdash.exception.FlashDashException;
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

    private static final Logger logger = LoggerFactory.getLogger(DeckService.class);

    private final DeckRepository deckRepository;
    private final CardRepository cardRepository;
    private final QuestionRepository questionRepository;

    public DeckService(DeckRepository deckRepository, CardRepository cardRepository, QuestionRepository questionRepository) {
        this.deckRepository = deckRepository;
        this.cardRepository = cardRepository;
        this.questionRepository = questionRepository;
    }

    public Deck createDeck(Deck deck, User user) {
        logger.info("Attempting to create a new deck for user with email: {}", user.getUsername());

        deck.setUser(user);
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

    public Deck updateDeck(Long deckId, Deck deckDetails, User user) {
        logger.info("Attempting to update deck with id: {} for user with email: {}", deckId, user.getUsername());

        Deck deck = deckRepository.findByIdAndUser(deckId, user)
                .orElseThrow(() -> {
                    logger.warn("Deck with id: {} not found for user with email: {}", deckId, user.getUsername());
                    return new FlashDashException(
                            ErrorCode.E404002,
                            "Deck with id " + deckId + " not found for the user."
                    );
                });

        if (!deck.getClass().equals(deckDetails.getClass())) {
            logger.error("Attempted to change deck type for deck with id: {}", deckId);
            throw new FlashDashException(
                    ErrorCode.E400001,
                    "Changing deck type is not allowed."
            );
        }

        deck.setName(deckDetails.getName());
        deck.setDescription(deckDetails.getDescription());
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

        if (deck instanceof CardDeck) {
            logger.info("Deleting all cards associated with CardDeck id: {}", deckId);
            cardRepository.deleteAllByDeck(deck);
        }
        if (deck instanceof QuestionDeck) {
            logger.info("Deleting all questions associated with QuestionDeck id: {}", deckId);
            questionRepository.deleteAllByDeck(deck);
        }

        deckRepository.delete(deck);
        logger.info("Successfully deleted deck with id: {} for user with email: {}", deckId, user.getUsername());
    }
}
