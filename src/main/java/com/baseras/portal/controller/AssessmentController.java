package com.baseras.portal.controller;

import com.baseras.portal.common.AppExceptions;
import com.baseras.portal.common.CurrentUser;
import com.baseras.portal.common.IdGen;
import com.baseras.portal.dto.AssessmentDtos.*;
import com.baseras.portal.dto.AuthDtos.OkResponse;
import com.baseras.portal.entity.*;
import com.baseras.portal.repository.AssessmentRepository;
import com.baseras.portal.repository.TeacherRepository;
import com.baseras.portal.service.AuditService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/assessments")
public class AssessmentController {

    private final AssessmentRepository assessments;
    private final TeacherRepository teachers;
    private final CurrentUser current;
    private final AuditService audit;

    public AssessmentController(AssessmentRepository assessments, TeacherRepository teachers,
                                CurrentUser current, AuditService audit) {
        this.assessments = assessments; this.teachers = teachers;
        this.current = current; this.audit = audit;
    }

    @GetMapping
    public List<Assessment> list() {
        User u = current.require();
        return switch (u.getRole()) {
            case STUDENT -> u.getClassId() == null ? List.of()
                    : assessments.findByClassIdAndDeletedAtIsNullOrderByCreatedAtDesc(u.getClassId());
            case TEACHER -> teachers.findByUserId(u.getId())
                    .map(t -> assessments.findByTeacherIdAndDeletedAtIsNullOrderByCreatedAtDesc(t.getId()))
                    .orElse(List.of());
            case ADMIN, PRINCIPAL -> assessments.findByDeletedAtIsNullOrderByCreatedAtDesc();
        };
    }

    @PostMapping
    @PreAuthorize("hasRole('TEACHER')")
    @Transactional
    public ResponseEntity<Assessment> create(@Valid @RequestBody CreateRequest req) {
        User u = current.require();
        Teacher t = teachers.findByUserId(u.getId())
                .orElseThrow(() -> new AppExceptions.ForbiddenException("Teacher profile missing"));
        Assessment a = Assessment.builder().id(IdGen.of("as"))
                .teacherId(t.getId()).classId(req.classId()).subjectId(req.subjectId())
                .sessionId("sess_2025_26").title(req.title()).description(req.description())
                .attachmentUrl(req.attachmentUrl()).dueDate(req.dueDate())
                .createdAt(OffsetDateTime.now())
                .build();
        assessments.save(a);
        audit.log(u.getId(), "CREATE_ASSESSMENT", "Assessment", a.getId(), Map.of("title", req.title()));
        return ResponseEntity.status(201).body(a);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('TEACHER')")
    @Transactional
    public Assessment update(@PathVariable String id, @RequestBody UpdateRequest req) {
        User u = current.require();
        Assessment a = assessments.findById(id)
                .filter(x -> x.getDeletedAt() == null)
                .orElseThrow(() -> new AppExceptions.NotFoundException("Assessment not found"));
        Teacher t = teachers.findByUserId(u.getId()).orElseThrow(() -> new AppExceptions.ForbiddenException("Not a teacher"));
        if (!a.getTeacherId().equals(t.getId())) throw new AppExceptions.ForbiddenException("Not your assessment");
        if (req.title() != null) a.setTitle(req.title());
        if (req.description() != null) a.setDescription(req.description());
        if (req.classId() != null) a.setClassId(req.classId());
        if (req.subjectId() != null) a.setSubjectId(req.subjectId());
        if (req.dueDate() != null) a.setDueDate(req.dueDate());
        if (req.attachmentUrl() != null) a.setAttachmentUrl(req.attachmentUrl());
        assessments.save(a);
        audit.log(u.getId(), "UPDATE_ASSESSMENT", "Assessment", a.getId(), Map.of());
        return a;
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('TEACHER')")
    @Transactional
    public OkResponse delete(@PathVariable String id) {
        User u = current.require();
        Assessment a = assessments.findById(id).orElseThrow(() -> new AppExceptions.NotFoundException("Assessment not found"));
        Teacher t = teachers.findByUserId(u.getId()).orElseThrow(() -> new AppExceptions.ForbiddenException("Not a teacher"));
        if (!a.getTeacherId().equals(t.getId())) throw new AppExceptions.ForbiddenException("Not your assessment");
        a.setDeletedAt(OffsetDateTime.now());
        assessments.save(a);
        audit.log(u.getId(), "DELETE_ASSESSMENT", "Assessment", a.getId(), Map.of());
        return OkResponse.OK;
    }
}
