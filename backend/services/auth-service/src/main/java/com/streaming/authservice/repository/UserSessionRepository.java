package com.streaming.authservice.repository;

import com.streaming.authservice.entity.UserSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserSessionRepository
        extends JpaRepository<UserSession, UUID> {

    Optional<UserSession> findByRefreshTokenHash(
            String refreshTokenHash
    );

    List<UserSession> findByUserId(UUID userId);

    @Modifying
    @Query("""
        UPDATE UserSession session
        SET session.revoked = true
        WHERE session.userId = :userId
          AND session.revoked = false
        """)
    int revokeAllActiveSessionsByUserId(
            @Param("userId") UUID userId
    );

    @Modifying
    @Query("""
        DELETE FROM UserSession session
        WHERE session.expiresAt < :expiredBefore
           OR (
               session.revoked = true
               AND session.updatedAt < :revokedBefore
           )
        """)
    int deleteExpiredAndOldRevokedSessions(
            @Param("expiredBefore") Instant expiredBefore,
            @Param("revokedBefore") Instant revokedBefore
    );

    Optional<UserSession> findByIdAndRevokedFalse(UUID id);

    List<UserSession> findByUserIdAndRevokedFalseOrderByLastAccessedAtDesc(
            UUID userId
    );

    Optional<UserSession> findByIdAndUserId(
            UUID id,
            UUID userId
    );

    List<UserSession> findByUserIdAndRevokedFalse(
            UUID userId
    );
}