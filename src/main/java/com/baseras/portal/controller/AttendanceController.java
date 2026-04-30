package com.baseras.portal.controller;

import com.baseras.portal.common.AppExceptions;
import com.baseras.portal.common.CurrentUser;
import com.baseras.portal.common.IdGen;
import com.baseras.portal.dto.AttendanceDtos.*;
import com.baseras.portal.dto.AuthDtos.OkResponse;
import com.baseras.portal.entity.*;
import com.baseras.portal.repository.*;
import com.baseras.portal.service.AuditService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/attendance")
public class AttendanceController {

    private final AttendanceRepository attendance;
    private final StudentRepository students;
    private final TeacherRepository teachers;
    private final CurrentUser current;
    private final AuditService audit;

    public AttendanceController(AttendanceRepository attendance, StudentRepository students,
                                TeacherRepository teachers, CurrentUser current, AuditService audit) {
        this.attendance = attendance; this.students = students; this.teachers = teachers;
        this.current = current; this.audit = audit;
    }

    @GetMapping
    public List<Attendance> list(@RequestParam(required = false) String classId,
                                 @RequestParam(required = false) String date,
                                 @RequestParam(required = false) String studentId,
                                 @RequestParam(required = false) String from,
                                 @RequestParam(required = false) String to) {
        User u = current.require();
        if (u.getRole() == Role.STUDENT) {
            Student me = students.findByUserId(u.getId())
                    .orElseThrow(() -> new AppExceptions.NotFoundException("Student profile missing"));
            return attendance.findByStudentIdOrderByDateDesc(me.getId());
        }
        List<String> studentIds;
        if (classId != null) {
            studentIds = students.findByClassId(classId).stream().map(Student::getId).toList();
        } else if (studentId != null) {
            studentIds = List.of(studentId);
        } else {
            studentIds = students.findAll().stream().map(Student::getId).toList();
        }
        if (studentIds.isEmpty()) return List.of();
        if (date != null) return attendance.findByStudentIdInAndDate(studentIds, LocalDate.parse(date));
        if (from != null && to != null) return attendance.findByStudentIdInAndDateBetween(studentIds, LocalDate.parse(from), LocalDate.parse(to));
        return attendance.findByStudentIdInOrderByDateDesc(studentIds);
    }

    @PutMapping("/bulk")
    @PreAuthorize("hasRole('TEACHER')")
    @Transactional
    public OkResponse markBulk(@RequestBody BulkRequest req) {
        User u = current.require();
        Teacher teacher = teachers.findByUserId(u.getId())
                .orElseThrow(() -> new AppExceptions.ForbiddenException("Teacher profile missing"));
        for (EntryUpsert e : req.entries()) {
            var existing = attendance.findByStudentIdAndDate(e.studentId(), req.date()).orElse(null);
            if (existing != null) {
                existing.setStatus(e.status());
                existing.setRemarks(e.remarks());
                existing.setMarkedBy(teacher.getId());
                attendance.save(existing);
            } else {
                attendance.save(Attendance.builder().id(IdGen.of("att"))
                        .studentId(e.studentId()).date(req.date())
                        .status(e.status()).remarks(e.remarks())
                        .markedBy(teacher.getId()).sessionId("sess_2025_26")
                        .build());
            }
        }
        audit.log(u.getId(), "MARK_ATTENDANCE", "Attendance",
                req.classId() + "-" + req.date(),
                Map.of("count", req.entries().size()));
        return OkResponse.OK;
    }
}
