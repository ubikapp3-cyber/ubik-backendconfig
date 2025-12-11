package com.ubik.usermanagement.infrastructure.adapter.out.persistence.repository;

import com.ubik.usermanagement.infrastructure.adapter.out.persistence.entity.ReservationEntity;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;

import java.time.LocalDateTime;

/**
 * Repositorio R2DBC para ReservationEntity
 * Extiende ReactiveCrudRepository para operaciones CRUD reactivas
 */
@Repository
public interface ReservationR2dbcRepository extends ReactiveCrudRepository<ReservationEntity, Long> {

    /**
     * Busca reservas por ID de habitación
     * @param roomId ID de la habitación
     * @return Flux con las reservas encontradas
     */
    Flux<ReservationEntity> findByRoomId(Long roomId);

    /**
     * Busca reservas por ID de usuario
     * @param userId ID del usuario
     * @return Flux con las reservas encontradas
     */
    Flux<ReservationEntity> findByUserId(Long userId);

    /**
     * Busca reservas por estado
     * @param status Estado de la reserva
     * @return Flux con las reservas encontradas
     */
    Flux<ReservationEntity> findByStatus(String status);

    /**
     * Busca reservas activas por ID de habitación
     * Las reservas activas son aquellas que no están canceladas ni en check-out
     * @param roomId ID de la habitación
     * @return Flux con las reservas activas
     */
    @Query("SELECT * FROM reservation WHERE room_id = :roomId AND status NOT IN ('CANCELLED', 'CHECKED_OUT')")
    Flux<ReservationEntity> findActiveReservationsByRoomId(Long roomId);

    /**
     * Busca reservas que se solapen con un rango de fechas para una habitación específica
     * @param roomId ID de la habitación
     * @param checkIn Fecha de check-in
     * @param checkOut Fecha de check-out
     * @return Flux con las reservas que se solapan
     */
    @Query("SELECT * FROM reservation WHERE room_id = :roomId AND " +
           "((check_in_date >= :checkIn AND check_in_date < :checkOut) OR " +
           "(check_out_date > :checkIn AND check_out_date <= :checkOut) OR " +
           "(check_in_date <= :checkIn AND check_out_date >= :checkOut))")
    Flux<ReservationEntity> findOverlappingReservations(Long roomId, LocalDateTime checkIn, LocalDateTime checkOut);
}
