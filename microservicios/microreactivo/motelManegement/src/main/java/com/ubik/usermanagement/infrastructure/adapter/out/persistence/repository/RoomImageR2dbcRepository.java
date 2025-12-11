package com.ubik.usermanagement.infrastructure.adapter.out.persistence.repository;

import com.ubik.usermanagement.infrastructure.adapter.out.persistence.entity.RoomImageEntity;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Repositorio R2DBC para RoomImageEntity
 * Extiende ReactiveCrudRepository para operaciones CRUD reactivas
 */
@Repository
public interface RoomImageR2dbcRepository extends ReactiveCrudRepository<RoomImageEntity, Long> {

    /**
     * Busca imágenes por ID de habitación ordenadas por orden de visualización
     * @param roomId ID de la habitación
     * @return Flux con las imágenes encontradas
     */
    Flux<RoomImageEntity> findByRoomIdOrderByDisplayOrder(Long roomId);

    /**
     * Elimina todas las imágenes de una habitación
     * @param roomId ID de la habitación
     * @return Mono vacío que completa cuando la operación termina
     */
    Mono<Void> deleteByRoomId(Long roomId);
}
