package com.flashdash.core.controller;

import com.flashdash.core.model.User;
import com.flashdash.core.service.LeaderboardService;
import com.p4r1nc3.flashdash.core.model.LeaderboardEntry;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/leaderboards")
public class LeaderboardController {

    private final LeaderboardService leaderboardService;

    public LeaderboardController(LeaderboardService leaderboardService) {
        this.leaderboardService = leaderboardService;
    }

    @GetMapping
    public ResponseEntity<List<LeaderboardEntry>> getLeaderboards(
            @RequestParam(defaultValue = "points") String criteria,
            @RequestParam(defaultValue = "false") boolean friendsOnly,
            @RequestParam(defaultValue = "10") int limit) {
        String userFrn = getAuthenticatedUser();
        return ResponseEntity.ok(leaderboardService.getLeaderboard(userFrn, criteria, friendsOnly, limit));
    }

    private String getAuthenticatedUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User user = (User) authentication.getPrincipal();
        return user.getUserFrn();
    }
}
