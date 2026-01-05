package com.ubik.usermanagement.infrastructure.adapter.in.web.dto;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Response DTO para sesiones de chat
 */
public record ChatSessionResponse(
        Long id,
        Long userId,
        String userRole,
        LocalDateTime startedAt,
        LocalDateTime lastActivityAt,
        String status,
        Map<String, String> context
) {
}
