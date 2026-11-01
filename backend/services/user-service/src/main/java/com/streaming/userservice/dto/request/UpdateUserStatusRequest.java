package com.streaming.userservice.dto.request;

import com.streaming.userservice.entity.UserStatus;
import jakarta.validation.constraints.NotNull;

public record UpdateUserStatusRequest(

        @NotNull
        UserStatus status

) {
}