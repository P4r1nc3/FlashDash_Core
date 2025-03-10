package com.flashdash.core.service;

import com.flashdash.core.exception.ErrorCode;
import com.flashdash.core.exception.FlashDashException;
import com.flashdash.core.model.FriendInvitation;
import com.flashdash.core.model.User;
import com.flashdash.core.repository.FriendInvitationRepository;
import com.flashdash.core.repository.UserRepository;
import com.flashdash.core.utils.FrnGenerator;
import com.flashdash.core.utils.ResourceType;
import com.p4r1nc3.flashdash.activity.model.LogActivityRequest.ActivityTypeEnum;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

@Service
public class FriendService {

    private final ActivityService activityService;
    private final EmailService emailService;
    private final UserRepository userRepository;
    private final FriendInvitationRepository friendInvitationRepository;

    public FriendService(ActivityService activityService,
                         EmailService emailService,
                         UserRepository userRepository,
                         FriendInvitationRepository friendInvitationRepository) {
        this.activityService = activityService;
        this.emailService = emailService;
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

        User sender = userRepository.findById(senderFrn)
                .orElseThrow(() -> new FlashDashException(ErrorCode.E404002, "Sender not found"));

        emailService.sendFriendInvitationEmail(
                recipient.getEmail(),
                sender.getFirstName(),
                sender.getLastName()
        );

        activityService.logUserActivity(senderFrn, invitation.getInvitationFrn(), ActivityTypeEnum.FRIEND_INVITE_SENT);
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

        activityService.logUserActivity(userFrn, invitation.getInvitationFrn(), ActivityTypeEnum.FRIEND_INVITE_RESPONDED);
    }

    public List<User> getFriends(String userFrn) {
        User user = userRepository.findById(userFrn)
                .orElseThrow(() -> new FlashDashException(ErrorCode.E404002, "User not found"));

        List<String> friendsFrnList = user.getFriendsFrnList();

        return friendsFrnList.isEmpty() ? Collections.emptyList() : userRepository.findByUserFrnIn(friendsFrnList);
    }

    @Transactional
    public void deleteFriend(String userFrn, String friendFrn) {
        User user = userRepository.findById(userFrn)
                .orElseThrow(() -> new FlashDashException(ErrorCode.E404002, "User not found"));
        User friend = userRepository.findById(friendFrn)
                .orElseThrow(() -> new FlashDashException(ErrorCode.E404002, "Friend not found"));

        if (!user.getFriendsFrnList().contains(friendFrn)) {
            throw new FlashDashException(ErrorCode.E404005, "This user is not your friend.");
        }

        removeFriendship(user, friend);

        activityService.logUserActivity(userFrn, friendFrn, ActivityTypeEnum.FRIEND_DELETED);
    }

    @Transactional
    public void removeAllFriends(String userFrn) {
        User user = userRepository.findById(userFrn)
                .orElseThrow(() -> new FlashDashException(ErrorCode.E404002, "User not found"));

        List<String> friendsList = user.getFriendsFrnList();

        for (String friendFrn : friendsList) {
            deleteFriend(userFrn, friendFrn);
        }

        userRepository.save(user);
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

    private void removeFriendship(User user, User friend) {
        List<String> userFriends = user.getFriendsFrnList();
        List<String> friendFriends = friend.getFriendsFrnList();

        userFriends.remove(friend.getUserFrn());
        friendFriends.remove(user.getUserFrn());

        user.setFriendsFrnList(userFriends);
        friend.setFriendsFrnList(friendFriends);

        userRepository.save(user);
        userRepository.save(friend);
    }
}
