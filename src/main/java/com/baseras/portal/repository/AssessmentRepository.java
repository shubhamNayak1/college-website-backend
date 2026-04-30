package com.baseras.portal.repository;

import com.baseras.portal.entity.Assessment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface AssessmentRepository extends JpaRepository<Assessment, String> {
    List<Assessment> findByDeletedAtIsNullOrderByCreatedAtDesc();
    List<Assessment> findByClassIdAndDeletedAtIsNullOrderByCreatedAtDesc(String classId);
    List<Assessment> findByTeacherIdAndDeletedAtIsNullOrderByCreatedAtDesc(String teacherId);
    List<Assessment> findByClassIdAndDueDateGreaterThanEqualAndDeletedAtIsNullOrderByDueDateAsc(String classId, LocalDate from);
    long countByTeacherIdAndDeletedAtIsNull(String teacherId);
}
