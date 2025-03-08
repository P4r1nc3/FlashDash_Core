package com.flashdash.core.service;

import com.flashdash.core.FlashDashCoreApplication;
import com.flashdash.core.config.JwtManager;
import com.p4r1nc3.flashdash.activity.ApiException;
import com.p4r1nc3.flashdash.activity.api.ActivitiesApi;
import com.p4r1nc3.flashdash.activity.model.ActivityResponse;
import com.p4r1nc3.flashdash.activity.model.LogActivityRequest;
import com.flashdash.core.exception.FlashDashException;
import com.flashdash.core.exception.ErrorCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@SpringBootTest(classes = FlashDashCoreApplication.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ActivityServiceTest {

    @Autowired
    private ActivityService activityService;

    @MockitoBean
    private JwtManager jwtManager;

    @MockitoBean
    private ActivitiesApi activitiesApi;

    private String userFrn;
    private String targetFrn;
    private String mockToken;

    @BeforeEach
    void setUp() {
        userFrn = "frn:user:123";
        targetFrn = "frn:deck:456";
        mockToken = "mock-jwt-token";

        when(jwtManager.generateToken(userFrn)).thenReturn(mockToken);
    }

    @Test
    void shouldLogActivitySuccessfully() throws ApiException {
        // Arrange
        LogActivityRequest logActivityRequest = new LogActivityRequest();
        logActivityRequest.setTargetFrn(targetFrn);
        logActivityRequest.setActivityType(LogActivityRequest.ActivityTypeEnum.GAME_STARTED);

        doNothing().when(activitiesApi).logActivity(any());

        // Act
        activityService.logUserActivity(userFrn, targetFrn, LogActivityRequest.ActivityTypeEnum.GAME_STARTED);

        // Assert
        ArgumentCaptor<LogActivityRequest> captor = ArgumentCaptor.forClass(LogActivityRequest.class);
        verify(activitiesApi, times(1)).logActivity(captor.capture());

        LogActivityRequest capturedRequest = captor.getValue();
        assertThat(capturedRequest.getTargetFrn()).isEqualTo(targetFrn);
        assertThat(capturedRequest.getActivityType()).isEqualTo(LogActivityRequest.ActivityTypeEnum.GAME_STARTED);
    }

    @Test
    void shouldThrowExceptionWhenLoggingActivityFails() throws ApiException {
        // Arrange
        doThrow(new ApiException("API failure")).when(activitiesApi).logActivity(any());

        // Act & Assert
        assertThatThrownBy(() -> activityService.logUserActivity(userFrn, targetFrn, LogActivityRequest.ActivityTypeEnum.GAME_STARTED))
                .isInstanceOf(FlashDashException.class)
                .hasMessageContaining("An error occurred while logging the activity")
                .extracting("errorCode").isEqualTo(ErrorCode.E500001);

        verify(activitiesApi, times(1)).logActivity(any());
    }

    @Test
    void shouldGetUserActivitiesSuccessfully() throws ApiException {
        // Arrange
        List<ActivityResponse> mockActivities = List.of(
                new ActivityResponse().userFrn(userFrn).targetFrn(targetFrn).activityType("GAME_STARTED"),
                new ActivityResponse().userFrn(userFrn).targetFrn("frn:question:789").activityType("QUESTION_CREATED")
        );

        when(activitiesApi.getUserActivities()).thenReturn(mockActivities);

        // Act
        List<ActivityResponse> activities = activityService.getUserActivities(userFrn);

        // Assert
        assertThat(activities).hasSize(2);
        assertThat(activities.get(0).getActivityType()).isEqualTo("GAME_STARTED");
        assertThat(activities.get(1).getActivityType()).isEqualTo("QUESTION_CREATED");

        verify(activitiesApi, times(1)).getUserActivities();
    }

    @Test
    void shouldReturnEmptyListWhenNoUserActivitiesFound() throws ApiException {
        // Arrange
        when(activitiesApi.getUserActivities()).thenReturn(List.of());

        // Act
        List<ActivityResponse> activities = activityService.getUserActivities(userFrn);

        // Assert
        assertThat(activities).isEmpty();
        verify(activitiesApi, times(1)).getUserActivities();
    }

    @Test
    void shouldThrowExceptionWhenGettingUserActivitiesFails() throws ApiException {
        // Arrange
        doThrow(new ApiException("API failure")).when(activitiesApi).getUserActivities();

        // Act & Assert
        assertThatThrownBy(() -> activityService.getUserActivities(userFrn))
                .isInstanceOf(FlashDashException.class)
                .hasMessageContaining("An error occurred while retrieving the user's activities")
                .extracting("errorCode").isEqualTo(ErrorCode.E500001);

        verify(activitiesApi, times(1)).getUserActivities();
    }
}
