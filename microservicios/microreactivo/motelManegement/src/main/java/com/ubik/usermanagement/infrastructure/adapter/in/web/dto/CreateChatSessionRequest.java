package com.ubik.usermanagement.infrastructure.adapter.in.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * Request DTO para crear una sesión de chat
 */
public record CreateChatSessionRequest(
        @NotNull(message = "El ID del usuario es requerido")
        Long userId,
        
        @NotBlank(message = "El rol del usuario es requerido")
        String userRole
) {
}
