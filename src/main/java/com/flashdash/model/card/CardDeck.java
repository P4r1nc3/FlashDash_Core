package com.flashdash.model.card;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.flashdash.model.Deck;
import jakarta.persistence.*;

import java.util.Set;

@Entity
@DiscriminatorValue("CARD")
public class CardDeck extends Deck {

    @JsonIgnore
    @OneToMany(mappedBy = "deck", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Card> cards;

    public CardDeck() {

    }

    public CardDeck(Set<Card> cards) {
        this.cards = cards;
    }

    public Set<Card> getCards() {
        return cards;
    }

    public void setCards(Set<Card> cards) {
        this.cards = cards;
    }
}
