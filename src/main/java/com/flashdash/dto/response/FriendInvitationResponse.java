package com.flashdash.dto.response;

import java.time.LocalDateTime;

public class FriendInvitationResponse {
    private Long id;
    private String senderFirstName;
    private String senderLastName;
    private String recipientFirstName;
    private String recipientLastName;
    private String status;
    private LocalDateTime createdAt;

    public FriendInvitationResponse(Long id, String senderFirstName, String senderLastName,
                                    String recipientFirstName, String recipientLastName,
                                    String status, LocalDateTime createdAt) {
        this.id = id;
        this.senderFirstName = senderFirstName;
        this.senderLastName = senderLastName;
        this.recipientFirstName = recipientFirstName;
        this.recipientLastName = recipientLastName;
        this.status = status;
        this.createdAt = createdAt;
    }

    public Long getId() {
        return id;
    }

    public String getSenderFirstName() {
        return senderFirstName;
    }

    public String getSenderLastName() {
        return senderLastName;
    }

    public String getRecipientFirstName() {
        return recipientFirstName;
    }

    public String getRecipientLastName() {
        return recipientLastName;
    }

    public String getStatus() {
        return status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
