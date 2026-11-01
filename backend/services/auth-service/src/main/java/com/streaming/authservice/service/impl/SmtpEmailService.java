package com.streaming.authservice.service.impl;

import com.streaming.authservice.service.EmailService;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SmtpEmailService implements EmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String senderEmail;

    @Value("${app.frontend.base-url}")
    private String frontendBaseUrl;

    @Override
    public void sendEmailVerification(
            UUID userId,
            String email,
            String token
    ) {

        String verificationUrl =
                frontendBaseUrl
                        + "/verify-email?token="
                        + token;

        String subject = "Verify your email address";

        String htmlContent = """
                <!DOCTYPE html>
                <html>
                <body>
                    <h2>Verify your email</h2>

                    <p>
                        Thank you for registering.
                    </p>

                    <p>
                        Click the link below to verify your email address:
                    </p>

                    <p>
                        <a href="%s">Verify email</a>
                    </p>

                    <p>
                        This link will expire after a limited time.
                    </p>

                    <p>
                        If you did not create this account,
                        you can ignore this email.
                    </p>
                </body>
                </html>
                """.formatted(verificationUrl);

        sendHtmlEmail(
                email,
                subject,
                htmlContent
        );
    }

    @Override
    public void sendPasswordResetEmail(
            UUID userId,
            String email,
            String token
    ) {

        String resetUrl =
                frontendBaseUrl
                        + "/reset-password?token="
                        + token;

        String subject = "Reset your password";

        String htmlContent = """
                <!DOCTYPE html>
                <html>
                <body>
                    <h2>Password reset request</h2>

                    <p>
                        We received a request to reset your password.
                    </p>

                    <p>
                        Click the link below to choose a new password:
                    </p>

                    <p>
                        <a href="%s">Reset password</a>
                    </p>

                    <p>
                        This link will expire after a limited time.
                    </p>

                    <p>
                        If you did not request a password reset,
                        you can ignore this email.
                    </p>
                </body>
                </html>
                """.formatted(resetUrl);

        sendHtmlEmail(
                email,
                subject,
                htmlContent
        );
    }

    private void sendHtmlEmail(
            String recipient,
            String subject,
            String htmlContent
    ) {

        try {
            MimeMessage message =
                    mailSender.createMimeMessage();

            MimeMessageHelper helper =
                    new MimeMessageHelper(
                            message,
                            false,
                            StandardCharsets.UTF_8.name()
                    );

            helper.setFrom(senderEmail);
            helper.setTo(recipient);
            helper.setSubject(subject);
            helper.setText(
                    htmlContent,
                    true
            );

            mailSender.send(message);

        } catch (MessagingException | MailException exception) {
            throw new IllegalStateException(
                    "Failed to send email.",
                    exception
            );
        }
    }
}