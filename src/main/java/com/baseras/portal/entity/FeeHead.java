package com.baseras.portal.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "fee_heads")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class FeeHead {
    @Id
    private String id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private boolean recurring;
}
