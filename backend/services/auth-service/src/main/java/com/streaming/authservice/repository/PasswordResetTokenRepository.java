package com.streaming.authservice.repository;

import com.streaming.authservice.entity.PasswordResetToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

public interface PasswordResetTokenRepository
        extends JpaRepository<PasswordResetToken, UUID> {

    Optional<PasswordResetToken> findByToken(String token);

    void deleteByUserId(UUID userId);

    @Modifying
    @Query("""
        DELETE FROM PasswordResetToken token
        WHERE token.expiresAt < :expiredBefore
           OR (
               token.used = true
               AND token.updatedAt < :usedBefore
           )
        """)
    int deleteExpiredAndOldUsedTokens(
            @Param("expiredBefore") Instant expiredBefore,
            @Param("usedBefore") Instant usedBefore
    );
}