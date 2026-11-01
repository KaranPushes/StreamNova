package com.streaming.authservice.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record ResendVerificationEmailRequest(

        @NotBlank
        @Email
        String email

) {
}