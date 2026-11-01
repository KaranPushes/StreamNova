package com.streaming.streamservice.mapper;

import com.streaming.streamservice.config.StreamingProperties;
import com.streaming.streamservice.dto.response.CreateStreamResponse;
import com.streaming.streamservice.dto.response.StreamResponse;
import com.streaming.streamservice.entity.Stream;
import com.streaming.streamservice.entity.StreamStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class StreamMapper {

    private final StreamingProperties streamingProperties;

    private String buildPlaybackUrl(Stream stream) {
        if (stream.getStatus() != StreamStatus.LIVE) {
            return null;
        }

        return streamingProperties.getPlaybackBaseUrl()
                + "/"
                + stream.getId()
                + "/master.m3u8";
    }

    public StreamResponse toResponse(Stream stream) {

        return new StreamResponse(
                stream.getId(),
                stream.getChannelId(),
                stream.getTitle(),
                stream.getDescription(),
                stream.getCategory(),
                stream.getThumbnailUrl(),
                stream.getStatus(),
                stream.getScheduledAt(),
                stream.getStartedAt(),
                stream.getEndedAt(),
                stream.getCreatedAt(),
                stream.getUpdatedAt(),
                buildPlaybackUrl(stream)
        );
    }

    public CreateStreamResponse toCreateResponse(Stream stream) {
        return new CreateStreamResponse(
                stream.getId(),
                stream.getChannelId(),
                stream.getTitle(),
                stream.getDescription(),
                stream.getCategory(),
                stream.getThumbnailUrl(),
                stream.getStatus(),
                stream.getScheduledAt(),
                stream.getStartedAt(),
                stream.getEndedAt(),
                stream.getCreatedAt(),
                stream.getUpdatedAt(),
                stream.getStreamKey()
        );
    }
}