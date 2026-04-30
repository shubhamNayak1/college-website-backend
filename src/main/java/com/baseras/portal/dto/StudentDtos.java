package com.baseras.portal.dto;

import jakarta.validation.constraints.NotBlank;

import java.time.LocalDate;
import java.util.List;

public class StudentDtos {

    public record CreateRequest(
            @NotBlank String rollNumber,
            String admissionNumber,
            @NotBlank String firstName,
            String middleName,
            @NotBlank String lastName,
            LocalDate dob,
            String gender,
            @NotBlank String classId,
            String parentName, String parentPhone, String parentEmail,
            String address, String photoUrl, String bloodGroup
    ) {}

    public record UpdateRequest(
            String admissionNumber,
            String firstName, String middleName, String lastName,
            LocalDate dob, String gender, String classId,
            String parentName, String parentPhone, String parentEmail,
            String address, String photoUrl, String bloodGroup
    ) {}

    public record StudentResponse(
            String id, String userId, String rollNumber, String admissionNumber,
            String firstName, String middleName, String lastName,
            LocalDate dob, String gender, String classId,
            String parentName, String parentPhone, String parentEmail,
            String address, String photoUrl, String bloodGroup,
            String defaultPassword
    ) {}

    public record BulkRequest(List<CreateRequest> rows) {}

    public record BulkRowResult(int row, boolean ok, String message, String defaultPassword) {}

    public record BulkResponse(List<BulkRowResult> results) {}
}
