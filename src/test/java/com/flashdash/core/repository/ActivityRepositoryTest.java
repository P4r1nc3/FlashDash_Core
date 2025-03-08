package com.flashdash.core.repository;

import com.flashdash.core.FlashDashCoreApplication;
import com.flashdash.core.TestUtils;
import com.flashdash.core.model.Activity;
import com.flashdash.core.model.ActivityType;
import com.flashdash.core.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@Transactional
@SpringBootTest(classes = FlashDashCoreApplication.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ActivityRepositoryTest {

    @Autowired
    private ActivityRepository activityRepository;

    @Autowired
    private UserRepository userRepository;

    @BeforeEach
    void setUp() {
        activityRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    void shouldFindAllActivitiesByUserFrn() {
        // Arrange
        User user = TestUtils.createUser();
        userRepository.save(user);

        Activity activity1 = TestUtils.createActivity(user.getUserFrn(), user.getUserFrn(), ActivityType.ACCOUNT_LOGIN);
        activityRepository.save(activity1);

        Activity activity2 = TestUtils.createActivity(user.getUserFrn(), "frn:deck:123", ActivityType.GAME_STARTED);
        activityRepository.save(activity2);

        // Act
        List<Activity> activities = activityRepository.findByUserFrn(user.getUserFrn());

        // Assert
        assertThat(activities).hasSize(2);
        assertThat(activities).extracting(Activity::getActivity)
                .containsExactlyInAnyOrder(ActivityType.ACCOUNT_LOGIN, ActivityType.GAME_STARTED);
    }

    @Test
    void shouldReturnEmptyListWhenUserHasNoActivities() {
        // Arrange
        User user = TestUtils.createUser();
        userRepository.save(user);

        // Act
        List<Activity> activities = activityRepository.findByUserFrn(user.getUserFrn());

        // Assert
        assertThat(activities).isEmpty();
    }

    @Test
    void shouldFindAllActivitiesByType() {
        // Arrange
        User user1 = TestUtils.createUser();
        userRepository.save(user1);

        User user2 = TestUtils.createUser();
        userRepository.save(user2);

        Activity activity1 = TestUtils.createActivity(user1.getUserFrn(), user1.getUserFrn(), ActivityType.ACCOUNT_LOGIN);
        activityRepository.save(activity1);

        Activity activity2 = TestUtils.createActivity(user2.getUserFrn(), user2.getUserFrn(), ActivityType.ACCOUNT_LOGIN);
        activityRepository.save(activity2);

        Activity activity3 = TestUtils.createActivity(user2.getUserFrn(), "frn:deck:456", ActivityType.ACCOUNT_REGISTRATION);
        activityRepository.save(activity3);

        // Act
        List<Activity> loginActivities = activityRepository.findByActivity(ActivityType.ACCOUNT_LOGIN);

        // Assert
        assertThat(loginActivities).hasSize(2);
        assertThat(loginActivities).extracting(Activity::getUserFrn)
                .containsExactlyInAnyOrder(user1.getUserFrn(), user2.getUserFrn());
    }

    @Test
    void shouldReturnEmptyListWhenNoActivitiesOfGivenType() {
        // Act
        List<Activity> activities = activityRepository.findByActivity(ActivityType.ACCOUNT_CONFIRMATION);

        // Assert
        assertThat(activities).isEmpty();
    }
}
