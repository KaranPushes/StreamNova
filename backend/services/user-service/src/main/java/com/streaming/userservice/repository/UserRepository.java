package com.streaming.userservice.repository;

import com.streaming.userservice.entity.User;
import com.streaming.userservice.entity.UserStatus;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {

    Optional<User> findByUsername(String username);

    Optional<User> findByEmail(String email);

    boolean existsByEmailIgnoreCase(String email);

    boolean existsByUsernameIgnoreCase(String username);

    Optional<User> findByEmailIgnoreCase(String email);

    Optional<User> findByUsernameIgnoreCase(String username);

    Optional<User> findByEmailAndStatus(String email, UserStatus status);

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);

    Optional<User> findByIdAndStatus(UUID id, UserStatus status);

    Optional<User> findByUsernameAndStatus(String username, UserStatus status);

    @Query("""
    SELECT u
    FROM User u
    WHERE u.status = :status
      AND (
            LOWER(u.username) LIKE LOWER(CONCAT('%', :query, '%'))
         OR LOWER(u.displayName) LIKE LOWER(CONCAT('%', :query, '%'))
      )
    ORDER BY u.username
    """)
    List<User> searchUsers(
            @Param("query") String query,
            @Param("status") UserStatus status,
            Pageable pageable
    );
}