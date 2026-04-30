package com.baseras.portal.repository;

import com.baseras.portal.entity.Attendance;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface AttendanceRepository extends JpaRepository<Attendance, String> {
    Optional<Attendance> findByStudentIdAndDate(String studentId, LocalDate date);
    List<Attendance> findByStudentIdInAndDate(List<String> studentIds, LocalDate date);
    List<Attendance> findByStudentIdInOrderByDateDesc(List<String> studentIds);
    List<Attendance> findByStudentIdOrderByDateDesc(String studentId);
    List<Attendance> findByStudentIdInAndDateBetween(List<String> studentIds, LocalDate from, LocalDate to);
}
