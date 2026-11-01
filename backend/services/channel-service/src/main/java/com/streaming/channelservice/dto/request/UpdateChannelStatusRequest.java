package com.streaming.channelservice.dto.request;

import com.streaming.channelservice.entity.ChannelStatus;
import jakarta.validation.constraints.NotNull;

public record UpdateChannelStatusRequest(

        @NotNull
        ChannelStatus status

) {
}