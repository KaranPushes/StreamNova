package com.streaming.authservice.scheduler;

import com.streaming.authservice.config.CleanupProperties;
import com.streaming.authservice.repository.EmailVerificationTokenRepository;
import com.streaming.authservice.repository.PasswordResetTokenRepository;
import com.streaming.authservice.repository.UserSessionRepository;
import com.streaming.authservice.service.TimeProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Slf4j
@Component
@RequiredArgsConstructor
public class AuthCleanupScheduler {

    private final UserSessionRepository userSessionRepository;
    private final EmailVerificationTokenRepository emailVerificationTokenRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final TimeProvider timeProvider;
    private final CleanupProperties cleanupProperties;

    @Scheduled(cron = "0 0 3 * * *")
    @Transactional
    public void cleanupAuthData() {

        Instant now = timeProvider.now();

        int deletedSessions =
                userSessionRepository.deleteExpiredAndOldRevokedSessions(
                        now,
                        now.minus(cleanupProperties.getRevokedSessionRetention())
                );

        int deletedVerificationTokens =
                emailVerificationTokenRepository
                        .deleteExpiredAndOldVerifiedTokens(
                                now,
                                now.minus(
                                        cleanupProperties
                                                .getVerifiedEmailTokenRetention()
                                )
                        );

        int deletedPasswordResetTokens =
                passwordResetTokenRepository
                        .deleteExpiredAndOldUsedTokens(
                                now,
                                now.minus(
                                        cleanupProperties
                                                .getUsedPasswordResetTokenRetention()
                                )
                        );

        log.info("""
                Auth cleanup completed.
                Sessions deleted: {}
                Verification tokens deleted: {}
                Password reset tokens deleted: {}
                """,
                deletedSessions,
                deletedVerificationTokens,
                deletedPasswordResetTokens
        );
    }
}