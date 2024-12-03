package com.flashdash.model.question;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.flashdash.model.Deck;
import jakarta.persistence.*;

import java.util.Set;

@Entity
@DiscriminatorValue("QUESTION")
public class QuestionDeck extends Deck {

    @JsonIgnore
    @OneToMany(mappedBy = "deck", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Question> questions;

    public QuestionDeck() {

    }

    public QuestionDeck(Set<Question> questions) {
        this.questions = questions;
    }

    public Set<Question> getQuestions() {
        return questions;
    }

    public void setQuestions(Set<Question> questions) {
        this.questions = questions;
    }
}
