package com.baseras.portal.dto;

import com.baseras.portal.entity.FeePayment;
import com.baseras.portal.entity.SchoolClass;
import com.baseras.portal.entity.Student;
import com.baseras.portal.entity.StudentFee;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public class FeesDtos {

    public record RecordPaymentRequest(
            BigDecimal amount, LocalDate paidOn, String paymentMode,
            String receiptNo, String remarks
    ) {}

    public record StudentFeesResponse(Student student, StudentFee fee, List<FeePayment> payments) {}

    public record FeesSummaryRow(Student student, SchoolClass clazz, StudentFee fee) {}
}
