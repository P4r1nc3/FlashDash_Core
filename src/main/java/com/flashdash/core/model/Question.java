package com.flashdash.core.model;

import com.flashdash.core.converter.JsonListConverter;
import com.p4r1nc3.flashdash.core.model.DifficultyEnum;
import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
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
    private DifficultyEnum difficulty;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public Question() {}

    public String getQuestionFrn() {
        return questionFrn;
    }

    public String getDeckFrn() {
        return deckFrn;
    }

    public String getQuestion() {
        return question;
    }

    public List<String> getCorrectAnswers() {
        return correctAnswers;
    }

    public List<String> getIncorrectAnswers() {
        return incorrectAnswers;
    }

    public DifficultyEnum getDifficulty() {
        return difficulty;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setQuestionFrn(String questionFrn) {
        this.questionFrn = questionFrn;
    }

    public void setDeckFrn(String deckFrn) {
        this.deckFrn = deckFrn;
    }

    public void setQuestion(String question) {
        this.question = question;
    }

    public void setCorrectAnswers(List<String> correctAnswers) {
        this.correctAnswers = correctAnswers;
    }

    public void setIncorrectAnswers(List<String> incorrectAnswers) {
        this.incorrectAnswers = incorrectAnswers;
    }

    public void setDifficulty(DifficultyEnum difficulty) {
        this.difficulty = difficulty;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
