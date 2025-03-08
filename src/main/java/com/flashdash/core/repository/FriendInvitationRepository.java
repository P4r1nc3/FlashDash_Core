package com.flashdash.core.repository;

import com.flashdash.core.model.FriendInvitation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface FriendInvitationRepository extends JpaRepository<FriendInvitation, String> {
    List<FriendInvitation> findAllBySentToFrn(String sentToFrn);
    List<FriendInvitation> findAllBySentByFrn(String sentByFrn);
    Optional<FriendInvitation> findBySentByFrnAndSentToFrn(String sentByFrn, String sentToFrn);
}
