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

    // CAST(:p AS String) forces Hibernate to bind null String parameters as
    // VARCHAR (otherwise PostgreSQL infers `bytea` and `lower()` blows up).
    @Query("""
            SELECT s FROM Student s
            WHERE (CAST(:classId AS String) IS NULL OR s.classId = :classId)
              AND (CAST(:q AS String) IS NULL OR LOWER(s.firstName)  LIKE LOWER(CONCAT('%', CAST(:q AS String), '%'))
                                              OR LOWER(s.lastName)   LIKE LOWER(CONCAT('%', CAST(:q AS String), '%'))
                                              OR LOWER(s.rollNumber) LIKE LOWER(CONCAT('%', CAST(:q AS String), '%')))
            ORDER BY s.rollNumber
            """)
    List<Student> search(String classId, String q);
}
