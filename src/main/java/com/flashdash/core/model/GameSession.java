package com.flashdash.core.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "game_sessions")
public class GameSession {

    @Id
    @Column(name = "game_session_frn", nullable = false, length = 256)
    private String gameSessionFrn;

    @Column(name = "user_frn", nullable = false, length = 256)
    private String userFrn;

    @Column(name = "deck_frn", nullable = false, length = 256)
    private String deckFrn;

    @Column(name = "status", nullable = false, length = 64)
    private String status;

    @Column(name = "total_score", nullable = false)
    private int totalScore;

    @Column(name = "correct_answers_count", nullable = false)
    private int correctAnswersCount;

    @Column(name = "wrong_answers_count", nullable = false)
    private int wrongAnswersCount;

    @Column(name = "question_count", nullable = false)
    private int questionCount;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Lob
    @Column(name = "session_details", columnDefinition = "TEXT")
    private String sessionDetails;

    public GameSession() {}

    public String getGameSessionFrn() {
        return gameSessionFrn;
    }

    public String getUserFrn() {
        return userFrn;
    }

    public String getDeckFrn() {
        return deckFrn;
    }

    public String getStatus() {
        return status;
    }

    public int getTotalScore() {
        return totalScore;
    }

    public int getCorrectAnswersCount() {
        return correctAnswersCount;
    }

    public int getWrongAnswersCount() {
        return wrongAnswersCount;
    }

    public int getQuestionCount() {
        return questionCount;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public String getSessionDetails() {
        return sessionDetails;
    }

    public void setGameSessionFrn(String gameSessionFrn) {
        this.gameSessionFrn = gameSessionFrn;
    }

    public void setUserFrn(String userFrn) {
        this.userFrn = userFrn;
    }

    public void setDeckFrn(String deckFrn) {
        this.deckFrn = deckFrn;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void setTotalScore(int totalScore) {
        this.totalScore = totalScore;
    }

    public void setCorrectAnswersCount(int correctAnswersCount) {
        this.correctAnswersCount = correctAnswersCount;
    }

    public void setWrongAnswersCount(int wrongAnswersCount) {
        this.wrongAnswersCount = wrongAnswersCount;
    }

    public void setQuestionCount(int questionCount) {
        this.questionCount = questionCount;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public void setSessionDetails(String sessionDetails) {
        this.sessionDetails = sessionDetails;
    }
}
