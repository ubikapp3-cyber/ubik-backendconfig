package com.ubik.notification.infrastructure.adapter.in.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * DTO para la actualización de notificaciones
 */
public record UpdateNotificationRequest(
        @NotBlank(message = "El título es requerido")
        @Size(max = 255, message = "El título no puede exceder 255 caracteres")
        String title,

        @NotBlank(message = "El mensaje es requerido")
        @Size(max = 2000, message = "El mensaje no puede exceder 2000 caracteres")
        String message,

        @NotBlank(message = "El tipo de notificación es requerido")
        String type
) {
}
