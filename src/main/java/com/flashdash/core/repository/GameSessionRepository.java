package com.flashdash.core.repository;

import com.flashdash.core.model.GameSession;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface GameSessionRepository extends JpaRepository<GameSession, String> {
    Optional<GameSession> findTopByDeckFrnAndUserFrnAndStatus(String deckFrn, String userFrn, String status);
    Optional<GameSession> findByDeckFrnAndGameSessionFrnAndUserFrn(String deckFrn, String gameSessionFrn, String userFrn);
    List<GameSession> findAllByUserFrn(String userFrn);
    List<GameSession> findAllByDeckFrnAndUserFrn(String deckFrn, String userFrn);
}
