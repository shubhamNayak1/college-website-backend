package com.baseras.portal.repository;

import com.baseras.portal.entity.InfrastructureImage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface InfrastructureImageRepository extends JpaRepository<InfrastructureImage, String> {
    List<InfrastructureImage> findAllByOrderByDisplayOrderAsc();
}
