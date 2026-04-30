package com.baseras.portal.repository;

import com.baseras.portal.entity.MarksAuditEntry;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MarksAuditRepository extends JpaRepository<MarksAuditEntry, String> {}
