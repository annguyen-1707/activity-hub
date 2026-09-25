package com.softdreams.activityhub.infrastructure.inbox;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface InboxEventRepository
        extends JpaRepository<InboxEvent, Long> {

    boolean existsByEventId(String eventId);

    Optional<InboxEvent> findByEventId(String eventId);
}
