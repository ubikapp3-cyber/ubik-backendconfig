package com.ubik.usermanagement.infrastructure.adapter.in.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * Request DTO para enviar un mensaje al chatbot
 * Incluye validaciones de seguridad para prevenir ataques
 */
public record ChatMessageRequest(
        @NotNull(message = "El ID de la sesión es requerido")
        Long sessionId,
        
        @NotBlank(message = "El mensaje no puede estar vacío")
        @Size(max = 2000, message = "El mensaje no puede exceder 2000 caracteres")
        String message,
        
        @NotNull(message = "El ID del usuario es requerido")
        Long userId,
        
        @NotBlank(message = "El rol del usuario es requerido")
        @Pattern(regexp = "^(USER|ADMIN|ROLE_USER|ROLE_ADMIN)$", 
                 message = "Rol inválido. Debe ser USER, ADMIN, ROLE_USER o ROLE_ADMIN")
        String userRole
) {
}
