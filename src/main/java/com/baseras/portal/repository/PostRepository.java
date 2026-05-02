package com.baseras.portal.repository;

import com.baseras.portal.entity.Post;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface PostRepository extends JpaRepository<Post, String> {
    // CAST(:p AS String) forces Hibernate to bind null String parameters as
    // VARCHAR (otherwise PostgreSQL infers `bytea` and `lower()` blows up).
    @Query("""
            SELECT p FROM Post p
            WHERE p.status = 'PUBLISHED'
              AND (CAST(:type AS String) IS NULL OR p.type = :type)
              AND (CAST(:q AS String) IS NULL OR LOWER(p.title) LIKE LOWER(CONCAT('%', CAST(:q AS String), '%'))
                                              OR LOWER(p.body)  LIKE LOWER(CONCAT('%', CAST(:q AS String), '%')))
            ORDER BY p.createdAt DESC
            """)
    List<Post> search(String type, String q);

    List<Post> findByStatusOrderByCreatedAtDesc(String status);
}
