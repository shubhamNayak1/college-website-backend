package com.baseras.portal.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.OffsetDateTime;

@Entity
@Table(name = "applications")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Application {
    @Id
    private String id;

    @Column(name = "post_id", nullable = false)
    private String postId;

    @Column(name = "applicant_name", nullable = false)
    private String applicantName;

    @Column(name = "applicant_email", nullable = false)
    private String applicantEmail;

    @Column(name = "applicant_phone", nullable = false)
    private String applicantPhone;

    @Column(name = "applicant_message", columnDefinition = "text")
    private String applicantMessage;

    @Column(name = "resume_url")
    private String resumeUrl;

    @Column(nullable = false)
    private String status;

    @Column(name = "submitted_at", nullable = false)
    private OffsetDateTime submittedAt;
}
