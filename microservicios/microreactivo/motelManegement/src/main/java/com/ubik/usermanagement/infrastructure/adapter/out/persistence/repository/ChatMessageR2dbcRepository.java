package com.ubik.usermanagement.infrastructure.adapter.out.persistence.repository;

import com.ubik.usermanagement.infrastructure.adapter.out.persistence.entity.ChatMessageEntity;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Repositorio R2DBC para ChatMessageEntity
 */
@Repository
public interface ChatMessageR2dbcRepository extends ReactiveCrudRepository<ChatMessageEntity, Long> {

    /**
     * Busca todos los mensajes de una sesión ordenados por timestamp
     */
    @Query("SELECT * FROM chat_messages WHERE session_id = :sessionId ORDER BY timestamp ASC")
    Flux<ChatMessageEntity> findBySessionIdOrderByTimestamp(Long sessionId);

    /**
     * Elimina todos los mensajes de una sesión
     */
    @Query("DELETE FROM chat_messages WHERE session_id = :sessionId")
    Mono<Void> deleteBySessionId(Long sessionId);
}
