package com.baseras.portal.dto;

import com.baseras.portal.entity.*;

import java.util.List;

public class PublicDtos {

    public record HomePayload(
            List<Notice> latestNotices,
            List<Post> latestPosts,
            Stats stats,
            List<Alumni> alumni
    ) {
        public record Stats(long students, long teachers, long classes, int established) {}
    }

    public record AboutPayload(
            PrincipalMessageDto principalMessage,
            List<StaffMember> staff,
            List<InfrastructureImage> infrastructure
    ) {
        public record PrincipalMessageDto(String name, String photoUrl, String message) {}
    }

    public record ApplyRequest(
            String postId,
            ApplicantData applicantData,
            String resumeUrl
    ) {
        public record ApplicantData(String name, String email, String phone, String message) {}
    }

    public record ResultCheckRequest(String rollNumber, String dob) {}

    public record ResultCheckResponse(StudentInfo student, List<ExamResult> results) {
        public record StudentInfo(String name, String rollNumber, String clazz) {}
        public record ExamResult(String examType, double total, double max, double percentage) {}
    }
}
