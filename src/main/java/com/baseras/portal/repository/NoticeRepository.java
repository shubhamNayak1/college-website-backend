package com.baseras.portal.repository;

import com.baseras.portal.entity.Notice;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NoticeRepository extends JpaRepository<Notice, String> {
    List<Notice> findByDeletedAtIsNullOrderByPublishedAtDesc();
    List<Notice> findByAudienceAndDeletedAtIsNullOrderByPublishedAtDesc(String audience);
}
