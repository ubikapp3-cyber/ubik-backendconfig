package com.ubik.usermanagement.infrastructure.adapter.in.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

/**
 * Request DTO para crear una sesión de chat
 * Incluye validaciones de seguridad
 */
public record CreateChatSessionRequest(
        @NotNull(message = "El ID del usuario es requerido")
        Long userId,
        
        @NotBlank(message = "El rol del usuario es requerido")
        @Pattern(regexp = "^(USER|ADMIN|ROLE_USER|ROLE_ADMIN)$", 
                 message = "Rol inválido. Debe ser USER, ADMIN, ROLE_USER o ROLE_ADMIN")
        String userRole
) {
}
