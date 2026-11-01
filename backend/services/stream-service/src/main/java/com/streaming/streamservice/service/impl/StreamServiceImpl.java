package com.streaming.streamservice.service.impl;

import com.streaming.streamservice.client.ChannelServiceClient;
import com.streaming.streamservice.client.MediaServiceClient;
import com.streaming.streamservice.client.NotificationServiceClient;
import com.streaming.streamservice.client.TranscoderClient;
import com.streaming.streamservice.dto.client.InternalChannelResponse;
import com.streaming.streamservice.dto.client.InternalMediaResponse;
import com.streaming.streamservice.dto.request.CreateStreamRequest;
import com.streaming.streamservice.dto.request.ScheduleStreamRequest;
import com.streaming.streamservice.dto.request.StreamAuthenticationRequest;
import com.streaming.streamservice.dto.request.UpdateStreamRequest;
import com.streaming.streamservice.dto.response.*;
import com.streaming.streamservice.dto.transcoder.StartTranscoderRequest;
import com.streaming.streamservice.dto.transcoder.StopTranscoderRequest;
import com.streaming.streamservice.entity.MediaType;
import com.streaming.streamservice.entity.Stream;
import com.streaming.streamservice.entity.StreamStatus;
import com.streaming.streamservice.exception.ChannelNotFoundException;
import com.streaming.streamservice.exception.InvalidStreamKeyException;
import com.streaming.streamservice.exception.InvalidStreamStateException;
import com.streaming.streamservice.exception.StreamNotFoundException;
import com.streaming.streamservice.mapper.StreamMapper;
import com.streaming.streamservice.repository.StreamRepository;
import com.streaming.streamservice.service.StreamService;
import com.streaming.streamservice.util.StreamKeyGenerator;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class StreamServiceImpl implements StreamService {

    private final StreamRepository streamRepository;
    private final StreamMapper streamMapper;
    private final ChannelServiceClient channelServiceClient;
    private final MediaServiceClient mediaServiceClient;
    private final StreamKeyGenerator streamKeyGenerator;
    private final TranscoderClient transcoderClient;
    private final NotificationServiceClient notificationServiceClient;

    @Override
    @Transactional
    public CreateStreamResponse createStream(
            UUID ownerUserId,
            CreateStreamRequest request) {

        InternalChannelResponse channel =
                getChannel(ownerUserId);

        validateChannelActive(channel);

        String thumbnailUrl = null;

        if (request.thumbnailId() != null) {
            thumbnailUrl = getThumbnailUrl(
                    request.thumbnailId(),
                    ownerUserId
            );
        }

        StreamStatus status =
                StreamStatus.SCHEDULED;

        Stream stream = Stream.builder()
                .channelId(channel.id())
                .title(request.title().trim())
                .description(normalize(request.description()))
                .category(normalize(request.category()))
                .thumbnailUrl(thumbnailUrl)
                .status(status)
                .scheduledAt(request.scheduledAt())
                .streamKey(streamKeyGenerator.generate())
                .build();

        Stream savedStream = streamRepository.save(stream);

        notificationServiceClient.notifyStreamScheduled(
                new NotificationServiceClient.StreamScheduledEvent(
                        savedStream.getId(),
                        savedStream.getChannelId(),
                        savedStream.getTitle(),
                        savedStream.getScheduledAt()
                )
        );

        return streamMapper.toCreateResponse(savedStream);
    }

    @Override
    @Transactional
    public StreamResponse updateStream(
            UUID ownerUserId,
            UUID streamId,
            UpdateStreamRequest request) {

        Stream stream = getOwnedStream(
                ownerUserId,
                streamId
        );

        if (stream.getStatus() == StreamStatus.ENDED) {
            throw new InvalidStreamStateException(
                    "Ended streams cannot be updated"
            );
        }

        if (request.title() != null) {
            stream.setTitle(request.title().trim());
        }

        if (request.description() != null) {
            stream.setDescription(
                    normalize(request.description())
            );
        }

        if (request.category() != null) {
            stream.setCategory(
                    normalize(request.category())
            );
        }

        if (request.thumbnailId() != null) {
            stream.setThumbnailUrl(
                    getThumbnailUrl(
                            request.thumbnailId(),
                            ownerUserId
                    )
            );
        }

        return streamMapper.toResponse(stream);
    }

    @Override
    @Transactional
    public StreamResponse scheduleStream(
            UUID ownerUserId,
            UUID streamId,
            ScheduleStreamRequest request) {

        Stream stream = getOwnedStream(
                ownerUserId,
                streamId
        );

        if (stream.getStatus() != StreamStatus.SCHEDULED) {
            throw new InvalidStreamStateException(
                    "Only scheduled streams can be rescheduled"
            );
        }

        if (request.scheduledAt().isBefore(Instant.now())) {
            throw new IllegalArgumentException(
                    "Scheduled time must be in the future"
            );
        }

        stream.setScheduledAt(request.scheduledAt());

        return streamMapper.toResponse(stream);
    }

    @Override
    public boolean canModerate(
            UUID userId,
            UUID streamId
    ) {
        return streamRepository
                .findById(streamId)
                .map(stream ->
                        channelServiceClient.isOwner(
                                stream.getChannelId(),
                                userId
                        )
                )
                .orElse(false);
    }

    @Override
    @Transactional
    public StreamResponse startStream(
            UUID ownerUserId,
            UUID streamId) {

        Stream stream = getOwnedStream(
                ownerUserId,
                streamId
        );

        if (stream.getStatus() != StreamStatus.SCHEDULED) {
            throw new InvalidStreamStateException(
                    "Only scheduled streams can be started"
            );
        }

        stream.setStatus(StreamStatus.LIVE);
        stream.setStartedAt(Instant.now());

        return streamMapper.toResponse(stream);
    }

    @Override
    @Transactional
    public StreamResponse endStream(
            UUID ownerUserId,
            UUID streamId) {

        Stream stream = getOwnedStream(
                ownerUserId,
                streamId
        );

        if (stream.getStatus() != StreamStatus.LIVE) {
            throw new InvalidStreamStateException(
                    "Only live streams can be ended"
            );
        }

        stream.setStatus(StreamStatus.ENDED);
        stream.setEndedAt(Instant.now());

        return streamMapper.toResponse(stream);
    }

    @Override
    @Transactional(readOnly = true)
    public StreamResponse getStream(UUID streamId) {

        Stream stream =
                streamRepository.findById(streamId)
                        .orElseThrow(() ->
                                new StreamNotFoundException(
                                        "Stream not found with id: "
                                                + streamId
                                )
                        );

        return streamMapper.toResponse(stream);
    }

    @Override
    @Transactional(readOnly = true)
    public List<StreamResponse> getChannelStreams(
            UUID channelId) {

        return streamRepository
                .findByChannelIdOrderByCreatedAtDesc(
                        channelId,
                        PageRequest.of(0, 50)
                )
                .stream()
                .map(streamMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<CategoryResponse> getLiveStreamCategories() {

        return streamRepository
                .countLiveStreamsByCategory(StreamStatus.LIVE)
                .stream()
                .map(row ->
                        new CategoryResponse(
                                (String) row[0],
                                ((Number) row[1]).longValue()
                        )
                )
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<StreamResponse> getLiveStreams(
            int page,
            int size,
            String category,
            String query,
            List<UUID> channelIds,
            String sort
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

        Sort sorting = switch (sort.toLowerCase()) {

            case "recent" ->
                    Sort.by(
                            Sort.Order.desc("startedAt"),
                            Sort.Order.desc("id")
                    );

            case "oldest" ->
                    Sort.by(
                            Sort.Order.asc("startedAt"),
                            Sort.Order.asc("id")
                    );

            default ->
                    throw new IllegalArgumentException(
                            "Unsupported sort: " + sort
                    );
        };

        Pageable pageable =
                PageRequest.of(
                        page,
                        size,
                        sorting
                );

        Page<Stream> streams;

        boolean hasQuery = query != null && !query.isBlank();
        boolean hasChannelIds = channelIds != null && !channelIds.isEmpty();

        if (hasQuery && !hasChannelIds) {
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

        if (!hasQuery) {

            if (category == null || category.isBlank()) {
                streams = streamRepository.findByStatus(
                        StreamStatus.LIVE,
                        pageable
                );
            } else {
                streams = streamRepository.findByStatusAndCategoryIgnoreCase(
                        StreamStatus.LIVE,
                        category.trim(),
                        pageable
                );
            }

        } else {

            if (category == null || category.isBlank()) {

                streams = streamRepository.searchLiveStreams(
                        StreamStatus.LIVE,
                        query.trim(),
                        hasChannelIds ? channelIds : List.of(),
                        pageable
                );

            } else {

                streams = streamRepository.searchLiveStreamsByCategory(
                        StreamStatus.LIVE,
                        category.trim(),
                        query.trim(),
                        hasChannelIds ? channelIds : List.of(),
                        pageable
                );
            }
        }

        return new PageResponse<>(
                streams
                        .map(streamMapper::toResponse)
                        .getContent(),
                streams.getNumber(),
                streams.getSize(),
                streams.getTotalElements(),
                streams.getTotalPages(),
                streams.isFirst(),
                streams.isLast()
        );
    }

    @Override
    @Transactional(readOnly = true)
    public List<ChannelLiveCountResponse> getLiveStreamCountsByChannel() {

        return streamRepository
                .countLiveStreamsByChannel(StreamStatus.LIVE)
                .stream()
                .map(row -> new ChannelLiveCountResponse(
                        (UUID) row[0],
                        ((Number) row[1]).longValue()
                ))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<StreamResponse> getLiveStreamsByChannels(
            List<UUID> channelIds,
            int page,
            int size,
            String sort
    ) {
        if (channelIds == null || channelIds.isEmpty()) {
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

        Sort sorting = switch (sort.toLowerCase()) {
            case "recent" ->
                    Sort.by(
                            Sort.Order.desc("startedAt"),
                            Sort.Order.desc("id")
                    );

            case "oldest" ->
                    Sort.by(
                            Sort.Order.asc("startedAt"),
                            Sort.Order.asc("id")
                    );

            default ->
                    throw new IllegalArgumentException(
                            "Unsupported sort: " + sort
                    );
        };

        Page<Stream> streams =
                streamRepository.findLiveStreamsByChannels(
                        StreamStatus.LIVE,
                        channelIds,
                        PageRequest.of(page, size, sorting)
                );

        return new PageResponse<>(
                streams.map(streamMapper::toResponse).getContent(),
                streams.getNumber(),
                streams.getSize(),
                streams.getTotalElements(),
                streams.getTotalPages(),
                streams.isFirst(),
                streams.isLast()
        );
    }

    private InternalChannelResponse getChannel(
            UUID ownerUserId) {

        try {
            return channelServiceClient.getChannelByOwner(
                    ownerUserId
            );
        } catch (FeignException.NotFound ex) {
            throw new ChannelNotFoundException(
                    "Channel not found for user: "
                            + ownerUserId
            );
        }
    }

    private String getThumbnailUrl(
            UUID mediaId,
            UUID ownerUserId) {

        try {
            InternalMediaResponse media =
                    mediaServiceClient.getMedia(
                            mediaId,
                            MediaType.STREAM_THUMBNAIL.name(),
                            ownerUserId
                    );

            return media.url();

        } catch (FeignException.NotFound ex) {
            throw new IllegalArgumentException(
                    "Thumbnail media not found"
            );
        } catch (FeignException.BadRequest ex) {
            throw new IllegalArgumentException(
                    "Selected media is not a valid thumbnail."
            );
        }
    }

    private Stream getOwnedStream(
            UUID ownerUserId,
            UUID streamId) {

        InternalChannelResponse channel =
                getChannel(ownerUserId);

        return streamRepository
                .findByIdAndChannelId(
                        streamId,
                        channel.id()
                )
                .orElseThrow(() ->
                        new StreamNotFoundException(
                                "Stream not found with id: "
                                        + streamId
                        )
                );
    }

    private void validateChannelActive(
            InternalChannelResponse channel) {

        if (channel.status() == null ||
                !"ACTIVE".equals(channel.status())) {

            throw new IllegalStateException(
                    "Channel is not active"
            );
        }
    }

    private String normalize(String value) {

        if (value == null) {
            return null;
        }

        String trimmed = value.trim();

        return trimmed.isEmpty() ? null : trimmed;
    }


    private static final String LIVE_PREFIX = "live/";

    @Override
    public void authenticateStreamKey(StreamAuthenticationRequest request) {

        log.info(
                "MediaMTX authentication request: action={}, path={}, protocol={}, ip={}",
                request.action(),
                request.path(),
                request.protocol(),
                request.ip()
        );

        if (!"publish".equalsIgnoreCase(request.action())) {
            log.warn(
                    "MediaMTX authentication rejected: unsupported action={}",
                    request.action()
            );
            throw new InvalidStreamKeyException("Unsupported authentication action");
        }

        if (request.path() == null || !request.path().startsWith("live/")) {
            log.warn(
                    "MediaMTX authentication rejected: invalid path={}",
                    request.path()
            );
            throw new InvalidStreamKeyException("Invalid stream path");
        }

        String streamKey = request.path().substring("live/".length());

        if (streamKey.isBlank()) {
            log.warn("MediaMTX authentication rejected: missing stream key");
            throw new InvalidStreamKeyException("Missing stream key");
        }

        log.info("Validating stream key for path={}", request.path());

        Stream stream = streamRepository.findByStreamKey(streamKey)
                .orElseThrow(() -> {
                    log.warn(
                            "MediaMTX authentication rejected: invalid stream key"
                    );
                    return new InvalidStreamKeyException("Invalid stream key");
                });

        if (stream.getStatus() == StreamStatus.ENDED) {
            log.warn(
                    "MediaMTX authentication rejected: stream already ended, streamId={}",
                    stream.getId()
            );
            throw new InvalidStreamStateException(
                    "Ended stream cannot be published again"
            );
        }

        log.info(
                "MediaMTX authentication SUCCESS: stream key accepted, streamId={}, path={}",
                stream.getId(),
                request.path()
        );
    }

    @Override
    @Transactional
    public void handleStreamPublish(String streamKey) {

        Stream stream = streamRepository.findByStreamKey(streamKey)
                .orElseThrow(() ->
                        new InvalidStreamKeyException("Invalid stream key")
                );

        if (stream.getStatus() == StreamStatus.LIVE) {
            return;
        }

        if (stream.getStatus() == StreamStatus.ENDED) {
            throw new InvalidStreamStateException(
                    "Ended stream cannot be published again"
            );
        }

        stream.setStatus(StreamStatus.LIVE);
        stream.setStartedAt(Instant.now());

        streamRepository.save(stream);

        notificationServiceClient.notifyStreamLive(
                new NotificationServiceClient.StreamLiveEvent(
                        stream.getId(),
                        stream.getChannelId(),
                        stream.getTitle()
                )
        );

        transcoderClient.start(
                new StartTranscoderRequest(
                        stream.getId(),
                        stream.getStreamKey()
                )
        );

        log.info(
                "Stream is now LIVE: streamId={}, channelId={}",
                stream.getId(),
                stream.getChannelId()
        );
    }

    @Override
    @Transactional
    public void handleStreamUnpublish(String streamKey) {

        Stream stream = streamRepository.findByStreamKey(streamKey)
                .orElseThrow(() ->
                        new InvalidStreamKeyException("Invalid stream key")
                );

        if (stream.getStatus() != StreamStatus.LIVE) {
            return;
        }

        stream.setStatus(StreamStatus.ENDED);
        stream.setEndedAt(Instant.now());

        streamRepository.save(stream);

        transcoderClient.stop(
                new StopTranscoderRequest(stream.getId())
        );

        log.info(
                "Stream has ENDED: streamId={}, channelId={}",
                stream.getId(),
                stream.getChannelId()
        );
    }
}