package com.worknear.api.user;

import com.worknear.api.user.domain.Role;
import com.worknear.api.user.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {
    Optional<User> findByPhone(String phone);

    Optional<User> findByPhoneAndRole(String phone, Role role);

    boolean existsByPhone(String phone);
}
