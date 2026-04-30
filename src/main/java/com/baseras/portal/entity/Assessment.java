package com.baseras.portal.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import java.time.OffsetDateTime;

@Entity
@Table(name = "assessments")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Assessment {
    @Id
    private String id;

    @Column(name = "teacher_id", nullable = false)
    private String teacherId;

    @Column(name = "class_id", nullable = false)
    private String classId;

    @Column(name = "subject_id", nullable = false)
    private String subjectId;

    @Column(name = "session_id", nullable = false)
    private String sessionId;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "text")
    private String description;

    @Column(name = "attachment_url")
    private String attachmentUrl;

    @Column(name = "due_date", nullable = false)
    private LocalDate dueDate;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    @Column(name = "deleted_at")
    private OffsetDateTime deletedAt;
}
