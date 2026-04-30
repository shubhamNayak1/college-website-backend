package com.baseras.portal.repository;

import com.baseras.portal.entity.PrincipalMessage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PrincipalMessageRepository extends JpaRepository<PrincipalMessage, String> {
    Optional<PrincipalMessage> findFirstByOrderByUpdatedAtDesc();
}
