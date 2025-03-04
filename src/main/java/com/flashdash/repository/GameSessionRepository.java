package com.flashdash.repository;

import com.flashdash.model.GameSession;
import com.flashdash.model.GameSessionStatus;
import com.flashdash.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface GameSessionRepository extends JpaRepository<GameSession, Long> {
    GameSession findTopByDeckIdAndUserIdAndStatus(Long deckId, Long userId, GameSessionStatus status);
    List<GameSession> findAllByUser(User user);
    List<GameSession> findAllByDeckIdAndUserId(Long deckId, Long userId);
}
