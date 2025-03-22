package com.flashdash.core.service;

import com.flashdash.core.TestUtils;
import com.flashdash.core.model.User;
import com.flashdash.core.repository.UserRepository;
import com.p4r1nc3.flashdash.core.model.LeaderboardEntry;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.when;

@SpringBootTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class LeaderboardServiceTest {

    @Autowired
    private LeaderboardService leaderboardService;

    @MockitoBean
    private UserRepository userRepository;

    @MockitoBean
    private UserService userService;

    @Test
    void shouldGetGlobalLeaderboardByPointsSuccessfully() {
        // Arrange
        String userFrn = "frn:flashdash:user:testUser";
        List<User> users = createSampleUsers();
        when(userRepository.findAll()).thenReturn(users);

        // Act
        List<LeaderboardEntry> leaderboard = leaderboardService.getLeaderboard(userFrn, "points", false, 10);

        // Assert
        assertThat(leaderboard).isNotNull();
        assertThat(leaderboard).hasSize(5);

        // Verify sorting by points (highest to lowest)
        assertThat(leaderboard.get(0).getRank()).isEqualTo(1);
        assertThat(leaderboard.get(0).getUserFrn()).isEqualTo("frn:flashdash:user:user2");
        assertThat(leaderboard.get(0).getScore()).isEqualTo(1000);

        assertThat(leaderboard.get(4).getRank()).isEqualTo(5);
        assertThat(leaderboard.get(4).getUserFrn()).isEqualTo("frn:flashdash:user:user5");
        assertThat(leaderboard.get(4).getScore()).isEqualTo(200);
    }

    @Test
    void shouldGetGlobalLeaderboardByStudyTimeSuccessfully() {
        // Arrange
        String userFrn = "frn:flashdash:user:testUser";
        List<User> users = createSampleUsers();
        when(userRepository.findAll()).thenReturn(users);

        // Act
        List<LeaderboardEntry> leaderboard = leaderboardService.getLeaderboard(userFrn, "studyTime", false, 10);

        // Assert
        assertThat(leaderboard).isNotNull();
        assertThat(leaderboard).hasSize(5);

        // Verify sorting by study time (highest to lowest)
        assertThat(leaderboard.get(0).getRank()).isEqualTo(1);
        assertThat(leaderboard.get(0).getUserFrn()).isEqualTo("frn:flashdash:user:user4");
        assertThat(leaderboard.get(0).getScore()).isEqualTo(600); // 10 hours = 600 minutes
    }

    @Test
    void shouldGetGlobalLeaderboardByGamesPlayedSuccessfully() {
        // Arrange
        String userFrn = "frn:flashdash:user:testUser";
        List<User> users = createSampleUsers();
        when(userRepository.findAll()).thenReturn(users);

        // Act
        List<LeaderboardEntry> leaderboard = leaderboardService.getLeaderboard(userFrn, "gamesPlayed", false, 10);

        // Assert
        assertThat(leaderboard).isNotNull();
        assertThat(leaderboard).hasSize(5);

        // Verify sorting by games played (highest to lowest)
        assertThat(leaderboard.get(0).getRank()).isEqualTo(1);
        assertThat(leaderboard.get(0).getUserFrn()).isEqualTo("frn:flashdash:user:user5");
        assertThat(leaderboard.get(0).getScore()).isEqualTo(50);
    }

    @Test
    void shouldGetGlobalLeaderboardByStreakSuccessfully() {
        // Arrange
        String userFrn = "frn:flashdash:user:testUser";
        List<User> users = createSampleUsers();
        when(userRepository.findAll()).thenReturn(users);

        // Act
        List<LeaderboardEntry> leaderboard = leaderboardService.getLeaderboard(userFrn, "streak", false, 10);

        // Assert
        assertThat(leaderboard).isNotNull();
        assertThat(leaderboard).hasSize(5);

        // Verify sorting by streak (highest to lowest)
        assertThat(leaderboard.get(0).getRank()).isEqualTo(1);
        assertThat(leaderboard.get(0).getUserFrn()).isEqualTo("frn:flashdash:user:user3");
        assertThat(leaderboard.get(0).getScore()).isEqualTo(30);
    }

    @Test
    void shouldGetFriendsOnlyLeaderboardSuccessfully() {
        // Arrange
        String userFrn = "frn:flashdash:user:testUser";
        User currentUser = TestUtils.createUser();
        currentUser.setUserFrn(userFrn);

        List<String> friendsFrn = Arrays.asList(
                "frn:flashdash:user:user1",
                "frn:flashdash:user:user3");
        currentUser.setFriendsFrnList(friendsFrn);

        List<User> friendsUsers = new ArrayList<>();
        friendsUsers.add(createUserWithDetails("frn:flashdash:user:user1", "User One", 500, 15, Duration.ofHours(5), 10));
        friendsUsers.add(createUserWithDetails("frn:flashdash:user:user3", "User Three", 700, 25, Duration.ofHours(7), 30));
        friendsUsers.add(currentUser);

        when(userService.getCurrentUser(userFrn)).thenReturn(currentUser);
        when(userRepository.findByUserFrnIn(anyList())).thenReturn(friendsUsers);

        // Act
        List<LeaderboardEntry> leaderboard = leaderboardService.getLeaderboard(userFrn, "points", true, 10);

        // Assert
        assertThat(leaderboard).isNotNull();
        assertThat(leaderboard).hasSize(3);

        // Verify sorting and content includes only friends
        assertThat(leaderboard.get(0).getUserFrn()).isEqualTo("frn:flashdash:user:user3");
        assertThat(leaderboard.get(1).getUserFrn()).isEqualTo("frn:flashdash:user:user1");
        assertThat(leaderboard.get(2).getUserFrn()).isEqualTo(userFrn);
    }

    @Test
    void shouldLimitLeaderboardResults() {
        // Arrange
        String userFrn = "frn:flashdash:user:testUser";
        List<User> users = createSampleUsers();
        when(userRepository.findAll()).thenReturn(users);

        // Act
        List<LeaderboardEntry> leaderboard = leaderboardService.getLeaderboard(userFrn, "points", false, 3);

        // Assert
        assertThat(leaderboard).isNotNull();
        assertThat(leaderboard).hasSize(3);

        // Verify only top 3 users are included
        assertThat(leaderboard.get(0).getUserFrn()).isEqualTo("frn:flashdash:user:user2");
        assertThat(leaderboard.get(1).getUserFrn()).isEqualTo("frn:flashdash:user:user3");
        assertThat(leaderboard.get(2).getUserFrn()).isEqualTo("frn:flashdash:user:user1");
    }

    @Test
    void shouldHandleEmptyUserList() {
        // Arrange
        String userFrn = "frn:flashdash:user:testUser";
        when(userRepository.findAll()).thenReturn(Collections.emptyList());

        // Act
        List<LeaderboardEntry> leaderboard = leaderboardService.getLeaderboard(userFrn, "points", false, 10);

        // Assert
        assertThat(leaderboard).isNotNull();
        assertThat(leaderboard).isEmpty();
    }

    @Test
    void shouldHandleNullStudyTime() {
        // Arrange
        String userFrn = "frn:flashdash:user:testUser";
        List<User> users = new ArrayList<>();

        User user = createUserWithDetails("frn:flashdash:user:user1", "User One", 500, 15, null, 10);
        users.add(user);

        when(userRepository.findAll()).thenReturn(users);

        // Act
        List<LeaderboardEntry> leaderboard = leaderboardService.getLeaderboard(userFrn, "studyTime", false, 10);

        // Assert
        assertThat(leaderboard).isNotNull();
        assertThat(leaderboard).hasSize(1);
        assertThat(leaderboard.get(0).getScore()).isEqualTo(0);
    }

    private List<User> createSampleUsers() {
        List<User> users = new ArrayList<>();

        users.add(createUserWithDetails("frn:flashdash:user:user1", "User One", 500, 15, Duration.ofHours(5), 10));
        users.add(createUserWithDetails("frn:flashdash:user:user2", "User Two", 1000, 20, Duration.ofHours(8), 15));
        users.add(createUserWithDetails("frn:flashdash:user:user3", "User Three", 700, 25, Duration.ofHours(7), 30));
        users.add(createUserWithDetails("frn:flashdash:user:user4", "User Four", 300, 30, Duration.ofHours(10), 5));
        users.add(createUserWithDetails("frn:flashdash:user:user5", "User Five", 200, 50, Duration.ofHours(3), 20));

        return users;
    }

    private User createUserWithDetails(String userFrn, String username, int points, int gamesPlayed, Duration studyTime, int streak) {
        User user = TestUtils.createUser();
        user.setUserFrn(userFrn);
        user.setUsername(username);
        user.setPoints(points);
        user.setGamesPlayed(gamesPlayed);
        user.setStudyTime(studyTime);
        user.setStrike(streak);
        return user;
    }
}