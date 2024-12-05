package com.flashdash.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import com.flashdash.exception.ErrorCode;
import com.flashdash.exception.FlashDashException;
import com.flashdash.model.card.Card;
import com.flashdash.model.card.CardDeck;
import com.flashdash.model.Deck;
import com.flashdash.model.User;
import com.flashdash.repository.CardRepository;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class CardService {

    private static final Logger logger = LoggerFactory.getLogger(CardService.class);

    private final CardRepository cardRepository;
    private final DeckService deckService;

    public CardService(CardRepository cardRepository, DeckService deckService) {
        this.cardRepository = cardRepository;
        this.deckService = deckService;
    }

    public Card addCardToDeck(Long deckId, Card card, User user) {
        logger.info("Attempting to add card to deck with id: {} for user with email: {}", deckId, user.getUsername());

        Deck deck = deckService.getDeckById(deckId, user);
        card.setDeck(validateDeckIsCardDeck(deck));
        card.setCreatedAt(LocalDateTime.now());
        card.setUpdatedAt(LocalDateTime.now());

        Card savedCard = cardRepository.save(card);
        logger.info("Successfully added card with id: {} to deck with id: {} for user with email: {}", savedCard.getCardId(), deckId, user.getUsername());
        return savedCard;
    }

    public List<Card> getAllCardsInDeck(Long deckId, User user) {
        logger.info("Fetching all cards for deck with id: {} for user with email: {}", deckId, user.getUsername());

        Deck deck = deckService.getDeckById(deckId, user);
        List<Card> cards = cardRepository.findAllByDeck(validateDeckIsCardDeck(deck));

        logger.info("Retrieved {} cards for deck with id: {} for user with email: {}", cards.size(), deckId, user.getUsername());
        return cards;
    }

    public Card getCardById(Long deckId, Long cardId, User user) {
        logger.info("Fetching card with id: {} from deck with id: {} for user with email: {}", cardId, deckId, user.getUsername());

        Deck deck = deckService.getDeckById(deckId, user);
        return cardRepository.findByDeckAndCardId(validateDeckIsCardDeck(deck), cardId)
                .orElseThrow(() -> {
                    logger.warn("Card with id: {} not found in deck with id: {} for user with email: {}", cardId, deckId, user.getUsername());
                    return new FlashDashException(
                            ErrorCode.E404003,
                            "Card with id " + cardId + " not found in deck with id " + deckId
                    );
                });
    }

    public Card updateCard(Long deckId, Long cardId, Card cardDetails, User user) {
        logger.info("Attempting to update card with id: {} in deck with id: {} for user with email: {}", cardId, deckId, user.getUsername());

        Deck deck = deckService.getDeckById(deckId, user);
        Card card = cardRepository.findByDeckAndCardId(validateDeckIsCardDeck(deck), cardId)
                .orElseThrow(() -> {
                    logger.warn("Card with id: {} not found in deck with id: {} for user with email: {}", cardId, deckId, user.getUsername());
                    return new FlashDashException(
                            ErrorCode.E404003,
                            "Card with id " + cardId + " not found in deck with id " + deckId
                    );
                });

        card.setQuestion(cardDetails.getQuestion());
        card.setAnswer(cardDetails.getAnswer());
        card.setDifficulty(cardDetails.getDifficulty());
        card.setUpdatedAt(LocalDateTime.now());

        Card updatedCard = cardRepository.save(card);
        logger.info("Successfully updated card with id: {} in deck with id: {} for user with email: {}", cardId, deckId, user.getUsername());
        return updatedCard;
    }

    public void deleteCard(Long deckId, Long cardId, User user) {
        logger.info("Attempting to delete card with id: {} from deck with id: {} for user with email: {}", cardId, deckId, user.getUsername());

        Deck deck = deckService.getDeckById(deckId, user);
        Card card = cardRepository.findByDeckAndCardId(validateDeckIsCardDeck(deck), cardId)
                .orElseThrow(() -> {
                    logger.warn("Card with id: {} not found in deck with id: {} for user with email: {}", cardId, deckId, user.getUsername());
                    return new FlashDashException(
                            ErrorCode.E404003,
                            "Card with id " + cardId + " not found in deck with id " + deckId
                    );
                });

        cardRepository.delete(card);
        logger.info("Successfully deleted card with id: {} from deck with id: {} for user with email: {}", cardId, deckId, user.getUsername());
    }

    private CardDeck validateDeckIsCardDeck(Deck deck) {
        if (!(deck instanceof CardDeck)) {
            logger.error("Provided deck with id: {} is not a CARD deck.", deck.getId());
            throw new FlashDashException(
                    ErrorCode.E400002,
                    "Provided deck with id " + deck.getId() + " is not a CARD deck."
            );
        }
        return (CardDeck) deck;
    }
}
