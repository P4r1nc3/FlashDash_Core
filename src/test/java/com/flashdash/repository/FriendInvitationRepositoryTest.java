package com.flashdash.repository;

import com.flashdash.FlashDashApplication;
import com.flashdash.TestUtils;
import com.flashdash.model.FriendInvitation;
import com.flashdash.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@Transactional
@SpringBootTest(classes = FlashDashApplication.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class FriendInvitationRepositoryTest {

    @Autowired
    private FriendInvitationRepository friendInvitationRepository;

    @Autowired
    private UserRepository userRepository;

    @BeforeEach
    void setUp() {
        friendInvitationRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    void shouldFindAllInvitationsBySentTo() {
        // Arrange
        User sender = TestUtils.createUser();
        userRepository.save(sender);

        User recipient = TestUtils.createFriendUser();
        userRepository.save(recipient);

        FriendInvitation invitation1 = TestUtils.createFriendInvitation(sender, recipient);
        friendInvitationRepository.save(invitation1);

        FriendInvitation invitation2 = TestUtils.createFriendInvitation(sender, recipient);
        friendInvitationRepository.save(invitation2);

        // Act
        List<FriendInvitation> invitations = friendInvitationRepository.findAllBySentTo(recipient);

        // Assert
        assertThat(invitations).hasSize(2);
        assertThat(invitations).extracting(FriendInvitation::getSentBy)
                .containsExactly(sender, sender);
    }

    @Test
    void shouldFindAllInvitationsBySentBy() {
        // Arrange
        User sender = TestUtils.createUser();
        userRepository.save(sender);

        User recipient = TestUtils.createFriendUser();
        userRepository.save(recipient);

        FriendInvitation invitation1 = TestUtils.createFriendInvitation(sender, recipient);
        friendInvitationRepository.save(invitation1);

        FriendInvitation invitation2 = TestUtils.createFriendInvitation(sender, recipient);
        friendInvitationRepository.save(invitation2);

        // Act
        List<FriendInvitation> invitations = friendInvitationRepository.findAllBySentBy(sender);

        // Assert
        assertThat(invitations).hasSize(2);
        assertThat(invitations).extracting(FriendInvitation::getSentTo)
                .containsExactly(recipient, recipient);
    }

    @Test
    void shouldFindSpecificInvitationBySentByAndSentTo() {
        // Arrange
        User sender = TestUtils.createUser();
        userRepository.save(sender);

        User recipient = TestUtils.createFriendUser();
        userRepository.save(recipient);

        FriendInvitation invitation = TestUtils.createFriendInvitation(sender, recipient);
        friendInvitationRepository.save(invitation);

        // Act
        Optional<FriendInvitation> foundInvitation = friendInvitationRepository.findBySentByAndSentTo(sender, recipient);

        // Assert
        assertThat(foundInvitation).isPresent();
        assertThat(foundInvitation.get().getSentBy()).isEqualTo(sender);
        assertThat(foundInvitation.get().getSentTo()).isEqualTo(recipient);
    }

    @Test
    void shouldReturnEmptyWhenInvitationNotExists() {
        // Arrange
        User sender = TestUtils.createUser();
        userRepository.save(sender);

        User recipient = TestUtils.createFriendUser();
        userRepository.save(recipient);

        // Act
        Optional<FriendInvitation> foundInvitation = friendInvitationRepository.findBySentByAndSentTo(sender, recipient);

        // Assert
        assertThat(foundInvitation).isNotPresent();
    }

    @Test
    void shouldDeleteAllInvitationsBySentTo() {
        // Arrange
        User sender = TestUtils.createUser();
        userRepository.save(sender);

        User recipient = TestUtils.createFriendUser();
        userRepository.save(recipient);

        FriendInvitation invitation1 = TestUtils.createFriendInvitation(sender, recipient);
        friendInvitationRepository.save(invitation1);

        FriendInvitation invitation2 = TestUtils.createFriendInvitation(sender, recipient);
        friendInvitationRepository.save(invitation2);

        // Act
        friendInvitationRepository.deleteAll(friendInvitationRepository.findAllBySentTo(recipient));
        List<FriendInvitation> invitations = friendInvitationRepository.findAllBySentTo(recipient);

        // Assert
        assertThat(invitations).isEmpty();
    }
}
