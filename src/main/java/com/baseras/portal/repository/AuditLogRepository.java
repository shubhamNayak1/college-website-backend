package com.baseras.portal.repository;

import com.baseras.portal.entity.AuditLogEntry;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AuditLogRepository extends JpaRepository<AuditLogEntry, String> {
    List<AuditLogEntry> findTop500ByOrderByCreatedAtDesc();
}
