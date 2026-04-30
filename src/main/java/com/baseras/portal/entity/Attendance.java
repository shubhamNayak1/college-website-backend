package com.baseras.portal.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;

@Entity
@Table(name = "attendance",
        uniqueConstraints = @UniqueConstraint(columnNames = {"student_id", "attendance_date"}))
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Attendance {
    @Id
    private String id;

    @Column(name = "student_id", nullable = false)
    private String studentId;

    @Column(name = "attendance_date", nullable = false)
    private LocalDate date;

    @Column(nullable = false)
    private String status;

    @Column(name = "marked_by", nullable = false)
    private String markedBy;

    @Column(name = "session_id", nullable = false)
    private String sessionId;

    @Column(columnDefinition = "text")
    private String remarks;
}
