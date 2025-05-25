package com.flashdash.core.service;

import com.flashdash.core.model.User;
import com.flashdash.core.repository.UserRepository;
import com.flashdash.core.utils.EntityToResponseMapper;
import com.p4r1nc3.flashdash.core.model.LeaderboardEntry;
import com.p4r1nc3.flashdash.core.model.UserSummary;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class LeaderboardService {

    private static final Logger logger = LoggerFactory.getLogger(LeaderboardService.class);

    private final EntityToResponseMapper entityToResponseMapper;
    private final UserService userService;
    private final UserRepository userRepository;


    public LeaderboardService(EntityToResponseMapper entityToResponseMapper,
                              UserService userService,
                              UserRepository userRepository) {
        this.entityToResponseMapper = entityToResponseMapper;
        this.userService = userService;
        this.userRepository = userRepository;
    }

    public List<LeaderboardEntry> getLeaderboard(String userFrn, String criteria, boolean friendsOnly, int limit) {
        logger.info("Retrieving leaderboard with criteria: {}, friendsOnly: {}, limit: {}",
                criteria, friendsOnly, limit);

        List<User> users;

        if (friendsOnly) {
            User currentUser = userService.getCurrentUser(userFrn);
            List<String> friendFrns = currentUser.getFriendsFrnList();

            friendFrns.add(userFrn);

            users = userRepository.findByUserFrnIn(friendFrns);
            logger.info("Retrieved {} friends for leaderboard", users.size());
        } else {
            users = userRepository.findAll();
            logger.info("Retrieved {} users for global leaderboard", users.size());
        }

        List<User> sortedUsers = sortUsersByCriteria(users, criteria);

        List<LeaderboardEntry> leaderboard = convertToLeaderboardEntries(sortedUsers, criteria)
                .stream()
                .limit(limit)
                .collect(Collectors.toList());

        logger.info("Returning leaderboard with {} entries", leaderboard.size());
        return leaderboard;
    }

    private List<User> sortUsersByCriteria(List<User> users, String criteria) {
        Comparator<User> comparator;

        switch (criteria.toLowerCase()) {
            case "studytime":
                comparator = Comparator.comparing(User::getStudyTime, Comparator.nullsLast(Comparator.naturalOrder())).reversed();
                break;
            case "gamesplayed":
                comparator = Comparator.comparing(User::getGamesPlayed).reversed();
                break;
            case "streak":
                comparator = Comparator.comparing(User::getStrike).reversed();
                break;
            case "points":
            default:
                comparator = Comparator.comparing(User::getPoints).reversed();
                break;
        }

        return users.stream()
                .sorted(comparator)
                .collect(Collectors.toList());
    }

    private List<LeaderboardEntry> convertToLeaderboardEntries(List<User> sortedUsers, String criteria) {
        List<LeaderboardEntry> entries = new ArrayList<>();

        for (int i = 0; i < sortedUsers.size(); i++) {
            User user = sortedUsers.get(i);

            // Create UserSummary from User
            UserSummary userSummary = new UserSummary()
                    .userId(entityToResponseMapper.extractId(user.getUserFrn()))
                    .userFrn(user.getUserFrn())
                    .username(user.getUsername())
                    .firstName(user.getFirstName())
                    .lastName(user.getLastName())
                    .email(user.getEmail());

            // Create LeaderboardEntry with UserSummary
            LeaderboardEntry entry = new LeaderboardEntry()
                    .rank(i + 1)
                    .user(userSummary);

            // Set score based on criteria
            switch (criteria.toLowerCase()) {
                case "studytime":
                    entry.score(user.getStudyTime() != null ? (int) user.getStudyTime().toMinutes() : 0);
                    break;
                case "gamesplayed":
                    entry.score(user.getGamesPlayed());
                    break;
                case "streak":
                    entry.score(user.getStrike());
                    break;
                case "points":
                default:
                    entry.score(user.getPoints());
                    break;
            }

            entries.add(entry);
        }

        return entries;
    }
}
