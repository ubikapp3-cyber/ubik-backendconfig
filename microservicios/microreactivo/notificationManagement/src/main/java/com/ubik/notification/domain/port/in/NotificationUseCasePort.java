package com.ubik.notification.domain.port.in;

import com.ubik.notification.domain.model.Notification;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Puerto de entrada (Input Port) para casos de uso de Notification
 * Define las operaciones disponibles desde la capa de aplicación
 */
public interface NotificationUseCasePort {

    /**
     * Crea una nueva notificación
     * @param notification Notificación a crear
     * @return Mono con la notificación creada
     */
    Mono<Notification> createNotification(Notification notification);

    /**
     * Obtiene una notificación por su ID
     * @param id ID de la notificación
     * @return Mono con la notificación encontrada
     */
    Mono<Notification> getNotificationById(Long id);

    /**
     * Obtiene todas las notificaciones
     * @return Flux con todas las notificaciones
     */
    Flux<Notification> getAllNotifications();

    /**
     * Obtiene notificaciones por destinatario
     * @param recipient Destinatario
     * @return Flux con las notificaciones del destinatario
     */
    Flux<Notification> getNotificationsByRecipient(String recipient);

    /**
     * Obtiene notificaciones por tipo
     * @param type Tipo de notificación
     * @return Flux con las notificaciones del tipo especificado
     */
    Flux<Notification> getNotificationsByType(String type);

    /**
     * Obtiene notificaciones por estado
     * @param status Estado de la notificación
     * @return Flux con las notificaciones en el estado especificado
     */
    Flux<Notification> getNotificationsByStatus(Notification.NotificationStatus status);

    /**
     * Envía una notificación (marca como enviada)
     * @param id ID de la notificación a enviar
     * @return Mono con la notificación enviada
     */
    Mono<Notification> sendNotification(Long id);

    /**
     * Marca una notificación como leída
     * @param id ID de la notificación
     * @return Mono con la notificación marcada como leída
     */
    Mono<Notification> markNotificationAsRead(Long id);

    /**
     * Actualiza una notificación existente
     * @param id ID de la notificación a actualizar
     * @param notification Datos actualizados de la notificación
     * @return Mono con la notificación actualizada
     */
    Mono<Notification> updateNotification(Long id, Notification notification);

    /**
     * Elimina una notificación
     * @param id ID de la notificación a eliminar
     * @return Mono vacío que completa cuando se elimina
     */
    Mono<Void> deleteNotification(Long id);

    /**
     * Obtiene el conteo de notificaciones no leídas por destinatario
     * @param recipient Destinatario
     * @return Mono con el conteo de notificaciones no leídas
     */
    Mono<Long> countUnreadNotificationsByRecipient(String recipient);
}
