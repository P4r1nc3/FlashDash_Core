package com.flashdash.core.repository;

import com.flashdash.core.TestUtils;
import com.flashdash.core.model.Deck;
import com.flashdash.core.model.GameSession;
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
@SpringBootTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class GameSessionRepositoryTest {

    @Autowired
    private GameSessionRepository gameSessionRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private DeckRepository deckRepository;

    @BeforeEach
    void setUp() {
        gameSessionRepository.deleteAll();
        deckRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    void shouldFindTopByDeckFrnAndUserFrnAndStatus() {
        // Arrange
        User user = TestUtils.createUser();
        userRepository.save(user);

        Deck deck = TestUtils.createDeck(user);
        deckRepository.save(deck);

        GameSession session = TestUtils.createGameSession(user, deck, "PENDING");
        gameSessionRepository.save(session);

        // Act
        Optional<GameSession> foundSession = gameSessionRepository.findTopByDeckFrnAndUserFrnAndStatus(
                deck.getDeckFrn(), user.getUserFrn(), "PENDING"
        );

        // Assert
        assertThat(foundSession).isPresent();
        assertThat(foundSession.get().getUserFrn()).isEqualTo(user.getUserFrn());
        assertThat(foundSession.get().getDeckFrn()).isEqualTo(deck.getDeckFrn());
        assertThat(foundSession.get().getStatus()).isEqualTo("PENDING");
    }

    @Test
    void shouldReturnEmptyWhenNoActivePendingSessionExists() {
        // Arrange
        User user = TestUtils.createUser();
        userRepository.save(user);

        Deck deck = TestUtils.createDeck(user);
        deckRepository.save(deck);

        // Act
        Optional<GameSession> foundSession = gameSessionRepository.findTopByDeckFrnAndUserFrnAndStatus(
                deck.getDeckFrn(), user.getUserFrn(), "PENDING"
        );

        // Assert
        assertThat(foundSession).isNotPresent();
    }

    @Test
    void shouldFindByDeckFrnAndGameSessionFrnAndUserFrn() {
        // Arrange
        User user = TestUtils.createUser();
        userRepository.save(user);

        Deck deck = TestUtils.createDeck(user);
        deckRepository.save(deck);

        GameSession session = TestUtils.createGameSession(user, deck, "FINISHED");
        gameSessionRepository.save(session);

        // Act
        Optional<GameSession> foundSession = gameSessionRepository.findByDeckFrnAndGameSessionFrnAndUserFrn(
                deck.getDeckFrn(), session.getGameSessionFrn(), user.getUserFrn()
        );

        // Assert
        assertThat(foundSession).isPresent();
        assertThat(foundSession.get().getGameSessionFrn()).isEqualTo(session.getGameSessionFrn());
        assertThat(foundSession.get().getDeckFrn()).isEqualTo(deck.getDeckFrn());
        assertThat(foundSession.get().getUserFrn()).isEqualTo(user.getUserFrn());
    }

    @Test
    void shouldReturnEmptyWhenGameSessionDoesNotExist() {
        // Arrange
        User user = TestUtils.createUser();
        userRepository.save(user);

        Deck deck = TestUtils.createDeck(user);
        deckRepository.save(deck);

        // Act
        Optional<GameSession> foundSession = gameSessionRepository.findByDeckFrnAndGameSessionFrnAndUserFrn(
                deck.getDeckFrn(), "frn:flashdash:game-session:nonexistent", user.getUserFrn()
        );

        // Assert
        assertThat(foundSession).isNotPresent();
    }

    @Test
    void shouldFindAllGameSessionsByUserFrn() {
        // Arrange
        User user = TestUtils.createUser();
        userRepository.save(user);

        Deck deck1 = TestUtils.createDeck(user);
        deckRepository.save(deck1);

        Deck deck2 = TestUtils.createDeck(user);
        deckRepository.save(deck2);

        GameSession session1 = TestUtils.createGameSession(user, deck1, "PENDING");
        GameSession session2 = TestUtils.createGameSession(user, deck2, "FINISHED");

        gameSessionRepository.saveAll(List.of(session1, session2));

        // Act
        List<GameSession> sessions = gameSessionRepository.findAllByUserFrn(user.getUserFrn());

        // Assert
        assertThat(sessions).hasSize(2);
        assertThat(sessions).extracting(GameSession::getUserFrn)
                .containsOnly(user.getUserFrn());
    }

    @Test
    void shouldReturnEmptyListWhenUserHasNoGameSessions() {
        // Arrange
        User user = TestUtils.createUser();
        userRepository.save(user);

        // Act
        List<GameSession> sessions = gameSessionRepository.findAllByUserFrn(user.getUserFrn());

        // Assert
        assertThat(sessions).isEmpty();
    }

    @Test
    void shouldFindAllGameSessionsByDeckFrnAndUserFrn() {
        // Arrange
        User user = TestUtils.createUser();
        userRepository.save(user);

        Deck deck1 = TestUtils.createDeck(user);
        deckRepository.save(deck1);

        Deck deck2 = TestUtils.createDeck(user);
        deckRepository.save(deck2);

        GameSession session1 = TestUtils.createGameSession(user, deck1, "PENDING");
        GameSession session2 = TestUtils.createGameSession(user, deck1, "FINISHED");
        GameSession session3 = TestUtils.createGameSession(user, deck2, "PENDING");

        gameSessionRepository.saveAll(List.of(session1, session2, session3));

        // Act
        List<GameSession> deck1Sessions = gameSessionRepository.findAllByDeckFrnAndUserFrn(deck1.getDeckFrn(), user.getUserFrn());

        // Assert
        assertThat(deck1Sessions).hasSize(2);
        assertThat(deck1Sessions).extracting(GameSession::getDeckFrn).containsOnly(deck1.getDeckFrn());
        assertThat(deck1Sessions).extracting(GameSession::getUserFrn).containsOnly(user.getUserFrn());
    }

    @Test
    void shouldReturnEmptyListWhenNoSessionsExistForUserAndDeck() {
        // Arrange
        User user = TestUtils.createUser();
        userRepository.save(user);

        Deck deck = TestUtils.createDeck(user);
        deckRepository.save(deck);

        // Act
        List<GameSession> sessions = gameSessionRepository.findAllByDeckFrnAndUserFrn(deck.getDeckFrn(), user.getUserFrn());

        // Assert
        assertThat(sessions).isEmpty();
    }
}
