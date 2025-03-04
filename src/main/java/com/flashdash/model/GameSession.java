package com.flashdash.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;

import java.time.Duration;
import java.time.LocalDateTime;

@Entity
@Table(name = "game_sessions")
public class GameSession {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    @JsonIgnore
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "deck_id")
    private Deck deck;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private LocalDateTime startTime;
    private LocalDateTime endTime;

    private int totalScore;
    private int correctAnswersCount;
    private int wrongAnswersCount;
    private int questionCount;

    @Enumerated(EnumType.STRING)
    private GameSessionStatus status;

    public GameSession() {
        this.status = GameSessionStatus.PENDING;
    }

    public GameSession(User user, Deck deck, LocalDateTime createdAt, LocalDateTime updatedAt, int questionCount) {
        this.user = user;
        this.deck = deck;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.totalScore = 0;
        this.correctAnswersCount = 0;
        this.wrongAnswersCount = 0;
        this.questionCount = questionCount;
        this.status = GameSessionStatus.PENDING;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Deck getDeck() {
        return deck;
    }

    public void setDeck(Deck deck) {
        this.deck = deck;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }

    public LocalDateTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
    }

    public int getTotalScore() {
        return totalScore;
    }

    public void setTotalScore(int totalScore) {
        this.totalScore = totalScore;
    }

    public int getCorrectAnswersCount() {
        return correctAnswersCount;
    }

    public void setCorrectAnswersCount(int correctAnswersCount) {
        this.correctAnswersCount = correctAnswersCount;
    }

    public int getWrongAnswersCount() {
        return wrongAnswersCount;
    }

    public void setWrongAnswersCount(int wrongAnswersCount) {
        this.wrongAnswersCount = wrongAnswersCount;
    }

    public int getQuestionCount() {
        return questionCount;
    }

    public void setQuestionCount(int questionCount) {
        this.questionCount = questionCount;
    }

    public GameSessionStatus getStatus() {
        return status;
    }

    public void setStatus(GameSessionStatus status) {
        this.status = status;
    }

    public long getDurationInSeconds() {
        if (startTime != null && endTime != null) {
            return Duration.between(startTime, endTime).getSeconds();
        }
        return 0;
    }

    public double getAccuracy() {
        if (questionCount == 0) return 0.0;
        return ((double) correctAnswersCount / questionCount) * 100;
    }
}
