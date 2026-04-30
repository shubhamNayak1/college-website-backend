package com.baseras.portal.service;

import com.baseras.portal.common.IdGen;
import com.baseras.portal.entity.AuditLogEntry;
import com.baseras.portal.entity.User;
import com.baseras.portal.repository.AuditLogRepository;
import com.baseras.portal.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.Map;

@Service
public class AuditService {

    private final AuditLogRepository repo;
    private final UserRepository users;
    private final ObjectMapper json;

    public AuditService(AuditLogRepository repo, UserRepository users, ObjectMapper json) {
        this.repo = repo;
        this.users = users;
        this.json = json;
    }

    public void log(String actorUserId, String action, String entityType, String entityId, Map<String, Object> metadata) {
        String actorName = users.findById(actorUserId).map(User::getFullName).orElse("system");
        String meta = "{}";
        try {
            if (metadata != null && !metadata.isEmpty()) meta = json.writeValueAsString(metadata);
        } catch (Exception ignored) {}
        repo.save(AuditLogEntry.builder()
                .id(IdGen.of("al"))
                .actorUserId(actorUserId).actorName(actorName)
                .action(action).entityType(entityType).entityId(entityId)
                .metadata(meta)
                .createdAt(OffsetDateTime.now())
                .build());
    }
}
