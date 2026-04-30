package com.baseras.portal.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;

@Entity
@Table(name = "fee_payments")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class FeePayment {
    @Id
    private String id;

    @Column(name = "student_fee_id", nullable = false)
    private String studentFeeId;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal amount;

    @Column(name = "paid_on", nullable = false)
    private LocalDate paidOn;

    @Column(name = "payment_mode", nullable = false)
    private String paymentMode;

    @Column(name = "receipt_no", nullable = false)
    private String receiptNo;

    @Column(name = "marked_by", nullable = false)
    private String markedBy;

    @Column(name = "marked_by_name", nullable = false)
    private String markedByName;

    @Column(columnDefinition = "text")
    private String remarks;

    @Column(name = "deleted_at")
    private OffsetDateTime deletedAt;
}
