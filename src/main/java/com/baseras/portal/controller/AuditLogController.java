package com.baseras.portal.controller;

import com.baseras.portal.entity.AuditLogEntry;
import com.baseras.portal.repository.AuditLogRepository;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/audit-log")
@PreAuthorize("hasAnyRole('ADMIN','PRINCIPAL')")
public class AuditLogController {

    private final AuditLogRepository repo;

    public AuditLogController(AuditLogRepository repo) { this.repo = repo; }

    @GetMapping
    public List<AuditLogEntry> list() {
        return repo.findTop500ByOrderByCreatedAtDesc();
    }
}
