package com.streaming.followservice.service.impl;

import com.streaming.followservice.client.ChannelServiceClient;
import com.streaming.followservice.client.NotificationServiceClient;
import com.streaming.followservice.dto.client.ChannelResponse;
import com.streaming.followservice.dto.response.*;
import com.streaming.followservice.entity.Follow;
import com.streaming.followservice.exception.AlreadyFollowingException;
import com.streaming.followservice.exception.CannotFollowOwnChannelException;
import com.streaming.followservice.exception.ChannelNotAvailableException;
import com.streaming.followservice.exception.NotFollowingException;
import com.streaming.followservice.mapper.FollowMapper;
import com.streaming.followservice.repository.FollowRepository;
import com.streaming.followservice.service.FollowService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FollowServiceImpl implements FollowService {

    private final FollowRepository followRepository;
    private final ChannelServiceClient channelServiceClient;
    private final FollowMapper followMapper;
    private final NotificationServiceClient notificationServiceClient;

    @Override
    @Transactional
    public FollowResponse followChannel(
            UUID userId,
            UUID channelId) {

        ChannelResponse channel =
                channelServiceClient.getChannelById(channelId);

        if (!"ACTIVE".equals(channel.status())) {
            throw new ChannelNotAvailableException();
        }

        if (userId.equals(channel.ownerUserId())) {
            throw new CannotFollowOwnChannelException();
        }

        if (followRepository.existsByUserIdAndChannelId(
                userId,
                channelId
        )) {
            throw new AlreadyFollowingException();
        }

        Follow follow = new Follow();

        follow.setUserId(userId);
        follow.setChannelId(channelId);

        Follow savedFollow =
                followRepository.save(follow);

        notificationServiceClient.notifyNewFollower(
                new NotificationServiceClient.NewFollowerEvent(
                        channel.ownerUserId(),
                        userId
                )
        );

        return followMapper.toResponse(savedFollow);
    }

    @Override
    @Transactional(readOnly = true)
    public List<UUID> getFollowerIds(UUID channelId) {
        return followRepository
                .findByChannelId(channelId)
                .stream()
                .map(Follow::getUserId)
                .toList();
    }

    @Override
    @Transactional
    public void unfollowChannel(
            UUID userId,
            UUID channelId) {

        Follow follow = followRepository
                .findByUserIdAndChannelId(userId, channelId)
                .orElseThrow(NotFollowingException::new);

        followRepository.delete(follow);
    }

    @Override
    @Transactional(readOnly = true)
    public FollowStatusResponse getFollowStatus(
            UUID userId,
            UUID channelId) {

        boolean following =
                followRepository.existsByUserIdAndChannelId(
                        userId,
                        channelId
                );

        return new FollowStatusResponse(
                channelId,
                following
        );
    }

    @Override
    @Transactional(readOnly = true)
    public FollowerCountResponse getFollowerCount(UUID channelId) {

        long count = followRepository.countByChannelId(channelId);

        return new FollowerCountResponse(
                channelId,
                count
        );
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<FollowedChannelResponse> getFollowedChannels(
            UUID userId,
            int page,
            int size) {

        Pageable pageable = PageRequest.of(
                page,
                size,
                Sort.by(Sort.Direction.DESC, "createdAt")
        );

        Page<Follow> followPage =
                followRepository.findByUserId(
                        userId,
                        pageable
                );

        List<FollowedChannelResponse> content =
                followPage.getContent()
                        .stream()
                        .map(follow ->
                                new FollowedChannelResponse(
                                        follow.getChannelId(),
                                        follow.getCreatedAt()
                                )
                        )
                        .toList();

        return new PageResponse<>(
                content,
                followPage.getNumber(),
                followPage.getSize(),
                followPage.getTotalElements(),
                followPage.getTotalPages(),
                followPage.isFirst(),
                followPage.isLast()
        );
    }
}