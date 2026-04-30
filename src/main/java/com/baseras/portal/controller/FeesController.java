package com.baseras.portal.controller;

import com.baseras.portal.common.AppExceptions;
import com.baseras.portal.common.CurrentUser;
import com.baseras.portal.common.IdGen;
import com.baseras.portal.dto.FeesDtos.*;
import com.baseras.portal.entity.*;
import com.baseras.portal.repository.*;
import com.baseras.portal.service.AuditService;
import com.baseras.portal.service.NotificationService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1")
public class FeesController {

    private final FeeStructureRepository structures;
    private final FeeHeadRepository heads;
    private final StudentFeeRepository studentFees;
    private final FeePaymentRepository payments;
    private final StudentRepository students;
    private final SchoolClassRepository classes;
    private final CurrentUser current;
    private final AuditService audit;
    private final NotificationService notify;

    public FeesController(FeeStructureRepository structures, FeeHeadRepository heads,
                          StudentFeeRepository studentFees, FeePaymentRepository payments,
                          StudentRepository students, SchoolClassRepository classes,
                          CurrentUser current, AuditService audit, NotificationService notify) {
        this.structures = structures; this.heads = heads;
        this.studentFees = studentFees; this.payments = payments;
        this.students = students; this.classes = classes;
        this.current = current; this.audit = audit; this.notify = notify;
    }

    @GetMapping("/fees/structure")
    public List<FeeStructure> structure(@RequestParam(required = false) String classId,
                                        @RequestParam(defaultValue = "sess_2025_26") String sessionId) {
        if (classId != null) return structures.findByClassIdAndSessionId(classId, sessionId);
        return structures.findAll();
    }

    @GetMapping("/fees/heads")
    public List<FeeHead> heads() { return heads.findAll(); }

    @GetMapping("/students/{id}/fees")
    public StudentFeesResponse forStudent(@PathVariable String id) {
        Student s = students.findById(id).orElseThrow(() -> new AppExceptions.NotFoundException("Student not found"));
        StudentFee fee = studentFees.findByStudentIdAndSessionId(s.getId(), "sess_2025_26").orElse(null);
        List<FeePayment> ps = fee == null ? List.of()
                : payments.findByStudentFeeIdAndDeletedAtIsNullOrderByPaidOnDesc(fee.getId());
        return new StudentFeesResponse(s, fee, ps);
    }

    @PostMapping("/students/{id}/fees/payments")
    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    public ResponseEntity<FeePayment> recordPayment(@PathVariable String id, @RequestBody RecordPaymentRequest req) {
        User u = current.require();
        Student s = students.findById(id).orElseThrow(() -> new AppExceptions.NotFoundException("Student not found"));
        StudentFee fee = studentFees.findByStudentIdAndSessionId(s.getId(), "sess_2025_26")
                .orElseThrow(() -> new AppExceptions.ValidationException("Fee record not found for student"));
        if (req.amount() == null || req.amount().signum() <= 0) {
            throw new AppExceptions.ValidationException("Amount must be positive");
        }
        FeePayment p = FeePayment.builder().id(IdGen.of("fp"))
                .studentFeeId(fee.getId()).amount(req.amount())
                .paidOn(req.paidOn() == null ? LocalDate.now() : req.paidOn())
                .paymentMode(req.paymentMode() == null ? "CASH" : req.paymentMode())
                .receiptNo(req.receiptNo() == null ? "RCP" + System.currentTimeMillis() : req.receiptNo())
                .markedBy(u.getId()).markedByName(u.getFullName())
                .remarks(req.remarks())
                .build();
        payments.save(p);
        fee.setTotalPaid(fee.getTotalPaid().add(req.amount()));
        BigDecimal balance = fee.getTotalDue().subtract(fee.getTotalPaid());
        fee.setStatus(balance.signum() <= 0 ? "PAID" : "PARTIAL");
        studentFees.save(fee);
        audit.log(u.getId(), "RECORD_PAYMENT", "FeePayment", p.getId(),
                Map.of("studentRoll", s.getRollNumber(), "amount", req.amount()));
        notify.notify(s.getUserId(), "FEE_PAYMENT", "Payment recorded",
                "Payment of ₹" + req.amount() + " recorded.", null);
        return ResponseEntity.status(201).body(p);
    }

    @GetMapping("/admin/fees/summary")
    public List<FeesSummaryRow> summary(@RequestParam(required = false) String classId,
                                        @RequestParam(defaultValue = "sess_2025_26") String sessionId) {
        var studentList = classId == null ? students.findAll() : students.findByClassId(classId);
        return studentList.stream().map(s -> {
            SchoolClass cls = classes.findById(s.getClassId()).orElse(null);
            StudentFee fee = studentFees.findByStudentIdAndSessionId(s.getId(), sessionId).orElse(null);
            return new FeesSummaryRow(s, cls, fee);
        }).toList();
    }
}
