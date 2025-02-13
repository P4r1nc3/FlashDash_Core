package com.flashdash.repository;

import com.flashdash.model.FriendInvitation;
import com.flashdash.model.GameSession;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GameSessionRepository extends JpaRepository<GameSession, Long> {
}
