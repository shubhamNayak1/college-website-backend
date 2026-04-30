package com.baseras.portal.repository;

import com.baseras.portal.entity.AcademicSession;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AcademicSessionRepository extends JpaRepository<AcademicSession, String> {
    Optional<AcademicSession> findByActiveTrue();
}
