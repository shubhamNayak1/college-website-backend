package com.baseras.portal.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.OffsetDateTime;

@Entity
@Table(name = "users")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class User {
    @Id
    private String id;

    @Column(nullable = false, unique = true)
    private String username;

    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    @Column(name = "full_name", nullable = false)
    private String fullName;

    private String email;

    @Column(name = "roll_number")
    private String rollNumber;

    @Column(name = "employee_id")
    private String employeeId;

    @Column(name = "class_id")
    private String classId;

    @Column(name = "must_reset_password", nullable = false)
    private boolean mustResetPassword;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    @Column(name = "deleted_at")
    private OffsetDateTime deletedAt;
}
