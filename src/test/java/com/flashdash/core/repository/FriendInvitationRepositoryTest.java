package com.flashdash.core.repository;

import com.flashdash.core.FlashDashCoreApplication;
import com.flashdash.core.TestUtils;
import com.flashdash.core.model.FriendInvitation;
import com.flashdash.core.model.User;
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
@SpringBootTest(classes = FlashDashCoreApplication.class)
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
    void shouldFindAllPendingInvitationsBySentToFrn() {
        // Arrange
        User sender = TestUtils.createUser();
        userRepository.save(sender);

        User recipient = TestUtils.createUser();
        userRepository.save(recipient);

        FriendInvitation pendingInvitation = TestUtils.createFriendInvitation(sender, recipient);
        pendingInvitation.setStatus("PENDING");
        friendInvitationRepository.save(pendingInvitation);

        FriendInvitation acceptedInvitation = TestUtils.createFriendInvitation(sender, recipient);
        acceptedInvitation.setStatus("ACCEPTED");
        friendInvitationRepository.save(acceptedInvitation);

        // Act
        List<FriendInvitation> invitations = friendInvitationRepository.findAllBySentToFrnAndStatus(
                recipient.getUserFrn(), "PENDING");

        // Assert
        assertThat(invitations).hasSize(1);
        assertThat(invitations.get(0).getStatus()).isEqualTo("PENDING");
    }

    @Test
    void shouldFindAllPendingInvitationsBySentByFrn() {
        // Arrange
        User sender = TestUtils.createUser();
        userRepository.save(sender);

        User recipient = TestUtils.createUser();
        userRepository.save(recipient);

        FriendInvitation pendingInvitation = TestUtils.createFriendInvitation(sender, recipient);
        pendingInvitation.setStatus("PENDING");
        friendInvitationRepository.save(pendingInvitation);

        FriendInvitation rejectedInvitation = TestUtils.createFriendInvitation(sender, recipient);
        rejectedInvitation.setStatus("REJECTED");
        friendInvitationRepository.save(rejectedInvitation);

        // Act
        List<FriendInvitation> invitations = friendInvitationRepository.findAllBySentByFrnAndStatus(
                sender.getUserFrn(), "PENDING");

        // Assert
        assertThat(invitations).hasSize(1);
        assertThat(invitations.get(0).getStatus()).isEqualTo("PENDING");
    }

    @Test
    void shouldReturnEmptyListWhenNoPendingInvitations() {
        // Arrange
        User sender = TestUtils.createUser();
        userRepository.save(sender);

        User recipient = TestUtils.createUser();
        userRepository.save(recipient);

        FriendInvitation acceptedInvitation = TestUtils.createFriendInvitation(sender, recipient);
        acceptedInvitation.setStatus("ACCEPTED");
        friendInvitationRepository.save(acceptedInvitation);

        // Act
        List<FriendInvitation> invitations = friendInvitationRepository.findAllBySentByFrnAndStatus(
                sender.getUserFrn(), "PENDING");

        // Assert
        assertThat(invitations).isEmpty();
    }

    @Test
    void shouldReturnEmptyWhenInvitationDoesNotExist() {
        // Arrange
        User sender = TestUtils.createUser();
        userRepository.save(sender);

        User recipient = TestUtils.createUser();
        userRepository.save(recipient);

        // Act
        Optional<FriendInvitation> foundInvitation = friendInvitationRepository.findBySentByFrnAndSentToFrn(
                sender.getUserFrn(), recipient.getUserFrn());

        // Assert
        assertThat(foundInvitation).isNotPresent();
    }

    @Test
    void shouldNotIncludeNonPendingInvitations() {
        // Arrange
        User sender = TestUtils.createUser();
        userRepository.save(sender);

        User recipient = TestUtils.createUser();
        userRepository.save(recipient);

        FriendInvitation pendingInvitation = TestUtils.createFriendInvitation(sender, recipient);
        pendingInvitation.setStatus("PENDING");
        friendInvitationRepository.save(pendingInvitation);

        FriendInvitation acceptedInvitation = TestUtils.createFriendInvitation(sender, recipient);
        acceptedInvitation.setStatus("ACCEPTED");
        friendInvitationRepository.save(acceptedInvitation);

        FriendInvitation rejectedInvitation = TestUtils.createFriendInvitation(sender, recipient);
        rejectedInvitation.setStatus("REJECTED");
        friendInvitationRepository.save(rejectedInvitation);

        // Act
        List<FriendInvitation> invitations = friendInvitationRepository.findAllBySentToFrnAndStatus(
                recipient.getUserFrn(), "PENDING");

        // Assert
        assertThat(invitations).hasSize(1);
        assertThat(invitations.get(0).getStatus()).isEqualTo("PENDING");
    }
}
