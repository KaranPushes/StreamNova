package com.streaming.streamservice.dto.transcoder;

import java.util.UUID;

public record StopTranscoderRequest(
        UUID streamId
) {}