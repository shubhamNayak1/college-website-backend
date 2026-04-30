package com.baseras.portal.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import java.util.List;

@Entity
@Table(name = "teachers")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Teacher {
    @Id
    private String id;

    @Column(name = "user_id", nullable = false, unique = true)
    private String userId;

    @Column(name = "employee_id", nullable = false, unique = true)
    private String employeeId;

    @Column(name = "first_name", nullable = false)
    private String firstName;

    @Column(name = "last_name", nullable = false)
    private String lastName;

    @Column(nullable = false)
    private String email;

    private String phone;

    private String qualification;

    @Column(name = "joined_on")
    private LocalDate joinedOn;

    @Column(name = "photo_url")
    private String photoUrl;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "teacher_subjects", joinColumns = @JoinColumn(name = "teacher_id"))
    @Column(name = "subject_id")
    private List<String> subjectIds;
}
