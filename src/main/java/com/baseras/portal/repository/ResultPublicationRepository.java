package com.baseras.portal.repository;

import com.baseras.portal.entity.ResultPublication;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ResultPublicationRepository extends JpaRepository<ResultPublication, String> {
    Optional<ResultPublication> findByClassIdAndExamTypeIdAndSessionId(String classId, String examTypeId, String sessionId);
    long countByStatus(String status);
}
