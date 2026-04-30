package com.baseras.portal.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;

@Entity
@Table(name = "fee_structures",
        uniqueConstraints = @UniqueConstraint(columnNames = {"class_id", "session_id", "fee_head_id"}))
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class FeeStructure {
    @Id
    private String id;

    @Column(name = "class_id", nullable = false)
    private String classId;

    @Column(name = "session_id", nullable = false)
    private String sessionId;

    @Column(name = "fee_head_id", nullable = false)
    private String feeHeadId;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal amount;
}
