package com.streaming.chat.controller;

import com.streaming.chat.client.StreamServiceClient;
import com.streaming.chat.dto.ChatMessageDeletedResponse;
import com.streaming.chat.dto.ModerateChatUserRequest;
import com.streaming.chat.entity.ChatMessage;
import com.streaming.chat.entity.ChatModeration;
import com.streaming.chat.entity.ChatModerator;
import com.streaming.chat.repository.ChatMessageRepository;
import com.streaming.chat.repository.ChatModerationRepository;
import com.streaming.chat.repository.ChatModeratorRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
public class ChatModerationController {

    private final ChatMessageRepository chatMessageRepository;
    private final SimpMessagingTemplate messagingTemplate;
    private final ChatModerationRepository chatModerationRepository;
    private final StreamServiceClient streamServiceClient;
    private final ChatModeratorRepository chatModeratorRepository;

    @GetMapping("/streams/{streamId}/can-moderate")
    public ResponseEntity<Map<String, Boolean>> canModerate(
            @PathVariable UUID streamId,
            Authentication authentication
    ) {
        if (authentication == null || !authentication.isAuthenticated() || authentication.getName() == null) {
            return ResponseEntity.ok(Map.of("isOwner", false, "isMod", false));
        }
        
        try {
            UUID userId = UUID.fromString(authentication.getName());
            boolean isOwner = streamServiceClient.canModerate(streamId, userId);
            boolean isMod = chatModeratorRepository.existsByStreamIdAndUserId(streamId, userId);
            return ResponseEntity.ok(Map.of(
                    "isOwner", isOwner,
                    "isMod", isMod
            ));
        } catch (Exception e) {
            return ResponseEntity.ok(Map.of("isOwner", false, "isMod", false));
        }
    }

    @DeleteMapping("/messages/{messageId}")
    public ResponseEntity<Void> deleteOwnMessage(
            @PathVariable UUID messageId,
            Authentication authentication
    ) {

        UUID userId =
                UUID.fromString(authentication.getName());

        ChatMessage message =
                chatMessageRepository
                        .findByIdAndUserId(
                                messageId,
                                userId
                        )
                        .orElse(null);

        if (message == null) {
            return ResponseEntity.notFound().build();
        }

        message.setDeleted(true);

        chatMessageRepository.save(message);

        ChatMessageDeletedResponse response =
                new ChatMessageDeletedResponse(
                        message.getId(),
                        message.getStreamId(),
                        message.getUserId(),
                        Instant.now()
                );

        messagingTemplate.convertAndSend(
                "/topic/streams/"
                        + message.getStreamId()
                        + "/chat.deleted",
                response
        );

        return ResponseEntity.noContent().build();
    }

    @PostMapping("/streams/{streamId}/moderators/{userId}")
    public ResponseEntity<Void> addModerator(
            @PathVariable UUID streamId,
            @PathVariable UUID userId,
            Authentication authentication
    ) {
        UUID ownerId = UUID.fromString(authentication.getName());

        boolean isOwner = streamServiceClient.canModerate(streamId, ownerId);

        if (!isOwner) {
            return ResponseEntity.status(403).build();
        }

        if (!chatModeratorRepository.existsByStreamIdAndUserId(
                streamId,
                userId
        )) {
            chatModeratorRepository.save(
                    new ChatModerator(streamId, userId)
            );
        }

        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/streams/{streamId}/moderators/{userId}")
    public ResponseEntity<Void> removeModerator(
            @PathVariable UUID streamId,
            @PathVariable UUID userId,
            Authentication authentication
    ) {
        UUID ownerId = UUID.fromString(authentication.getName());

        boolean isOwner = streamServiceClient.canModerate(streamId, ownerId);

        if (!isOwner) {
            return ResponseEntity.status(403).build();
        }

        chatModeratorRepository.deleteByStreamIdAndUserId(
                streamId,
                userId
        );

        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/streams/{streamId}/messages/{messageId}")
    public ResponseEntity<Void> deleteMessageAsModerator(
            @PathVariable UUID streamId,
            @PathVariable UUID messageId,
            Authentication authentication
    ) {
        UUID moderatorId = UUID.fromString(authentication.getName());

        boolean isOwner = streamServiceClient.canModerate(streamId, moderatorId);

        boolean isModerator =
                chatModeratorRepository.existsByStreamIdAndUserId(
                        streamId,
                        moderatorId
                );

        if (!isOwner && !isModerator) {
            return ResponseEntity.status(403).build();
        }

        ChatMessage message = chatMessageRepository
                .findById(messageId)
                .orElse(null);

        if (message == null || !message.getStreamId().equals(streamId)) {
            return ResponseEntity.notFound().build();
        }

        boolean allowed = canModerateUser(streamId, message.getUserId(), moderatorId);

        if (!allowed) {
            return ResponseEntity.status(403).build();
        }

        if (message.isDeleted()) {
            return ResponseEntity.noContent().build();
        }

        message.setDeleted(true);
        chatMessageRepository.save(message);

        ChatMessageDeletedResponse response =
                new ChatMessageDeletedResponse(
                        message.getId(),
                        message.getStreamId(),
                        message.getUserId(),
                        java.time.Instant.now()
                );

        messagingTemplate.convertAndSend(
                "/topic/streams/" + streamId + "/chat.deleted",
                response
        );

        return ResponseEntity.noContent().build();
    }

    @PostMapping("/streams/{streamId}/moderation")
    public ResponseEntity<Void> moderateUser(
            @PathVariable UUID streamId,
            @Valid @RequestBody ModerateChatUserRequest request,
            Authentication authentication
    ) {

        UUID moderatorId =
                UUID.fromString(authentication.getName());

        boolean allowed = canModerateUser(streamId, request.userId(), moderatorId);

        if (!allowed) {
            return ResponseEntity.status(403).build();
        }

        ChatModeration.ModerationType type =
                ChatModeration.ModerationType.valueOf(
                        request.action().name()
                );

        ChatModeration moderation =
                new ChatModeration(
                        streamId,
                        request.userId(),
                        type,
                        request.expiresAt()
                );

        chatModerationRepository.save(moderation);

        return ResponseEntity.ok().build();
    }

    @DeleteMapping(
            "/streams/{streamId}/moderation/{userId}/{action}"
    )
    public ResponseEntity<Void> removeModeration(
            @PathVariable UUID streamId,
            @PathVariable UUID userId,
            @PathVariable ModerateChatUserRequest.ModerationAction action,
            Authentication authentication
    ) {

        UUID moderatorId =
                UUID.fromString(authentication.getName());

        boolean allowed = canModerateUser(streamId, userId, moderatorId);

        if (!allowed) {
            return ResponseEntity.status(403).build();
        }

        ChatModeration.ModerationType type =
                ChatModeration.ModerationType.valueOf(
                        action.name()
                );

        chatModerationRepository
                .deleteByStreamIdAndUserIdAndType(
                        streamId,
                        userId,
                        type
                );

        return ResponseEntity.noContent().build();
    }

    private boolean canModerateUser(
            UUID streamId,
            UUID targetUserId,
            UUID moderatorId
            ) {
        // Channel owner can moderate anyone
        boolean isOwner = streamServiceClient.canModerate(streamId, moderatorId);

        if (isOwner) {
            return true;
        }

        // Moderator cannot moderate themselves
        if (moderatorId.equals(targetUserId)) {
            return false;
        }

        // Moderator cannot moderate another moderator
        return !chatModeratorRepository.existsByStreamIdAndUserId(
                streamId,
                targetUserId
        );
    }
}