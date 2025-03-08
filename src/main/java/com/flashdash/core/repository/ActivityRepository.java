package com.flashdash.core.repository;

import com.flashdash.core.model.Activity;
import com.flashdash.core.model.ActivityType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ActivityRepository extends JpaRepository<Activity, String> {
    List<Activity> findByUserFrn(String userFrn);
    List<Activity> findByActivity(ActivityType activity);
}
