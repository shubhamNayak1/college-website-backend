package com.baseras.portal.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "classes")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class SchoolClass {
    @Id
    private String id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String section;

    @Column(name = "class_teacher_id")
    private String classTeacherId;
}
