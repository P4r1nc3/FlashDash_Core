package com.flashdash.model.question;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.flashdash.model.Deck;
import jakarta.persistence.*;
import lombok.*;

import java.util.Set;

@Getter
@Setter
@Entity
@DiscriminatorValue("QUESTION")
public class QuestionDeck extends Deck {

    @JsonIgnore
    @OneToMany(mappedBy = "deck", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Question> questions;
}
