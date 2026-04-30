package com.baseras.portal.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;

@Entity
@Table(name = "academic_sessions")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class AcademicSession {
    @Id
    private String id;

    @Column(nullable = false, unique = true)
    private String label;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;

    @Column(name = "is_active", nullable = false)
    private boolean active;
}
