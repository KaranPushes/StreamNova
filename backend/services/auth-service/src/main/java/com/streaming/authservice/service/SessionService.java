package com.streaming.authservice.service;

import com.streaming.authservice.dto.response.SessionSummary;
import com.streaming.authservice.dto.session.SessionInfo;
import com.streaming.authservice.security.JwtUserPrincipal;
import jakarta.servlet.http.HttpServletRequest;

import java.util.List;
import java.util.UUID;

public interface SessionService {

    List<SessionSummary> getSessions(
            JwtUserPrincipal principal
    );

    SessionInfo extractSessionInfo(HttpServletRequest request);

    void revokeSession(
            JwtUserPrincipal principal,
            UUID sessionId
    );

    void revokeOtherSessions(
            JwtUserPrincipal principal
    );

    void revokeAllSessions(JwtUserPrincipal principal);
}