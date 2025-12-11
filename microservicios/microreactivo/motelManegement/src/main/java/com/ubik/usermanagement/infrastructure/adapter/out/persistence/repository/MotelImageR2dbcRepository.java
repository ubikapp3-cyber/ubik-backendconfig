package com.ubik.usermanagement.infrastructure.adapter.out.persistence.repository;

import com.ubik.usermanagement.infrastructure.adapter.out.persistence.entity.MotelImageEntity;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Repositorio R2DBC para MotelImageEntity
 * Extiende ReactiveCrudRepository para operaciones CRUD reactivas
 */
@Repository
public interface MotelImageR2dbcRepository extends ReactiveCrudRepository<MotelImageEntity, Long> {

    /**
     * Busca imágenes por ID de motel ordenadas por orden de visualización
     * @param motelId ID del motel
     * @return Flux con las imágenes encontradas
     */
    Flux<MotelImageEntity> findByMotelIdOrderByDisplayOrder(Long motelId);

    /**
     * Elimina todas las imágenes de un motel
     * @param motelId ID del motel
     * @return Mono vacío que completa cuando la operación termina
     */
    Mono<Void> deleteByMotelId(Long motelId);
}
