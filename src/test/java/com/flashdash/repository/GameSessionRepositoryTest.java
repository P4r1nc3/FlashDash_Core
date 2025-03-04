package com.flashdash.repository;

import com.flashdash.FlashDashApplication;
import com.flashdash.TestUtils;
import com.flashdash.model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(classes = FlashDashApplication.class)
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
    void shouldFindTopByDeckIdAndUserIdAndStatus() {
        // Arrange
        User user = TestUtils.createUser();
        userRepository.save(user);

        Deck deck = TestUtils.createDeck(user);
        deckRepository.save(deck);

        GameSession session = TestUtils.createGameSession(user, deck, GameSessionStatus.PENDING);
        gameSessionRepository.save(session);

        // Act
        GameSession foundSession = gameSessionRepository.findTopByDeckIdAndUserIdAndStatus(
                deck.getId(), user.getId(), GameSessionStatus.PENDING
        );

        // Assert
        assertThat(foundSession).isNotNull();
        assertThat(foundSession.getUser().getId()).isEqualTo(user.getId());
        assertThat(foundSession.getDeck().getId()).isEqualTo(deck.getId());
        assertThat(foundSession.getStatus()).isEqualTo(GameSessionStatus.PENDING);
    }

    @Test
    void shouldReturnNullWhenNoActivePendingSessionExists() {
        // Arrange
        User user = TestUtils.createUser();
        userRepository.save(user);

        Deck deck = TestUtils.createDeck(user);
        deckRepository.save(deck);

        // Act
        GameSession foundSession = gameSessionRepository.findTopByDeckIdAndUserIdAndStatus(
                deck.getId(), user.getId(), GameSessionStatus.PENDING
        );

        // Assert
        assertThat(foundSession).isNull();
    }

    @Test
    void shouldFindAllGameSessionsByUser() {
        // Arrange
        User user = TestUtils.createUser();
        userRepository.save(user);

        Deck deck1 = TestUtils.createDeck(user);
        deckRepository.save(deck1);

        Deck deck2 = TestUtils.createDeck(user);
        deckRepository.save(deck2);

        GameSession session1 = TestUtils.createGameSession(user, deck1, GameSessionStatus.PENDING);
        GameSession session2 = TestUtils.createGameSession(user, deck2, GameSessionStatus.FINISHED);
        session2.setTotalScore(50);
        session2.setCorrectAnswersCount(5);
        session2.setEndTime(LocalDateTime.now());

        gameSessionRepository.save(session1);
        gameSessionRepository.save(session2);

        // Act
        List<GameSession> sessions = gameSessionRepository.findAllByUser(user);

        // Assert
        assertThat(sessions).hasSize(2);
        assertThat(sessions).extracting(GameSession::getStatus)
                .containsExactlyInAnyOrder(GameSessionStatus.PENDING, GameSessionStatus.FINISHED);
        assertThat(sessions).extracting(GameSession::getTotalScore)
                .containsExactlyInAnyOrder(0, 50);
        assertThat(sessions).extracting(GameSession::getCorrectAnswersCount)
                .containsExactlyInAnyOrder(0, 5);
    }

    @Test
    void shouldReturnEmptyListWhenUserHasNoGameSessions() {
        // Arrange
        User user = TestUtils.createUser();
        userRepository.save(user);

        // Act
        List<GameSession> sessions = gameSessionRepository.findAllByUser(user);

        // Assert
        assertThat(sessions).isEmpty();
    }

    @Test
    void shouldFindAllGameSessionsForSpecificDeckAndUser() {
        // Arrange
        User user = TestUtils.createUser();
        userRepository.save(user);

        Deck deck1 = TestUtils.createDeck(user);
        deckRepository.save(deck1);

        Deck deck2 = TestUtils.createDeck(user);
        deckRepository.save(deck2);

        GameSession session1 = TestUtils.createGameSession(user, deck1, GameSessionStatus.PENDING);
        GameSession session2 = TestUtils.createGameSession(user, deck1, GameSessionStatus.FINISHED);
        GameSession session3 = TestUtils.createGameSession(user, deck2, GameSessionStatus.PENDING);

        gameSessionRepository.saveAll(List.of(session1, session2, session3));

        // Act
        List<GameSession> deck1Sessions = gameSessionRepository.findAllByDeckIdAndUserId(deck1.getId(), user.getId());

        // Assert
        assertThat(deck1Sessions).hasSize(2);
        assertThat(deck1Sessions).extracting(GameSession::getDeck).extracting(Deck::getId).containsOnly(deck1.getId());
        assertThat(deck1Sessions).extracting(GameSession::getUser).extracting(User::getId).containsOnly(user.getId());
    }

    @Test
    void shouldReturnEmptyListWhenNoSessionsExistForUserAndDeck() {
        // Arrange
        User user = TestUtils.createUser();
        userRepository.save(user);

        Deck deck = TestUtils.createDeck(user);
        deckRepository.save(deck);

        // Act
        List<GameSession> sessions = gameSessionRepository.findAllByDeckIdAndUserId(deck.getId(), user.getId());

        // Assert
        assertThat(sessions).isEmpty();
    }
}
