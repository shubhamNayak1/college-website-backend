package com.baseras.portal.repository;

import com.baseras.portal.entity.StudentFee;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface StudentFeeRepository extends JpaRepository<StudentFee, String> {
    Optional<StudentFee> findByStudentIdAndSessionId(String studentId, String sessionId);
}
