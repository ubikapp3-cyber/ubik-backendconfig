package com.ubik.notification.infrastructure.adapter.in.web.dto;

import java.time.LocalDateTime;

/**
 * DTO de respuesta para notificaciones
 */
public record NotificationResponse(
        Long id,
        String title,
        String message,
        String type,
        String recipient,
        String recipientType,
        String status,
        LocalDateTime createdAt,
        LocalDateTime sentAt,
        LocalDateTime readAt,
        String metadata
) {
}
