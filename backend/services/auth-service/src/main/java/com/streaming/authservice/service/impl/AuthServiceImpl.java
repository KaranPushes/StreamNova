package com.streaming.authservice.service.impl;

import com.streaming.authservice.client.UserServiceClient;
import com.streaming.authservice.config.AccountLockProperties;
import com.streaming.authservice.config.EmailVerificationProperties;
import com.streaming.authservice.config.JwtProperties;
import com.streaming.authservice.config.PasswordResetProperties;
import com.streaming.authservice.dto.client.CreateUserRequest;
import com.streaming.authservice.dto.client.UserResponse;
import com.streaming.authservice.dto.request.*;
import com.streaming.authservice.dto.response.*;
import com.streaming.authservice.dto.session.SessionInfo;
import com.streaming.authservice.entity.*;
import com.streaming.authservice.exception.*;
import com.streaming.authservice.repository.AuthUserRepository;
import com.streaming.authservice.repository.EmailVerificationTokenRepository;
import com.streaming.authservice.repository.PasswordResetTokenRepository;
import com.streaming.authservice.repository.UserSessionRepository;
import com.streaming.authservice.security.JwtUserPrincipal;
import com.streaming.authservice.security.SecureTokenGenerator;
import com.streaming.authservice.service.*;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class AuthServiceImpl implements AuthService {

    private final PasswordEncoder passwordEncoder;
    private final UserServiceClient userServiceClient;
    private final AuthUserRepository authUserRepository;
    private final TokenService tokenService;
    private final TimeProvider timeProvider;
    private final EmailVerificationTokenRepository emailVerificationTokenRepository;
    private final EmailService emailService;
    private final SecureTokenGenerator secureTokenGenerator;
    private final EmailVerificationProperties emailVerificationProperties;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final PasswordResetProperties passwordResetProperties;
    private final AccountLockProperties accountLockProperties;
    private final UserSessionRepository userSessionRepository;
    private final JwtProperties jwtProperties;
    private final SessionService sessionService;
    private final TokenHashService tokenHashService;

    @Override
    @Transactional
    public RegisterResponse register(RegisterRequest request) {

        String hashedPassword = passwordEncoder.encode(request.password());

        UserResponse user = userServiceClient.createUser(
                CreateUserRequest.builder()
                        .email(request.email())
                        .username(request.username())
                        .displayName(request.displayName())
                        .build()
        );

        AuthUser authUser = AuthUser.builder()
                .passwordHash(hashedPassword)
                .emailVerified(false)
                .role(Role.USER)
                .build();

        authUser.setId(user.id());

        authUserRepository.save(authUser);

        createAndSendVerificationEmail(
                user.id(),
                user.email()
        );

        return new RegisterResponse(
                user.id(),
                "Registration successful"
        );
    }

    @Override
    @Transactional(noRollbackFor = InvalidCredentialsException.class)
    public LoginResponse login(
            LoginRequest request,
            HttpServletRequest httpRequest) {

        UserResponse user;

        try {
            user = userServiceClient.getUserByEmail(request.email());
        } catch (UserNotFoundException ex) {
            throw new InvalidCredentialsException();
        }

        AuthUser authUser = authUserRepository.findById(user.id())
                .orElseThrow(InvalidCredentialsException::new);

        if (authUser.getAccountLockedUntil() != null) {

            if (authUser.getAccountLockedUntil().isAfter(timeProvider.now())) {
                throw new AccountLockedException();
            }

            resetAccountLock(authUser);

            authUserRepository.save(authUser);
        }

        if (!authUser.isEmailVerified()) {
            throw new EmailNotVerifiedException();
        }

        if (!passwordEncoder.matches(
                request.password(),
                authUser.getPasswordHash())) {

            authUser.setFailedLoginAttempts(
                    authUser.getFailedLoginAttempts() + 1
            );

            if (authUser.getFailedLoginAttempts() >= accountLockProperties.getMaxAttempts()) {

                authUser.setAccountLockedUntil(
                        timeProvider.now()
                                .plus(accountLockProperties.getDuration())
                );
            }

            authUserRepository.save(authUser);

            throw new InvalidCredentialsException();
        }

        resetAccountLock(authUser);

        authUser.setLastLoginAt(timeProvider.now());

        authUserRepository.save(authUser);

        SessionInfo sessionInfo =
                sessionService.extractSessionInfo(httpRequest);

        String rawRefreshToken =
                tokenService.generateRefreshToken();

        String refreshTokenHash =
                tokenHashService.hash(rawRefreshToken);

        UserSession session = UserSession.builder()
                .userId(user.id())
                .refreshTokenHash(refreshTokenHash)
                .expiresAt(
                        timeProvider.now()
                                .plus(jwtProperties.getRefreshTokenExpiration())
                )
                .lastAccessedAt(timeProvider.now())
                .revoked(false)
                .deviceName(sessionInfo.deviceName())
                .browser(sessionInfo.browser())
                .operatingSystem(sessionInfo.operatingSystem())
                .userAgent(sessionInfo.userAgent())
                .ipAddress(sessionInfo.ipAddress())
                .build();

        session.generateIdIfAbsent();

        String accessToken = tokenService.generateAccessToken(
                user,
                authUser.getRole(),
                session.getId()
        );

        userSessionRepository.save(session);

        return tokenService.buildLoginResponse(
                accessToken,
                rawRefreshToken
        );
    }

    @Override
    @Transactional
    public RefreshTokenResponse refreshToken(
            RefreshTokenRequest request) {

        String currentRefreshTokenHash =
                tokenHashService.hash(request.refreshToken());

        UserSession session =
                validateUserSession(currentRefreshTokenHash);

        UserResponse user =
                userServiceClient.getUserById(session.getUserId());

        AuthUser authUser =
                authUserRepository.findById(session.getUserId())
                        .orElseThrow(
                                InvalidRefreshTokenException::new
                        );

        String accessToken =
                tokenService.generateAccessToken(
                        user,
                        authUser.getRole(),
                        session.getId()
                );

        String newRawRefreshToken =
                tokenService.generateRefreshToken();

        String newRefreshTokenHash =
                tokenHashService.hash(newRawRefreshToken);

        session.setRefreshTokenHash(
                newRefreshTokenHash
        );

        session.setLastAccessedAt(
                timeProvider.now()
        );

        session.setExpiresAt(
                timeProvider.now()
                        .plus(
                                jwtProperties
                                        .getRefreshTokenExpiration()
                        )
        );

        userSessionRepository.save(session);

        return tokenService.buildRefreshTokenResponse(
                accessToken,
                newRawRefreshToken
        );
    }

    @Override
    @Transactional
    public void logout(LogoutRequest request) {

        String refreshTokenHash =
                tokenHashService.hash(request.refreshToken());

        UserSession session =
                validateUserSession(refreshTokenHash);

        session.setRevoked(true);

        userSessionRepository.save(session);
    }

    @Override
    @Transactional
    public VerifyEmailResponse verifyEmail(VerifyEmailRequest request) {

        EmailVerificationToken verificationToken =
                emailVerificationTokenRepository
                        .findByToken(request.token())
                        .orElseThrow(InvalidEmailVerificationTokenException::new);

        if (verificationToken.isVerified()) {
            throw new InvalidEmailVerificationTokenException();
        }

        if (verificationToken.getExpiresAt().isBefore(timeProvider.now())) {
            throw new InvalidEmailVerificationTokenException();
        }

        AuthUser authUser = authUserRepository
                .findById(verificationToken.getUserId())
                .orElseThrow(InvalidEmailVerificationTokenException::new);

        authUser.setEmailVerified(true);

        verificationToken.setVerified(true);

        authUserRepository.save(authUser);
        emailVerificationTokenRepository.save(verificationToken);

        return new VerifyEmailResponse(
                "Email verified successfully."
        );
    }

    @Override
    @Transactional
    public ResendVerificationEmailResponse resendVerificationEmail(
            ResendVerificationEmailRequest request) {

        UserResponse user;

        try {
            user = userServiceClient.getUserByEmail(request.email());
        } catch (UserNotFoundException ex) {
            throw new InvalidCredentialsException();
        }

        AuthUser authUser = authUserRepository.findById(user.id())
                .orElseThrow(InvalidCredentialsException::new);

        if (authUser.isEmailVerified()) {
            throw new EmailAlreadyVerifiedException();
        }

        createAndSendVerificationEmail(
                user.id(),
                user.email()
        );

        return new ResendVerificationEmailResponse(
                "Verification email sent successfully."
        );
    }

    @Override
    @Transactional
    public ChangePasswordResponse changePassword(
            JwtUserPrincipal principal,
            ChangePasswordRequest request) {

        AuthUser authUser = authUserRepository.findById(principal.userId())
                .orElseThrow(InvalidCredentialsException::new);

        if (!passwordEncoder.matches(
                request.currentPassword(),
                authUser.getPasswordHash())) {

            throw new IncorrectPasswordException();
        }

        authUser.setPasswordHash(
                passwordEncoder.encode(request.newPassword())
        );

        authUserRepository.save(authUser);

        userSessionRepository.revokeAllActiveSessionsByUserId(
                principal.userId()
        );

        return new ChangePasswordResponse(
                "Password changed successfully."
        );
    }

    @Override
    @Transactional
    public ForgotPasswordResponse forgotPassword(
            ForgotPasswordRequest request) {

        UserResponse user;

        try {
            user = userServiceClient.getUserByEmail(request.email());
        } catch (UserNotFoundException ex) {
            return new ForgotPasswordResponse(
                    "If an account exists, a password reset email has been sent."
            );
        }

        createAndSendPasswordResetToken(
                user.id(),
                user.email()
        );

        return new ForgotPasswordResponse(
                "If an account exists, a password reset email has been sent."
        );
    }

    @Override
    @Transactional
    public ResetPasswordResponse resetPassword(
            ResetPasswordRequest request) {

        PasswordResetToken resetToken = passwordResetTokenRepository
                .findByToken(request.token())
                .orElseThrow(InvalidPasswordResetTokenException::new);

        if (resetToken.isUsed()) {
            throw new InvalidPasswordResetTokenException();
        }

        if (resetToken.getExpiresAt().isBefore(timeProvider.now())) {
            throw new InvalidPasswordResetTokenException();
        }

        AuthUser authUser = authUserRepository
                .findById(resetToken.getUserId())
                .orElseThrow(InvalidPasswordResetTokenException::new);

        authUser.setPasswordHash(
                passwordEncoder.encode(request.newPassword())
        );

        authUserRepository.save(authUser);

        userSessionRepository.revokeAllActiveSessionsByUserId(
                authUser.getId()
        );

        resetToken.setUsed(true);

        passwordResetTokenRepository.save(resetToken);

        return new ResetPasswordResponse(
                "Password reset successfully."
        );
    }

    private UserSession validateUserSession(
            String refreshTokenHash) {

        UserSession session = userSessionRepository
                .findByRefreshTokenHash(refreshTokenHash)
                .orElseThrow(
                        InvalidRefreshTokenException::new
                );

        if (session.isRevoked()) {
            throw new InvalidRefreshTokenException();
        }

        if (!session.getExpiresAt()
                .isAfter(timeProvider.now())) {

            throw new InvalidRefreshTokenException();
        }

        return session;
    }

    private void createAndSendVerificationEmail(
            UUID userId,
            String email) {

        emailVerificationTokenRepository.deleteByUserId(userId);

        String verificationToken = secureTokenGenerator.generate();

        EmailVerificationToken token = EmailVerificationToken.builder()
                .userId(userId)
                .token(verificationToken)
                .expiresAt(
                        timeProvider.now()
                                .plus(emailVerificationProperties.getExpiration())
                )
                .verified(false)
                .build();

        emailVerificationTokenRepository.save(token);

        emailService.sendEmailVerification(
                userId,
                email,
                verificationToken
        );
    }

    private void createAndSendPasswordResetToken(
            UUID userId,
            String email) {

        passwordResetTokenRepository.deleteByUserId(userId);

        String token = secureTokenGenerator.generate();

        PasswordResetToken resetToken = PasswordResetToken.builder()
                .userId(userId)
                .token(token)
                .expiresAt(
                        timeProvider.now()
                                .plus(passwordResetProperties.getExpiration())
                )
                .used(false)
                .build();

        passwordResetTokenRepository.save(resetToken);

        emailService.sendPasswordResetEmail(
                userId,
                email,
                token
        );
    }

    private void resetAccountLock(AuthUser authUser) {
        authUser.setFailedLoginAttempts(0);
        authUser.setAccountLockedUntil(null);
    }
}