package com.baseras.portal.repository;

import com.baseras.portal.entity.FeePayment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FeePaymentRepository extends JpaRepository<FeePayment, String> {
    List<FeePayment> findByStudentFeeIdAndDeletedAtIsNullOrderByPaidOnDesc(String studentFeeId);
}
