package com.flashdash.core.controller;

import com.flashdash.core.TestUtils;
import com.flashdash.core.model.User;
import com.flashdash.core.service.LeaderboardService;
import com.p4r1nc3.flashdash.core.model.LeaderboardEntry;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.mockito.MockedStatic;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

@SpringBootTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class LeaderboardControllerTest {

    private LeaderboardController leaderboardController;

    @MockitoBean
    private LeaderboardService leaderboardService;

    private User user;
    private MockedStatic<SecurityContextHolder> mockedSecurityContextHolder;

    @BeforeEach
    void setUp() {
        leaderboardController = new LeaderboardController(leaderboardService);
        user = TestUtils.createUser();

        Authentication authentication = mock(Authentication.class);
        when(authentication.getPrincipal()).thenReturn(user);

        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);

        mockedSecurityContextHolder = mockStatic(SecurityContextHolder.class);
        mockedSecurityContextHolder.when(SecurityContextHolder::getContext).thenReturn(securityContext);
    }

    @AfterEach
    void tearDown() {
        mockedSecurityContextHolder.close();
    }

    @Test
    void testGetLeaderboardsSuccessful() {
        // Arrange
        List<LeaderboardEntry> expectedLeaderboard = createSampleLeaderboard();
        when(leaderboardService.getLeaderboard(eq(user.getUserFrn()), anyString(), anyBoolean(), anyInt()))
                .thenReturn(expectedLeaderboard);

        // Act
        ResponseEntity<List<LeaderboardEntry>> responseEntity = leaderboardController.getLeaderboards("points", false, 10);

        // Assert
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertEquals(expectedLeaderboard, responseEntity.getBody());
    }

    @Test
    void testGetLeaderboardsWithDifferentCriteria() {
        // Arrange
        List<LeaderboardEntry> expectedLeaderboard = createSampleLeaderboard();
        when(leaderboardService.getLeaderboard(eq(user.getUserFrn()), eq("studyTime"), anyBoolean(), anyInt()))
                .thenReturn(expectedLeaderboard);

        // Act
        ResponseEntity<List<LeaderboardEntry>> responseEntity = leaderboardController.getLeaderboards("studyTime", false, 10);

        // Assert
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertEquals(expectedLeaderboard, responseEntity.getBody());
    }

    @Test
    void testGetLeaderboardsFriendsOnly() {
        // Arrange
        List<LeaderboardEntry> expectedLeaderboard = createSampleLeaderboard();
        when(leaderboardService.getLeaderboard(eq(user.getUserFrn()), anyString(), eq(true), anyInt()))
                .thenReturn(expectedLeaderboard);

        // Act
        ResponseEntity<List<LeaderboardEntry>> responseEntity = leaderboardController.getLeaderboards("points", true, 10);

        // Assert
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertEquals(expectedLeaderboard, responseEntity.getBody());
    }

    @Test
    void testGetLeaderboardsWithLimit() {
        // Arrange
        List<LeaderboardEntry> expectedLeaderboard = createSampleLeaderboard().subList(0, 3);
        when(leaderboardService.getLeaderboard(eq(user.getUserFrn()), anyString(), anyBoolean(), eq(3)))
                .thenReturn(expectedLeaderboard);

        // Act
        ResponseEntity<List<LeaderboardEntry>> responseEntity = leaderboardController.getLeaderboards("points", false, 3);

        // Assert
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertEquals(expectedLeaderboard, responseEntity.getBody());
        assertEquals(3, responseEntity.getBody().size());
    }

    @Test
    void testGetLeaderboardsWithStreak() {
        // Arrange
        List<LeaderboardEntry> expectedLeaderboard = createSampleLeaderboard();
        when(leaderboardService.getLeaderboard(eq(user.getUserFrn()), eq("streak"), anyBoolean(), anyInt()))
                .thenReturn(expectedLeaderboard);

        // Act
        ResponseEntity<List<LeaderboardEntry>> responseEntity = leaderboardController.getLeaderboards("streak", false, 10);

        // Assert
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertEquals(expectedLeaderboard, responseEntity.getBody());
    }

    @Test
    void testGetLeaderboardsWithGamesPlayed() {
        // Arrange
        List<LeaderboardEntry> expectedLeaderboard = createSampleLeaderboard();
        when(leaderboardService.getLeaderboard(eq(user.getUserFrn()), eq("gamesPlayed"), anyBoolean(), anyInt()))
                .thenReturn(expectedLeaderboard);

        // Act
        ResponseEntity<List<LeaderboardEntry>> responseEntity = leaderboardController.getLeaderboards("gamesPlayed", false, 10);

        // Assert
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertEquals(expectedLeaderboard, responseEntity.getBody());
    }

    private List<LeaderboardEntry> createSampleLeaderboard() {
        List<LeaderboardEntry> leaderboard = new ArrayList<>();

        leaderboard.add(new LeaderboardEntry()
                .rank(1)
                .userFrn("frn:flashdash:user:user1")
                .score(1000));

        leaderboard.add(new LeaderboardEntry()
                .rank(2)
                .userFrn("frn:flashdash:user:user2")
                .score(900));

        leaderboard.add(new LeaderboardEntry()
                .rank(3)
                .userFrn("frn:flashdash:user:user3")
                .score(800));

        leaderboard.add(new LeaderboardEntry()
                .rank(4)
                .userFrn("frn:flashdash:user:user4")
                .score(700));

        leaderboard.add(new LeaderboardEntry()
                .rank(5)
                .userFrn("frn:flashdash:user:user5")
                .score(600));

        return leaderboard;
    }
}