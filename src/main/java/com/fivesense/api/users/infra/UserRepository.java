package com.fivesense.api.users.infra;

import com.fivesense.api.users.domain.*;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.*;

public interface UserRepository extends JpaRepository<AppUser, UUID> {
    Optional<AppUser> findByEmailIgnoreCase(String email);
    boolean existsByEmailIgnoreCase(String email);
    Page<AppUser> findByRoleIn(Collection<UserRole> roles, Pageable pageable);
    List<AppUser> findByRoleInAndStatus(Collection<UserRole> roles, UserStatus status);
    List<AppUser> findByRoleIn(Collection<UserRole> roles);
    boolean existsByRole(UserRole role);
}
