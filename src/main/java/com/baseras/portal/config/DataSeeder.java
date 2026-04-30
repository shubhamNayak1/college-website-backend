package com.baseras.portal.config;

import com.baseras.portal.common.IdGen;
import com.baseras.portal.entity.*;
import com.baseras.portal.repository.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.util.*;

@Configuration
public class DataSeeder {

    @Bean
    @Transactional
    public CommandLineRunner seed(
            UserRepository users,
            StudentRepository students,
            TeacherRepository teachers,
            SchoolClassRepository classes,
            SubjectRepository subjects,
            AcademicSessionRepository sessions,
            ExamTypeRepository examTypes,
            TeachingAssignmentRepository assignments,
            NoticeRepository notices,
            PostRepository posts,
            AlumniRepository alumni,
            MarkRepository marks,
            ResultPublicationRepository pubs,
            AssessmentRepository assessments,
            FeeHeadRepository feeHeads,
            FeeStructureRepository feeStructures,
            StudentFeeRepository studentFees,
            FeePaymentRepository feePayments,
            AttendanceRepository attendance,
            TimetableEntryRepository timetable,
            StaffMemberRepository staff,
            InfrastructureImageRepository infra,
            PrincipalMessageRepository pmsg,
            PasswordEncoder pe
    ) {
        return args -> {
            if (users.count() > 0) return; // idempotent — only seed empty DB

            OffsetDateTime now = OffsetDateTime.now();

            // ---- Sessions ----
            AcademicSession s2025 = AcademicSession.builder().id("sess_2025_26").label("2025-2026")
                    .startDate(LocalDate.parse("2025-04-01")).endDate(LocalDate.parse("2026-03-31")).active(true).build();
            AcademicSession s2024 = AcademicSession.builder().id("sess_2024_25").label("2024-2025")
                    .startDate(LocalDate.parse("2024-04-01")).endDate(LocalDate.parse("2025-03-31")).active(false).build();
            sessions.saveAll(List.of(s2025, s2024));

            // ---- Classes ----
            String[] classNames = {"LKG", "UKG", "1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12"};
            List<SchoolClass> classList = new ArrayList<>();
            for (String n : classNames) {
                classList.add(SchoolClass.builder().id("cls_" + n.toLowerCase() + "_a").name(n).section("A").build());
            }
            classes.saveAll(classList);

            // ---- Subjects ----
            List<Subject> subjList = List.of(
                    Subject.builder().id("subj_eng").name("English").code("ENG").build(),
                    Subject.builder().id("subj_math").name("Mathematics").code("MATH").build(),
                    Subject.builder().id("subj_sci").name("Science").code("SCI").build(),
                    Subject.builder().id("subj_soc").name("Social Studies").code("SOC").build(),
                    Subject.builder().id("subj_hin").name("Hindi").code("HIN").build(),
                    Subject.builder().id("subj_comp").name("Computer Science").code("CS").build()
            );
            subjects.saveAll(subjList);

            // ---- Exam types ----
            examTypes.saveAll(List.of(
                    ExamType.builder().id("ex_ut1").name("Unit Test 1").weight(0.1).sequence(1).build(),
                    ExamType.builder().id("ex_mid").name("Mid Term").weight(0.3).sequence(2).build(),
                    ExamType.builder().id("ex_ut2").name("Unit Test 2").weight(0.1).sequence(3).build(),
                    ExamType.builder().id("ex_final").name("Final").weight(0.5).sequence(4).build()
            ));

            // ---- Admin + Principal ----
            users.save(User.builder().id("u_admin").username("admin").role(Role.ADMIN)
                    .fullName("Ravi Verma").email("admin@baseras.edu.in")
                    .passwordHash(pe.encode("admin123")).mustResetPassword(false).createdAt(now).build());
            users.save(User.builder().id("u_principal").username("principal").role(Role.PRINCIPAL)
                    .fullName("Dr. Anjali Mehta").email("principal@baseras.edu.in")
                    .passwordHash(pe.encode("principal123")).mustResetPassword(false).createdAt(now).build());

            // ---- Teachers ----
            String[][] teacherSeed = {
                    {"Priya", "Sharma", "subj_eng", "M.A. English"},
                    {"Arjun", "Kapoor", "subj_math", "M.Sc. Mathematics"},
                    {"Neha", "Iyer", "subj_sci", "M.Sc. Physics"},
                    {"Rahul", "Nair", "subj_soc", "M.A. History"},
                    {"Kavita", "Joshi", "subj_hin", "M.A. Hindi"},
                    {"Aman", "Khan", "subj_comp", "M.C.A."}
            };
            List<Teacher> teacherList = new ArrayList<>();
            for (int i = 0; i < teacherSeed.length; i++) {
                String first = teacherSeed[i][0], last = teacherSeed[i][1];
                String emp = "EMP00" + (i + 1);
                String uid = "u_t_" + emp;
                String tid = "t_" + emp;
                String email = first.toLowerCase() + "." + last.toLowerCase() + "@baseras.edu.in";
                users.save(User.builder().id(uid).username("T00" + (i + 1)).role(Role.TEACHER)
                        .fullName(first + " " + last).email(email).employeeId(emp)
                        .passwordHash(pe.encode("teacher123")).mustResetPassword(false).createdAt(now).build());
                Teacher t = Teacher.builder().id(tid).userId(uid).employeeId(emp)
                        .firstName(first).lastName(last).email(email).phone("+91 9876" + (10000 + i))
                        .qualification(teacherSeed[i][3]).joinedOn(LocalDate.parse("2020-06-01"))
                        .subjectIds(new ArrayList<>(List.of(teacherSeed[i][2]))).build();
                teacherList.add(t);
            }
            teachers.saveAll(teacherList);

            // Class teachers
            for (int i = 0; i < classList.size(); i++) {
                classList.get(i).setClassTeacherId(teacherList.get(i % teacherList.size()).getId());
            }
            classes.saveAll(classList);

            // Teaching assignments (every teacher to every class for their subject)
            List<TeachingAssignment> taList = new ArrayList<>();
            for (SchoolClass c : classList) {
                for (Teacher t : teacherList) {
                    for (String sid : t.getSubjectIds()) {
                        taList.add(TeachingAssignment.builder().id(IdGen.of("ta"))
                                .teacherId(t.getId()).classId(c.getId()).subjectId(sid).sessionId(s2025.getId()).build());
                    }
                }
            }
            assignments.saveAll(taList);

            // ---- Students: 5 per class × 14 classes = 70 ----
            String[][] studentNames = {
                    {"Aarav", "Kumar", "MALE"}, {"Bhavna", "Singh", "FEMALE"}, {"Chirag", "Patel", "MALE"},
                    {"Diya", "Reddy", "FEMALE"}, {"Eshan", "Mehta", "MALE"}, {"Fatima", "Khan", "FEMALE"},
                    {"Gaurav", "Yadav", "MALE"}, {"Hina", "Das", "FEMALE"}, {"Ishaan", "Gupta", "MALE"},
                    {"Jiya", "Saxena", "FEMALE"}, {"Kabir", "Roy", "MALE"}, {"Lavanya", "Pillai", "FEMALE"}
            };
            int rollSeq = 1;
            List<Student> studentList = new ArrayList<>();
            for (SchoolClass c : classList) {
                int baseYob = "LKG".equals(c.getName()) ? 2020 :
                        "UKG".equals(c.getName()) ? 2019 :
                        2024 - Integer.parseInt(c.getName());
                for (int i = 0; i < 5; i++) {
                    String[] s = studentNames[i % studentNames.length];
                    String roll = "R" + String.format("%03d", rollSeq);
                    rollSeq++;
                    String dob = baseYob + "-0" + (((i % 9) + 1)) + "-1" + (i + 1);
                    String uid = "u_s_" + roll;
                    String sid = "s_" + roll;
                    String pwd = IdGen.defaultStudentPassword(roll, s[0], dob);
                    users.save(User.builder().id(uid).username(roll).role(Role.STUDENT)
                            .fullName(s[0] + " " + s[1]).rollNumber(roll).classId(c.getId())
                            .mustResetPassword(i == 0)
                            .passwordHash(pe.encode(pwd)).createdAt(now).build());
                    studentList.add(Student.builder().id(sid).userId(uid).rollNumber(roll)
                            .admissionNumber("ADM" + String.format("%05d", 2000 + rollSeq))
                            .firstName(s[0]).lastName(s[1])
                            .dob(LocalDate.parse(dob)).gender(s[2]).classId(c.getId())
                            .parentName(s[0] + "'s Parent")
                            .parentPhone("+91 98" + String.format("%08d", rollSeq))
                            .parentEmail("parent." + roll.toLowerCase() + "@example.com")
                            .bloodGroup(new String[]{"A+", "B+", "O+", "AB+"}[i % 4])
                            .build());
                }
            }
            students.saveAll(studentList);

            // ---- Notices ----
            notices.saveAll(List.of(
                    Notice.builder().id("not_1").title("Annual Day Celebrations 2025")
                            .body("We are delighted to announce the Annual Day on **15th December 2025** at the school auditorium. Performances by all classes. Parents are cordially invited.")
                            .audience("ALL_PUBLIC").attachmentUrls(new ArrayList<>())
                            .publishedAt(now.minusDays(40)).createdBy("u_principal").createdByName("Dr. Anjali Mehta").version(1).build(),
                    Notice.builder().id("not_2").title("Mid-Term Examination Schedule")
                            .body("Mid-term exams begin from **5th October 2025**. Detailed timetable attached.")
                            .audience("STUDENTS").attachmentUrls(new ArrayList<>())
                            .publishedAt(now.minusDays(70)).createdBy("u_admin").createdByName("Ravi Verma").version(2).build(),
                    Notice.builder().id("not_3").title("Faculty Development Workshop")
                            .body("All teaching staff are required to attend the FDP on Modern Pedagogy on **30th November 2025**.")
                            .audience("TEACHERS").attachmentUrls(new ArrayList<>())
                            .publishedAt(now.minusDays(50)).createdBy("u_principal").createdByName("Dr. Anjali Mehta").version(1).build(),
                    Notice.builder().id("not_4").title("Diwali Holidays Announcement")
                            .body("School will remain closed from **20th October to 27th October 2025**.")
                            .audience("ALL_PUBLIC").attachmentUrls(new ArrayList<>())
                            .publishedAt(now.minusDays(60)).createdBy("u_admin").createdByName("Ravi Verma").version(1).build()
            ));

            // ---- Posts ----
            posts.saveAll(List.of(
                    Post.builder().id("post_1").type("ADMISSION").title("Admissions Open for 2026-27")
                            .body("Admissions are now open for classes LKG to 11. Limited seats. Apply online before 31st January 2026.")
                            .imageUrl("https://images.unsplash.com/photo-1503676260728-1c00da094a0b?w=800")
                            .applyEnabled(true).status("PUBLISHED").createdAt(now.minusDays(30)).build(),
                    Post.builder().id("post_2").type("JOB_OPENING").title("Mathematics Teacher (Senior Section)")
                            .body("We are seeking a passionate Mathematics teacher for classes 9-12. Minimum M.Sc. + B.Ed.")
                            .applyEnabled(true).status("PUBLISHED").createdAt(now.minusDays(20)).build(),
                    Post.builder().id("post_3").type("EVENT").title("Inter-School Science Fair")
                            .body("Hosting the inter-school science fair on 18th December 2025.")
                            .imageUrl("https://images.unsplash.com/photo-1532094349884-543bc11b234d?w=800")
                            .applyEnabled(true).status("PUBLISHED").createdAt(now.minusDays(15)).build(),
                    Post.builder().id("post_4").type("ALUMNI").title("Baseras Alumni Meet 2026")
                            .body("Calling all Baseras alumni — join us for the Annual Alumni Meet on 12th February 2026.")
                            .applyEnabled(false).status("PUBLISHED").createdAt(now.minusDays(5)).build()
            ));

            alumni.saveAll(List.of(
                    Alumni.builder().id("al_1").name("Sneha Bansal").batchYear(2010).currentRole("Software Engineer at Google").achievements("IIT Delhi · Bay Area, USA").build(),
                    Alumni.builder().id("al_2").name("Rohan Mehta").batchYear(2008).currentRole("Cardiologist, AIIMS").achievements("AIIMS Delhi · Published 12 papers").build(),
                    Alumni.builder().id("al_3").name("Aishwarya Rao").batchYear(2015).currentRole("UX Designer at Adobe").achievements("NID Ahmedabad · Cannes Bronze Lion").build(),
                    Alumni.builder().id("al_4").name("Vikram Singh").batchYear(2005).currentRole("Civil Servant (IAS)").achievements("AIR 47 · Currently DM Pune").build()
            ));

            // ---- Marks for class 10-A ----
            String cls10Id = "cls_10_a";
            List<Student> cls10students = studentList.stream().filter(s -> cls10Id.equals(s.getClassId())).toList();
            String[] examIds = {"ex_ut1", "ex_mid", "ex_ut2", "ex_final"};
            String[] examStatuses = {"PUBLISHED", "PUBLISHED", "SUBMITTED", "DRAFT"};
            Random rnd = new Random(42);
            List<Mark> markList = new ArrayList<>();
            for (Student stu : cls10students) {
                for (Subject sub : subjList) {
                    for (int e = 0; e < examIds.length; e++) {
                        String status = examStatuses[e];
                        Teacher t = teacherList.stream().filter(x -> x.getSubjectIds().contains(sub.getId())).findFirst().orElse(teacherList.get(0));
                        markList.add(Mark.builder().id(IdGen.of("mk")).studentId(stu.getId())
                                .subjectId(sub.getId()).examTypeId(examIds[e]).sessionId(s2025.getId())
                                .marksObtained((double) (60 + rnd.nextInt(35))).maxMarks(100)
                                .status(status).enteredBy(t.getId())
                                .submittedAt(!status.equals("DRAFT") ? now.minusDays(15) : null)
                                .publishedAt("PUBLISHED".equals(status) ? now.minusDays(10) : null)
                                .version(1).build());
                    }
                }
            }
            marks.saveAll(markList);

            pubs.saveAll(List.of(
                    ResultPublication.builder().id("rp_1").classId(cls10Id).examTypeId("ex_ut1").sessionId(s2025.getId())
                            .status("PUBLISHED").publishedBy("u_principal").publishedAt(now.minusDays(80))
                            .totalSubjects(6).submittedSubjects(6).build(),
                    ResultPublication.builder().id("rp_2").classId(cls10Id).examTypeId("ex_mid").sessionId(s2025.getId())
                            .status("PUBLISHED").publishedBy("u_principal").publishedAt(now.minusDays(20))
                            .totalSubjects(6).submittedSubjects(6).build(),
                    ResultPublication.builder().id("rp_3").classId(cls10Id).examTypeId("ex_ut2").sessionId(s2025.getId())
                            .status("READY").totalSubjects(6).submittedSubjects(6).build(),
                    ResultPublication.builder().id("rp_4").classId(cls10Id).examTypeId("ex_final").sessionId(s2025.getId())
                            .status("PENDING").totalSubjects(6).submittedSubjects(0).build()
            ));

            assessments.saveAll(List.of(
                    Assessment.builder().id("as_1").teacherId(teacherList.get(1).getId()).classId(cls10Id)
                            .subjectId("subj_math").sessionId(s2025.getId())
                            .title("Quadratic Equations — Worksheet 3")
                            .description("Solve all 20 problems. Show steps clearly. Submit by Monday.")
                            .dueDate(LocalDate.now().plusDays(7)).createdAt(now).build(),
                    Assessment.builder().id("as_2").teacherId(teacherList.get(0).getId()).classId(cls10Id)
                            .subjectId("subj_eng").sessionId(s2025.getId())
                            .title("Essay: Climate and the Future")
                            .description("500 words. Reference at least two sources. Original work only.")
                            .dueDate(LocalDate.now().plusDays(10)).createdAt(now).build()
            ));

            // ---- Fees ----
            feeHeads.saveAll(List.of(
                    FeeHead.builder().id("fh_tuition").name("Tuition Fee").recurring(true).build(),
                    FeeHead.builder().id("fh_admission").name("Admission Fee").recurring(false).build(),
                    FeeHead.builder().id("fh_lab").name("Lab Fee").recurring(true).build(),
                    FeeHead.builder().id("fh_transport").name("Transport").recurring(true).build()
            ));
            List<FeeStructure> structures = new ArrayList<>();
            for (SchoolClass c : classList) {
                int base;
                if ("LKG".equals(c.getName()) || "UKG".equals(c.getName())) base = 25000;
                else if (Integer.parseInt(c.getName()) <= 5) base = 35000;
                else if (Integer.parseInt(c.getName()) <= 8) base = 45000;
                else if (Integer.parseInt(c.getName()) <= 10) base = 60000;
                else base = 75000;
                structures.add(FeeStructure.builder().id(IdGen.of("fs"))
                        .classId(c.getId()).sessionId(s2025.getId()).feeHeadId("fh_tuition").amount(BigDecimal.valueOf(base)).build());
                structures.add(FeeStructure.builder().id(IdGen.of("fs"))
                        .classId(c.getId()).sessionId(s2025.getId()).feeHeadId("fh_lab").amount(BigDecimal.valueOf(5000)).build());
                structures.add(FeeStructure.builder().id(IdGen.of("fs"))
                        .classId(c.getId()).sessionId(s2025.getId()).feeHeadId("fh_transport").amount(BigDecimal.valueOf(12000)).build());
            }
            feeStructures.saveAll(structures);

            List<StudentFee> sfees = new ArrayList<>();
            List<FeePayment> sfpays = new ArrayList<>();
            for (Student stu : studentList) {
                BigDecimal due = structures.stream()
                        .filter(fs -> fs.getClassId().equals(stu.getClassId()) && fs.getSessionId().equals(s2025.getId()))
                        .map(FeeStructure::getAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
                BigDecimal paid = due.multiply(BigDecimal.valueOf(0.4 + rnd.nextDouble() * 0.6))
                        .setScale(0, java.math.RoundingMode.DOWN);
                String sfId = IdGen.of("sf");
                String status = paid.compareTo(due) >= 0 ? "PAID" : paid.signum() == 0 ? "PENDING" : "PARTIAL";
                sfees.add(StudentFee.builder().id(sfId).studentId(stu.getId()).sessionId(s2025.getId())
                        .totalDue(due).totalPaid(paid).status(status).build());
                if (paid.signum() > 0) {
                    sfpays.add(FeePayment.builder().id(IdGen.of("fp")).studentFeeId(sfId)
                            .amount(paid).paidOn(LocalDate.parse("2025-06-15")).paymentMode("BANK_TRANSFER")
                            .receiptNo("RCP" + rnd.nextInt(100000)).markedBy("u_admin").markedByName("Ravi Verma").build());
                }
            }
            studentFees.saveAll(sfees);
            feePayments.saveAll(sfpays);

            // ---- Attendance: last 30 days for class 10-A ----
            List<Attendance> attList = new ArrayList<>();
            for (int d = 0; d < 30; d++) {
                LocalDate date = LocalDate.now().minusDays(d);
                if (date.getDayOfWeek().getValue() == 7) continue; // skip Sundays
                for (Student stu : cls10students) {
                    double r = rnd.nextDouble();
                    String st = r < 0.85 ? "PRESENT" : r < 0.92 ? "ABSENT" : "LATE";
                    attList.add(Attendance.builder().id(IdGen.of("att"))
                            .studentId(stu.getId()).date(date).status(st)
                            .markedBy(teacherList.get(0).getId()).sessionId(s2025.getId()).build());
                }
            }
            attendance.saveAll(attList);

            // ---- Timetable for class 10-A ----
            String[] subjRotation = {"subj_eng", "subj_math", "subj_sci", "subj_soc", "subj_hin", "subj_comp", "subj_math", "subj_eng"};
            List<TimetableEntry> ttList = new ArrayList<>();
            for (int day = 1; day <= 6; day++) {
                for (int p = 1; p <= 8; p++) {
                    String subjId = subjRotation[(p + day) % subjRotation.length];
                    Teacher t = teacherList.stream().filter(x -> x.getSubjectIds().contains(subjId)).findFirst().orElse(teacherList.get(0));
                    int startHour = 8 + (int) ((p - 1) * 0.75);
                    ttList.add(TimetableEntry.builder().id(IdGen.of("tt"))
                            .classId(cls10Id).sessionId(s2025.getId())
                            .dayOfWeek(day).period(p)
                            .startTime(LocalTime.of(startHour, 0)).endTime(LocalTime.of(startHour, 45))
                            .subjectId(subjId).teacherId(t.getId()).build());
                }
            }
            timetable.saveAll(ttList);

            // ---- Staff + Infra + Principal Message ----
            staff.saveAll(List.of(
                    StaffMember.builder().id("s_1").name("Dr. Anjali Mehta").designation("Principal").department("Administration")
                            .photoUrl("https://i.pravatar.cc/200?img=47").bio("Ph.D. in Education · 25 years experience").displayOrder(1).build(),
                    StaffMember.builder().id("s_2").name("Ravi Verma").designation("Administrator").department("Administration")
                            .photoUrl("https://i.pravatar.cc/200?img=12").displayOrder(2).build(),
                    StaffMember.builder().id("s_3").name("Priya Sharma").designation("Head of English").department("Languages")
                            .photoUrl("https://i.pravatar.cc/200?img=45").displayOrder(3).build(),
                    StaffMember.builder().id("s_4").name("Arjun Kapoor").designation("Head of Mathematics").department("Sciences")
                            .photoUrl("https://i.pravatar.cc/200?img=33").displayOrder(4).build(),
                    StaffMember.builder().id("s_5").name("Neha Iyer").designation("Head of Sciences").department("Sciences")
                            .photoUrl("https://i.pravatar.cc/200?img=44").displayOrder(5).build(),
                    StaffMember.builder().id("s_6").name("Aman Khan").designation("Head of Computer Science").department("Technology")
                            .photoUrl("https://i.pravatar.cc/200?img=15").displayOrder(6).build()
            ));

            infra.saveAll(List.of(
                    InfrastructureImage.builder().id("i_1").category("LAB").imageUrl("https://images.unsplash.com/photo-1581093588401-fbb62a02f120?w=800").caption("Physics Laboratory").displayOrder(1).build(),
                    InfrastructureImage.builder().id("i_2").category("LAB").imageUrl("https://images.unsplash.com/photo-1554475901-4538ddfbccc2?w=800").caption("Chemistry Laboratory").displayOrder(2).build(),
                    InfrastructureImage.builder().id("i_3").category("CLASSROOM").imageUrl("https://images.unsplash.com/photo-1580582932707-520aed937b7b?w=800").caption("Smart Classroom").displayOrder(3).build(),
                    InfrastructureImage.builder().id("i_4").category("LIBRARY").imageUrl("https://images.unsplash.com/photo-1521587760476-6c12a4b040da?w=800").caption("Central Library").displayOrder(4).build(),
                    InfrastructureImage.builder().id("i_5").category("SPORTS").imageUrl("https://images.unsplash.com/photo-1461896836934-ffe607ba8211?w=800").caption("Sports Field").displayOrder(5).build(),
                    InfrastructureImage.builder().id("i_6").category("OTHER").imageUrl("https://images.unsplash.com/photo-1577896851231-70ef18881754?w=800").caption("Auditorium").displayOrder(6).build()
            ));

            pmsg.save(PrincipalMessage.builder().id("pmsg_1").principalName("Dr. Anjali Mehta")
                    .photoUrl("https://i.pravatar.cc/200?img=47")
                    .message("At Baseras School, we believe education is the most powerful tool to shape character and ignite curiosity. Our students are nurtured to think independently, lead with empathy, and pursue excellence with integrity. Welcome to our family.")
                    .updatedAt(now).build());

            System.out.println("[Baseras] Seeded " + users.count() + " users, " + students.count() + " students, " + teachers.count() + " teachers");
        };
    }
}
