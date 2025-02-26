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
    private final EmailService emailService;

    public FriendService(FriendInvitationRepository friendInvitationRepository,
                         UserRepository userRepository,
                         EmailService emailService) {
        this.friendInvitationRepository = friendInvitationRepository;
        this.userRepository = userRepository;
        this.emailService = emailService;
    }

    public void sendFriendInvitation(String senderEmail, String recipientEmail) {
        if (senderEmail.equals(recipientEmail)) {
            throw new FlashDashException(ErrorCode.E403003, "You cannot send an invitation to yourself.");
        }

        User sender = userRepository.findByEmail(senderEmail)
                .orElseThrow(() -> new FlashDashException(ErrorCode.E404001, "User not found: " + senderEmail));
        User recipient = userRepository.findByEmail(recipientEmail)
                .orElseThrow(() -> new FlashDashException(ErrorCode.E404001, "User not found: " + recipientEmail));

        // Check if an invitation already exists
        Optional<FriendInvitation> existingInvitation = friendInvitationRepository.findBySentByAndSentTo(sender, recipient);
        if (existingInvitation.isPresent()) {
            throw new FlashDashException(ErrorCode.E409002, "Friend invitation already sent.");
        }

        if (sender.getFriends().contains(recipient)) {
            throw new FlashDashException(ErrorCode.E409003, "You are already friends with this user.");
        }

        FriendInvitation invitation = new FriendInvitation();
        invitation.setSentBy(sender);
        invitation.setSentTo(recipient);
        invitation.setStatus(FriendInvitation.InvitationStatus.PENDING);
        friendInvitationRepository.save(invitation);
        emailService.sendFriendInvitationEmail(recipient.getUsername(), sender.getFirstName(), sender.getLastName());
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

    public void respondToFriendInvitation(Long invitationId, String userEmail, FriendInvitation.InvitationStatus status) {
        FriendInvitation invitation = friendInvitationRepository.findById(invitationId)
                .orElseThrow(() -> new FlashDashException(ErrorCode.E404004, "Invitation not found"));

        User sender = invitation.getSentBy();
        User recipient = invitation.getSentTo();

        boolean isSender = sender.getUsername().equals(userEmail);
        boolean isRecipient = recipient.getUsername().equals(userEmail);

        if (!isSender && !isRecipient) {
            throw new FlashDashException(ErrorCode.E403001, "Unauthorized to respond to this invitation.");
        }

        if (isSender) {
            if (status != FriendInvitation.InvitationStatus.REJECTED) {
                throw new FlashDashException(ErrorCode.E403002, "You can only cancel your invitation.");
            }
            friendInvitationRepository.delete(invitation);
            return;
        }

        if (isRecipient) {
            invitation.setStatus(status);
            friendInvitationRepository.save(invitation);

            if (status == FriendInvitation.InvitationStatus.ACCEPTED) {
                sender.getFriends().add(recipient);
                recipient.getFriends().add(sender);
                userRepository.save(sender);
                userRepository.save(recipient);
            }

            friendInvitationRepository.delete(invitation);
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
                        friend.isDailyNotifications(),
                        friend.getCreatedAt(),
                        friend.getUpdatedAt()
                ))
                .collect(Collectors.toList());
    }

    public void deleteFriend(String userEmail, String friendEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new FlashDashException(ErrorCode.E404001, "User not found: " + userEmail));

        User friend = userRepository.findByEmail(friendEmail)
                .orElseThrow(() -> new FlashDashException(ErrorCode.E404005, "Friend not found: " + friendEmail));

        if (!user.getFriends().contains(friend)) {
            throw new FlashDashException(ErrorCode.E404005, "This user is not your friend.");
        }

        user.getFriends().remove(friend);
        friend.getFriends().remove(user);

        userRepository.save(user);
        userRepository.save(friend);
    }

    public void removeAllFriends(User user) {
        List<User> friends = List.copyOf(user.getFriends());
        for (User friend : friends) {
            user.getFriends().remove(friend);
            friend.getFriends().remove(user);
            userRepository.save(friend);
        }

        userRepository.save(user);
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
