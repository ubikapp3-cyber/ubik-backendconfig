package com.ubik.notification.infrastructure.adapter.in.web.mapper;

import com.ubik.notification.domain.model.Notification;
import com.ubik.notification.infrastructure.adapter.in.web.dto.CreateNotificationRequest;
import com.ubik.notification.infrastructure.adapter.in.web.dto.NotificationResponse;
import com.ubik.notification.infrastructure.adapter.in.web.dto.UpdateNotificationRequest;
import org.springframework.stereotype.Component;

/**
 * Mapper entre DTOs (web) y modelos de dominio
 */
@Component
public class NotificationDtoMapper {

    /**
     * Convierte CreateNotificationRequest a Notification (dominio)
     */
    public Notification toDomain(CreateNotificationRequest request) {
        if (request == null) {
            return null;
        }

        return Notification.createNew(
                request.title(),
                request.message(),
                request.type(),
                request.recipient(),
                request.recipientType(),
                request.metadata()
        );
    }

    /**
     * Convierte UpdateNotificationRequest a Notification (dominio)
     * Nota: Solo contiene los campos actualizables
     */
    public Notification toDomain(UpdateNotificationRequest request) {
        if (request == null) {
            return null;
        }

        // Solo para actualización, los demás campos se conservarán del objeto existente
        return new Notification(
                null,
                request.title(),
                request.message(),
                request.type(),
                null,
                null,
                null,
                null,
                null,
                null,
                null
        );
    }

    /**
     * Convierte Notification (dominio) a NotificationResponse
     */
    public NotificationResponse toResponse(Notification notification) {
        if (notification == null) {
            return null;
        }

        return new NotificationResponse(
                notification.id(),
                notification.title(),
                notification.message(),
                notification.type(),
                notification.recipient(),
                notification.recipientType(),
                notification.status().name(),
                notification.createdAt(),
                notification.sentAt(),
                notification.readAt(),
                notification.metadata()
        );
    }
}
