package com.baseras.portal.dto;

import com.baseras.portal.entity.Role;
import com.baseras.portal.entity.User;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class AuthDtos {

    public record LoginRequest(@NotBlank String username, @NotBlank String password) {}

    public record LoginResponse(String accessToken, String refreshToken, UserDto user) {}

    public record RefreshRequest(@NotBlank String refreshToken) {}

    public record RefreshResponse(String accessToken, String refreshToken) {}

    public record ChangePasswordRequest(@NotBlank String oldPassword, @NotBlank @Size(min = 6) String newPassword) {}

    public record UserDto(
            String id, String username, Role role, String fullName,
            String email, String rollNumber, String employeeId, String classId,
            boolean mustResetPassword
    ) {
        public static UserDto from(User u) {
            return new UserDto(u.getId(), u.getUsername(), u.getRole(), u.getFullName(),
                    u.getEmail(), u.getRollNumber(), u.getEmployeeId(), u.getClassId(),
                    u.isMustResetPassword());
        }
    }

    public record OkResponse(boolean ok) {
        public static final OkResponse OK = new OkResponse(true);
    }
}
