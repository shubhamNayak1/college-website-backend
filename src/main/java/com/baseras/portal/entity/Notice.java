package com.baseras.portal.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.OffsetDateTime;
import java.util.List;

@Entity
@Table(name = "notices")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Notice {
    @Id
    private String id;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false, columnDefinition = "text")
    private String body;

    @Column(nullable = false)
    private String audience;

    @Column(name = "class_id")
    private String classId;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "notice_attachments", joinColumns = @JoinColumn(name = "notice_id"))
    @Column(name = "url")
    private List<String> attachmentUrls;

    @Column(name = "published_at", nullable = false)
    private OffsetDateTime publishedAt;

    @Column(name = "created_by", nullable = false)
    private String createdBy;

    @Column(name = "created_by_name", nullable = false)
    private String createdByName;

    @Column(nullable = false)
    private int version;

    @Column(name = "deleted_at")
    private OffsetDateTime deletedAt;
}
