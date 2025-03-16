package com.flashdash.core.service;

import com.flashdash.core.TestUtils;
import com.flashdash.core.exception.ErrorCode;
import com.flashdash.core.exception.FlashDashException;
import com.flashdash.core.model.FriendInvitation;
import com.flashdash.core.model.User;
import com.flashdash.core.repository.FriendInvitationRepository;
import com.flashdash.core.repository.UserRepository;
import com.flashdash.core.service.api.ActivityService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@SpringBootTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class FriendServiceTest {

    @Autowired
    private FriendService friendService;

    @MockitoBean
    private ActivityService activityService;

    @MockitoBean
    private EmailService emailService;

    @MockitoBean
    private UserRepository userRepository;

    @MockitoBean
    private FriendInvitationRepository friendInvitationRepository;

    private User sender;
    private User recipient;

    @BeforeEach
    void setUp() {
        sender = TestUtils.createUser();
        recipient = TestUtils.createUser();
    }

    @Test
    void shouldSendFriendInvitationSuccessfully() {
        when(userRepository.findById(sender.getUserFrn())).thenReturn(Optional.of(sender));
        when(userRepository.findByEmail(recipient.getEmail())).thenReturn(Optional.of(recipient));
        when(friendInvitationRepository.findBySentByFrnAndSentToFrnAndStatus(sender.getUserFrn(), recipient.getUserFrn(), "PENDING"))
                .thenReturn(Optional.empty());

        friendService.sendFriendInvitation(sender.getUserFrn(), recipient.getEmail());

        verify(friendInvitationRepository).save(any(FriendInvitation.class));
        verify(emailService, times(1)).sendFriendInvitationEmail(recipient.getEmail(), sender.getFirstName(), sender.getLastName());
    }

    @Test
    void shouldThrowExceptionWhenSendingInvitationToSelf() {
        // Arrange
        when(userRepository.findByEmail(sender.getEmail())).thenReturn(Optional.of(sender));

        // Act & Assert
        assertThatThrownBy(() -> friendService.sendFriendInvitation(sender.getUserFrn(), sender.getEmail()))
                .isInstanceOf(FlashDashException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.E403003);

        verify(friendInvitationRepository, never()).save(any(FriendInvitation.class));
    }

    @Test
    void shouldThrowExceptionWhenInvitationAlreadyExists() {
        // Arrange
        FriendInvitation existingInvitation = TestUtils.createFriendInvitation(sender, recipient);

        when(userRepository.findByEmail(recipient.getEmail())).thenReturn(Optional.of(recipient));
        when(friendInvitationRepository.findBySentByFrnAndSentToFrnAndStatus(sender.getUserFrn(), recipient.getUserFrn(), "PENDING"))
                .thenReturn(Optional.of(existingInvitation));

        // Act & Assert
        assertThatThrownBy(() -> friendService.sendFriendInvitation(sender.getUserFrn(), recipient.getEmail()))
                .isInstanceOf(FlashDashException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.E409002);
    }

    @Test
    void shouldThrowExceptionWhenSenderNotFound() {
        when(userRepository.findById(sender.getUserFrn())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> friendService.sendFriendInvitation(sender.getUserFrn(), recipient.getEmail()))
                .isInstanceOf(FlashDashException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.E404002);
    }

    @Test
    void shouldThrowExceptionWhenRecipientNotFound() {
        when(userRepository.findByEmail(recipient.getEmail())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> friendService.sendFriendInvitation(sender.getUserFrn(), recipient.getEmail()))
                .isInstanceOf(FlashDashException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.E404002);
    }

    @Test
    void shouldReturnOnlyPendingReceivedInvitations() {
        FriendInvitation invitation = TestUtils.createFriendInvitation(sender, recipient);
        invitation.setStatus("PENDING");

        FriendInvitation acceptedInvitation = TestUtils.createFriendInvitation(sender, recipient);
        acceptedInvitation.setStatus("ACCEPTED");

        when(friendInvitationRepository.findAllBySentToFrnAndStatus(recipient.getUserFrn(), "PENDING"))
                .thenReturn(List.of(invitation));

        List<FriendInvitation> invitations = friendService.getReceivedFriendInvitations(recipient.getUserFrn());

        assertThat(invitations).hasSize(1);
        assertThat(invitations.get(0).getStatus()).isEqualTo("PENDING");
    }

    @Test
    void shouldReturnOnlyPendingSentInvitations() {
        FriendInvitation invitation = TestUtils.createFriendInvitation(sender, recipient);
        invitation.setStatus("PENDING");

        FriendInvitation rejectedInvitation = TestUtils.createFriendInvitation(sender, recipient);
        rejectedInvitation.setStatus("REJECTED");

        when(friendInvitationRepository.findAllBySentByFrnAndStatus(sender.getUserFrn(), "PENDING"))
                .thenReturn(List.of(invitation));

        List<FriendInvitation> invitations = friendService.getSentFriendInvitations(sender.getUserFrn());

        assertThat(invitations).hasSize(1);
        assertThat(invitations.get(0).getStatus()).isEqualTo("PENDING");
    }

    @Test
    void shouldCancelFriendInvitationSuccessfully() {
        // Arrange: Create sender, recipient, and an invitation
        FriendInvitation invitation = TestUtils.createFriendInvitation(sender, recipient);
        invitation.setStatus("PENDING");

        when(friendInvitationRepository.findById(invitation.getInvitationFrn())).thenReturn(Optional.of(invitation));
        when(friendInvitationRepository.save(any(FriendInvitation.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act: Sender cancels the invitation
        friendService.respondToFriendInvitation(invitation.getInvitationFrn(), sender.getUserFrn(), "CANCELLED");

        // Assert: The invitation status is updated to CANCELLED
        assertThat(invitation.getStatus()).isEqualTo("CANCELLED");
        verify(friendInvitationRepository, times(1)).save(invitation);
    }

    @Test
    void shouldRespondToFriendInvitationSuccessfully() {
        FriendInvitation invitation = TestUtils.createFriendInvitation(sender, recipient);

        when(friendInvitationRepository.findById(invitation.getInvitationFrn())).thenReturn(Optional.of(invitation));
        when(userRepository.findById(sender.getUserFrn())).thenReturn(Optional.of(sender));
        when(userRepository.findById(recipient.getUserFrn())).thenReturn(Optional.of(recipient));

        friendService.respondToFriendInvitation(invitation.getInvitationFrn(), recipient.getUserFrn(), "ACCEPTED");

        verify(userRepository, times(2)).save(any(User.class));
    }

    @Test
    void shouldThrowExceptionWhenRespondingToUnauthorizedInvitation() {
        FriendInvitation invitation = TestUtils.createFriendInvitation(sender, recipient);
        when(friendInvitationRepository.findById(invitation.getInvitationFrn())).thenReturn(Optional.of(invitation));

        assertThatThrownBy(() -> friendService.respondToFriendInvitation(invitation.getInvitationFrn(), "random-frn", "ACCEPTED"))
                .isInstanceOf(FlashDashException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.E403001);
    }

    @Test
    void shouldGetFriendsSuccessfully() {
        // Arrange
        User user = TestUtils.createUser();
        User friend1 = TestUtils.createUser();
        User friend2 = TestUtils.createUser();

        user.setFriendsFrnList(List.of(friend1.getUserFrn(), friend2.getUserFrn()));

        when(userRepository.findById(user.getUserFrn())).thenReturn(Optional.of(user));
        when(userRepository.findByUserFrnIn(user.getFriendsFrnList())).thenReturn(List.of(friend1, friend2));

        // Act
        List<User> friends = friendService.getFriends(user.getUserFrn());

        // Assert
        assertThat(friends).hasSize(2);
        assertThat(friends).extracting(User::getUserFrn)
                .containsExactlyInAnyOrder(friend1.getUserFrn(), friend2.getUserFrn());

        verify(userRepository).findById(user.getUserFrn());
        verify(userRepository).findByUserFrnIn(user.getFriendsFrnList());
    }

    @Test
    void shouldReturnEmptyListWhenUserHasNoFriends() {
        // Arrange
        User user = TestUtils.createUser();
        user.setFriendsFrnList(Collections.emptyList());

        when(userRepository.findById(user.getUserFrn())).thenReturn(Optional.of(user));

        // Act
        List<User> friends = friendService.getFriends(user.getUserFrn());

        // Assert
        assertThat(friends).isEmpty();
        verify(userRepository).findById(user.getUserFrn());
        verify(userRepository, never()).findByUserFrnIn(any());
    }

    @Test
    void shouldRemoveAllFriendsSuccessfully() {
        sender.getFriendsFrnList().add(recipient.getUserFrn());
        recipient.getFriendsFrnList().add(sender.getUserFrn());

        when(userRepository.findById(sender.getUserFrn())).thenReturn(Optional.of(sender));

        friendService.removeAllFriends(sender.getUserFrn());

        assertThat(sender.getFriendsFrnList()).isEmpty();
        verify(userRepository, times(1)).save(sender);
    }

    @Test
    void shouldDeleteFriendSuccessfully() {
        sender.setFriendsFrnList(List.of(recipient.getUserFrn()));
        recipient.setFriendsFrnList(List.of(sender.getUserFrn()));

        when(userRepository.findById(sender.getUserFrn())).thenReturn(Optional.of(sender));
        when(userRepository.findById(recipient.getUserFrn())).thenReturn(Optional.of(recipient));

        friendService.deleteFriend(sender.getUserFrn(), recipient.getUserFrn());

        assertThat(sender.getFriendsFrnList()).doesNotContain(recipient.getUserFrn());
        assertThat(recipient.getFriendsFrnList()).doesNotContain(sender.getUserFrn());
        verify(userRepository, times(1)).save(sender);
        verify(userRepository, times(1)).save(recipient);
    }

    @Test
    void shouldThrowExceptionWhenDeletingNonFriend() {
        // Arrange
        User user = TestUtils.createUser();
        User stranger = TestUtils.createUser();

        when(userRepository.findById(user.getUserFrn())).thenReturn(Optional.of(user));
        when(userRepository.findById(stranger.getUserFrn())).thenReturn(Optional.of(stranger));

        // Act & Assert
        assertThatThrownBy(() -> friendService.deleteFriend(user.getUserFrn(), stranger.getUserFrn()))
                .isInstanceOf(FlashDashException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.E404005);
    }
}
