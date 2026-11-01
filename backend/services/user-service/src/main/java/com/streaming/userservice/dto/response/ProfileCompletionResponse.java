package com.streaming.userservice.dto.response;

import lombok.Builder;

import java.util.List;

@Builder
public record ProfileCompletionResponse(

        int percentage,
        List<String> missingFields

) {
}