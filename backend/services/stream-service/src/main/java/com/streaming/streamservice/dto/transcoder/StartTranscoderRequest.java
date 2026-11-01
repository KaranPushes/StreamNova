package com.streaming.streamservice.dto.transcoder;

import java.util.UUID;

public record StartTranscoderRequest(
        UUID streamId,
        String streamKey
) {}