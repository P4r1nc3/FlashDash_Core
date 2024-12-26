package com.flashdash.repository;

import com.flashdash.model.FriendInvitation;
import com.flashdash.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface FriendInvitationRepository extends JpaRepository<FriendInvitation, Long> {
    List<FriendInvitation> findAllBySentTo(User sentTo);

    List<FriendInvitation> findAllBySentBy(User sentBy);

    Optional<FriendInvitation> findBySentByAndSentTo(User sentBy, User sentTo);
}
