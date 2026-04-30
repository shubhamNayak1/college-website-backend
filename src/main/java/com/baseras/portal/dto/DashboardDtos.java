package com.baseras.portal.dto;

import com.baseras.portal.entity.*;

import java.math.BigDecimal;
import java.util.List;

public class DashboardDtos {

    public record StudentDashboard(
            Student student, SchoolClass clazz,
            double attendancePercent,
            StudentFee fee,
            LatestResult latestResult,
            List<TimetableEntry> todayTimetable,
            List<Assessment> upcomingAssessments,
            long noticesCount
    ) {
        public record LatestResult(String examName, double percent) {}
    }

    public record TeacherDashboard(
            Teacher teacher,
            int classesCount, int subjectsCount,
            long assessmentsCount,
            long pendingMarks
    ) {}

    public record FeeStats(BigDecimal totalDue, BigDecimal totalPaid, double percent) {}

    public record AdminDashboard(
            long students, long teachers, long classes,
            long noticesActive,
            FeeStats feeCollection,
            long pendingApplications
    ) {}

    public record ClassAttendancePoint(String classId, String name, double percent) {}

    public record PrincipalDashboard(
            long students, long teachers, long classes,
            long pendingResultPublications,
            FeeStats feeCollection,
            List<ClassAttendancePoint> classWiseAttendance
    ) {}
}
