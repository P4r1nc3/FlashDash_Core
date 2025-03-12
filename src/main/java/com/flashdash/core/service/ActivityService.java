package com.flashdash.core.service;

import com.flashdash.core.config.JwtManager;
import com.flashdash.core.model.User;
import com.flashdash.core.repository.UserRepository;
import com.p4r1nc3.flashdash.activity.ApiClient;
import com.p4r1nc3.flashdash.activity.ApiException;
import com.p4r1nc3.flashdash.activity.api.ActivitiesApi;
import com.p4r1nc3.flashdash.activity.model.ActivityResponse;
import com.p4r1nc3.flashdash.activity.model.LogActivityRequest;
import com.flashdash.core.exception.FlashDashException;
import com.flashdash.core.exception.ErrorCode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ActivityService {

    private static final Logger logger = LoggerFactory.getLogger(ActivityService.class);

    private final JwtManager jwtManager;
    private final UserRepository userRepository;

    public ActivityService(JwtManager jwtManager, UserRepository userRepository) {
        this.jwtManager = jwtManager;
        this.userRepository = userRepository;
    }

    private ActivitiesApi createActivitiesApi(String userFrn) {
        Optional<User> userOptional = userRepository.findByUserFrn(userFrn);

        if (userOptional.isEmpty()) {
            throw new FlashDashException(ErrorCode.E401001, "Invalid user reference.");
        }

        User user = userOptional.get();

        ApiClient apiClient = new ApiClient();
        String token = jwtManager.generateToken(userFrn, user.getEmail());
        apiClient.setBearerToken(token);
        return new ActivitiesApi(apiClient);
    }

    public void logUserActivity(String userFrn, String targetFrn, LogActivityRequest.ActivityTypeEnum activityType) {
        LogActivityRequest logActivityRequest = new LogActivityRequest();
        logActivityRequest.setTargetFrn(targetFrn);
        logActivityRequest.setActivityType(activityType);

        ActivitiesApi activitiesApi = createActivitiesApi(userFrn);

        try {
            activitiesApi.logActivity(logActivityRequest);
        } catch (ApiException e) {
            logger.error("Error logging activity for user {}: {}", userFrn, e.getMessage());
            throw new FlashDashException(ErrorCode.E500001, "An error occurred while logging the activity. Please try again later.");
        }
    }

    public List<ActivityResponse> getUserActivities(String userFrn) {
        ActivitiesApi activitiesApi = createActivitiesApi(userFrn);

        try {
            return activitiesApi.getUserActivities();
        } catch (ApiException e) {
            logger.error("Error retrieving activities for user {}: {}", userFrn, e.getMessage());
            throw new FlashDashException(ErrorCode.E500001, "An error occurred while retrieving the user's activities. Please try again later.");
        }
    }
}
