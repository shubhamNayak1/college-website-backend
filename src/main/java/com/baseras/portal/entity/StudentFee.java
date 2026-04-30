package com.baseras.portal.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;

@Entity
@Table(name = "student_fees",
        uniqueConstraints = @UniqueConstraint(columnNames = {"student_id", "session_id"}))
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class StudentFee {
    @Id
    private String id;

    @Column(name = "student_id", nullable = false)
    private String studentId;

    @Column(name = "session_id", nullable = false)
    private String sessionId;

    @Column(name = "total_due", nullable = false, precision = 12, scale = 2)
    private BigDecimal totalDue;

    @Column(name = "total_paid", nullable = false, precision = 12, scale = 2)
    private BigDecimal totalPaid;

    @Column(nullable = false)
    private String status;

    @Transient
    public BigDecimal getBalance() {
        return totalDue.subtract(totalPaid);
    }
}
