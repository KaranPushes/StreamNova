package com.streaming.authservice.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.streaming.authservice.constants.SecurityConstants;
import com.streaming.authservice.service.SessionValidationService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Instant;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final SessionValidationService sessionValidationService;
    private final ObjectMapper objectMapper;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        String authHeader =
                request.getHeader(HttpHeaders.AUTHORIZATION);

        if (authHeader == null ||
                !authHeader.startsWith(SecurityConstants.TOKEN_PREFIX)) {

            filterChain.doFilter(request, response);
            return;
        }

        String token = authHeader.substring(
                SecurityConstants.TOKEN_PREFIX.length()
        );

        if (!jwtService.isTokenValid(token)) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        JwtUserPrincipal principal =
                jwtService.extractPrincipal(token);

        try {
            if (!sessionValidationService.isSessionValid(
                    principal.sessionId())) {

                filterChain.doFilter(request, response);
                return;
            }
        } catch (DataAccessException exception) {
            writeServiceUnavailableResponse(response);
            return;
        }

        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(
                        principal,
                        null,
                        principal.getAuthorities()
                );

        if (SecurityContextHolder.getContext()
                .getAuthentication() == null) {

            SecurityContextHolder.getContext()
                    .setAuthentication(authentication);
        }

        filterChain.doFilter(request, response);
    }

    private void writeServiceUnavailableResponse(
            HttpServletResponse response
    ) throws IOException {

        response.setStatus(
                HttpServletResponse.SC_SERVICE_UNAVAILABLE
        );

        response.setContentType(
                MediaType.APPLICATION_JSON_VALUE
        );

        Map<String, Object> body = Map.of(
                "timestamp", Instant.now().toString(),
                "status", HttpServletResponse.SC_SERVICE_UNAVAILABLE,
                "error", "Service Unavailable",
                "message", "Authentication service is temporarily unavailable."
        );

        objectMapper.writeValue(
                response.getOutputStream(),
                body
        );
    }
}