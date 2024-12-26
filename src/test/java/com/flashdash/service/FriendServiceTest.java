package com.flashdash.service;

import com.flashdash.FlashDashApplication;
import com.flashdash.TestUtils;
import com.flashdash.dto.response.FriendInvitationResponse;
import com.flashdash.dto.response.UserResponse;
import com.flashdash.exception.ErrorCode;
import com.flashdash.exception.FlashDashException;
import com.flashdash.model.FriendInvitation;
import com.flashdash.model.User;
import com.flashdash.repository.FriendInvitationRepository;
import com.flashdash.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@SpringBootTest(classes = FlashDashApplication.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class FriendServiceTest {

    @Autowired
    private FriendService friendService;

    @MockBean
    private UserRepository userRepository;

    @MockBean
    private FriendInvitationRepository friendInvitationRepository;

    @Test
    void shouldSendFriendInvitationSuccessfully() {
        // Arrange
        User sender = TestUtils.createUser();
        User recipient = TestUtils.createFriendUser();
        when(userRepository.findByEmail(sender.getUsername())).thenReturn(Optional.of(sender));
        when(userRepository.findByEmail(recipient.getUsername())).thenReturn(Optional.of(recipient));
        when(friendInvitationRepository.findBySentByAndSentTo(sender, recipient)).thenReturn(Optional.empty());

        // Act
        friendService.sendFriendInvitation(sender.getUsername(), recipient.getUsername());

        // Assert
        verify(friendInvitationRepository).save(any(FriendInvitation.class));
    }

    @Test
    void shouldThrowExceptionWhenSenderNotFound() {
        // Arrange
        User recipient = TestUtils.createFriendUser();
        when(userRepository.findByEmail("nonexistent@example.com")).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> friendService.sendFriendInvitation("nonexistent@example.com", recipient.getUsername()))
                .isInstanceOf(FlashDashException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.E404001);
    }

    @Test
    void shouldThrowExceptionWhenRecipientNotFound() {
        // Arrange
        User sender = TestUtils.createUser();
        when(userRepository.findByEmail(sender.getUsername())).thenReturn(Optional.of(sender));
        when(userRepository.findByEmail("nonexistent@example.com")).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> friendService.sendFriendInvitation(sender.getUsername(), "nonexistent@example.com"))
                .isInstanceOf(FlashDashException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.E404001);
    }

    @Test
    void shouldThrowExceptionWhenInvitationAlreadyExists() {
        // Arrange
        User sender = TestUtils.createUser();
        User recipient = TestUtils.createFriendUser();
        FriendInvitation existingInvitation = TestUtils.createFriendInvitation(sender, recipient);
        when(userRepository.findByEmail(sender.getUsername())).thenReturn(Optional.of(sender));
        when(userRepository.findByEmail(recipient.getUsername())).thenReturn(Optional.of(recipient));
        when(friendInvitationRepository.findBySentByAndSentTo(sender, recipient)).thenReturn(Optional.of(existingInvitation));

        // Act & Assert
        assertThatThrownBy(() -> friendService.sendFriendInvitation(sender.getUsername(), recipient.getUsername()))
                .isInstanceOf(FlashDashException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.E409002);
    }

    @Test
    void shouldGetReceivedFriendInvitationsSuccessfully() {
        // Arrange
        User recipient = TestUtils.createFriendUser();
        FriendInvitation invitation = TestUtils.createFriendInvitation(TestUtils.createUser(), recipient);
        when(userRepository.findByEmail(recipient.getUsername())).thenReturn(Optional.of(recipient));
        when(friendInvitationRepository.findAllBySentTo(recipient)).thenReturn(List.of(invitation));

        // Act
        List<FriendInvitationResponse> invitations = friendService.getReceivedFriendInvitations(recipient.getUsername());

        // Assert
        assertThat(invitations).hasSize(1);
        assertThat(invitations.get(0).getRecipientFirstName()).isEqualTo(recipient.getFirstName());
        verify(friendInvitationRepository).findAllBySentTo(recipient);
    }

    @Test
    void shouldGetSentFriendInvitationsSuccessfully() {
        // Arrange
        User sender = TestUtils.createUser();
        FriendInvitation invitation = TestUtils.createFriendInvitation(sender, TestUtils.createFriendUser());
        when(userRepository.findByEmail(sender.getUsername())).thenReturn(Optional.of(sender));
        when(friendInvitationRepository.findAllBySentBy(sender)).thenReturn(List.of(invitation));

        // Act
        List<FriendInvitationResponse> invitations = friendService.getSentFriendInvitations(sender.getUsername());

        // Assert
        assertThat(invitations).hasSize(1);
        assertThat(invitations.get(0).getSenderFirstName()).isEqualTo(sender.getFirstName());
        verify(friendInvitationRepository).findAllBySentBy(sender);
    }

    @Test
    void shouldRespondToFriendInvitationSuccessfully() {
        // Arrange
        User sender = TestUtils.createUser();
        User recipient = TestUtils.createFriendUser();
        FriendInvitation invitation = TestUtils.createFriendInvitation(sender, recipient);
        when(friendInvitationRepository.findById(invitation.getId())).thenReturn(Optional.of(invitation));

        // Act
        friendService.respondToFriendInvitation(invitation.getId(), recipient.getUsername(), FriendInvitation.InvitationStatus.ACCEPTED);

        // Assert
        assertThat(invitation.getStatus()).isEqualTo(FriendInvitation.InvitationStatus.ACCEPTED);
        verify(friendInvitationRepository).save(invitation);
        verify(userRepository).save(sender);
        verify(userRepository).save(recipient);
    }

    @Test
    void shouldThrowExceptionWhenRespondingToUnauthorizedInvitation() {
        // Arrange
        User recipient = TestUtils.createFriendUser();
        FriendInvitation invitation = TestUtils.createFriendInvitation(TestUtils.createUser(), recipient);
        when(friendInvitationRepository.findById(invitation.getId())).thenReturn(Optional.of(invitation));

        // Act & Assert
        assertThatThrownBy(() -> friendService.respondToFriendInvitation(invitation.getId(), "unauthorized@example.com", FriendInvitation.InvitationStatus.ACCEPTED))
                .isInstanceOf(FlashDashException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.E403001);
    }

    @Test
    void shouldGetFriendsSuccessfully() {
        // Arrange
        User user = TestUtils.createUser();
        User friend = TestUtils.createFriendUser();
        user.getFriends().add(friend);
        when(userRepository.findByEmail(user.getUsername())).thenReturn(Optional.of(user));

        // Act
        List<UserResponse> friends = friendService.getFriends(user.getUsername());

        // Assert
        assertThat(friends).hasSize(1);
        assertThat(friends.get(0).getFirstName()).isEqualTo(friend.getFirstName());
        verify(userRepository).findByEmail(user.getUsername());
    }
}
