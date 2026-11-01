package com.streaming.authservice.service.impl;

import com.streaming.authservice.entity.UserSession;
import com.streaming.authservice.repository.UserSessionRepository;
import com.streaming.authservice.service.SessionValidationService;
import com.streaming.authservice.service.TimeProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SessionValidationServiceImpl
        implements SessionValidationService {

    private final UserSessionRepository userSessionRepository;
    private final TimeProvider timeProvider;

    @Override
    @Transactional(readOnly = true)
    public boolean isSessionValid(UUID sessionId) {

        if (sessionId == null) {
            return false;
        }

        UserSession session = userSessionRepository
                .findById(sessionId)
                .orElse(null);

        if (session == null) {
            return false;
        }

        if (session.isRevoked()) {
            return false;
        }

        return session.getExpiresAt()
                .isAfter(timeProvider.now());
    }
}