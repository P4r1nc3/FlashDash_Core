package com.flashdash.repository;

import com.flashdash.model.GameSession;
import com.flashdash.model.GameSessionStatus;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GameSessionRepository extends JpaRepository<GameSession, Long> {

    GameSession findTopByDeckIdAndUserIdAndStatus(Long deckId, Long userId, GameSessionStatus status);
}
