package com.streaming.chat.controller;

import com.streaming.chat.client.StreamServiceClient;
import com.streaming.chat.client.UserServiceClient;
import com.streaming.chat.dto.ChatErrorResponse;
import com.streaming.chat.dto.ChatMessageRequest;
import com.streaming.chat.dto.ChatMessageResponse;
import com.streaming.chat.entity.ChatMessage;
import com.streaming.chat.entity.ChatModeration;
import com.streaming.chat.exception.ChatException;
import com.streaming.chat.repository.ChatMessageRepository;
import com.streaming.chat.repository.ChatModerationRepository;
import com.streaming.chat.service.ChatRateLimiter;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Controller;

import java.security.Principal;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Controller
@RequiredArgsConstructor
public class StreamChatController {

    private final SimpMessagingTemplate messagingTemplate;
    private final StreamServiceClient streamServiceClient;
    private final UserServiceClient userServiceClient;
    private final ChatMessageRepository chatMessageRepository;
    private final ChatModerationRepository chatModerationRepository;
    private final ChatRateLimiter chatRateLimiter;

    @MessageMapping("/chat.send")
    public void sendMessage(
            @Valid ChatMessageRequest request,
            StompHeaderAccessor accessor
    ) {

        Principal principal = accessor.getUser();

        if (principal == null) {
            throw new IllegalStateException(
                    "Authenticated user not found"
            );
        }

        Map<String, Object> sessionAttributes =
                accessor.getSessionAttributes();

        if (sessionAttributes == null) {
            throw new IllegalStateException(
                    "WebSocket session attributes are missing"
            );
        }

        String authorization =
                (String) sessionAttributes.get("Authorization");

        if (authorization == null) {
            throw new IllegalStateException(
                    "Authorization token not found in WebSocket session"
            );
        }

        StreamServiceClient.StreamResponse stream =
                streamServiceClient.getStream(
                        request.streamId()
                );

        if (!"LIVE".equalsIgnoreCase(stream.status())) {
            throw new ChatException(
                    "CHAT_NOT_LIVE",
                    "Chat is only available while the stream is live"
            );
        }

        UUID userId =
                UUID.fromString(principal.getName());

        if (!chatRateLimiter.allow(userId)) {
            if (!chatRateLimiter.allow(userId)) {

                System.out.println(
                        "CHAT RATE LIMITED - user=" + userId
                                + ", stream=" + request.streamId()
                );

                messagingTemplate.convertAndSend(
                        "/topic/streams/" + request.streamId() + "/chat.errors",
                        new ChatErrorResponse(
                                "CHAT_RATE_LIMITED",
                                "You are sending messages too quickly"
                        )
                );

                return;
            }
        }

        List<ChatModeration> moderations =
                chatModerationRepository
                        .findByStreamIdAndUserIdOrderByCreatedAtDesc(
                                request.streamId(),
                                userId
                        );

        ChatModeration activeModeration =
                moderations.stream()
                        .filter(ChatModeration::isActive)
                        .findFirst()
                        .orElse(null);

        if (activeModeration != null) {

            throw new ChatException(
                    "CHAT_" + activeModeration.getType().name(),
                    "You are " +
                            activeModeration.getType().name().toLowerCase() +
                            " from this chat"
            );
        }

        String username = "Unknown";
        try {
            var userRes = userServiceClient.getInternalUserById(userId);
            if (userRes != null && userRes.username() != null) {
                username = userRes.username();
            }
        } catch (Exception e) {
            System.err.println("Failed to fetch username for user: " + userId + " - " + e.getMessage());
        }

        ChatMessage chatMessage =
                new ChatMessage(
                        request.streamId(),
                        userId,
                        request.message(),
                        Instant.now()
                );

        chatMessageRepository.save(chatMessage);

        ChatMessageResponse response =
                new ChatMessageResponse(
                        chatMessage.getId(),
                        request.streamId(),
                        principal.getName(),
                        username,
                        request.message(),
                        Instant.now()
                );

        messagingTemplate.convertAndSend(
                "/topic/streams/"
                        + request.streamId()
                        + "/chat",
                response
        );
    }
}