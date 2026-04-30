package com.baseras.portal.repository;

import com.baseras.portal.entity.LoginAttempt;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.OffsetDateTime;

public interface LoginAttemptRepository extends JpaRepository<LoginAttempt, String> {
    long countByIpAddressAndSuccessFalseAndAttemptedAtAfter(String ip, OffsetDateTime cutoff);
}
