package com.baseras.portal.repository;

import com.baseras.portal.entity.Mark;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface MarkRepository extends JpaRepository<Mark, String> {

    Optional<Mark> findByStudentIdAndSubjectIdAndExamTypeIdAndSessionId(
            String studentId, String subjectId, String examTypeId, String sessionId);

    List<Mark> findByStudentIdInAndSubjectIdAndExamTypeIdAndSessionId(
            List<String> studentIds, String subjectId, String examTypeId, String sessionId);

    List<Mark> findByStudentIdInAndExamTypeIdAndSessionId(
            List<String> studentIds, String examTypeId, String sessionId);

    List<Mark> findByStudentIdAndStatus(String studentId, String status);

    List<Mark> findByStudentIdInAndSessionId(List<String> studentIds, String sessionId);

    List<Mark> findByEnteredByAndStatus(String enteredBy, String status);
}
