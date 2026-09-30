package com.ecommerce.project.security.repository;

import com.ecommerce.project.model.AppRole;
import com.ecommerce.project.model.Role;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {
    Optional<Role> findByName(AppRole appRole);
}
