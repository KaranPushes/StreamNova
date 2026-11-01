package com.streaming.notification.service;

import com.streaming.notification.dto.NotificationResponse;
import com.streaming.notification.entity.Notification;
import com.streaming.notification.entity.NotificationType;
import com.streaming.notification.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final SimpMessagingTemplate messagingTemplate;

    @Transactional
    public Notification create(
            UUID userId,
            NotificationType type,
            String title,
            String message,
            UUID referenceId
    ) {
        Notification notification = new Notification(
                userId,
                type,
                title,
                message,
                referenceId
        );

        Notification saved = notificationRepository.save(notification);

        messagingTemplate.convertAndSend(
                "/topic/users/" + saved.getUserId() + "/notifications",
                toResponse(saved)
        );

        return saved;
    }

    @Transactional(readOnly = true)
    public Page<NotificationResponse> getNotifications(
            UUID userId,
            int page,
            int size,
            boolean unreadOnly
    ) {
        PageRequest pageable =
                PageRequest.of(page, size);

        Page<Notification> notifications;

        if (unreadOnly) {
            notifications =
                    notificationRepository
                            .findByUserIdAndReadFalseOrderByCreatedAtDesc(
                                    userId,
                                    pageable
                            );
        } else {
            notifications =
                    notificationRepository
                            .findByUserIdOrderByCreatedAtDesc(
                                    userId,
                                    pageable
                            );
        }

        return notifications.map(this::toResponse);
    }

    @Transactional
    public void markAsRead(
            UUID userId,
            UUID notificationId
    ) {
        Notification notification =
                notificationRepository
                        .findById(notificationId)
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Notification not found"
                                )
                        );

        if (!notification.getUserId().equals(userId)) {
            throw new IllegalArgumentException(
                    "Notification does not belong to user"
            );
        }

        notification.markAsRead();
    }

    @Transactional
    public void markAllAsRead(UUID userId) {
        notificationRepository
                .findByUserIdAndReadFalseOrderByCreatedAtDesc(
                        userId,
                        PageRequest.of(0, 1000)
                )
                .forEach(Notification::markAsRead);
    }

    @Transactional(readOnly = true)
    public long getUnreadCount(UUID userId) {
        return notificationRepository
                .countByUserIdAndReadFalse(userId);
    }

    private NotificationResponse toResponse(
            Notification notification
    ) {
        return new NotificationResponse(
                notification.getId(),
                notification.getType(),
                notification.getTitle(),
                notification.getMessage(),
                notification.getReferenceId(),
                notification.isRead(),
                notification.getCreatedAt()
        );
    }
}