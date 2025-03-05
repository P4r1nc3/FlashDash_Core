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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

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

    @MockitoBean
    private UserRepository userRepository;

    @MockitoBean
    private FriendInvitationRepository friendInvitationRepository;

    @MockitoBean
    private EmailService emailService;

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
        when(userRepository.findById(recipient.getUserFrn())).thenReturn(Optional.of(recipient));
        when(friendInvitationRepository.findBySentByFrnAndSentToFrn(sender.getUserFrn(), recipient.getUserFrn()))
                .thenReturn(Optional.empty());

        friendService.sendFriendInvitation(sender.getUserFrn(), recipient.getUserFrn());

        verify(friendInvitationRepository).save(any(FriendInvitation.class));
        verify(emailService, times(1)).sendFriendInvitationEmail(recipient.getEmail(), sender.getFirstName(), sender.getLastName());
    }

    @Test
    void shouldThrowExceptionWhenSendingInvitationToSelf() {
        assertThatThrownBy(() -> friendService.sendFriendInvitation(sender.getUserFrn(), sender.getUserFrn()))
                .isInstanceOf(FlashDashException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.E403003);

        verify(friendInvitationRepository, never()).save(any(FriendInvitation.class));
    }

    @Test
    void shouldThrowExceptionWhenInvitationAlreadyExists() {
        FriendInvitation existingInvitation = TestUtils.createFriendInvitation(sender, recipient);
        when(friendInvitationRepository.findBySentByFrnAndSentToFrn(sender.getUserFrn(), recipient.getUserFrn()))
                .thenReturn(Optional.of(existingInvitation));

        assertThatThrownBy(() -> friendService.sendFriendInvitation(sender.getUserFrn(), recipient.getUserFrn()))
                .isInstanceOf(FlashDashException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.E409002);
    }

    @Test
    void shouldThrowExceptionWhenSenderNotFound() {
        when(userRepository.findById(sender.getUserFrn())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> friendService.sendFriendInvitation(sender.getUserFrn(), recipient.getUserFrn()))
                .isInstanceOf(FlashDashException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.E404002);
    }

    @Test
    void shouldThrowExceptionWhenRecipientNotFound() {
        when(userRepository.findById(recipient.getUserFrn())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> friendService.sendFriendInvitation(sender.getUserFrn(), recipient.getUserFrn()))
                .isInstanceOf(FlashDashException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.E404002);
    }

    @Test
    void shouldGetReceivedFriendInvitationsSuccessfully() {
        FriendInvitation invitation = TestUtils.createFriendInvitation(sender, recipient);
        when(friendInvitationRepository.findAllBySentToFrn(recipient.getUserFrn())).thenReturn(List.of(invitation));

        List<FriendInvitationResponse> invitations = friendService.getReceivedFriendInvitations(recipient.getUserFrn());

        assertThat(invitations).hasSize(1);
        verify(friendInvitationRepository).findAllBySentToFrn(recipient.getUserFrn());
    }

    @Test
    void shouldRespondToFriendInvitationSuccessfully() {
        // Arrange
        FriendInvitation invitation = TestUtils.createFriendInvitation(sender, recipient);

        when(friendInvitationRepository.findById(invitation.getInvitationFrn())).thenReturn(Optional.of(invitation));
        when(userRepository.findById(sender.getUserFrn())).thenReturn(Optional.of(sender));
        when(userRepository.findById(recipient.getUserFrn())).thenReturn(Optional.of(recipient));

        // Act
        friendService.respondToFriendInvitation(invitation.getInvitationFrn(), recipient.getUserFrn(), "ACCEPTED");

        // Assert
        verify(friendInvitationRepository).delete(invitation);
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
        User friend = TestUtils.createUser();
        user.setFriendsFrnList(List.of(friend.getUserFrn()));
        friend.setFriendsFrnList(List.of(user.getUserFrn()));

        when(userRepository.findById(user.getUserFrn())).thenReturn(Optional.of(user));
        when(userRepository.findById(friend.getUserFrn())).thenReturn(Optional.of(friend));

        // Act
        List<UserResponse> friends = friendService.getFriends(user.getUserFrn());

        // Assert
        assertThat(friends).hasSize(1);
        assertThat(friends.get(0).getUserFrn()).isEqualTo(friend.getUserFrn());
        verify(userRepository).findById(user.getUserFrn());
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
