package com.baseras.portal.dto;

import jakarta.validation.constraints.NotBlank;

import java.time.LocalDate;

public class AssessmentDtos {

    public record CreateRequest(
            @NotBlank String classId, @NotBlank String subjectId,
            @NotBlank String title, String description, String attachmentUrl,
            LocalDate dueDate
    ) {}

    public record UpdateRequest(
            String classId, String subjectId,
            String title, String description, String attachmentUrl,
            LocalDate dueDate
    ) {}
}
