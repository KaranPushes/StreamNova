package com.streaming.channelservice.mapper;

import com.streaming.channelservice.dto.client.InternalChannelResponse;
import com.streaming.channelservice.dto.response.ChannelResponse;
import com.streaming.channelservice.dto.response.ChannelSearchResponse;
import com.streaming.channelservice.dto.response.PublicChannelResponse;
import com.streaming.channelservice.entity.Channel;
import org.springframework.stereotype.Component;

@Component
public class ChannelMapper {

    public ChannelResponse toResponse(Channel channel) {

        return new ChannelResponse(
                channel.getId(),
                channel.getOwnerUserId(),
                channel.getSlug(),
                channel.getDisplayName(),
                channel.getDescription(),
                channel.getAvatarUrl(),
                channel.getBannerUrl(),
                channel.getStatus(),
                channel.getCreatedAt(),
                channel.getUpdatedAt()
        );
    }

    public PublicChannelResponse toPublicResponse(Channel channel) {

        return new PublicChannelResponse(
                channel.getId(),
                channel.getSlug(),
                channel.getDisplayName(),
                channel.getDescription(),
                channel.getAvatarUrl(),
                channel.getBannerUrl(),
                channel.getCreatedAt()
        );
    }

    public ChannelSearchResponse toSearchResponse(Channel channel) {

        return new ChannelSearchResponse(
                channel.getId(),
                channel.getSlug(),
                channel.getDisplayName(),
                channel.getAvatarUrl()
        );
    }

    public InternalChannelResponse toInternalResponse(Channel channel) {

        return new InternalChannelResponse(
                channel.getId(),
                channel.getOwnerUserId(),
                channel.getSlug(),
                channel.getDisplayName(),
                channel.getStatus()
        );
    }
}