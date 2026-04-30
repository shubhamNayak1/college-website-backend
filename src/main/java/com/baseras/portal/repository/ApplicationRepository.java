package com.baseras.portal.repository;

import com.baseras.portal.entity.Application;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ApplicationRepository extends JpaRepository<Application, String> {
    long countByStatus(String status);
}
