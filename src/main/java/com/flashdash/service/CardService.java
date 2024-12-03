package com.flashdash.service;

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

    private final CardRepository cardRepository;
    private final DeckService deckService;

    public CardService(CardRepository cardRepository, DeckService deckService) {
        this.cardRepository = cardRepository;
        this.deckService = deckService;
    }

    public Card addCardToDeck(Long deckId, Card card, User user) {
        Deck deck = deckService.getDeckById(deckId, user);
        card.setDeck(validateDeckIsCardDeck(deck));
        card.setCreatedAt(LocalDateTime.now());
        card.setUpdatedAt(LocalDateTime.now());

        return cardRepository.save(card);
    }

    public List<Card> getAllCardsInDeck(Long deckId, User user) {
        Deck deck = deckService.getDeckById(deckId, user);
        return cardRepository.findAllByDeck(validateDeckIsCardDeck(deck));
    }

    public Card getCardById(Long deckId, Long cardId, User user) {
        Deck deck = deckService.getDeckById(deckId, user);
        return cardRepository.findByDeckAndCardId(validateDeckIsCardDeck(deck), cardId)
                .orElseThrow(() -> new FlashDashException(
                        ErrorCode.E404003,
                        "Card with id " + cardId + " not found in deck with id " + deckId
                ));
    }

    public Card updateCard(Long deckId, Long cardId, Card cardDetails, User user) {
        Deck deck = deckService.getDeckById(deckId, user);
        Card card = cardRepository.findByDeckAndCardId(validateDeckIsCardDeck(deck), cardId)
                .orElseThrow(() -> new FlashDashException(
                        ErrorCode.E404003,
                        "Card with id " + cardId + " not found in deck with id " + deckId
                ));

        card.setQuestion(cardDetails.getQuestion());
        card.setAnswer(cardDetails.getAnswer());
        card.setDifficulty(cardDetails.getDifficulty());
        card.setUpdatedAt(LocalDateTime.now());

        return cardRepository.save(card);
    }

    public void deleteCard(Long deckId, Long cardId, User user) {
        Deck deck = deckService.getDeckById(deckId, user);
        Card card = cardRepository.findByDeckAndCardId(validateDeckIsCardDeck(deck), cardId)
                .orElseThrow(() -> new FlashDashException(
                        ErrorCode.E404003,
                        "Card with id " + cardId + " not found in deck with id " + deckId
                ));

        cardRepository.delete(card);
    }

    private CardDeck validateDeckIsCardDeck(Deck deck) {
        if (!(deck instanceof CardDeck)) {
            throw new FlashDashException(
                    ErrorCode.E400002,
                    "Provided deck with id " + deck.getId() + " is not a CARD deck."
            );
        }
        return (CardDeck) deck;
    }
}
