package com.ubik.notification.domain.service;

import com.ubik.notification.domain.model.Notification;
import com.ubik.notification.domain.port.in.NotificationUseCasePort;
import com.ubik.notification.domain.port.out.NotificationRepositoryPort;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Servicio de dominio que implementa los casos de uso de Notification
 * Contiene la lógica de negocio
 */
@Service
public class NotificationService implements NotificationUseCasePort {

    private final NotificationRepositoryPort notificationRepositoryPort;

    public NotificationService(NotificationRepositoryPort notificationRepositoryPort) {
        this.notificationRepositoryPort = notificationRepositoryPort;
    }

    @Override
    public Mono<Notification> createNotification(Notification notification) {
        // Validaciones de negocio
        return validateNotification(notification)
                .then(notificationRepositoryPort.save(notification));
    }

    @Override
    public Mono<Notification> getNotificationById(Long id) {
        return notificationRepositoryPort.findById(id)
                .switchIfEmpty(Mono.error(new RuntimeException("Notificación no encontrada con ID: " + id)));
    }

    @Override
    public Flux<Notification> getAllNotifications() {
        return notificationRepositoryPort.findAll();
    }

    @Override
    public Flux<Notification> getNotificationsByRecipient(String recipient) {
        if (recipient == null || recipient.trim().isEmpty()) {
            return Flux.error(new IllegalArgumentException("El destinatario es requerido"));
        }
        return notificationRepositoryPort.findByRecipient(recipient);
    }

    @Override
    public Flux<Notification> getNotificationsByType(String type) {
        if (type == null || type.trim().isEmpty()) {
            return Flux.error(new IllegalArgumentException("El tipo de notificación es requerido"));
        }
        return notificationRepositoryPort.findByType(type);
    }

    @Override
    public Flux<Notification> getNotificationsByStatus(Notification.NotificationStatus status) {
        if (status == null) {
            return Flux.error(new IllegalArgumentException("El estado de la notificación es requerido"));
        }
        return notificationRepositoryPort.findByStatus(status);
    }

    @Override
    public Mono<Notification> sendNotification(Long id) {
        return notificationRepositoryPort.findById(id)
                .switchIfEmpty(Mono.error(new RuntimeException("Notificación no encontrada con ID: " + id)))
                .flatMap(notification -> {
                    if (notification.status() == Notification.NotificationStatus.SENT ||
                        notification.status() == Notification.NotificationStatus.READ) {
                        return Mono.error(new IllegalStateException("La notificación ya ha sido enviada"));
                    }
                    if (notification.status() == Notification.NotificationStatus.CANCELLED) {
                        return Mono.error(new IllegalStateException("La notificación ha sido cancelada"));
                    }
                    Notification sentNotification = notification.markAsSent();
                    return notificationRepositoryPort.update(sentNotification);
                });
    }

    @Override
    public Mono<Notification> markNotificationAsRead(Long id) {
        return notificationRepositoryPort.findById(id)
                .switchIfEmpty(Mono.error(new RuntimeException("Notificación no encontrada con ID: " + id)))
                .flatMap(notification -> {
                    if (notification.status() != Notification.NotificationStatus.SENT &&
                        notification.status() != Notification.NotificationStatus.READ) {
                        return Mono.error(new IllegalStateException(
                                "Solo se pueden marcar como leídas las notificaciones enviadas"));
                    }
                    Notification readNotification = notification.markAsRead();
                    return notificationRepositoryPort.update(readNotification);
                });
    }

    @Override
    public Mono<Notification> updateNotification(Long id, Notification notification) {
        return notificationRepositoryPort.findById(id)
                .switchIfEmpty(Mono.error(new RuntimeException("Notificación no encontrada con ID: " + id)))
                .flatMap(existingNotification -> {
                    // Solo se puede actualizar si está en estado PENDING
                    if (existingNotification.status() != Notification.NotificationStatus.PENDING) {
                        return Mono.error(new IllegalStateException(
                                "Solo se pueden actualizar notificaciones en estado PENDING"));
                    }
                    Notification updatedNotification = existingNotification.withUpdatedInfo(
                            notification.title(),
                            notification.message(),
                            notification.type()
                    );
                    return validateNotification(updatedNotification)
                            .then(notificationRepositoryPort.update(updatedNotification));
                });
    }

    @Override
    public Mono<Void> deleteNotification(Long id) {
        return notificationRepositoryPort.existsById(id)
                .flatMap(exists -> {
                    if (!exists) {
                        return Mono.error(new RuntimeException("Notificación no encontrada con ID: " + id));
                    }
                    return notificationRepositoryPort.deleteById(id);
                });
    }

    @Override
    public Mono<Long> countUnreadNotificationsByRecipient(String recipient) {
        if (recipient == null || recipient.trim().isEmpty()) {
            return Mono.error(new IllegalArgumentException("El destinatario es requerido"));
        }
        return notificationRepositoryPort.countUnreadByRecipient(recipient);
    }

    /**
     * Validaciones de negocio para una notificación
     */
    private Mono<Void> validateNotification(Notification notification) {
        if (notification.title() == null || notification.title().trim().isEmpty()) {
            return Mono.error(new IllegalArgumentException("El título de la notificación es requerido"));
        }
        if (notification.message() == null || notification.message().trim().isEmpty()) {
            return Mono.error(new IllegalArgumentException("El mensaje de la notificación es requerido"));
        }
        if (notification.type() == null || notification.type().trim().isEmpty()) {
            return Mono.error(new IllegalArgumentException("El tipo de notificación es requerido"));
        }
        if (notification.recipient() == null || notification.recipient().trim().isEmpty()) {
            return Mono.error(new IllegalArgumentException("El destinatario de la notificación es requerido"));
        }
        if (notification.recipientType() == null || notification.recipientType().trim().isEmpty()) {
            return Mono.error(new IllegalArgumentException("El tipo de destinatario es requerido"));
        }
        if (notification.title().length() > 255) {
            return Mono.error(new IllegalArgumentException("El título no puede exceder 255 caracteres"));
        }
        if (notification.message().length() > 2000) {
            return Mono.error(new IllegalArgumentException("El mensaje no puede exceder 2000 caracteres"));
        }
        return Mono.empty();
    }
}
