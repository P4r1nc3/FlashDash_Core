package com.flashdash.core.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
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

    @Column(name = "total_score", nullable = true)
    private int totalScore;

    @Column(name = "correct_answers_count", nullable = true)
    private int correctAnswersCount;

    @Column(name = "wrong_answers_count", nullable = true)
    private int wrongAnswersCount;

    @Column(name = "question_count", nullable = true)
    private int questionCount;

    @Column(name = "start_time", nullable = true)
    private LocalDateTime startTime;

    @Column(name = "end_time", nullable = true)
    private LocalDateTime endTime;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public GameSession() {}
}
