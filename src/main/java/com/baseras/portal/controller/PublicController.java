package com.baseras.portal.controller;

import com.baseras.portal.common.AppExceptions;
import com.baseras.portal.common.IdGen;
import com.baseras.portal.dto.PublicDtos.*;
import com.baseras.portal.entity.*;
import com.baseras.portal.repository.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.Comparator;
import java.util.List;

@RestController
@RequestMapping("/api/v1/public")
public class PublicController {

    private final NoticeRepository notices;
    private final PostRepository posts;
    private final AlumniRepository alumni;
    private final StudentRepository students;
    private final TeacherRepository teachers;
    private final SchoolClassRepository classes;
    private final ExamTypeRepository examTypes;
    private final ResultPublicationRepository pubs;
    private final MarkRepository marks;
    private final ApplicationRepository applications;
    private final StaffMemberRepository staff;
    private final InfrastructureImageRepository infra;
    private final PrincipalMessageRepository pmsg;
    private final LoginAttemptRepository attempts;
    private final int rateLimitAttempts;
    private final int rateLimitWindowMinutes;

    public PublicController(NoticeRepository notices, PostRepository posts, AlumniRepository alumni,
                            StudentRepository students, TeacherRepository teachers,
                            SchoolClassRepository classes, ExamTypeRepository examTypes,
                            ResultPublicationRepository pubs, MarkRepository marks,
                            ApplicationRepository applications,
                            StaffMemberRepository staff, InfrastructureImageRepository infra,
                            PrincipalMessageRepository pmsg, LoginAttemptRepository attempts,
                            @Value("${baseras.result-check.rate-limit-attempts}") int rla,
                            @Value("${baseras.result-check.rate-limit-window-minutes}") int rlw) {
        this.notices = notices; this.posts = posts; this.alumni = alumni;
        this.students = students; this.teachers = teachers; this.classes = classes;
        this.examTypes = examTypes; this.pubs = pubs; this.marks = marks;
        this.applications = applications; this.staff = staff; this.infra = infra;
        this.pmsg = pmsg; this.attempts = attempts;
        this.rateLimitAttempts = rla; this.rateLimitWindowMinutes = rlw;
    }

    @GetMapping("/home")
    public HomePayload home() {
        var latestNotices = notices.findByAudienceAndDeletedAtIsNullOrderByPublishedAtDesc("ALL_PUBLIC")
                .stream().limit(5).toList();
        var latestPosts = posts.findByStatusOrderByCreatedAtDesc("PUBLISHED")
                .stream().limit(4).toList();
        var stats = new HomePayload.Stats(students.count(), teachers.count(), classes.count(), 1985);
        var alumniList = alumni.findAll().stream().limit(4).toList();
        return new HomePayload(latestNotices, latestPosts, stats, alumniList);
    }

    @GetMapping("/about")
    public AboutPayload about() {
        var pm = pmsg.findFirstByOrderByUpdatedAtDesc()
                .map(p -> new AboutPayload.PrincipalMessageDto(p.getPrincipalName(), p.getPhotoUrl(), p.getMessage()))
                .orElse(new AboutPayload.PrincipalMessageDto("Principal", null, ""));
        return new AboutPayload(pm, staff.findAllByOrderByDisplayOrderAsc(), infra.findAllByOrderByDisplayOrderAsc());
    }

    @GetMapping("/posts")
    public List<Post> posts(@RequestParam(required = false) String type,
                            @RequestParam(required = false) String search) {
        return posts.search(type, search);
    }

    @GetMapping("/posts/{id}")
    public Post post(@PathVariable String id) {
        return posts.findById(id).orElseThrow(() -> new AppExceptions.NotFoundException("Post not found"));
    }

    @GetMapping("/alumni")
    public List<Alumni> alumni() { return alumni.findAll(); }

    @GetMapping("/notices")
    public List<Notice> publicNotices() {
        return notices.findByAudienceAndDeletedAtIsNullOrderByPublishedAtDesc("ALL_PUBLIC");
    }

    @PostMapping("/applications")
    public ResponseEntity<Application> apply(@RequestBody ApplyRequest req) {
        if (req.applicantData() == null
                || isBlank(req.applicantData().name())
                || isBlank(req.applicantData().email())
                || isBlank(req.applicantData().phone())) {
            throw new AppExceptions.ValidationException("Name, email and phone are required");
        }
        if (posts.findById(req.postId()).isEmpty()) throw new AppExceptions.NotFoundException("Post not found");
        var app = Application.builder().id(IdGen.of("app"))
                .postId(req.postId())
                .applicantName(req.applicantData().name())
                .applicantEmail(req.applicantData().email())
                .applicantPhone(req.applicantData().phone())
                .applicantMessage(req.applicantData().message())
                .resumeUrl(req.resumeUrl())
                .status("NEW").submittedAt(OffsetDateTime.now())
                .build();
        applications.save(app);
        return ResponseEntity.status(201).body(app);
    }

    @PostMapping("/result-check")
    public ResultCheckResponse checkResult(@RequestBody ResultCheckRequest req, HttpServletRequest http) {
        String ip = http.getRemoteAddr();
        OffsetDateTime cutoff = OffsetDateTime.now().minusMinutes(rateLimitWindowMinutes);
        long failedRecent = attempts.countByIpAddressAndSuccessFalseAndAttemptedAtAfter(ip, cutoff);
        if (failedRecent >= rateLimitAttempts) {
            throw new AppExceptions.TooManyRequestsException(
                    "Too many failed attempts. Please try again in " + rateLimitWindowMinutes + " minutes.");
        }
        Student student = students.findByRollNumber(req.rollNumber().trim())
                .filter(s -> req.dob() != null && s.getDob().equals(LocalDate.parse(req.dob())))
                .orElse(null);
        if (student == null) {
            attempts.save(LoginAttempt.builder().id(IdGen.of("la"))
                    .ipAddress(ip).username(req.rollNumber()).success(false)
                    .attemptedAt(OffsetDateTime.now()).build());
            throw new AppExceptions.NotFoundException("No matching record found. Check roll number and date of birth.");
        }
        attempts.save(LoginAttempt.builder().id(IdGen.of("la"))
                .ipAddress(ip).username(req.rollNumber()).success(true)
                .attemptedAt(OffsetDateTime.now()).build());

        SchoolClass cls = classes.findById(student.getClassId()).orElse(null);
        var publishedPubs = pubs.findAll().stream()
                .filter(p -> p.getClassId().equals(student.getClassId()) && "PUBLISHED".equals(p.getStatus()))
                .toList();

        List<ResultCheckResponse.ExamResult> results = publishedPubs.stream().map(rp -> {
            var ex = examTypes.findById(rp.getExamTypeId()).orElse(null);
            var ms = marks.findByStudentIdInAndExamTypeIdAndSessionId(
                    List.of(student.getId()), rp.getExamTypeId(), rp.getSessionId()).stream()
                    .filter(m -> "PUBLISHED".equals(m.getStatus())).toList();
            double total = ms.stream().mapToDouble(m -> m.getMarksObtained() == null ? 0 : m.getMarksObtained()).sum();
            double max = ms.stream().mapToDouble(Mark::getMaxMarks).sum();
            double pct = max > 0 ? Math.round(total / max * 10000.0) / 100.0 : 0;
            return new ResultCheckResponse.ExamResult(ex == null ? rp.getExamTypeId() : ex.getName(), total, max, pct);
        }).sorted(Comparator.comparing(ResultCheckResponse.ExamResult::examType)).toList();

        return new ResultCheckResponse(
                new ResultCheckResponse.StudentInfo(
                        student.getFirstName() + " " + student.getLastName(),
                        student.getRollNumber(),
                        cls == null ? "" : cls.getName() + "-" + cls.getSection()),
                results);
    }

    private static boolean isBlank(String s) { return s == null || s.isBlank(); }
}
