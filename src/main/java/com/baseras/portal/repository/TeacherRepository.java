package com.baseras.portal.repository;

import com.baseras.portal.entity.Teacher;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TeacherRepository extends JpaRepository<Teacher, String> {
    Optional<Teacher> findByUserId(String userId);
    Optional<Teacher> findByEmployeeId(String employeeId);
}
