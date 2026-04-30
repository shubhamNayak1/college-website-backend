package com.baseras.portal.service;

import com.baseras.portal.common.IdGen;
import com.baseras.portal.entity.Notification;
import com.baseras.portal.repository.NotificationRepository;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;

@Service
public class NotificationService {

    private final NotificationRepository repo;

    public NotificationService(NotificationRepository repo) {
        this.repo = repo;
    }

    public void notify(String userId, String type, String title, String body, String link) {
        repo.save(Notification.builder()
                .id(IdGen.of("n"))
                .userId(userId).type(type).title(title).body(body).link(link)
                .createdAt(OffsetDateTime.now())
                .build());
    }
}
