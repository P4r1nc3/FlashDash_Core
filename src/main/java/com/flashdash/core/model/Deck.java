package com.flashdash.core.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@Table(name = "decks")
public class Deck {

    @Id
    @Column(name = "deck_frn", nullable = false, length = 256)
    private String deckFrn;

    @Column(name = "user_frn", nullable = false, length = 256)
    private String userFrn;

    @Column(name = "name", nullable = false, length = 256)
    private String name;

    @Column(name = "description", nullable = true, length = 512)
    private String description;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public Deck() {}
}
