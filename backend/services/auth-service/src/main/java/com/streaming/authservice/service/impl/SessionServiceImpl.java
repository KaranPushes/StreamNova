package com.streaming.authservice.service.impl;

import com.streaming.authservice.dto.response.SessionSummary;
import com.streaming.authservice.dto.session.SessionInfo;
import com.streaming.authservice.entity.UserSession;
import com.streaming.authservice.exception.SessionNotFoundException;
import com.streaming.authservice.repository.UserSessionRepository;
import com.streaming.authservice.security.JwtUserPrincipal;
import com.streaming.authservice.service.SessionService;
import com.streaming.authservice.util.UserAgentParser;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SessionServiceImpl implements SessionService {

    private final UserAgentParser userAgentParser;
    private final UserSessionRepository userSessionRepository;

    @Override
    public SessionInfo extractSessionInfo(HttpServletRequest request) {

        String userAgent = request.getHeader("User-Agent");

        String browser =
                userAgentParser.detectBrowser(userAgent);

        String operatingSystem =
                userAgentParser.detectOperatingSystem(userAgent);

        return SessionInfo.builder()
                .browser(browser)
                .operatingSystem(operatingSystem)
                .deviceName(
                        userAgentParser.buildDeviceName(
                                browser,
                                operatingSystem
                        )
                )
                .userAgent(userAgent)
                .ipAddress(normalizeIpAddress(request.getRemoteAddr()))
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public List<SessionSummary> getSessions(
            JwtUserPrincipal principal) {

        return userSessionRepository
                .findByUserIdAndRevokedFalseOrderByLastAccessedAtDesc(
                        principal.userId()
                )
                .stream()
                .map(session -> SessionSummary.builder()
                        .id(session.getId())
                        .deviceName(session.getDeviceName())
                        .browser(session.getBrowser())
                        .operatingSystem(session.getOperatingSystem())
                        .ipAddress(session.getIpAddress())
                        .lastAccessedAt(session.getLastAccessedAt())
                        .createdAt(session.getCreatedAt())
                        .current(session.getId().equals(principal.sessionId()))
                        .build())
                .toList();
    }

    private String normalizeIpAddress(String ipAddress) {

        if ("0:0:0:0:0:0:0:1".equals(ipAddress)
                || "::1".equals(ipAddress)) {
            return "127.0.0.1";
        }

        return ipAddress;
    }

    @Override
    @Transactional
    public void revokeSession(
            JwtUserPrincipal principal,
            UUID sessionId) {

        UserSession session = getActiveSession(
                sessionId,
                principal.userId()
        );

        if (!session.isRevoked()) {
            session.setRevoked(true);
        }
    }

    @Override
    @Transactional
    public void revokeOtherSessions(
            JwtUserPrincipal principal) {

        List<UserSession> sessions =
                userSessionRepository.findByUserIdAndRevokedFalse(
                        principal.userId()
                );

        for (UserSession session : sessions) {

            if (!session.getId().equals(principal.sessionId())) {
                session.setRevoked(true);
            }
        }
    }

    private UserSession getActiveSession(
            UUID sessionId,
            UUID userId) {

        return userSessionRepository
                .findByIdAndUserId(sessionId, userId)
                .orElseThrow(() ->
                        new SessionNotFoundException(
                                "Session not found."
                        ));
    }

    @Override
    @Transactional
    public void revokeAllSessions(JwtUserPrincipal principal) {

        List<UserSession> sessions =
                userSessionRepository.findByUserIdAndRevokedFalse(
                        principal.userId()
                );

        for (UserSession session : sessions) {
            session.setRevoked(true);
        }
    }
}