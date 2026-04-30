package com.baseras.portal.controller;

import com.baseras.portal.common.AppExceptions;
import com.baseras.portal.common.CurrentUser;
import com.baseras.portal.common.IdGen;
import com.baseras.portal.dto.AuthDtos.OkResponse;
import com.baseras.portal.dto.StudentDtos.*;
import com.baseras.portal.entity.Role;
import com.baseras.portal.entity.Student;
import com.baseras.portal.entity.User;
import com.baseras.portal.repository.StudentRepository;
import com.baseras.portal.repository.UserRepository;
import com.baseras.portal.service.AuditService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/admin/students")
public class StudentController {

    private final StudentRepository students;
    private final UserRepository users;
    private final PasswordEncoder pe;
    private final AuditService audit;
    private final CurrentUser current;

    public StudentController(StudentRepository students, UserRepository users,
                             PasswordEncoder pe, AuditService audit, CurrentUser current) {
        this.students = students; this.users = users; this.pe = pe;
        this.audit = audit; this.current = current;
    }

    @GetMapping
    public List<StudentResponse> list(@RequestParam(required = false) String classId,
                                      @RequestParam(required = false) String search) {
        return students.search(classId, search).stream().map(this::toResponse).toList();
    }

    @GetMapping("/{id}")
    public StudentResponse get(@PathVariable String id) {
        return toResponse(students.findById(id).orElseThrow(() -> new AppExceptions.NotFoundException("Student not found")));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    public ResponseEntity<StudentResponse> create(@Valid @RequestBody CreateRequest req) {
        User actor = current.require();
        if (students.findByRollNumber(req.rollNumber()).isPresent()) {
            throw new AppExceptions.ValidationException("Roll number already exists");
        }
        String dobStr = req.dob().toString();
        String pwd = IdGen.defaultStudentPassword(req.rollNumber(), req.firstName(), dobStr);
        String userId = "u_s_" + req.rollNumber();
        String studentId = "s_" + req.rollNumber();
        users.save(User.builder().id(userId).username(req.rollNumber()).role(Role.STUDENT)
                .fullName(req.firstName() + " " + req.lastName()).rollNumber(req.rollNumber())
                .classId(req.classId()).mustResetPassword(true)
                .passwordHash(pe.encode(pwd)).createdAt(OffsetDateTime.now()).build());
        Student s = Student.builder().id(studentId).userId(userId)
                .rollNumber(req.rollNumber())
                .admissionNumber(req.admissionNumber() == null ? "ADM" + System.currentTimeMillis() : req.admissionNumber())
                .firstName(req.firstName()).middleName(req.middleName()).lastName(req.lastName())
                .dob(req.dob()).gender(req.gender() == null ? "OTHER" : req.gender())
                .classId(req.classId())
                .parentName(req.parentName()).parentPhone(req.parentPhone()).parentEmail(req.parentEmail())
                .address(req.address()).photoUrl(req.photoUrl()).bloodGroup(req.bloodGroup())
                .build();
        students.save(s);
        audit.log(actor.getId(), "CREATE_STUDENT", "Student", studentId, Map.of("rollNumber", req.rollNumber()));
        return ResponseEntity.status(201).body(toResponseWithPwd(s, pwd));
    }

    @PostMapping("/bulk")
    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    public BulkResponse bulk(@RequestBody BulkRequest body) {
        User actor = current.require();
        List<BulkRowResult> results = new ArrayList<>();
        int idx = 0;
        for (CreateRequest r : body.rows()) {
            idx++;
            try {
                if (r == null || r.rollNumber() == null || r.firstName() == null
                        || r.lastName() == null || r.dob() == null || r.classId() == null) {
                    results.add(new BulkRowResult(idx, false, "Missing required fields", null));
                    continue;
                }
                if (students.findByRollNumber(r.rollNumber()).isPresent()) {
                    results.add(new BulkRowResult(idx, false, "Roll " + r.rollNumber() + " already exists", null));
                    continue;
                }
                String pwd = IdGen.defaultStudentPassword(r.rollNumber(), r.firstName(), r.dob().toString());
                String userId = "u_s_" + r.rollNumber();
                String studentId = "s_" + r.rollNumber();
                users.save(User.builder().id(userId).username(r.rollNumber()).role(Role.STUDENT)
                        .fullName(r.firstName() + " " + r.lastName()).rollNumber(r.rollNumber())
                        .classId(r.classId()).mustResetPassword(true)
                        .passwordHash(pe.encode(pwd)).createdAt(OffsetDateTime.now()).build());
                Student s = Student.builder().id(studentId).userId(userId).rollNumber(r.rollNumber())
                        .admissionNumber(r.admissionNumber() == null ? "ADM" + System.currentTimeMillis() + idx : r.admissionNumber())
                        .firstName(r.firstName()).middleName(r.middleName()).lastName(r.lastName())
                        .dob(r.dob()).gender(r.gender() == null ? "OTHER" : r.gender())
                        .classId(r.classId())
                        .parentName(r.parentName()).parentPhone(r.parentPhone()).parentEmail(r.parentEmail())
                        .address(r.address()).bloodGroup(r.bloodGroup()).build();
                students.save(s);
                results.add(new BulkRowResult(idx, true, "Created", pwd));
            } catch (Exception e) {
                results.add(new BulkRowResult(idx, false, e.getMessage() == null ? "Unknown error" : e.getMessage(), null));
            }
        }
        audit.log(actor.getId(), "BULK_CREATE_STUDENTS", "Student", "bulk",
                Map.of("rows", body.rows().size(),
                        "succeeded", results.stream().filter(BulkRowResult::ok).count()));
        return new BulkResponse(results);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    public StudentResponse update(@PathVariable String id, @RequestBody UpdateRequest req) {
        User actor = current.require();
        Student s = students.findById(id).orElseThrow(() -> new AppExceptions.NotFoundException("Student not found"));
        if (req.firstName() != null) s.setFirstName(req.firstName());
        if (req.middleName() != null) s.setMiddleName(req.middleName());
        if (req.lastName() != null) s.setLastName(req.lastName());
        if (req.dob() != null) s.setDob(req.dob());
        if (req.gender() != null) s.setGender(req.gender());
        if (req.classId() != null) s.setClassId(req.classId());
        if (req.parentName() != null) s.setParentName(req.parentName());
        if (req.parentPhone() != null) s.setParentPhone(req.parentPhone());
        if (req.parentEmail() != null) s.setParentEmail(req.parentEmail());
        if (req.address() != null) s.setAddress(req.address());
        if (req.photoUrl() != null) s.setPhotoUrl(req.photoUrl());
        if (req.bloodGroup() != null) s.setBloodGroup(req.bloodGroup());
        if (req.admissionNumber() != null) s.setAdmissionNumber(req.admissionNumber());
        students.save(s);
        audit.log(actor.getId(), "UPDATE_STUDENT", "Student", s.getId(), Map.of());
        return toResponse(s);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    public OkResponse delete(@PathVariable String id) {
        User actor = current.require();
        Student s = students.findById(id).orElseThrow(() -> new AppExceptions.NotFoundException("Student not found"));
        students.delete(s);
        users.findById(s.getUserId()).ifPresent(u -> {
            u.setDeletedAt(OffsetDateTime.now());
            users.save(u);
        });
        audit.log(actor.getId(), "DELETE_STUDENT", "Student", id, Map.of());
        return OkResponse.OK;
    }

    private StudentResponse toResponse(Student s) {
        String pwd = IdGen.defaultStudentPassword(s.getRollNumber(), s.getFirstName(), s.getDob().toString());
        return toResponseWithPwd(s, pwd);
    }

    private StudentResponse toResponseWithPwd(Student s, String pwd) {
        return new StudentResponse(s.getId(), s.getUserId(), s.getRollNumber(), s.getAdmissionNumber(),
                s.getFirstName(), s.getMiddleName(), s.getLastName(), s.getDob(), s.getGender(), s.getClassId(),
                s.getParentName(), s.getParentPhone(), s.getParentEmail(), s.getAddress(),
                s.getPhotoUrl(), s.getBloodGroup(), pwd);
    }
}
