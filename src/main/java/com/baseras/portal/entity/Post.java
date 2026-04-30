package com.baseras.portal.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.OffsetDateTime;

@Entity
@Table(name = "posts")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Post {
    @Id
    private String id;

    @Column(nullable = false)
    private String type;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false, columnDefinition = "text")
    private String body;

    @Column(name = "image_url")
    private String imageUrl;

    @Column(name = "apply_enabled", nullable = false)
    private boolean applyEnabled;

    @Column(nullable = false)
    private String status;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;
}
