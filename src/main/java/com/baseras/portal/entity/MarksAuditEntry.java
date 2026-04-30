package com.baseras.portal.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.OffsetDateTime;

@Entity
@Table(name = "marks_audit")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class MarksAuditEntry {
    @Id
    private String id;

    @Column(name = "marks_id", nullable = false)
    private String marksId;

    @Column(nullable = false)
    private String action;

    @Column(name = "old_marks")
    private Double oldMarks;

    @Column(name = "new_marks")
    private Double newMarks;

    @Column(name = "old_status")
    private String oldStatus;

    @Column(name = "new_status")
    private String newStatus;

    @Column(name = "performed_by", nullable = false)
    private String performedBy;

    @Column(name = "performed_by_name", nullable = false)
    private String performedByName;

    @Column(name = "performed_at", nullable = false)
    private OffsetDateTime performedAt;
}
