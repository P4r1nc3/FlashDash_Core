package com.flashdash.core.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
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

    public String getInvitationFrn() {
        return invitationFrn;
    }

    public String getSentByFrn() {
        return sentByFrn;
    }

    public String getSentToFrn() {
        return sentToFrn;
    }

    public String getStatus() {
        return status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setInvitationFrn(String invitationFrn) {
        this.invitationFrn = invitationFrn;
    }

    public void setSentByFrn(String sentByFrn) {
        this.sentByFrn = sentByFrn;
    }

    public void setSentToFrn(String sentToFrn) {
        this.sentToFrn = sentToFrn;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
