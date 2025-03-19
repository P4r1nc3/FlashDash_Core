package com.flashdash.core.repository;

import com.flashdash.core.model.GameSession;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface GameSessionRepository extends JpaRepository<GameSession, String> {
    Optional<GameSession> findTopByDeckFrnAndUserFrnAndStatus(String deckFrn, String userFrn, String status);
    Optional<GameSession> findByDeckFrnAndGameSessionFrnAndUserFrnAndStatus(String deckFrn, String gameSessionFrn, String userFrn, String status);
    List<GameSession> findAllByUserFrn(String userFrn);
    List<GameSession> findAllByDeckFrnAndUserFrnAndStatus(String deckFrn, String userFrn, String status);
}
