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
     * Este método crea una notificación parcial que solo contiene los campos actualizables.
     * Los demás campos deben ser preservados del objeto existente en el servicio de dominio.
     */
    public Notification toDomainForUpdate(UpdateNotificationRequest request, Notification existing) {
        if (request == null || existing == null) {
            return null;
        }

        return existing.withUpdatedInfo(
                request.title(),
                request.message(),
                request.type()
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
