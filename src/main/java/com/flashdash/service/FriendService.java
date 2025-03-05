package com.flashdash.service;

import com.flashdash.dto.response.FriendInvitationResponse;
import com.flashdash.dto.response.UserResponse;
import com.flashdash.exception.ErrorCode;
import com.flashdash.exception.FlashDashException;
import com.flashdash.model.FriendInvitation;
import com.flashdash.repository.FriendInvitationRepository;
import com.flashdash.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class FriendService {
    private final FriendInvitationRepository friendInvitationRepository;
    private final UserRepository userRepository;
    private final EmailService emailService;

    public FriendService(FriendInvitationRepository friendInvitationRepository,
                         UserRepository userRepository,
                         EmailService emailService) {
        this.friendInvitationRepository = friendInvitationRepository;
        this.userRepository = userRepository;
        this.emailService = emailService;
    }

    public void sendFriendInvitation(String senderFrn, String recipientFrn) {
        if (senderFrn.equals(recipientFrn)) {
            throw new FlashDashException(ErrorCode.E403003, "You cannot send an invitation to yourself.");
        }

        if (friendInvitationRepository.findBySentByFrnAndSentToFrn(senderFrn, recipientFrn).isPresent()) {
            throw new FlashDashException(ErrorCode.E409002, "Friend invitation already sent.");
        }

        FriendInvitation invitation = new FriendInvitation();
        invitation.setInvitationFrn(generateFrn("friend-invitation"));
        invitation.setSentByFrn(senderFrn);
        invitation.setSentToFrn(recipientFrn);
        invitation.setStatus("PENDING");

        friendInvitationRepository.save(invitation);
        //TODO
        emailService.sendFriendInvitationEmail(recipientFrn, senderFrn, "ds");
    }

    public List<FriendInvitationResponse> getReceivedFriendInvitations(String recipientFrn) {
        return friendInvitationRepository.findAllBySentToFrn(recipientFrn).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public List<FriendInvitationResponse> getSentFriendInvitations(String senderFrn) {
        return friendInvitationRepository.findAllBySentByFrn(senderFrn).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public void respondToFriendInvitation(String invitationFrn, String userFrn, String status) {
        FriendInvitation invitation = friendInvitationRepository.findById(invitationFrn)
                .orElseThrow(() -> new FlashDashException(ErrorCode.E404004, "Invitation not found"));

        boolean isSender = invitation.getSentByFrn().equals(userFrn);
        boolean isRecipient = invitation.getSentToFrn().equals(userFrn);

        if (!isSender && !isRecipient) {
            throw new FlashDashException(ErrorCode.E403001, "Unauthorized to respond to this invitation.");
        }

        if (isSender && !"REJECTED".equals(status)) {
            throw new FlashDashException(ErrorCode.E403002, "You can only cancel your invitation.");
        }

        if (isRecipient) {
            invitation.setStatus(status);
            friendInvitationRepository.save(invitation);

            if ("ACCEPTED".equals(status)) {
                userRepository.addFriend(invitation.getSentByFrn(), invitation.getSentToFrn());
            }

            friendInvitationRepository.delete(invitation);
        }
    }

    public List<UserResponse> getFriends(String userFrn) {
        return userRepository.findFriendsByUserFrn(userFrn).stream()
                .map(UserResponse::new)
                .collect(Collectors.toList());
    }

    public void deleteFriend(String userFrn, String friendFrn) {
        userRepository.removeFriend(userFrn, friendFrn);
    }

    @Transactional
    public void removeAllFriends(String userFrn) {
        userRepository.removeAllFriends(userFrn);
    }

    private FriendInvitationResponse mapToResponse(FriendInvitation invitation) {
        return new FriendInvitationResponse(
                invitation.getInvitationFrn(),
                invitation.getSentByFrn(),
                invitation.getSentToFrn(),
                invitation.getStatus(),
                invitation.getCreatedAt()
        );
    }

    private String generateFrn(String resourceType) {
        return "frn:flashdash:" + resourceType + ":" + UUID.randomUUID();
    }
}
