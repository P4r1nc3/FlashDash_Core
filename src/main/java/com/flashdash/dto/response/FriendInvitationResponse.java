package com.flashdash.dto.response;

import java.time.LocalDateTime;

public class FriendInvitationResponse {
    private String invitationFrn;
    private String senderFrn;
    private String recipientFrn;
    private String status;
    private LocalDateTime createdAt;

    public FriendInvitationResponse(String invitationFrn, String senderFrn, String recipientFrn,
                                    String status, LocalDateTime createdAt) {
        this.invitationFrn = invitationFrn;
        this.senderFrn = senderFrn;
        this.recipientFrn = recipientFrn;
        this.status = status;
        this.createdAt = createdAt;
    }

    public String getInvitationFrn() {
        return invitationFrn;
    }

    public String getSenderFrn() {
        return senderFrn;
    }

    public String getRecipientFrn() {
        return recipientFrn;
    }

    public String getStatus() {
        return status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
