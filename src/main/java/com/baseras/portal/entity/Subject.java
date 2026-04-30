package com.baseras.portal.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "subjects")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Subject {
    @Id
    private String id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, unique = true)
    private String code;
}
