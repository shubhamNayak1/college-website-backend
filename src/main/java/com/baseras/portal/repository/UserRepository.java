package com.baseras.portal.repository;

import com.baseras.portal.entity.Role;
import com.baseras.portal.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, String> {
    Optional<User> findByUsernameIgnoreCase(String username);
    List<User> findByRole(Role role);
}
