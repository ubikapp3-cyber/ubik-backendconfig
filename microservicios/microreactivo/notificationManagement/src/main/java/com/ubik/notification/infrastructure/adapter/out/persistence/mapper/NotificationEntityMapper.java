package com.ubik.notification.infrastructure.adapter.out.persistence.mapper;

import com.ubik.notification.domain.model.Notification;
import com.ubik.notification.infrastructure.adapter.out.persistence.entity.NotificationEntity;
import org.springframework.stereotype.Component;

/**
 * Mapper entre NotificationEntity (infraestructura) y Notification (dominio)
 */
@Component
public class NotificationEntityMapper {

    /**
     * Convierte de Entity a Domain
     */
    public Notification toDomain(NotificationEntity entity) {
        if (entity == null) {
            return null;
        }

        return new Notification(
                entity.id(),
                entity.title(),
                entity.message(),
                entity.type(),
                entity.recipient(),
                entity.recipientType(),
                Notification.NotificationStatus.valueOf(entity.status()),
                entity.createdAt(),
                entity.sentAt(),
                entity.readAt(),
                entity.metadata()
        );
    }

    /**
     * Convierte de Domain a Entity
     */
    public NotificationEntity toEntity(Notification notification) {
        if (notification == null) {
            return null;
        }

        return new NotificationEntity(
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
