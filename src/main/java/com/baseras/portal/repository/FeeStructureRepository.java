package com.baseras.portal.repository;

import com.baseras.portal.entity.FeeStructure;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FeeStructureRepository extends JpaRepository<FeeStructure, String> {
    List<FeeStructure> findByClassIdAndSessionId(String classId, String sessionId);
}
