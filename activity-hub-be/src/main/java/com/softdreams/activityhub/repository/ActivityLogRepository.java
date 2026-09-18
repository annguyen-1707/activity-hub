package com.softdreams.activityhub.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.softdreams.activityhub.entity.ActivityLog;
import com.softdreams.activityhub.enums.EventType;
import com.softdreams.activityhub.enums.TargetType;

@Repository
public interface ActivityLogRepository extends JpaRepository<ActivityLog, String> {

    boolean existsByEventId(String eventId);

    @Query("""
        SELECT al FROM ActivityLog al LEFT JOIN al.user u
        WHERE (:keyword IS NULL OR :keyword = ''
            OR LOWER(u.username) LIKE LOWER(CONCAT('%', :keyword, '%'))
            OR LOWER(u.firstName) LIKE LOWER(CONCAT('%', :keyword, '%'))
            OR LOWER(u.lastName) LIKE LOWER(CONCAT('%', :keyword, '%'))
            OR LOWER(al.eventId) LIKE LOWER(CONCAT('%', :keyword, '%'))
            OR LOWER(al.targetId) LIKE LOWER(CONCAT('%', :keyword, '%')))
          AND (:eventType IS NULL OR al.eventType = :eventType)
          AND (:targetType IS NULL OR al.targetType = :targetType)
        """)
    Page<ActivityLog> search(
            @Param("keyword") String keyword,
            @Param("eventType") EventType eventType,
            @Param("targetType") TargetType targetType,
            Pageable pageable);
}
