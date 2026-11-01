package com.streaming.streamservice.service;

import com.streaming.streamservice.dto.request.CreateStreamRequest;
import com.streaming.streamservice.dto.request.ScheduleStreamRequest;
import com.streaming.streamservice.dto.request.StreamAuthenticationRequest;
import com.streaming.streamservice.dto.request.UpdateStreamRequest;
import com.streaming.streamservice.dto.response.*;

import java.util.List;
import java.util.UUID;

public interface StreamService {

    CreateStreamResponse createStream(
            UUID ownerUserId,
            CreateStreamRequest request
    );

    StreamResponse updateStream(
            UUID ownerUserId,
            UUID streamId,
            UpdateStreamRequest request
    );

    StreamResponse scheduleStream(
            UUID ownerUserId,
            UUID streamId,
            ScheduleStreamRequest request
    );

    StreamResponse startStream(
            UUID ownerUserId,
            UUID streamId
    );

    StreamResponse endStream(
            UUID ownerUserId,
            UUID streamId
    );

    boolean canModerate(
            UUID userId,
            UUID streamId
    );

    StreamResponse getStream(UUID streamId);

    List<StreamResponse> getChannelStreams(UUID channelId);

    PageResponse<StreamResponse> getLiveStreams(
            int page,
            int size,
            String category,
            String query,
            List<UUID> channelIds,
            String sort
    );

    PageResponse<StreamResponse> getLiveStreamsByChannels(
            List<UUID> channelIds,
            int page,
            int size,
            String sort
    );

    List<ChannelLiveCountResponse> getLiveStreamCountsByChannel();

    List<CategoryResponse> getLiveStreamCategories();

    void authenticateStreamKey(StreamAuthenticationRequest request);

    void handleStreamPublish(String streamKey);

    void handleStreamUnpublish(String streamKey);
}