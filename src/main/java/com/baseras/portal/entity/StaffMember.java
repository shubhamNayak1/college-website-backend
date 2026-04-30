package com.baseras.portal.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "staff_directory")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class StaffMember {
    @Id
    private String id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String designation;

    @Column(nullable = false)
    private String department;

    @Column(name = "photo_url")
    private String photoUrl;

    @Column(columnDefinition = "text")
    private String bio;

    @Column(name = "display_order", nullable = false)
    private int displayOrder;
}
