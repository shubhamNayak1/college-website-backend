package com.baseras.portal.controller;

import com.baseras.portal.entity.*;
import com.baseras.portal.repository.*;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1")
public class LookupController {

    private final SchoolClassRepository classes;
    private final SubjectRepository subjects;
    private final AcademicSessionRepository sessions;
    private final ExamTypeRepository examTypes;

    public LookupController(SchoolClassRepository classes, SubjectRepository subjects,
                            AcademicSessionRepository sessions, ExamTypeRepository examTypes) {
        this.classes = classes; this.subjects = subjects;
        this.sessions = sessions; this.examTypes = examTypes;
    }

    @GetMapping("/classes") public List<SchoolClass> classes() { return classes.findAll(); }
    @GetMapping("/subjects") public List<Subject> subjects() { return subjects.findAll(); }
    @GetMapping("/sessions") public List<AcademicSession> sessions() { return sessions.findAll(); }
    @GetMapping("/exam-types") public List<ExamType> examTypes() { return examTypes.findAll(); }
}
