// Requirements: User data access - find by email (login), find by role (admin filtering)
package com.samuellaw.quick_job_system.repository;

import com.samuellaw.quick_job_system.entity.User;
import com.samuellaw.quick_job_system.enums.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    List<User> findByRole(Role role);
}
