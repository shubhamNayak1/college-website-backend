package com.baseras.portal.controller;

import com.baseras.portal.common.AppExceptions;
import com.baseras.portal.common.CurrentUser;
import com.baseras.portal.common.IdGen;
import com.baseras.portal.dto.MarksDtos.*;
import com.baseras.portal.entity.*;
import com.baseras.portal.repository.*;
import com.baseras.portal.service.AuditService;
import com.baseras.portal.service.NotificationService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.time.OffsetDateTime;
import java.util.*;

@RestController
@RequestMapping("/api/v1")
public class MarksController {

    private final MarkRepository marks;
    private final MarksAuditRepository marksAudit;
    private final ResultPublicationRepository pubs;
    private final StudentRepository students;
    private final TeacherRepository teachers;
    private final SubjectRepository subjects;
    private final SchoolClassRepository classes;
    private final ExamTypeRepository examTypes;
    private final UserRepository users;
    private final CurrentUser current;
    private final AuditService audit;
    private final NotificationService notify;

    public MarksController(MarkRepository marks, MarksAuditRepository marksAudit,
                           ResultPublicationRepository pubs, StudentRepository students,
                           TeacherRepository teachers, SubjectRepository subjects,
                           SchoolClassRepository classes, ExamTypeRepository examTypes,
                           UserRepository users, CurrentUser current,
                           AuditService audit, NotificationService notify) {
        this.marks = marks; this.marksAudit = marksAudit; this.pubs = pubs;
        this.students = students; this.teachers = teachers; this.subjects = subjects;
        this.classes = classes; this.examTypes = examTypes; this.users = users;
        this.current = current; this.audit = audit; this.notify = notify;
    }

    @GetMapping("/marks")
    public List<Mark> list(@RequestParam(required = false) String classId,
                           @RequestParam(required = false) String subjectId,
                           @RequestParam(required = false) String examTypeId,
                           @RequestParam(required = false) String studentId,
                           @RequestParam(defaultValue = "sess_2025_26") String sessionId) {
        User u = current.require();
        List<String> studentIds;
        if (u.getRole() == Role.STUDENT) {
            Student me = students.findByUserId(u.getId())
                    .orElseThrow(() -> new AppExceptions.NotFoundException("Student profile missing"));
            studentIds = List.of(me.getId());
        } else if (classId != null) {
            studentIds = students.findByClassId(classId).stream().map(Student::getId).toList();
        } else if (studentId != null) {
            studentIds = List.of(studentId);
        } else {
            studentIds = students.findAll().stream().map(Student::getId).toList();
        }
        if (studentIds.isEmpty()) return List.of();

        List<Mark> result;
        if (subjectId != null && examTypeId != null) {
            result = marks.findByStudentIdInAndSubjectIdAndExamTypeIdAndSessionId(studentIds, subjectId, examTypeId, sessionId);
        } else if (examTypeId != null) {
            result = marks.findByStudentIdInAndExamTypeIdAndSessionId(studentIds, examTypeId, sessionId);
        } else {
            result = marks.findByStudentIdInAndSessionId(studentIds, sessionId);
        }
        if (u.getRole() == Role.STUDENT) {
            return result.stream().filter(m -> "PUBLISHED".equals(m.getStatus())).toList();
        }
        return result;
    }

    @PutMapping("/marks/bulk")
    @PreAuthorize("hasRole('TEACHER')")
    @Transactional
    public BulkResponse upsertBulk(@RequestBody BulkRequest req) {
        User u = current.require();
        Teacher teacher = teachers.findByUserId(u.getId())
                .orElseThrow(() -> new AppExceptions.ForbiddenException("Teacher profile missing"));
        OffsetDateTime now = OffsetDateTime.now();
        int count = 0;
        for (EntryUpsert e : req.entries()) {
            var existing = marks.findByStudentIdAndSubjectIdAndExamTypeIdAndSessionId(
                    e.studentId(), req.subjectId(), req.examTypeId(), req.sessionId());
            if (existing.isPresent()) {
                Mark m = existing.get();
                if ("PUBLISHED".equals(m.getStatus())) continue;
                Double oldMarks = m.getMarksObtained();
                m.setMarksObtained(e.marksObtained());
                m.setMaxMarks(e.maxMarks() == null ? m.getMaxMarks() : e.maxMarks());
                m.setVersion(m.getVersion() + 1);
                marks.save(m);
                marksAudit.save(MarksAuditEntry.builder().id(IdGen.of("ma"))
                        .marksId(m.getId()).action("UPDATE")
                        .oldMarks(oldMarks).newMarks(m.getMarksObtained())
                        .oldStatus(m.getStatus()).newStatus(m.getStatus())
                        .performedBy(u.getId()).performedByName(u.getFullName())
                        .performedAt(now).build());
            } else {
                Mark m = Mark.builder().id(IdGen.of("mk"))
                        .studentId(e.studentId()).subjectId(req.subjectId())
                        .examTypeId(req.examTypeId()).sessionId(req.sessionId())
                        .marksObtained(e.marksObtained()).maxMarks(e.maxMarks() == null ? 100 : e.maxMarks())
                        .status("DRAFT").enteredBy(teacher.getId()).version(1).build();
                marks.save(m);
                marksAudit.save(MarksAuditEntry.builder().id(IdGen.of("ma"))
                        .marksId(m.getId()).action("CREATE")
                        .oldMarks(null).newMarks(m.getMarksObtained())
                        .oldStatus(null).newStatus("DRAFT")
                        .performedBy(u.getId()).performedByName(u.getFullName())
                        .performedAt(now).build());
            }
            count++;
        }
        audit.log(u.getId(), "UPDATE_MARKS_BULK", "Marks",
                req.classId() + "-" + req.subjectId() + "-" + req.examTypeId(),
                Map.of("count", count));
        return new BulkResponse(true, count);
    }

    @PostMapping("/marks/submit")
    @PreAuthorize("hasRole('TEACHER')")
    @Transactional
    public SubmitResponse submit(@RequestBody SubmitRequest req) {
        User u = current.require();
        OffsetDateTime now = OffsetDateTime.now();
        List<String> classStudentIds = students.findByClassId(req.classId()).stream().map(Student::getId).toList();
        var classMarksForExam = marks.findByStudentIdInAndExamTypeIdAndSessionId(classStudentIds, req.examTypeId(), req.sessionId());
        int count = 0;
        for (Mark m : classMarksForExam) {
            if (req.subjectId().equals(m.getSubjectId()) && "DRAFT".equals(m.getStatus())) {
                m.setStatus("SUBMITTED");
                m.setSubmittedAt(now);
                marks.save(m);
                count++;
            }
        }

        ResultPublication rp = pubs.findByClassIdAndExamTypeIdAndSessionId(req.classId(), req.examTypeId(), req.sessionId())
                .orElseGet(() -> pubs.save(ResultPublication.builder().id(IdGen.of("rp"))
                        .classId(req.classId()).examTypeId(req.examTypeId()).sessionId(req.sessionId())
                        .status("PENDING").totalSubjects((int) subjects.count())
                        .submittedSubjects(0).build()));
        Set<String> submittedSubjectIds = new HashSet<>();
        for (Mark m : classMarksForExam) {
            if ("SUBMITTED".equals(m.getStatus()) || "PUBLISHED".equals(m.getStatus())) {
                submittedSubjectIds.add(m.getSubjectId());
            }
        }
        rp.setSubmittedSubjects(submittedSubjectIds.size());
        rp.setStatus(rp.getSubmittedSubjects() >= rp.getTotalSubjects() ? "READY" : "PENDING");
        pubs.save(rp);

        audit.log(u.getId(), "SUBMIT_MARKS", "Marks",
                req.classId() + "-" + req.subjectId() + "-" + req.examTypeId(),
                Map.of("count", count));
        if ("READY".equals(rp.getStatus())) {
            users.findByRole(Role.PRINCIPAL).forEach(p -> {
                SchoolClass cls = classes.findById(req.classId()).orElse(null);
                ExamType ex = examTypes.findById(req.examTypeId()).orElse(null);
                String label = (cls == null ? req.classId() : cls.getName() + "-" + cls.getSection())
                        + " " + (ex == null ? req.examTypeId() : ex.getName());
                notify.notify(p.getId(), "RESULT_READY",
                        "Result ready: " + label,
                        "All teachers have submitted marks. Ready to publish.",
                        "/portal/results");
            });
        }
        return new SubmitResponse(true, count, rp.getStatus());
    }
}
