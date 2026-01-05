package com.ubik.usermanagement.infrastructure.adapter.in.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * Request DTO para enviar un mensaje al chatbot
 */
public record ChatMessageRequest(
        @NotNull(message = "El ID de la sesión es requerido")
        Long sessionId,
        
        @NotBlank(message = "El mensaje no puede estar vacío")
        String message,
        
        @NotNull(message = "El ID del usuario es requerido")
        Long userId,
        
        @NotBlank(message = "El rol del usuario es requerido")
        String userRole
) {
}
