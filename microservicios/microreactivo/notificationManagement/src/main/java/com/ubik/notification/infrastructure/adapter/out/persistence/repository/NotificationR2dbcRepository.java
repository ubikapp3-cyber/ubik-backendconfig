package com.ubik.notification.infrastructure.adapter.out.persistence.repository;

import com.ubik.notification.infrastructure.adapter.out.persistence.entity.NotificationEntity;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Repositorio R2DBC para operaciones de persistencia reactiva
 */
@Repository
public interface NotificationR2dbcRepository extends R2dbcRepository<NotificationEntity, Long> {

    /**
     * Busca notificaciones por destinatario
     */
    Flux<NotificationEntity> findByRecipient(String recipient);

    /**
     * Busca notificaciones por tipo
     */
    Flux<NotificationEntity> findByType(String type);

    /**
     * Busca notificaciones por estado
     */
    Flux<NotificationEntity> findByStatus(String status);

    /**
     * Busca notificaciones por destinatario y estado
     */
    Flux<NotificationEntity> findByRecipientAndStatus(String recipient, String status);

    /**
     * Cuenta las notificaciones no leídas de un destinatario
     */
    @Query("SELECT COUNT(*) FROM notifications WHERE recipient = :recipient AND status IN ('PENDING', 'SENT')")
    Mono<Long> countUnreadByRecipient(String recipient);
}
