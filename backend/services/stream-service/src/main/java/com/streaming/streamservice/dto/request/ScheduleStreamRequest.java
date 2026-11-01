package com.streaming.streamservice.dto.request;

import jakarta.validation.constraints.NotNull;

import java.time.Instant;

public record ScheduleStreamRequest(

        @NotNull
        Instant scheduledAt
) {}