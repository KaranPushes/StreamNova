package com.streaming.chat.controller;

import com.streaming.chat.client.UserServiceClient;
import com.streaming.chat.repository.ChatMessageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
public class ChatHistoryController {

    private final ChatMessageRepository chatMessageRepository;
    private final UserServiceClient userServiceClient;

    @GetMapping("/streams/{streamId}/messages")
    public List<ChatMessageResponse> getMessages(
            @PathVariable UUID streamId
    ) {

        return chatMessageRepository
                .findByStreamIdOrderByCreatedAtDesc(
                        streamId,
                        PageRequest.of(
                                0,
                                50,
                                Sort.by(
                                        Sort.Direction.DESC,
                                        "createdAt"
                                )
                        )
                )
                .stream()
                .map(message -> {
                    String username = "Unknown";
                    try {
                        var userRes = userServiceClient.getInternalUserById(message.getUserId());
                        if (userRes != null && userRes.username() != null) {
                            username = userRes.username();
                        }
                    } catch (Exception e) {
                        System.err.println("Failed to fetch username for history: " + message.getUserId());
                    }
                    
                    return new ChatMessageResponse(
                        message.getId(),
                        message.getStreamId(),
                        message.getUserId(),
                        username,
                        message.isDeleted()
                                ? "[message deleted]"
                                : message.getMessage(),
                        message.getCreatedAt()
                    );
                })
                .toList();
    }

    public record ChatMessageResponse(
            UUID id,
            UUID streamId,
            UUID userId,
            String username,
            String message,
            java.time.Instant createdAt
    ) {
    }
}