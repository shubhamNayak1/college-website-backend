package com.baseras.portal.repository;

import com.baseras.portal.entity.Post;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface PostRepository extends JpaRepository<Post, String> {
    @Query("""
            SELECT p FROM Post p
            WHERE p.status = 'PUBLISHED'
              AND (:type IS NULL OR p.type = :type)
              AND (:q IS NULL OR LOWER(p.title) LIKE LOWER(CONCAT('%', :q, '%'))
                              OR LOWER(p.body)  LIKE LOWER(CONCAT('%', :q, '%')))
            ORDER BY p.createdAt DESC
            """)
    List<Post> search(String type, String q);

    List<Post> findByStatusOrderByCreatedAtDesc(String status);
}
