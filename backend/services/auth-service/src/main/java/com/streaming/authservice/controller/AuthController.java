package com.streaming.authservice.controller;

import com.streaming.authservice.dto.request.*;
import com.streaming.authservice.dto.response.*;
import com.streaming.authservice.security.JwtUserPrincipal;
import com.streaming.authservice.service.AuthService;
import com.streaming.authservice.service.SessionService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final SessionService sessionService;

    @PostMapping("/register")
    public RegisterResponse register(
            @Valid @RequestBody RegisterRequest request) {

        return authService.register(request);
    }

    @PostMapping("/login")
    public LoginResponse login(
            @Valid @RequestBody LoginRequest request,
            HttpServletRequest httpRequest) {

        return authService.login(
                request,
                httpRequest
        );
    }

    @GetMapping("/me")
    public JwtUserPrincipal me(
            Authentication authentication) {

        return (JwtUserPrincipal) authentication.getPrincipal();
    }

    @PostMapping("/refresh")
    public RefreshTokenResponse refreshToken(
            @Valid @RequestBody RefreshTokenRequest request) {

        return authService.refreshToken(request);
    }

    @PostMapping("/logout")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void logout(
            @Valid @RequestBody LogoutRequest request) {

        authService.logout(request);
    }

    @PostMapping("/verify-email")
    public VerifyEmailResponse verifyEmail(
            @Valid @RequestBody VerifyEmailRequest request) {

        return authService.verifyEmail(request);
    }

    @PostMapping("/resend-verification")
    public ResendVerificationEmailResponse resendVerificationEmail(
            @Valid @RequestBody ResendVerificationEmailRequest request) {

        return authService.resendVerificationEmail(request);
    }

    @PostMapping("/change-password")
    public ChangePasswordResponse changePassword(
            Authentication authentication,
            @Valid @RequestBody ChangePasswordRequest request) {

        JwtUserPrincipal principal =
                (JwtUserPrincipal) authentication.getPrincipal();

        return authService.changePassword(
                principal,
                request
        );
    }

    @PostMapping("/forgot-password")
    public ForgotPasswordResponse forgotPassword(
            @Valid @RequestBody ForgotPasswordRequest request) {

        return authService.forgotPassword(request);
    }

    @PostMapping("/reset-password")
    public ResetPasswordResponse resetPassword(
            @Valid @RequestBody ResetPasswordRequest request) {

        return authService.resetPassword(request);
    }

    @GetMapping("/sessions")
    public List<SessionSummary> getSessions(
            @AuthenticationPrincipal JwtUserPrincipal principal) {

        return sessionService.getSessions(principal);
    }

    @DeleteMapping("/sessions/{sessionId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void revokeSession(
            @AuthenticationPrincipal JwtUserPrincipal principal,
            @PathVariable UUID sessionId) {

        sessionService.revokeSession(
                principal,
                sessionId
        );
    }

    @DeleteMapping("/sessions/others")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void revokeOtherSessions(
            @AuthenticationPrincipal JwtUserPrincipal principal) {

        sessionService.revokeOtherSessions(principal);
    }

    @DeleteMapping("/sessions")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void revokeAllSessions(
            @AuthenticationPrincipal JwtUserPrincipal principal) {

        sessionService.revokeAllSessions(principal);
    }
}