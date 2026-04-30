package com.baseras.portal.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;

@Entity
@Table(name = "students")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Student {
    @Id
    private String id;

    @Column(name = "user_id", nullable = false, unique = true)
    private String userId;

    @Column(name = "roll_number", nullable = false, unique = true)
    private String rollNumber;

    @Column(name = "admission_number", nullable = false)
    private String admissionNumber;

    @Column(name = "first_name", nullable = false)
    private String firstName;

    @Column(name = "middle_name")
    private String middleName;

    @Column(name = "last_name", nullable = false)
    private String lastName;

    @Column(nullable = false)
    private LocalDate dob;

    @Column(nullable = false)
    private String gender;

    @Column(name = "class_id", nullable = false)
    private String classId;

    @Column(name = "parent_name")
    private String parentName;

    @Column(name = "parent_phone")
    private String parentPhone;

    @Column(name = "parent_email")
    private String parentEmail;

    private String address;

    @Column(name = "photo_url")
    private String photoUrl;

    @Column(name = "blood_group")
    private String bloodGroup;
}
