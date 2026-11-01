package com.streaming.chat.dto;

public record ChatErrorResponse(
        String code,
        String message
) {}