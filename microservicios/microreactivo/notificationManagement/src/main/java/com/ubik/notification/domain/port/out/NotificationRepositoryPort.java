package com.ubik.notification.domain.port.out;

import com.ubik.notification.domain.model.Notification;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Puerto de salida (Output Port) para operaciones de persistencia de Notification
 * Define el contrato que debe implementar la infraestructura
 * Parte de la arquitectura hexagonal
 */
public interface NotificationRepositoryPort {

    /**
     * Guarda una nueva notificación
     * @param notification Notificación a guardar
     * @return Mono con la notificación guardada incluyendo su ID generado
     */
    Mono<Notification> save(Notification notification);

    /**
     * Busca una notificación por su ID
     * @param id ID de la notificación
     * @return Mono con la notificación encontrada o vacío
     */
    Mono<Notification> findById(Long id);

    /**
     * Busca todas las notificaciones
     * @return Flux con todas las notificaciones
     */
    Flux<Notification> findAll();

    /**
     * Busca notificaciones por destinatario
     * @param recipient Destinatario
     * @return Flux con las notificaciones del destinatario
     */
    Flux<Notification> findByRecipient(String recipient);

    /**
     * Busca notificaciones por tipo
     * @param type Tipo de notificación
     * @return Flux con las notificaciones del tipo especificado
     */
    Flux<Notification> findByType(String type);

    /**
     * Busca notificaciones por estado
     * @param status Estado de la notificación
     * @return Flux con las notificaciones en el estado especificado
     */
    Flux<Notification> findByStatus(Notification.NotificationStatus status);

    /**
     * Busca notificaciones por destinatario y estado
     * @param recipient Destinatario
     * @param status Estado de la notificación
     * @return Flux con las notificaciones que coinciden
     */
    Flux<Notification> findByRecipientAndStatus(String recipient, Notification.NotificationStatus status);

    /**
     * Actualiza una notificación existente
     * @param notification Notificación con los datos actualizados
     * @return Mono con la notificación actualizada
     */
    Mono<Notification> update(Notification notification);

    /**
     * Elimina una notificación por su ID
     * @param id ID de la notificación a eliminar
     * @return Mono vacío que completa cuando se elimina
     */
    Mono<Void> deleteById(Long id);

    /**
     * Verifica si existe una notificación con el ID dado
     * @param id ID de la notificación
     * @return Mono con true si existe, false en caso contrario
     */
    Mono<Boolean> existsById(Long id);

    /**
     * Cuenta las notificaciones no leídas de un destinatario
     * @param recipient Destinatario
     * @return Mono con el conteo de notificaciones no leídas
     */
    Mono<Long> countUnreadByRecipient(String recipient);
}
