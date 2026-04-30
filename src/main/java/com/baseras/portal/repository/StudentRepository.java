package com.baseras.portal.repository;

import com.baseras.portal.entity.Student;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface StudentRepository extends JpaRepository<Student, String> {
    Optional<Student> findByUserId(String userId);
    Optional<Student> findByRollNumber(String rollNumber);
    List<Student> findByClassId(String classId);

    @Query("""
            SELECT s FROM Student s
            WHERE (:classId IS NULL OR s.classId = :classId)
              AND (:q IS NULL OR LOWER(s.firstName) LIKE LOWER(CONCAT('%', :q, '%'))
                              OR LOWER(s.lastName)  LIKE LOWER(CONCAT('%', :q, '%'))
                              OR LOWER(s.rollNumber) LIKE LOWER(CONCAT('%', :q, '%')))
            ORDER BY s.rollNumber
            """)
    List<Student> search(String classId, String q);
}
