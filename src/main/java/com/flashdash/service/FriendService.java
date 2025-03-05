package com.flashdash.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.flashdash.dto.response.FriendInvitationResponse;
import com.flashdash.dto.response.UserResponse;
import com.flashdash.exception.ErrorCode;
import com.flashdash.exception.FlashDashException;
import com.flashdash.model.FriendInvitation;
import com.flashdash.model.User;
import com.flashdash.repository.FriendInvitationRepository;
import com.flashdash.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class FriendService {
    private final FriendInvitationRepository friendInvitationRepository;
    private final UserRepository userRepository;
    private final EmailService emailService;
    private final ObjectMapper objectMapper = new ObjectMapper();

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
        invitation.setCreatedAt(LocalDateTime.now());
        invitation.setUpdatedAt(LocalDateTime.now());

        friendInvitationRepository.save(invitation);

        User sender = userRepository.findById(senderFrn)
                .orElseThrow(() -> new FlashDashException(ErrorCode.E404002, "Sender not found"));
        User recipient = userRepository.findById(recipientFrn)
                .orElseThrow(() -> new FlashDashException(ErrorCode.E404002, "Recipient not found"));

        emailService.sendFriendInvitationEmail(
                recipient.getEmail(),
                sender.getFirstName(),
                sender.getLastName()
        );
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

    @Transactional
    public void respondToFriendInvitation(String invitationFrn, String userFrn, String status) {
        FriendInvitation invitation = friendInvitationRepository.findById(invitationFrn)
                .orElseThrow(() -> new FlashDashException(ErrorCode.E404004, "Invitation not found"));

        boolean isRecipient = invitation.getSentToFrn().equals(userFrn);

        if (!isRecipient) {
            throw new FlashDashException(ErrorCode.E403001, "Unauthorized to respond to this invitation.");
        }

        if ("ACCEPTED".equals(status)) {
            addFriendship(invitation.getSentByFrn(), invitation.getSentToFrn());
        }

        friendInvitationRepository.delete(invitation);
    }

    public List<UserResponse> getFriends(String userFrn) {
        User user = userRepository.findById(userFrn)
                .orElseThrow(() -> new FlashDashException(ErrorCode.E404002, "User not found"));

        List<String> friendsFrnList = user.getFriendsFrnList();

        return friendsFrnList.stream()
                .map(friendFrn -> userRepository.findById(friendFrn)
                        .map(UserResponse::new)
                        .orElse(null))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
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
    }

    @Transactional
    public void removeAllFriends(String userFrn) {
        User user = userRepository.findById(userFrn)
                .orElseThrow(() -> new FlashDashException(ErrorCode.E404002, "User not found"));
        user.setFriendsFrnList(List.of());
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
