package com.flashdash.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@Table(name = "friend_invitations")
public class FriendInvitation {

    @Id
    @Column(name = "invitation_frn", nullable = false, length = 256)
    private String invitationFrn;

    @Column(name = "sent_by_frn", nullable = false, length = 256)
    private String sentByFrn;

    @Column(name = "sent_to_frn", nullable = false, length = 256)
    private String sentToFrn;

    @Column(name = "status", nullable = false, length = 64)
    private String status;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public FriendInvitation() {}
}
