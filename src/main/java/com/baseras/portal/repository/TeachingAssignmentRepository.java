package com.baseras.portal.repository;

import com.baseras.portal.entity.TeachingAssignment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TeachingAssignmentRepository extends JpaRepository<TeachingAssignment, String> {
    List<TeachingAssignment> findByTeacherId(String teacherId);
}
