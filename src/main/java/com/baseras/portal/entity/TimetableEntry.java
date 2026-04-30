package com.baseras.portal.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalTime;

@Entity
@Table(name = "timetable",
        uniqueConstraints = @UniqueConstraint(columnNames = {"class_id", "session_id", "day_of_week", "period"}))
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class TimetableEntry {
    @Id
    private String id;

    @Column(name = "class_id", nullable = false)
    private String classId;

    @Column(name = "session_id", nullable = false)
    private String sessionId;

    @Column(name = "day_of_week", nullable = false)
    private int dayOfWeek;

    @Column(nullable = false)
    private int period;

    @Column(name = "start_time", nullable = false)
    private LocalTime startTime;

    @Column(name = "end_time", nullable = false)
    private LocalTime endTime;

    @Column(name = "subject_id", nullable = false)
    private String subjectId;

    @Column(name = "teacher_id", nullable = false)
    private String teacherId;
}
