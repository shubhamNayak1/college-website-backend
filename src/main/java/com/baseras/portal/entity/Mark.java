package com.baseras.portal.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.OffsetDateTime;

@Entity
@Table(name = "marks",
        uniqueConstraints = @UniqueConstraint(columnNames = {"student_id", "subject_id", "exam_type_id", "session_id"}))
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Mark {
    @Id
    private String id;

    @Column(name = "student_id", nullable = false)
    private String studentId;

    @Column(name = "subject_id", nullable = false)
    private String subjectId;

    @Column(name = "exam_type_id", nullable = false)
    private String examTypeId;

    @Column(name = "session_id", nullable = false)
    private String sessionId;

    @Column(name = "marks_obtained")
    private Double marksObtained;

    @Column(name = "max_marks", nullable = false)
    private double maxMarks;

    @Column(nullable = false)
    private String status;

    @Column(name = "entered_by")
    private String enteredBy;

    @Column(name = "submitted_at")
    private OffsetDateTime submittedAt;

    @Column(name = "published_at")
    private OffsetDateTime publishedAt;

    @Column(nullable = false)
    private int version;
}
