package com.flashdash.model;

import com.flashdash.converter.JsonListConverter;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@Table(name = "questions")
public class Question {

    @Id
    @Column(name = "question_frn", nullable = false, length = 256)
    private String questionFrn;

    @Column(name = "deck_frn", nullable = false, length = 256)
    private String deckFrn;

    @Column(name = "question", nullable = false, length = 512)
    private String question;

    @Convert(converter = JsonListConverter.class)
    @Column(name = "correct_answers", nullable = false, length = 1024)
    private List<String> correctAnswers = new ArrayList<>();

    @Convert(converter = JsonListConverter.class)
    @Column(name = "incorrect_answers", nullable = false, length = 1024)
    private List<String> incorrectAnswers = new ArrayList<>();

    @Column(name = "difficulty", nullable = false, length = 64)
    private String difficulty;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public Question() {}
}
