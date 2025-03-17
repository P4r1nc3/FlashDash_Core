package com.flashdash.core.service;

import com.flashdash.core.exception.ErrorCode;
import com.flashdash.core.exception.FlashDashException;
import com.flashdash.core.model.FriendInvitation;
import com.flashdash.core.model.User;
import com.flashdash.core.repository.FriendInvitationRepository;
import com.flashdash.core.repository.UserRepository;
import com.flashdash.core.service.api.ActivityService;
import com.flashdash.core.service.api.NotificationService;
import com.flashdash.core.utils.FrnGenerator;
import com.flashdash.core.utils.ResourceType;
import com.p4r1nc3.flashdash.activity.model.LogActivityRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class InvitationService {

    private final ActivityService activityService;
    private final NotificationService notificationService;
    private final UserRepository userRepository;
    private final FriendInvitationRepository friendInvitationRepository;

    public InvitationService(ActivityService activityService,
                             NotificationService notificationService,
                             UserRepository userRepository,
                             FriendInvitationRepository friendInvitationRepository) {
        this.activityService = activityService;
        this.notificationService = notificationService;
        this.userRepository = userRepository;
        this.friendInvitationRepository = friendInvitationRepository;
    }

    public void sendFriendInvitation(String senderFrn, String recipientEmail) {
        User recipient = userRepository.findByEmail(recipientEmail)
                .orElseThrow(() -> new FlashDashException(ErrorCode.E404002, "Recipient not found"));

        String recipientFrn = recipient.getUserFrn();

        if (senderFrn.equals(recipientFrn)) {
            throw new FlashDashException(ErrorCode.E403003, "You cannot send an invitation to yourself.");
        }

        if (friendInvitationRepository.findBySentByFrnAndSentToFrnAndStatus(senderFrn, recipientFrn, "PENDING").isPresent()) {
            throw new FlashDashException(ErrorCode.E409002, "Friend invitation already sent.");
        }

        FriendInvitation invitation = new FriendInvitation();
        invitation.setInvitationFrn(FrnGenerator.generateFrn(ResourceType.INVITATION));
        invitation.setSentByFrn(senderFrn);
        invitation.setSentToFrn(recipientFrn);
        invitation.setStatus("PENDING");
        invitation.setCreatedAt(LocalDateTime.now());
        invitation.setUpdatedAt(LocalDateTime.now());
        friendInvitationRepository.save(invitation);

        activityService.logUserActivity(senderFrn, invitation.getInvitationFrn(), LogActivityRequest.ActivityTypeEnum.FRIEND_INVITE_SENT);
        notificationService.sendFriendInviteEmail(recipientFrn);
    }

    public List<FriendInvitation> getReceivedFriendInvitations(String recipientFrn) {
        return friendInvitationRepository.findAllBySentToFrnAndStatus(recipientFrn, "PENDING");
    }

    public List<FriendInvitation> getSentFriendInvitations(String senderFrn) {
        return friendInvitationRepository.findAllBySentByFrnAndStatus(senderFrn, "PENDING");
    }

    @Transactional
    public void respondToFriendInvitation(String invitationFrn, String userFrn, String status) {
        FriendInvitation invitation = friendInvitationRepository.findById(invitationFrn)
                .orElseThrow(() -> new FlashDashException(ErrorCode.E404004, "Invitation not found"));

        boolean isRecipient = invitation.getSentToFrn().equals(userFrn);
        boolean isSender = invitation.getSentByFrn().equals(userFrn);

        if (!isRecipient && !isSender) {
            throw new FlashDashException(ErrorCode.E403001, "Unauthorized to respond to this invitation.");
        }

        if (isRecipient && !"ACCEPTED".equals(status) && !"REJECTED".equals(status)) {
            throw new FlashDashException(ErrorCode.E400001, "Invalid status. Must be ACCEPTED or REJECTED for recipients.");
        }

        if (isSender && !"CANCELLED".equals(status)) {
            throw new FlashDashException(ErrorCode.E403001, "Unauthorized to respond to this invitation.");
        }

        // Update invitation status
        invitation.setStatus(status);
        invitation.setUpdatedAt(LocalDateTime.now());
        friendInvitationRepository.save(invitation);

        // If accepted, add friendship
        if ("ACCEPTED".equals(status)) {
            addFriendship(invitation.getSentByFrn(), invitation.getSentToFrn());
        }

        activityService.logUserActivity(userFrn, invitation.getInvitationFrn(), LogActivityRequest.ActivityTypeEnum.FRIEND_INVITE_RESPONDED);
    }

    private void addFriendship(String userFrn, String friendFrn) {
        User user = userRepository.findById(userFrn)
                .orElseThrow(() -> new FlashDashException(ErrorCode.E404002, "User not found"));
        User friend = userRepository.findById(friendFrn)
                .orElseThrow(() -> new FlashDashException(ErrorCode.E404002, "Friend not found"));

        List<String> userFriends = user.getFriendsFrnList();
        List<String> friendFriends = friend.getFriendsFrnList();

        if (!userFriends.contains(friendFrn)) userFriends.add(friendFrn);
        if (!friendFriends.contains(userFrn)) friendFriends.add(userFrn);

        user.setFriendsFrnList(userFriends);
        friend.setFriendsFrnList(friendFriends);

        userRepository.save(user);
        userRepository.save(friend);
    }
}
