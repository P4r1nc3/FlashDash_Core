package com.flashdash.model.card;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.flashdash.model.Deck;
import jakarta.persistence.*;
import lombok.*;

import java.util.Set;

@Getter
@Setter
@Entity
@DiscriminatorValue("CARD")
public class CardDeck extends Deck {

    @JsonIgnore
    @OneToMany(mappedBy = "deck", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Card> cards;
}
