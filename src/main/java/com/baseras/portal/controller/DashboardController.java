package com.baseras.portal.controller;

import com.baseras.portal.common.AppExceptions;
import com.baseras.portal.common.CurrentUser;
import com.baseras.portal.dto.DashboardDtos.*;
import com.baseras.portal.entity.*;
import com.baseras.portal.repository.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;

@RestController
@RequestMapping("/api/v1/dashboard")
public class DashboardController {

    private final StudentRepository students;
    private final TeacherRepository teachers;
    private final SchoolClassRepository classes;
    private final NoticeRepository notices;
    private final ApplicationRepository applications;
    private final ResultPublicationRepository pubs;
    private final MarkRepository marks;
    private final ExamTypeRepository examTypes;
    private final AttendanceRepository attendance;
    private final StudentFeeRepository studentFees;
    private final AssessmentRepository assessments;
    private final TimetableEntryRepository tt;
    private final TeachingAssignmentRepository assignments;
    private final CurrentUser current;

    public DashboardController(StudentRepository students, TeacherRepository teachers,
                               SchoolClassRepository classes, NoticeRepository notices,
                               ApplicationRepository applications, ResultPublicationRepository pubs,
                               MarkRepository marks, ExamTypeRepository examTypes,
                               AttendanceRepository attendance, StudentFeeRepository studentFees,
                               AssessmentRepository assessments, TimetableEntryRepository tt,
                               TeachingAssignmentRepository assignments, CurrentUser current) {
        this.students = students; this.teachers = teachers; this.classes = classes;
        this.notices = notices; this.applications = applications; this.pubs = pubs;
        this.marks = marks; this.examTypes = examTypes; this.attendance = attendance;
        this.studentFees = studentFees; this.assessments = assessments; this.tt = tt;
        this.assignments = assignments; this.current = current;
    }

    @GetMapping("/student")
    @PreAuthorize("hasRole('STUDENT')")
    public StudentDashboard student() {
        User u = current.require();
        Student me = students.findByUserId(u.getId())
                .orElseThrow(() -> new AppExceptions.NotFoundException("Student profile missing"));
        SchoolClass cls = classes.findById(me.getClassId()).orElse(null);

        var att = attendance.findByStudentIdOrderByDateDesc(me.getId());
        long present = att.stream().filter(a -> "PRESENT".equals(a.getStatus()) || "LATE".equals(a.getStatus())).count();
        double pct = att.isEmpty() ? 0 : Math.round((double) present / att.size() * 1000.0) / 10.0;

        StudentFee fee = studentFees.findByStudentIdAndSessionId(me.getId(), "sess_2025_26").orElse(null);

        var publishedPubs = pubs.findAll().stream()
                .filter(p -> p.getClassId().equals(me.getClassId()) && "PUBLISHED".equals(p.getStatus()))
                .sorted(Comparator.comparing(ResultPublication::getPublishedAt, Comparator.nullsLast(Comparator.reverseOrder())))
                .toList();
        StudentDashboard.LatestResult latest = null;
        if (!publishedPubs.isEmpty()) {
            ResultPublication rp = publishedPubs.get(0);
            var ms = marks.findByStudentIdInAndExamTypeIdAndSessionId(List.of(me.getId()), rp.getExamTypeId(), rp.getSessionId())
                    .stream().filter(m -> "PUBLISHED".equals(m.getStatus())).toList();
            double total = ms.stream().mapToDouble(m -> m.getMarksObtained() == null ? 0 : m.getMarksObtained()).sum();
            double max = ms.stream().mapToDouble(Mark::getMaxMarks).sum();
            ExamType ex = examTypes.findById(rp.getExamTypeId()).orElse(null);
            latest = new StudentDashboard.LatestResult(
                    ex == null ? rp.getExamTypeId() : ex.getName(),
                    max > 0 ? Math.round(total / max * 10000.0) / 100.0 : 0);
        }

        int dow = LocalDate.now().getDayOfWeek().getValue(); // 1=Mon..7=Sun
        var today = tt.findByClassIdAndSessionIdAndDayOfWeek(me.getClassId(), "sess_2025_26", dow);

        var upcoming = assessments.findByClassIdAndDueDateGreaterThanEqualAndDeletedAtIsNullOrderByDueDateAsc(
                me.getClassId(), LocalDate.now()).stream().limit(3).toList();

        long noticesCount = notices.findByDeletedAtIsNullOrderByPublishedAtDesc().stream()
                .filter(n -> "ALL_PUBLIC".equals(n.getAudience()) || "STUDENTS".equals(n.getAudience())).count();

        return new StudentDashboard(me, cls, pct, fee, latest, today, upcoming, noticesCount);
    }

    @GetMapping("/teacher")
    @PreAuthorize("hasRole('TEACHER')")
    public TeacherDashboard teacher() {
        User u = current.require();
        Teacher t = teachers.findByUserId(u.getId())
                .orElseThrow(() -> new AppExceptions.NotFoundException("Teacher profile missing"));
        var ta = assignments.findByTeacherId(t.getId());
        long classesCount = ta.stream().map(TeachingAssignment::getClassId).distinct().count();
        long subjectsCount = ta.stream().map(TeachingAssignment::getSubjectId).distinct().count();
        long assessmentsCount = assessments.countByTeacherIdAndDeletedAtIsNull(t.getId());
        long pendingMarks = marks.findByEnteredByAndStatus(t.getId(), "DRAFT").size();
        return new TeacherDashboard(t, (int) classesCount, (int) subjectsCount, assessmentsCount, pendingMarks);
    }

    @GetMapping("/admin")
    @PreAuthorize("hasRole('ADMIN')")
    public AdminDashboard admin() {
        FeeStats fs = computeFeeStats();
        long noticesActive = notices.findByDeletedAtIsNullOrderByPublishedAtDesc().size();
        return new AdminDashboard(students.count(), teachers.count(), classes.count(),
                noticesActive, fs, applications.countByStatus("NEW"));
    }

    @GetMapping("/principal")
    @PreAuthorize("hasRole('PRINCIPAL')")
    public PrincipalDashboard principal() {
        FeeStats fs = computeFeeStats();
        long pending = pubs.countByStatus("READY");
        var classList = classes.findAll().stream().limit(8).toList();
        List<ClassAttendancePoint> caps = classList.stream().map(c -> {
            var ids = students.findByClassId(c.getId()).stream().map(Student::getId).toList();
            if (ids.isEmpty()) return new ClassAttendancePoint(c.getId(), c.getName() + "-" + c.getSection(), 0);
            var att = attendance.findByStudentIdInOrderByDateDesc(ids);
            long present = att.stream().filter(a -> "PRESENT".equals(a.getStatus()) || "LATE".equals(a.getStatus())).count();
            double pct = att.isEmpty() ? 0 : Math.round((double) present / att.size() * 1000.0) / 10.0;
            return new ClassAttendancePoint(c.getId(), c.getName() + "-" + c.getSection(), pct);
        }).toList();
        return new PrincipalDashboard(students.count(), teachers.count(), classes.count(),
                pending, fs, caps);
    }

    private FeeStats computeFeeStats() {
        var all = studentFees.findAll();
        BigDecimal due = all.stream().map(StudentFee::getTotalDue).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal paid = all.stream().map(StudentFee::getTotalPaid).reduce(BigDecimal.ZERO, BigDecimal::add);
        double pct = due.signum() == 0 ? 0
                : paid.multiply(BigDecimal.valueOf(100)).divide(due, 1, java.math.RoundingMode.HALF_UP).doubleValue();
        return new FeeStats(due, paid, pct);
    }
}
