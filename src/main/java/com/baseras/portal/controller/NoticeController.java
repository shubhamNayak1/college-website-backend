package com.baseras.portal.controller;

import com.baseras.portal.common.AppExceptions;
import com.baseras.portal.common.CurrentUser;
import com.baseras.portal.common.IdGen;
import com.baseras.portal.dto.AuthDtos.OkResponse;
import com.baseras.portal.dto.NoticeDtos.*;
import com.baseras.portal.entity.Notice;
import com.baseras.portal.entity.NoticeVersion;
import com.baseras.portal.entity.Role;
import com.baseras.portal.entity.User;
import com.baseras.portal.repository.NoticeRepository;
import com.baseras.portal.repository.NoticeVersionRepository;
import com.baseras.portal.repository.UserRepository;
import com.baseras.portal.service.AuditService;
import com.baseras.portal.service.NotificationService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/notices")
public class NoticeController {

    private final NoticeRepository notices;
    private final NoticeVersionRepository versions;
    private final UserRepository users;
    private final CurrentUser current;
    private final AuditService audit;
    private final NotificationService notify;

    public NoticeController(NoticeRepository notices, NoticeVersionRepository versions,
                            UserRepository users, CurrentUser current, AuditService audit,
                            NotificationService notify) {
        this.notices = notices; this.versions = versions; this.users = users;
        this.current = current; this.audit = audit; this.notify = notify;
    }

    @GetMapping
    public List<Notice> list() {
        User u = current.require();
        var all = notices.findByDeletedAtIsNullOrderByPublishedAtDesc();
        return all.stream().filter(n -> visibleTo(n, u)).toList();
    }

    private boolean visibleTo(Notice n, User u) {
        return switch (u.getRole()) {
            case STUDENT -> "ALL_PUBLIC".equals(n.getAudience())
                    || "STUDENTS".equals(n.getAudience())
                    || ("CLASS".equals(n.getAudience()) && u.getClassId() != null && u.getClassId().equals(n.getClassId()));
            case TEACHER -> !"STUDENTS".equals(n.getAudience()) || true; // teachers see students-targeted too
            case ADMIN, PRINCIPAL -> true;
        };
    }

    @GetMapping("/{id}")
    public Notice get(@PathVariable String id) {
        Notice n = notices.findById(id).orElseThrow(() -> new AppExceptions.NotFoundException("Notice not found"));
        if (n.getDeletedAt() != null) throw new AppExceptions.NotFoundException("Notice not found");
        return n;
    }

    @GetMapping("/{id}/versions")
    public List<NoticeVersion> versions(@PathVariable String id) {
        return versions.findByNoticeIdOrderByVersionDesc(id);
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','PRINCIPAL')")
    @Transactional
    public ResponseEntity<Notice> create(@Valid @RequestBody CreateRequest req) {
        User u = current.require();
        Notice n = Notice.builder()
                .id(IdGen.of("not")).title(req.title()).body(req.body())
                .audience(req.audience()).classId(req.classId())
                .attachmentUrls(req.attachmentUrls() == null ? new ArrayList<>() : new ArrayList<>(req.attachmentUrls()))
                .publishedAt(OffsetDateTime.now())
                .createdBy(u.getId()).createdByName(u.getFullName())
                .version(1).build();
        notices.save(n);
        audit.log(u.getId(), "CREATE_NOTICE", "Notice", n.getId(), Map.of("title", n.getTitle()));
        users.findByRole(Role.STUDENT).forEach(s ->
                notify.notify(s.getId(), "NOTICE", "New Notice", n.getTitle(), "/portal/notices/" + n.getId()));
        return ResponseEntity.status(201).body(n);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','PRINCIPAL')")
    @Transactional
    public Notice update(@PathVariable String id, @RequestBody UpdateRequest req) {
        User u = current.require();
        Notice n = notices.findById(id).filter(x -> x.getDeletedAt() == null)
                .orElseThrow(() -> new AppExceptions.NotFoundException("Notice not found"));
        // snapshot
        versions.save(NoticeVersion.builder().id(IdGen.of("nv"))
                .noticeId(n.getId()).version(n.getVersion())
                .title(n.getTitle()).body(n.getBody())
                .audience(n.getAudience()).classId(n.getClassId())
                .editedBy(u.getId()).editedByName(u.getFullName())
                .editedAt(OffsetDateTime.now())
                .build());
        if (req.title() != null) n.setTitle(req.title());
        if (req.body() != null) n.setBody(req.body());
        if (req.audience() != null) n.setAudience(req.audience());
        if (req.classId() != null) n.setClassId(req.classId());
        if (req.attachmentUrls() != null) n.setAttachmentUrls(new ArrayList<>(req.attachmentUrls()));
        n.setVersion(n.getVersion() + 1);
        notices.save(n);
        audit.log(u.getId(), "UPDATE_NOTICE", "Notice", n.getId(), Map.of("newVersion", n.getVersion()));
        return n;
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','PRINCIPAL')")
    @Transactional
    public OkResponse delete(@PathVariable String id) {
        User u = current.require();
        Notice n = notices.findById(id).orElseThrow(() -> new AppExceptions.NotFoundException("Notice not found"));
        n.setDeletedAt(OffsetDateTime.now());
        notices.save(n);
        audit.log(u.getId(), "DELETE_NOTICE", "Notice", n.getId(), Map.of());
        return OkResponse.OK;
    }
}
