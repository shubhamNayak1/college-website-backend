package com.baseras.portal.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "infrastructure_gallery")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class InfrastructureImage {
    @Id
    private String id;

    @Column(nullable = false)
    private String category;

    @Column(name = "image_url", nullable = false)
    private String imageUrl;

    @Column(nullable = false)
    private String caption;

    @Column(name = "display_order", nullable = false)
    private int displayOrder;
}
