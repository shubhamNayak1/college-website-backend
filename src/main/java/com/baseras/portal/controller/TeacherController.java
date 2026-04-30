package com.baseras.portal.controller;

import com.baseras.portal.common.AppExceptions;
import com.baseras.portal.common.CurrentUser;
import com.baseras.portal.dto.AuthDtos.OkResponse;
import com.baseras.portal.dto.TeacherDtos.*;
import com.baseras.portal.entity.Role;
import com.baseras.portal.entity.Teacher;
import com.baseras.portal.entity.User;
import com.baseras.portal.repository.TeacherRepository;
import com.baseras.portal.repository.UserRepository;
import com.baseras.portal.service.AuditService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/admin/teachers")
public class TeacherController {

    private final TeacherRepository teachers;
    private final UserRepository users;
    private final PasswordEncoder pe;
    private final AuditService audit;
    private final CurrentUser current;

    public TeacherController(TeacherRepository teachers, UserRepository users,
                             PasswordEncoder pe, AuditService audit, CurrentUser current) {
        this.teachers = teachers; this.users = users; this.pe = pe;
        this.audit = audit; this.current = current;
    }

    @GetMapping
    public List<TeacherResponse> list() {
        return teachers.findAll().stream().map(TeacherResponse::from).toList();
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    public ResponseEntity<TeacherResponse> create(@Valid @RequestBody CreateRequest req) {
        User actor = current.require();
        if (teachers.findByEmployeeId(req.employeeId()).isPresent()) {
            throw new AppExceptions.ValidationException("Employee ID already exists");
        }
        String userId = "u_t_" + req.employeeId();
        String tid = "t_" + req.employeeId();
        users.save(User.builder().id(userId).username(req.employeeId()).role(Role.TEACHER)
                .fullName(req.firstName() + " " + req.lastName()).employeeId(req.employeeId())
                .email(req.email()).mustResetPassword(true)
                .passwordHash(pe.encode("teacher123")).createdAt(OffsetDateTime.now()).build());
        Teacher t = Teacher.builder().id(tid).userId(userId).employeeId(req.employeeId())
                .firstName(req.firstName()).lastName(req.lastName())
                .email(req.email()).phone(req.phone()).qualification(req.qualification())
                .joinedOn(req.joinedOn() == null ? LocalDate.now() : req.joinedOn())
                .photoUrl(req.photoUrl())
                .subjectIds(req.subjectIds() == null ? new ArrayList<>() : new ArrayList<>(req.subjectIds()))
                .build();
        teachers.save(t);
        audit.log(actor.getId(), "CREATE_TEACHER", "Teacher", tid, Map.of("employeeId", req.employeeId()));
        return ResponseEntity.status(201).body(TeacherResponse.from(t));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    public TeacherResponse update(@PathVariable String id, @RequestBody UpdateRequest req) {
        User actor = current.require();
        Teacher t = teachers.findById(id).orElseThrow(() -> new AppExceptions.NotFoundException("Teacher not found"));
        if (req.firstName() != null) t.setFirstName(req.firstName());
        if (req.lastName() != null) t.setLastName(req.lastName());
        if (req.email() != null) t.setEmail(req.email());
        if (req.phone() != null) t.setPhone(req.phone());
        if (req.qualification() != null) t.setQualification(req.qualification());
        if (req.joinedOn() != null) t.setJoinedOn(req.joinedOn());
        if (req.photoUrl() != null) t.setPhotoUrl(req.photoUrl());
        if (req.subjectIds() != null) t.setSubjectIds(new ArrayList<>(req.subjectIds()));
        teachers.save(t);
        audit.log(actor.getId(), "UPDATE_TEACHER", "Teacher", t.getId(), Map.of());
        return TeacherResponse.from(t);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    public OkResponse delete(@PathVariable String id) {
        User actor = current.require();
        Teacher t = teachers.findById(id).orElseThrow(() -> new AppExceptions.NotFoundException("Teacher not found"));
        teachers.delete(t);
        users.findById(t.getUserId()).ifPresent(u -> {
            u.setDeletedAt(OffsetDateTime.now());
            users.save(u);
        });
        audit.log(actor.getId(), "DELETE_TEACHER", "Teacher", id, Map.of());
        return OkResponse.OK;
    }
}
