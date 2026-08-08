package com.findseat.repository;

import com.findseat.entity.User;
import com.findseat.enums.Role;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    List<User> findAllByOrderByCreatedAtDesc();

    long countByRole(Role role);
}
