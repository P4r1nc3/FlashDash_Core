package com.flashdash.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import jakarta.persistence.*;
import lombok.*;
import com.flashdash.model.card.CardDeck;
import com.flashdash.model.question.QuestionDeck;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "decks")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "deck_type", discriminatorType = DiscriminatorType.STRING)
@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.PROPERTY,
        property = "deckType")
@JsonSubTypes({
        @JsonSubTypes.Type(value = CardDeck.class, name = "CARD"),
        @JsonSubTypes.Type(value = QuestionDeck.class, name = "QUESTION")
})
public abstract class Deck {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    private String description;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;
}
