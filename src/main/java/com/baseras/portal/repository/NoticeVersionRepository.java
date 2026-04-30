package com.baseras.portal.repository;

import com.baseras.portal.entity.NoticeVersion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NoticeVersionRepository extends JpaRepository<NoticeVersion, String> {
    List<NoticeVersion> findByNoticeIdOrderByVersionDesc(String noticeId);
}
