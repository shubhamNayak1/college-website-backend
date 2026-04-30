package com.baseras.portal.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "teaching_assignments",
        uniqueConstraints = @UniqueConstraint(columnNames = {"teacher_id", "class_id", "subject_id", "session_id"}))
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class TeachingAssignment {
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
}
