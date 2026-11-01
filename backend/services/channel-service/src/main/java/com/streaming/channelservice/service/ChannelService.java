package com.streaming.channelservice.service;

import com.streaming.channelservice.dto.client.InternalChannelResponse;
import com.streaming.channelservice.dto.request.CreateChannelRequest;
import com.streaming.channelservice.dto.request.UpdateAvatarRequest;
import com.streaming.channelservice.dto.request.UpdateBannerRequest;
import com.streaming.channelservice.dto.request.UpdateChannelRequest;
import com.streaming.channelservice.dto.response.*;
import com.streaming.channelservice.entity.ChannelStatus;

import java.util.List;
import java.util.UUID;

public interface ChannelService {

    ChannelResponse createChannel(
            UUID ownerUserId,
            CreateChannelRequest request
    );

    ChannelResponse getCurrentChannel(UUID ownerUserId);

    ChannelResponse updateChannel(
            UUID ownerUserId,
            UpdateChannelRequest request
    );

    void deactivateChannel(UUID ownerUserId);

    PublicChannelResponse getPublicChannel(String slug);

    SlugAvailabilityResponse checkSlugAvailability(String slug);

    ChannelResponse updateChannelStatus(
            UUID channelId,
            ChannelStatus status
    );

    ChannelResponse reactivateChannel(UUID ownerUserId);

    ChannelResponse updateAvatar(
            UUID ownerUserId,
            UpdateAvatarRequest request
    );

    void deleteAvatar(UUID ownerUserId);

    ChannelResponse updateBanner(
            UUID ownerUserId,
            UpdateBannerRequest request
    );

    void deleteBanner(UUID ownerUserId);

    PageResponse<ChannelSearchResponse> searchChannels(
            String query,
            int page,
            int size
    );

    List<InternalChannelResponse> getChannelsByIds(List<UUID> ids);

    InternalChannelResponse getInternalChannel(UUID channelId);

    List<ChannelSearchResponse> getActiveChannels(
            int page,
            int size
    );
}