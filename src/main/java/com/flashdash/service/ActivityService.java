package com.flashdash.service;

import com.flashdash.model.Activity;
import com.flashdash.model.ActivityType;
import com.flashdash.repository.ActivityRepository;
import com.flashdash.utils.FrnGenerator;
import com.flashdash.utils.ResourceType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ActivityService {

    private final ActivityRepository activityRepository;

    public ActivityService(ActivityRepository activityRepository) {
        this.activityRepository = activityRepository;
    }

    @Transactional
    public void logActivity(String userFrn, String targetFrn, ActivityType activityType) {
        Activity activity = new Activity();
        activity.setActivityFrn(FrnGenerator.generateFrn(ResourceType.ACTIVITY));
        activity.setUserFrn(userFrn);
        activity.setTargetFrn(targetFrn);
        activity.setActivity(activityType);
        activity.setCreatedAt(LocalDateTime.now());
        activity.setUpdatedAt(LocalDateTime.now());

        activityRepository.save(activity);
    }

    public List<Activity> getUserActivities(String userFrn) {
        return activityRepository.findByUserFrn(userFrn);
    }

    public List<Activity> getActivitiesByType(ActivityType activityType) {
        return activityRepository.findByActivity(activityType);
    }
}
