package com.baseras.portal.repository;

import com.baseras.portal.entity.StaffMember;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface StaffMemberRepository extends JpaRepository<StaffMember, String> {
    List<StaffMember> findAllByOrderByDisplayOrderAsc();
}
