package com.softdreams.activityhub.repository;

import com.softdreams.activityhub.entity.ActivityLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ActivityLogRepository extends JpaRepository<ActivityLog, String> {

    boolean existsByEventId(String eventId);
}
