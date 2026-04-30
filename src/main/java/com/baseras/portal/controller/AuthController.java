package com.baseras.portal.controller;

import com.baseras.portal.common.AppExceptions;
import com.baseras.portal.common.CurrentUser;
import com.baseras.portal.common.IdGen;
import com.baseras.portal.config.JwtService;
import com.baseras.portal.dto.AuthDtos.*;
import com.baseras.portal.entity.LoginAttempt;
import com.baseras.portal.entity.User;
import com.baseras.portal.repository.LoginAttemptRepository;
import com.baseras.portal.repository.UserRepository;
import com.baseras.portal.service.AuditService;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.time.OffsetDateTime;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final UserRepository users;
    private final PasswordEncoder pe;
    private final JwtService jwt;
    private final LoginAttemptRepository attempts;
    private final AuditService audit;
    private final CurrentUser current;

    public AuthController(UserRepository users, PasswordEncoder pe, JwtService jwt,
                          LoginAttemptRepository attempts, AuditService audit, CurrentUser current) {
        this.users = users; this.pe = pe; this.jwt = jwt; this.attempts = attempts;
        this.audit = audit; this.current = current;
    }

    @PostMapping("/login")
    public LoginResponse login(@Valid @RequestBody LoginRequest req, HttpServletRequest http) {
        String ip = http.getRemoteAddr();
        User user = users.findByUsernameIgnoreCase(req.username().trim()).orElse(null);
        if (user == null || user.getDeletedAt() != null || !pe.matches(req.password(), user.getPasswordHash())) {
            attempts.save(LoginAttempt.builder().id(IdGen.of("la"))
                    .ipAddress(ip).username(req.username()).success(false)
                    .attemptedAt(OffsetDateTime.now()).build());
            throw new AppExceptions.ApiException(
                    org.springframework.http.HttpStatus.UNAUTHORIZED,
                    "Invalid credentials",
                    "Roll number / username or password is incorrect");
        }
        attempts.save(LoginAttempt.builder().id(IdGen.of("la"))
                .ipAddress(ip).username(req.username()).success(true)
                .attemptedAt(OffsetDateTime.now()).build());
        audit.log(user.getId(), "LOGIN", "User", user.getId(), Map.of());
        return new LoginResponse(
                jwt.issueAccessToken(user.getId(), user.getRole().name()),
                jwt.issueRefreshToken(user.getId()),
                UserDto.from(user));
    }

    @PostMapping("/refresh")
    public RefreshResponse refresh(@Valid @RequestBody RefreshRequest req) {
        try {
            Claims claims = jwt.parse(req.refreshToken());
            if (!"refresh".equals(claims.get("typ"))) throw new AppExceptions.UnauthorizedException("Invalid token");
            String userId = claims.getSubject();
            User user = users.findById(userId).orElseThrow(() -> new AppExceptions.UnauthorizedException("User not found"));
            return new RefreshResponse(
                    jwt.issueAccessToken(user.getId(), user.getRole().name()),
                    jwt.issueRefreshToken(user.getId()));
        } catch (Exception e) {
            throw new AppExceptions.UnauthorizedException("Invalid refresh token");
        }
    }

    @PostMapping("/logout")
    public OkResponse logout() {
        current.opt().ifPresent(u -> audit.log(u.getId(), "LOGOUT", "User", u.getId(), Map.of()));
        return OkResponse.OK;
    }

    @GetMapping("/me")
    public UserDto me() {
        return UserDto.from(current.require());
    }

    @PostMapping("/change-password")
    public OkResponse changePassword(@Valid @RequestBody ChangePasswordRequest req) {
        User user = current.require();
        if (!pe.matches(req.oldPassword(), user.getPasswordHash())) {
            throw new AppExceptions.ValidationException("Old password is incorrect");
        }
        user.setPasswordHash(pe.encode(req.newPassword()));
        user.setMustResetPassword(false);
        users.save(user);
        audit.log(user.getId(), "CHANGE_PASSWORD", "User", user.getId(), Map.of());
        return OkResponse.OK;
    }
}
