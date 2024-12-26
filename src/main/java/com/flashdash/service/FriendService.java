package com.flashdash.service;

import com.flashdash.dto.response.FriendInvitationResponse;
import com.flashdash.dto.response.UserResponse;
import com.flashdash.exception.ErrorCode;
import com.flashdash.exception.FlashDashException;
import com.flashdash.model.FriendInvitation;
import com.flashdash.model.User;
import com.flashdash.repository.FriendInvitationRepository;
import com.flashdash.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class FriendService {
    private final FriendInvitationRepository friendInvitationRepository;
    private final UserRepository userRepository;

    public FriendService(FriendInvitationRepository friendInvitationRepository, UserRepository userRepository) {
        this.friendInvitationRepository = friendInvitationRepository;
        this.userRepository = userRepository;
    }

    public void sendFriendInvitation(String senderEmail, String recipientEmail) {
        User sender = userRepository.findByEmail(senderEmail)
                .orElseThrow(() -> new FlashDashException(ErrorCode.E404001, "User not found: " + senderEmail));
        User recipient = userRepository.findByEmail(recipientEmail)
                .orElseThrow(() -> new FlashDashException(ErrorCode.E404001, "User not found: " + recipientEmail));

        // Check if an invitation already exists
        Optional<FriendInvitation> existingInvitation = friendInvitationRepository.findBySentByAndSentTo(sender, recipient);
        if (existingInvitation.isPresent()) {
            throw new FlashDashException(ErrorCode.E409002, "Friend invitation already sent.");
        }

        FriendInvitation invitation = new FriendInvitation();
        invitation.setSentBy(sender);
        invitation.setSentTo(recipient);
        invitation.setStatus(FriendInvitation.InvitationStatus.PENDING);
        friendInvitationRepository.save(invitation);
    }

    public List<FriendInvitationResponse> getReceivedFriendInvitations(String recipientEmail) {
        User recipient = userRepository.findByEmail(recipientEmail)
                .orElseThrow(() -> new FlashDashException(ErrorCode.E404001, "User not found: " + recipientEmail));

        return friendInvitationRepository.findAllBySentTo(recipient).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public List<FriendInvitationResponse> getSentFriendInvitations(String senderEmail) {
        User sender = userRepository.findByEmail(senderEmail)
                .orElseThrow(() -> new FlashDashException(ErrorCode.E404001, "User not found: " + senderEmail));

        return friendInvitationRepository.findAllBySentBy(sender).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public void respondToFriendInvitation(Long invitationId, String recipientEmail, FriendInvitation.InvitationStatus status) {
        FriendInvitation invitation = friendInvitationRepository.findById(invitationId)
                .orElseThrow(() -> new FlashDashException(ErrorCode.E404005, "Invitation not found"));

        if (!invitation.getSentTo().getUsername().equals(recipientEmail)) {
            throw new FlashDashException(ErrorCode.E403001, "Unauthorized to respond to this invitation.");
        }

        invitation.setStatus(status);
        friendInvitationRepository.save(invitation);

        // Automatically add to friends list if accepted
        if (status == FriendInvitation.InvitationStatus.ACCEPTED) {
            User sender = invitation.getSentBy();
            User recipient = invitation.getSentTo();
            sender.getFriends().add(recipient);
            recipient.getFriends().add(sender);

            userRepository.save(sender);
            userRepository.save(recipient);
        }
    }

    public List<UserResponse> getFriends(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new FlashDashException(ErrorCode.E404001, "User not found"));

        return user.getFriends().stream()
                .map(friend -> new UserResponse(
                        friend.getFirstName(),
                        friend.getLastName(),
                        friend.getUsername(),
                        friend.getCreatedAt(),
                        friend.getUpdatedAt()
                ))
                .collect(Collectors.toList());
    }

    private FriendInvitationResponse mapToResponse(FriendInvitation invitation) {
        return new FriendInvitationResponse(
                invitation.getId(),
                invitation.getSentBy().getFirstName(),
                invitation.getSentBy().getLastName(),
                invitation.getSentTo().getFirstName(),
                invitation.getSentTo().getLastName(),
                invitation.getStatus().name(),
                invitation.getCreatedAt()
        );
    }
}
