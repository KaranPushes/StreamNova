package com.streaming.followservice.mapper;

import com.streaming.followservice.dto.response.FollowResponse;
import com.streaming.followservice.entity.Follow;
import org.springframework.stereotype.Component;

@Component
public class FollowMapper {

    public FollowResponse toResponse(Follow follow) {

        return new FollowResponse(
                follow.getId(),
                follow.getChannelId(),
                follow.getCreatedAt()
        );
    }
}