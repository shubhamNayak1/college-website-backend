package com.baseras.portal.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "alumni")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Alumni {
    @Id
    private String id;

    @Column(nullable = false)
    private String name;

    @Column(name = "batch_year", nullable = false)
    private int batchYear;

    @Column(name = "photo_url")
    private String photoUrl;

    @Column(name = "current_position")
    private String currentRole;

    @Column(columnDefinition = "text")
    private String achievements;

    @Column(name = "linkedin_url")
    private String linkedinUrl;
}
