package com.streaming.authservice.repository;

import com.streaming.authservice.entity.EmailVerificationToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

public interface EmailVerificationTokenRepository
        extends JpaRepository<EmailVerificationToken, UUID> {

    Optional<EmailVerificationToken> findByToken(String token);

    Optional<EmailVerificationToken> findByUserId(UUID userId);

    void deleteByUserId(UUID userId);

    @Modifying
    @Query("""
        DELETE FROM EmailVerificationToken token
        WHERE token.expiresAt < :expiredBefore
           OR (
               token.verified = true
               AND token.updatedAt < :verifiedBefore
           )
        """)
    int deleteExpiredAndOldVerifiedTokens(
            @Param("expiredBefore") Instant expiredBefore,
            @Param("verifiedBefore") Instant verifiedBefore
    );
}