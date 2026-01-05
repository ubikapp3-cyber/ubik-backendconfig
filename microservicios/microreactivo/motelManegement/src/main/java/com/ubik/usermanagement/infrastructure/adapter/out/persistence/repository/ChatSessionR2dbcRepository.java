package com.ubik.usermanagement.infrastructure.adapter.out.persistence.repository;

import com.ubik.usermanagement.infrastructure.adapter.out.persistence.entity.ChatSessionEntity;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;

/**
 * Repositorio R2DBC para ChatSessionEntity
 */
@Repository
public interface ChatSessionR2dbcRepository extends ReactiveCrudRepository<ChatSessionEntity, Long> {

    /**
     * Busca sesiones activas de un usuario
     */
    @Query("SELECT * FROM chat_sessions WHERE user_id = :userId AND status = 'ACTIVE' ORDER BY last_activity_at DESC")
    Flux<ChatSessionEntity> findActiveSessionsByUserId(Long userId);
}
