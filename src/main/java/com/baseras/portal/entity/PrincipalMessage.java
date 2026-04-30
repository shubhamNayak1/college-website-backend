package com.baseras.portal.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.OffsetDateTime;

@Entity
@Table(name = "principal_message")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class PrincipalMessage {
    @Id
    private String id;

    @Column(name = "principal_name", nullable = false)
    private String principalName;

    @Column(name = "photo_url")
    private String photoUrl;

    @Column(nullable = false, columnDefinition = "text")
    private String message;

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;
}
