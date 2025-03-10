package com.flashdash.core.repository;

import com.flashdash.core.model.FriendInvitation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface FriendInvitationRepository extends JpaRepository<FriendInvitation, String> {
    List<FriendInvitation> findAllBySentToFrnAndStatus(String sentByFrn, String status);
    List<FriendInvitation> findAllBySentByFrnAndStatus(String sentByFrn, String status);
    Optional<FriendInvitation> findBySentByFrnAndSentToFrnAndStatus(String sentByFrn, String sentToFrn, String status);
}
