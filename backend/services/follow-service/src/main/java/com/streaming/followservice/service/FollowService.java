package com.streaming.followservice.service;

import com.streaming.followservice.dto.response.*;

import java.util.List;
import java.util.UUID;

public interface FollowService {

    FollowResponse followChannel(
            UUID userId,
            UUID channelId
    );

    void unfollowChannel(
            UUID userId,
            UUID channelId
    );

    FollowStatusResponse getFollowStatus(
            UUID userId,
            UUID channelId
    );

    FollowerCountResponse getFollowerCount(UUID channelId);

    PageResponse<FollowedChannelResponse> getFollowedChannels(
            UUID userId,
            int page,
            int size
    );

    List<UUID> getFollowerIds(UUID channelId);
}