package com.baseras.portal.controller;

import com.baseras.portal.common.AppExceptions;
import com.baseras.portal.common.CurrentUser;
import com.baseras.portal.entity.Mark;
import com.baseras.portal.entity.ResultPublication;
import com.baseras.portal.entity.Student;
import com.baseras.portal.entity.User;
import com.baseras.portal.repository.MarkRepository;
import com.baseras.portal.repository.ResultPublicationRepository;
import com.baseras.portal.repository.StudentRepository;
import com.baseras.portal.service.AuditService;
import com.baseras.portal.service.NotificationService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/results")
public class ResultController {

    private final ResultPublicationRepository pubs;
    private final MarkRepository marks;
    private final StudentRepository students;
    private final CurrentUser current;
    private final AuditService audit;
    private final NotificationService notify;

    public ResultController(ResultPublicationRepository pubs, MarkRepository marks,
                            StudentRepository students, CurrentUser current,
                            AuditService audit, NotificationService notify) {
        this.pubs = pubs; this.marks = marks; this.students = students;
        this.current = current; this.audit = audit; this.notify = notify;
    }

    @GetMapping("/publications")
    public List<ResultPublication> list() { return pubs.findAll(); }

    @PostMapping("/{id}/publish")
    @PreAuthorize("hasRole('PRINCIPAL')")
    @Transactional
    public ResultPublication publish(@PathVariable String id) {
        User u = current.require();
        ResultPublication rp = pubs.findById(id).orElseThrow(() -> new AppExceptions.NotFoundException("Publication not found"));
        if (!"READY".equals(rp.getStatus())) {
            throw new AppExceptions.ValidationException("Result not ready to publish");
        }
        OffsetDateTime now = OffsetDateTime.now();
        rp.setStatus("PUBLISHED");
        rp.setPublishedBy(u.getId());
        rp.setPublishedAt(now);
        pubs.save(rp);

        List<Student> classStudents = students.findByClassId(rp.getClassId());
        var ids = classStudents.stream().map(Student::getId).toList();
        var marksForExam = marks.findByStudentIdInAndExamTypeIdAndSessionId(ids, rp.getExamTypeId(), rp.getSessionId());
        for (Mark m : marksForExam) {
            if ("SUBMITTED".equals(m.getStatus())) {
                m.setStatus("PUBLISHED");
                m.setPublishedAt(now);
                marks.save(m);
            }
        }
        audit.log(u.getId(), "PUBLISH_RESULT", "ResultPublication", rp.getId(),
                Map.of("classId", rp.getClassId(), "examTypeId", rp.getExamTypeId()));
        classStudents.forEach(s -> notify.notify(s.getUserId(), "RESULT_PUBLISHED",
                "Your result is published", "Tap to view your marksheet.", "/portal/marks"));
        return rp;
    }
}
