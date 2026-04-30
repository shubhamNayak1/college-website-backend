package com.baseras.portal.repository;

import com.baseras.portal.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

public interface NotificationRepository extends JpaRepository<Notification, String> {
    List<Notification> findByUserIdOrderByCreatedAtDesc(String userId);
    Optional<Notification> findByIdAndUserId(String id, String userId);

    @Modifying
    @Query("UPDATE Notification n SET n.readAt = :now WHERE n.userId = :userId AND n.readAt IS NULL")
    int markAllRead(String userId, OffsetDateTime now);
}
