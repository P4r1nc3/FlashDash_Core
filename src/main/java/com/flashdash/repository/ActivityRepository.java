package com.flashdash.repository;

import com.flashdash.model.Activity;
import com.flashdash.model.ActivityType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ActivityRepository extends JpaRepository<Activity, String> {
    List<Activity> findByUserFrn(String userFrn);
    List<Activity> findByActivity(ActivityType activity);
}
