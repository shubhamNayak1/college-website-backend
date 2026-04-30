package com.baseras.portal.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "exam_types")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ExamType {
    @Id
    private String id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private double weight;

    @Column(nullable = false)
    private int sequence;
}
