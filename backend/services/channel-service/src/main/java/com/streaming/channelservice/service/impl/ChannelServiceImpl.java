package com.streaming.channelservice.service.impl;

import com.streaming.channelservice.client.MediaServiceClient;
import com.streaming.channelservice.dto.client.InternalChannelResponse;
import com.streaming.channelservice.dto.client.InternalMediaResponse;
import com.streaming.channelservice.dto.request.CreateChannelRequest;
import com.streaming.channelservice.dto.request.UpdateAvatarRequest;
import com.streaming.channelservice.dto.request.UpdateBannerRequest;
import com.streaming.channelservice.dto.request.UpdateChannelRequest;
import com.streaming.channelservice.dto.response.*;
import com.streaming.channelservice.entity.Channel;
import com.streaming.channelservice.entity.ChannelStatus;
import com.streaming.channelservice.exception.ChannelAlreadyExistsException;
import com.streaming.channelservice.exception.ChannelNotFoundException;
import com.streaming.channelservice.exception.InvalidChannelStateException;
import com.streaming.channelservice.exception.SlugAlreadyExistsException;
import com.streaming.channelservice.mapper.ChannelMapper;
import com.streaming.channelservice.repository.ChannelRepository;
import com.streaming.channelservice.service.ChannelService;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Locale;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ChannelServiceImpl implements ChannelService {

    private final ChannelRepository channelRepository;
    private final ChannelMapper channelMapper;
    private final MediaServiceClient mediaServiceClient;

    @Override
    @Transactional
    public ChannelResponse createChannel(
            UUID ownerUserId,
            CreateChannelRequest request) {

        if (channelRepository.existsByOwnerUserId(ownerUserId)) {
            throw new ChannelAlreadyExistsException();
        }

        String normalizedSlug =
                request.slug()
                        .trim()
                        .toLowerCase(Locale.ROOT);

        if (channelRepository.existsBySlugIgnoreCase(normalizedSlug)) {
            throw new SlugAlreadyExistsException(normalizedSlug);
        }

        Channel channel = Channel.builder()
                .ownerUserId(ownerUserId)
                .slug(normalizedSlug)
                .displayName(request.displayName().trim())
                .status(ChannelStatus.ACTIVE)
                .build();

        Channel savedChannel =
                channelRepository.save(channel);

        return channelMapper.toResponse(savedChannel);
    }

    @Override
    @Transactional(readOnly = true)
    public ChannelResponse getCurrentChannel(UUID ownerUserId) {

        Channel channel = channelRepository
                .findByOwnerUserId(ownerUserId)
                .orElseThrow(() ->
                        new ChannelNotFoundException("Channel not found for user: " + ownerUserId)
                );

        return channelMapper.toResponse(channel);
    }

    @Override
    @Transactional
    public ChannelResponse updateChannel(
            UUID ownerUserId,
            UpdateChannelRequest request) {

        Channel channel = channelRepository
                .findByOwnerUserId(ownerUserId)
                .orElseThrow(() ->
                        new ChannelNotFoundException(
                                "Channel not found for current user"
                        )
                );

        if (request.slug() != null) {

            String normalizedSlug = request.slug()
                    .trim()
                    .toLowerCase(Locale.ROOT);

            if (!normalizedSlug.equalsIgnoreCase(channel.getSlug())
                    && channelRepository.existsBySlugIgnoreCase(normalizedSlug)) {

                throw new SlugAlreadyExistsException(normalizedSlug);
            }

            channel.setSlug(normalizedSlug);
        }

        if (request.displayName() != null) {
            channel.setDisplayName(
                    request.displayName().trim()
            );
        }

        if (request.description() != null) {
            channel.setDescription(
                    request.description().trim()
            );
        }

        return channelMapper.toResponse(channel);
    }

    @Override
    @Transactional
    public void deactivateChannel(UUID ownerUserId) {

        Channel channel = getChannelByOwner(ownerUserId);

        channel.setStatus(ChannelStatus.DEACTIVATED);
    }

    @Override
    @Transactional(readOnly = true)
    public PublicChannelResponse getPublicChannel(String slug) {

        String normalizedSlug = slug
                .trim()
                .toLowerCase(Locale.ROOT);

        Channel channel = channelRepository
                .findBySlugIgnoreCase(normalizedSlug)
                .orElseThrow(() ->
                        new ChannelNotFoundException(
                                "Channel not found with slug: " + normalizedSlug
                        )
                );

        if (channel.getStatus() != ChannelStatus.ACTIVE) {
            throw new ChannelNotFoundException(
                    "Channel not found with slug: " + normalizedSlug
            );
        }

        return channelMapper.toPublicResponse(channel);
    }

    @Override
    @Transactional(readOnly = true)
    public SlugAvailabilityResponse checkSlugAvailability(String slug) {

        String normalizedSlug = slug
                .trim()
                .toLowerCase(Locale.ROOT);

        boolean available =
                !channelRepository.existsBySlugIgnoreCase(normalizedSlug);

        return new SlugAvailabilityResponse(
                normalizedSlug,
                available
        );
    }

    @Override
    @Transactional
    public ChannelResponse updateChannelStatus(
            UUID channelId,
            ChannelStatus status) {

        Channel channel = channelRepository
                .findById(channelId)
                .orElseThrow(() ->
                        new ChannelNotFoundException(
                                "Channel not found with id: " + channelId
                        )
                );

        channel.setStatus(status);

        return channelMapper.toResponse(channel);
    }

    @Override
    @Transactional
    public ChannelResponse reactivateChannel(UUID ownerUserId) {

        Channel channel = channelRepository
                .findByOwnerUserId(ownerUserId)
                .orElseThrow(() ->
                        new ChannelNotFoundException(
                                "Channel not found for current user"
                        )
                );

        if (channel.getStatus() != ChannelStatus.DEACTIVATED) {
            throw new InvalidChannelStateException(
                    "Only a deactivated channel can be reactivated"
            );
        }

        channel.setStatus(ChannelStatus.ACTIVE);

        return channelMapper.toResponse(channel);
    }

    @Override
    @Transactional
    public ChannelResponse updateAvatar(
            UUID ownerUserId,
            UpdateAvatarRequest request) {

        Channel channel = getChannelByOwner(ownerUserId);

        try {

            InternalMediaResponse media =
                    mediaServiceClient.getMedia(
                            request.mediaId(),
                            "AVATAR",
                            ownerUserId
                    );

            channel.setAvatarUrl(media.url());

            return channelMapper.toResponse(channel);

        } catch (FeignException.NotFound ex) {

            throw new ChannelNotFoundException(
                    "Media not found with id: " + request.mediaId()
            );

        } catch (FeignException.BadRequest ex) {

            throw new IllegalArgumentException(
                    "Selected media is not a valid avatar."
            );
        }
    }

    @Override
    @Transactional
    public void deleteAvatar(UUID ownerUserId) {

        Channel channel = getChannelByOwner(ownerUserId);

        channel.setAvatarUrl(null);
    }

    @Override
    @Transactional
    public ChannelResponse updateBanner(
            UUID ownerUserId,
            UpdateBannerRequest request) {

        Channel channel = getChannelByOwner(ownerUserId);

        try {

            InternalMediaResponse media =
                    mediaServiceClient.getMedia(
                            request.mediaId(),
                            "BANNER",
                            ownerUserId
                    );

            channel.setBannerUrl(media.url());

            return channelMapper.toResponse(channel);

        } catch (FeignException.NotFound ex) {

            throw new ChannelNotFoundException(
                    "Media not found with id: " + request.mediaId()
            );

        } catch (FeignException.BadRequest ex) {

            throw new IllegalArgumentException(
                    "Selected media is not a valid banner."
            );
        }
    }

    @Override
    @Transactional
    public void deleteBanner(UUID ownerUserId) {

        Channel channel = getChannelByOwner(ownerUserId);

        channel.setBannerUrl(null);
    }

    private Channel getChannelByOwner(UUID ownerUserId) {

        return channelRepository
                .findByOwnerUserId(ownerUserId)
                .orElseThrow(() ->
                        new ChannelNotFoundException(
                                "Channel not found for current user"
                        )
                );
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<ChannelSearchResponse> searchChannels(
            String query,
            int page,
            int size
    ) {
        if (page < 0) {
            throw new IllegalArgumentException(
                    "Page must not be negative"
            );
        }

        if (size < 1 || size > 50) {
            throw new IllegalArgumentException(
                    "Page size must be between 1 and 50"
            );
        }

        String normalizedQuery =
                query == null
                        ? ""
                        : query.trim().toLowerCase(Locale.ROOT);

        if (normalizedQuery.isBlank()) {
            return new PageResponse<>(
                    List.of(),
                    page,
                    size,
                    0,
                    0,
                    true,
                    true
            );
        }

        Page<Channel> channels =
                channelRepository.searchChannels(
                        normalizedQuery,
                        ChannelStatus.ACTIVE,
                        PageRequest.of(page, size)
                );

        return new PageResponse<>(
                channels.map(channelMapper::toSearchResponse).getContent(),
                channels.getNumber(),
                channels.getSize(),
                channels.getTotalElements(),
                channels.getTotalPages(),
                channels.isFirst(),
                channels.isLast()
        );
    }

    @Override
    @Transactional(readOnly = true)
    public List<InternalChannelResponse> getChannelsByIds(
            List<UUID> ids
    ) {
        if (ids == null || ids.isEmpty()) {
            return List.of();
        }

        return channelRepository.findAllById(ids)
                .stream()
                .map(channelMapper::toInternalResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public InternalChannelResponse getInternalChannel(UUID channelId) {

        Channel channel = channelRepository.findById(channelId)
                .orElseThrow(() ->
                        new ChannelNotFoundException(
                                "Channel not found with id: " + channelId
                        )
                );

        return channelMapper.toInternalResponse(channel);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ChannelSearchResponse> getActiveChannels(
            int page,
            int size
    ) {
        return channelRepository
                .findByStatus(
                        ChannelStatus.ACTIVE,
                        PageRequest.of(page, size)
                )
                .stream()
                .map(channelMapper::toSearchResponse)
                .toList();
    }
}