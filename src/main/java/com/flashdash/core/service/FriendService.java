package com.flashdash.core.service;

import com.flashdash.core.exception.ErrorCode;
import com.flashdash.core.exception.FlashDashException;
import com.flashdash.core.model.User;
import com.flashdash.core.repository.UserRepository;
import com.flashdash.core.service.api.ActivityService;
import com.p4r1nc3.flashdash.activity.model.LogActivityRequest.ActivityTypeEnum;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class FriendService {

    private final ActivityService activityService;
    private final UserRepository userRepository;

    public FriendService(ActivityService activityService,
                         UserRepository userRepository) {
        this.activityService = activityService;
        this.userRepository = userRepository;
    }

    public List<User> getFriends(String userFrn) {
        User user = userRepository.findByUserFrn(userFrn)
                .orElseThrow(() -> new FlashDashException(ErrorCode.E404002, "User not found"));

        List<String> friendsFrnList = user.getFriendsFrnList();

        if (friendsFrnList.isEmpty()) {
            return Collections.emptyList();
        }

        return friendsFrnList.stream()
                .map(friendFrn -> getFriend(userFrn, friendFrn))
                .collect(Collectors.toList());
    }

    public User getFriend(String userFrn, String friendFrn) {
        User user = userRepository.findByUserFrn(userFrn)
                .orElseThrow(() -> new FlashDashException(ErrorCode.E404002, "User not found"));

        List<String> friendsFrnList = user.getFriendsFrnList();

        if (!friendsFrnList.contains(friendFrn)) {
            throw new FlashDashException(ErrorCode.E404003, "Friend not found in user's friend list.");
        }

        return userRepository.findByUserFrn(friendFrn)
                .orElseThrow(() -> new FlashDashException(ErrorCode.E404002, "Friend not found"));
    }

    @Transactional
    public void deleteFriend(String userFrn, String friendFrn) {
        User user = userRepository.findById(userFrn)
                .orElseThrow(() -> new FlashDashException(ErrorCode.E404002, "User not found"));
        User friend = userRepository.findById(friendFrn)
                .orElseThrow(() -> new FlashDashException(ErrorCode.E404002, "Friend not found"));

        if (!user.getFriendsFrnList().contains(friendFrn)) {
            throw new FlashDashException(ErrorCode.E404005, "This user is not your friend.");
        }

        removeFriendship(user, friend);

        activityService.logUserActivity(userFrn, friendFrn, ActivityTypeEnum.FRIEND_DELETED);
    }

    @Transactional
    public void removeAllFriends(String userFrn) {
        User user = userRepository.findById(userFrn)
                .orElseThrow(() -> new FlashDashException(ErrorCode.E404002, "User not found"));

        List<String> friendsList = user.getFriendsFrnList();

        for (String friendFrn : friendsList) {
            deleteFriend(userFrn, friendFrn);
        }

        userRepository.save(user);
    }

    private void removeFriendship(User user, User friend) {
        List<String> userFriends = user.getFriendsFrnList();
        List<String> friendFriends = friend.getFriendsFrnList();

        userFriends.remove(friend.getUserFrn());
        friendFriends.remove(user.getUserFrn());

        user.setFriendsFrnList(userFriends);
        friend.setFriendsFrnList(friendFriends);

        userRepository.save(user);
        userRepository.save(friend);
    }
}
