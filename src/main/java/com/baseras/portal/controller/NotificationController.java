package com.baseras.portal.controller;

import com.baseras.portal.common.AppExceptions;
import com.baseras.portal.common.CurrentUser;
import com.baseras.portal.dto.AuthDtos.OkResponse;
import com.baseras.portal.entity.Notification;
import com.baseras.portal.entity.User;
import com.baseras.portal.repository.NotificationRepository;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.time.OffsetDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/v1/notifications")
public class NotificationController {

    private final NotificationRepository repo;
    private final CurrentUser current;

    public NotificationController(NotificationRepository repo, CurrentUser current) {
        this.repo = repo; this.current = current;
    }

    @GetMapping
    public List<Notification> list() {
        return repo.findByUserIdOrderByCreatedAtDesc(current.requireUserId());
    }

    @PatchMapping("/{id}/read")
    @Transactional
    public OkResponse markRead(@PathVariable String id) {
        User u = current.require();
        Notification n = repo.findByIdAndUserId(id, u.getId())
                .orElseThrow(() -> new AppExceptions.NotFoundException("Notification not found"));
        n.setReadAt(OffsetDateTime.now());
        repo.save(n);
        return OkResponse.OK;
    }

    @PatchMapping("/read-all")
    @Transactional
    public OkResponse markAllRead() {
        repo.markAllRead(current.requireUserId(), OffsetDateTime.now());
        return OkResponse.OK;
    }
}
