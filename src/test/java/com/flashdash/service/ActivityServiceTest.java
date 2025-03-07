package com.flashdash.service;

import com.flashdash.FlashDashApplication;
import com.flashdash.TestUtils;
import com.flashdash.model.Activity;
import com.flashdash.model.ActivityType;
import com.flashdash.repository.ActivityRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@SpringBootTest(classes = FlashDashApplication.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ActivityServiceTest {

    @Autowired
    private ActivityService activityService;

    @MockitoBean
    private ActivityRepository activityRepository;

    private String userFrn;
    private String targetFrn;

    @BeforeEach
    void setUp() {
        userFrn = "frn:user:123";
        targetFrn = "frn:deck:456";
    }

    @Test
    void shouldLogActivitySuccessfully() {
        // Act
        activityService.logActivity(userFrn, targetFrn, ActivityType.GAME_STARTED);

        // Assert
        ArgumentCaptor<Activity> captor = ArgumentCaptor.forClass(Activity.class);
        verify(activityRepository).save(captor.capture());

        Activity savedActivity = captor.getValue();
        assertThat(savedActivity).isNotNull();
        assertThat(savedActivity.getUserFrn()).isEqualTo(userFrn);
        assertThat(savedActivity.getTargetFrn()).isEqualTo(targetFrn);
        assertThat(savedActivity.getActivity()).isEqualTo(ActivityType.GAME_STARTED);
        assertThat(savedActivity.getCreatedAt()).isNotNull();
        assertThat(savedActivity.getUpdatedAt()).isNotNull();
    }

    @Test
    void shouldGetUserActivitiesSuccessfully() {
        // Arrange
        List<Activity> mockActivities = List.of(
                TestUtils.createActivity(userFrn, targetFrn, ActivityType.GAME_STARTED),
                TestUtils.createActivity(userFrn, "frn:question:789", ActivityType.QUESTION_CREATED)
        );

        when(activityRepository.findByUserFrn(userFrn)).thenReturn(mockActivities);

        // Act
        List<Activity> activities = activityService.getUserActivities(userFrn);

        // Assert
        assertThat(activities).hasSize(2);
        assertThat(activities.get(0).getActivity()).isEqualTo(ActivityType.GAME_STARTED);
        assertThat(activities.get(1).getActivity()).isEqualTo(ActivityType.QUESTION_CREATED);
        verify(activityRepository, times(1)).findByUserFrn(userFrn);
    }

    @Test
    void shouldReturnEmptyListWhenNoUserActivitiesFound() {
        // Arrange
        when(activityRepository.findByUserFrn(userFrn)).thenReturn(List.of());

        // Act
        List<Activity> activities = activityService.getUserActivities(userFrn);

        // Assert
        assertThat(activities).isEmpty();
        verify(activityRepository, times(1)).findByUserFrn(userFrn);
    }

    @Test
    void shouldGetActivitiesByTypeSuccessfully() {
        // Arrange
        List<Activity> mockActivities = List.of(
                TestUtils.createActivity(userFrn, targetFrn, ActivityType.LOGIN),
                TestUtils.createActivity("frn:user:456", "frn:user:456", ActivityType.LOGIN)
        );

        when(activityRepository.findByActivity(ActivityType.LOGIN)).thenReturn(mockActivities);

        // Act
        List<Activity> activities = activityService.getActivitiesByType(ActivityType.LOGIN);

        // Assert
        assertThat(activities).hasSize(2);
        assertThat(activities).allMatch(activity -> activity.getActivity() == ActivityType.LOGIN);
        verify(activityRepository, times(1)).findByActivity(ActivityType.LOGIN);
    }

    @Test
    void shouldReturnEmptyListWhenNoActivitiesOfTypeFound() {
        // Arrange
        when(activityRepository.findByActivity(ActivityType.ACCOUNT_CONFIRMATION)).thenReturn(List.of());

        // Act
        List<Activity> activities = activityService.getActivitiesByType(ActivityType.ACCOUNT_CONFIRMATION);

        // Assert
        assertThat(activities).isEmpty();
        verify(activityRepository, times(1)).findByActivity(ActivityType.ACCOUNT_CONFIRMATION);
    }
}
