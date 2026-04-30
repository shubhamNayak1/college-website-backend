package com.baseras.portal.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.OffsetDateTime;

@Entity
@Table(name = "notice_versions")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class NoticeVersion {
    @Id
    private String id;

    @Column(name = "notice_id", nullable = false)
    private String noticeId;

    @Column(nullable = false)
    private int version;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false, columnDefinition = "text")
    private String body;

    @Column(nullable = false)
    private String audience;

    @Column(name = "class_id")
    private String classId;

    @Column(name = "edited_by", nullable = false)
    private String editedBy;

    @Column(name = "edited_by_name", nullable = false)
    private String editedByName;

    @Column(name = "edited_at", nullable = false)
    private OffsetDateTime editedAt;
}
