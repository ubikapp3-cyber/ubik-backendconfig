package com.ubik.usermanagement.domain.port.out;

import com.ubik.usermanagement.domain.model.Reservation;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;

/**
 * Puerto de salida (Output Port) para operaciones de persistencia de Reservation
 * Define el contrato que debe implementar la infraestructura
 */
public interface ReservationRepositoryPort {

    /**
     * Guarda una nueva reserva
     * @param reservation Reserva a guardar
     * @return Mono con la reserva guardada incluyendo su ID generado
     */
    Mono<Reservation> save(Reservation reservation);

    /**
     * Busca una reserva por su ID
     * @param id ID de la reserva
     * @return Mono con la reserva encontrada o vacío
     */
    Mono<Reservation> findById(Long id);

    /**
     * Busca todas las reservas
     * @return Flux con todas las reservas
     */
    Flux<Reservation> findAll();

    /**
     * Busca reservas por ID de habitación
     * @param roomId ID de la habitación
     * @return Flux con las reservas de la habitación
     */
    Flux<Reservation> findByRoomId(Long roomId);

    /**
     * Busca reservas por ID de usuario
     * @param userId ID del usuario
     * @return Flux con las reservas del usuario
     */
    Flux<Reservation> findByUserId(Long userId);

    /**
     * Busca reservas activas por ID de habitación
     * @param roomId ID de la habitación
     * @return Flux con las reservas activas
     */
    Flux<Reservation> findActiveReservationsByRoomId(Long roomId);

    /**
     * Busca reservas por estado
     * @param status Estado de la reserva
     * @return Flux con las reservas en ese estado
     */
    Flux<Reservation> findByStatus(Reservation.ReservationStatus status);

    /**
     * Busca reservas que se solapen con un rango de fechas
     * @param roomId ID de la habitación
     * @param checkIn Fecha de check-in
     * @param checkOut Fecha de check-out
     * @return Flux con las reservas que se solapan
     */
    Flux<Reservation> findOverlappingReservations(Long roomId, LocalDateTime checkIn, LocalDateTime checkOut);

    /**
     * Actualiza una reserva existente
     * @param reservation Reserva con los datos actualizados
     * @return Mono con la reserva actualizada
     */
    Mono<Reservation> update(Reservation reservation);

    /**
     * Elimina una reserva por su ID
     * @param id ID de la reserva a eliminar
     * @return Mono vacío que completa cuando se elimina
     */
    Mono<Void> deleteById(Long id);

    /**
     * Verifica si existe una reserva con el ID dado
     * @param id ID de la reserva
     * @return Mono con true si existe, false en caso contrario
     */
    Mono<Boolean> existsById(Long id);
}
