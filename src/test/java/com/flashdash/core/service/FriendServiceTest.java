package com.flashdash.core.service;

import com.flashdash.core.TestUtils;
import com.flashdash.core.exception.ErrorCode;
import com.flashdash.core.exception.FlashDashException;
import com.flashdash.core.model.User;
import com.flashdash.core.repository.FriendInvitationRepository;
import com.flashdash.core.repository.UserRepository;
import com.flashdash.core.service.api.ActivityService;
import com.flashdash.core.service.api.NotificationService;
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
    private NotificationService notificationService;

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
    void shouldGetFriendSuccessfully() {
        // Arrange
        User user = TestUtils.createUser();
        User friend = TestUtils.createUser();

        user.setFriendsFrnList(List.of(friend.getUserFrn()));

        when(userRepository.findByUserFrn(user.getUserFrn())).thenReturn(Optional.of(user));
        when(userRepository.findByUserFrn(friend.getUserFrn())).thenReturn(Optional.of(friend));

        // Act
        User retrievedFriend = friendService.getFriend(user.getUserFrn(), friend.getUserFrn());

        // Assert
        assertThat(retrievedFriend).isNotNull();
        assertThat(retrievedFriend.getUserFrn()).isEqualTo(friend.getUserFrn());

        verify(userRepository, times(1)).findByUserFrn(user.getUserFrn());
        verify(userRepository, times(1)).findByUserFrn(friend.getUserFrn());
    }

    @Test
    void shouldThrowExceptionWhenFriendNotInList() {
        // Arrange
        User user = TestUtils.createUser();
        User stranger = TestUtils.createUser();
        when(userRepository.findByUserFrn(user.getUserFrn())).thenReturn(Optional.of(user));

        // Act & Assert
        assertThatThrownBy(() -> friendService.getFriend(user.getUserFrn(), stranger.getUserFrn()))
                .isInstanceOf(FlashDashException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.E404003)
                .hasMessage("Friend not found in user's friend list.");

        verify(userRepository, times(1)).findByUserFrn(user.getUserFrn());
        verify(userRepository, never()).findByUserFrn(stranger.getUserFrn());
    }

    @Test
    void shouldThrowExceptionWhenFriendNotFound() {
        // Arrange
        User user = TestUtils.createUser();
        User friend = TestUtils.createUser();

        when(userRepository.findByUserFrn(user.getUserFrn())).thenReturn(Optional.of(user));
        when(userRepository.findByUserFrn(friend.getUserFrn())).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> friendService.getFriend(user.getUserFrn(), friend.getUserFrn()))
                .isInstanceOf(FlashDashException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.E404002)
                .hasMessage("Friend not found");

        verify(userRepository, times(1)).findByUserFrn(user.getUserFrn());
        verify(userRepository, times(1)).findByUserFrn(friend.getUserFrn());
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
