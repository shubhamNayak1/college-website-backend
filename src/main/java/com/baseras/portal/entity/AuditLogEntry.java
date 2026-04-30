package com.baseras.portal.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.OffsetDateTime;

@Entity
@Table(name = "audit_log")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class AuditLogEntry {
    @Id
    private String id;

    @Column(name = "actor_user_id", nullable = false)
    private String actorUserId;

    @Column(name = "actor_name", nullable = false)
    private String actorName;

    @Column(nullable = false)
    private String action;

    @Column(name = "entity_type", nullable = false)
    private String entityType;

    @Column(name = "entity_id", nullable = false)
    private String entityId;

    @Column(columnDefinition = "text")
    private String metadata;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;
}
