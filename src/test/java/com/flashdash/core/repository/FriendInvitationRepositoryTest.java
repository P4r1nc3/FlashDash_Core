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
    void shouldFindAllInvitationsBySentToFrn() {
        // Arrange
        User sender = TestUtils.createUser();
        userRepository.save(sender);

        User recipient = TestUtils.createUser();
        userRepository.save(recipient);

        FriendInvitation invitation1 = TestUtils.createFriendInvitation(sender, recipient);
        friendInvitationRepository.save(invitation1);

        FriendInvitation invitation2 = TestUtils.createFriendInvitation(sender, recipient);
        friendInvitationRepository.save(invitation2);

        // Act
        List<FriendInvitation> invitations = friendInvitationRepository.findAllBySentToFrn(recipient.getUserFrn());

        // Assert
        assertThat(invitations).hasSize(2);
        assertThat(invitations).extracting(FriendInvitation::getSentByFrn)
                .containsExactly(sender.getUserFrn(), sender.getUserFrn());
    }

    @Test
    void shouldFindAllInvitationsBySentByFrn() {
        // Arrange
        User sender = TestUtils.createUser();
        userRepository.save(sender);

        User recipient = TestUtils.createUser();
        userRepository.save(recipient);

        FriendInvitation invitation1 = TestUtils.createFriendInvitation(sender, recipient);
        friendInvitationRepository.save(invitation1);

        FriendInvitation invitation2 = TestUtils.createFriendInvitation(sender, recipient);
        friendInvitationRepository.save(invitation2);

        // Act
        List<FriendInvitation> invitations = friendInvitationRepository.findAllBySentByFrn(sender.getUserFrn());

        // Assert
        assertThat(invitations).hasSize(2);
        assertThat(invitations).extracting(FriendInvitation::getSentToFrn)
                .containsExactly(recipient.getUserFrn(), recipient.getUserFrn());
    }

    @Test
    void shouldFindSpecificInvitationBySentByFrnAndSentToFrn() {
        // Arrange
        User sender = TestUtils.createUser();
        userRepository.save(sender);

        User recipient = TestUtils.createUser();
        userRepository.save(recipient);

        FriendInvitation invitation = TestUtils.createFriendInvitation(sender, recipient);
        friendInvitationRepository.save(invitation);

        // Act
        Optional<FriendInvitation> foundInvitation = friendInvitationRepository.findBySentByFrnAndSentToFrn(
                sender.getUserFrn(), recipient.getUserFrn());

        // Assert
        assertThat(foundInvitation).isPresent();
        assertThat(foundInvitation.get().getSentByFrn()).isEqualTo(sender.getUserFrn());
        assertThat(foundInvitation.get().getSentToFrn()).isEqualTo(recipient.getUserFrn());
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
}
