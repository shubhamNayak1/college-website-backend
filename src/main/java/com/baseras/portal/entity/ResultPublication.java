package com.baseras.portal.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.OffsetDateTime;

@Entity
@Table(name = "result_publications",
        uniqueConstraints = @UniqueConstraint(columnNames = {"class_id", "exam_type_id", "session_id"}))
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ResultPublication {
    @Id
    private String id;

    @Column(name = "class_id", nullable = false)
    private String classId;

    @Column(name = "exam_type_id", nullable = false)
    private String examTypeId;

    @Column(name = "session_id", nullable = false)
    private String sessionId;

    @Column(nullable = false)
    private String status;

    @Column(name = "published_by")
    private String publishedBy;

    @Column(name = "published_at")
    private OffsetDateTime publishedAt;

    @Column(name = "total_subjects", nullable = false)
    private int totalSubjects;

    @Column(name = "submitted_subjects", nullable = false)
    private int submittedSubjects;
}
