package com.baseras.portal.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.OffsetDateTime;

@Entity
@Table(name = "login_attempts", indexes = {
        @Index(name = "idx_login_attempts_ip_at", columnList = "ip_address, attempted_at")
})
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class LoginAttempt {
    @Id
    private String id;

    @Column(name = "ip_address", nullable = false)
    private String ipAddress;

    @Column(nullable = false)
    private String username;

    @Column(nullable = false)
    private boolean success;

    @Column(name = "attempted_at", nullable = false)
    private OffsetDateTime attemptedAt;
}
