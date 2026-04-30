package com.baseras.portal.dto;

import com.baseras.portal.entity.Teacher;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

import java.time.LocalDate;
import java.util.List;

public class TeacherDtos {

    public record CreateRequest(
            @NotBlank String employeeId,
            @NotBlank String firstName,
            @NotBlank String lastName,
            @NotBlank @Email String email,
            String phone, String qualification,
            LocalDate joinedOn, String photoUrl,
            List<String> subjectIds
    ) {}

    public record UpdateRequest(
            String firstName, String lastName, String email, String phone,
            String qualification, LocalDate joinedOn, String photoUrl,
            List<String> subjectIds
    ) {}

    public record TeacherResponse(
            String id, String userId, String employeeId,
            String firstName, String lastName, String email, String phone,
            String qualification, LocalDate joinedOn, String photoUrl,
            List<String> subjectIds
    ) {
        public static TeacherResponse from(Teacher t) {
            return new TeacherResponse(t.getId(), t.getUserId(), t.getEmployeeId(),
                    t.getFirstName(), t.getLastName(), t.getEmail(), t.getPhone(),
                    t.getQualification(), t.getJoinedOn(), t.getPhotoUrl(),
                    t.getSubjectIds());
        }
    }
}
