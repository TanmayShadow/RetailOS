package com.example.RetailOS.user.repository;

import com.example.RetailOS.platform.security.permission.Role;
import com.example.RetailOS.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {
    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);

    List<User> findAllByShopIdOrderByFirstNameAsc(UUID shopId);
    Optional<User> findByIdAndShopId(UUID id, UUID shopId);

    /** Backs the sole-owner guard on role changes/removal. */
    long countByShopIdAndRole(UUID shopId, Role role);
}
