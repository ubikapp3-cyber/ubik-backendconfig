package com.ubik.usermanagement.infrastructure.adapter.in.web.dto;

import java.time.LocalDateTime;

/**
 * Response DTO para mensajes del chatbot
 */
public record ChatMessageResponse(
        Long id,
        Long sessionId,
        String message,
        String response,
        String messageType,
        LocalDateTime timestamp
) {
}
