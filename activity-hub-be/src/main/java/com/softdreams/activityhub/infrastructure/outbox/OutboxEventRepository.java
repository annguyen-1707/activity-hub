package com.softdreams.activityhub.infrastructure.outbox;

import com.softdreams.activityhub.enums.OutboxStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

@Repository
public interface OutboxEventRepository
        extends JpaRepository<OutboxEvent, Long> {

    List<OutboxEvent> findTop100ByStatusInOrderByCreatedAtAsc(
            Collection<OutboxStatus> statuses
    );
}
