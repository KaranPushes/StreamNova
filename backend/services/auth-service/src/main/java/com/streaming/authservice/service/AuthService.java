package com.streaming.authservice.service;

import com.streaming.authservice.dto.request.*;
import com.streaming.authservice.dto.response.*;
import com.streaming.authservice.security.JwtUserPrincipal;
import jakarta.servlet.http.HttpServletRequest;

public interface AuthService {

    RegisterResponse register(RegisterRequest request);

    LoginResponse login(LoginRequest request,
                        HttpServletRequest httpRequest);

    RefreshTokenResponse refreshToken(RefreshTokenRequest request);

    void logout(LogoutRequest request);

    VerifyEmailResponse verifyEmail(
            VerifyEmailRequest request
    );

    ResendVerificationEmailResponse resendVerificationEmail(
            ResendVerificationEmailRequest request
    );

    ChangePasswordResponse changePassword(
            JwtUserPrincipal principal,
            ChangePasswordRequest request
    );

    ForgotPasswordResponse forgotPassword(
            ForgotPasswordRequest request
    );

    ResetPasswordResponse resetPassword(
            ResetPasswordRequest request
    );

}