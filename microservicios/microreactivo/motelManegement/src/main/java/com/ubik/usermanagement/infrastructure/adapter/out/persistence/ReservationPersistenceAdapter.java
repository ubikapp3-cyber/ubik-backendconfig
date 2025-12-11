package com.ubik.usermanagement.infrastructure.adapter.out.persistence;

import com.ubik.usermanagement.domain.model.Reservation;
import com.ubik.usermanagement.domain.port.out.ReservationRepositoryPort;
import com.ubik.usermanagement.infrastructure.adapter.out.persistence.mapper.ReservationMapper;
import com.ubik.usermanagement.infrastructure.adapter.out.persistence.repository.ReservationR2dbcRepository;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;

/**
 * Adaptador de persistencia para Reservation
 * Implementa el puerto de salida ReservationRepositoryPort
 */
@Component
public class ReservationPersistenceAdapter implements ReservationRepositoryPort {

    private final ReservationR2dbcRepository reservationR2dbcRepository;
    private final ReservationMapper reservationMapper;

    public ReservationPersistenceAdapter(
            ReservationR2dbcRepository reservationR2dbcRepository,
            ReservationMapper reservationMapper) {
        this.reservationR2dbcRepository = reservationR2dbcRepository;
        this.reservationMapper = reservationMapper;
    }

    @Override
    public Mono<Reservation> save(Reservation reservation) {
        return Mono.just(reservation)
                .map(reservationMapper::toEntity)
                .flatMap(reservationR2dbcRepository::save)
                .map(reservationMapper::toDomain);
    }

    @Override
    public Mono<Reservation> findById(Long id) {
        return reservationR2dbcRepository.findById(id)
                .map(reservationMapper::toDomain);
    }

    @Override
    public Flux<Reservation> findAll() {
        return reservationR2dbcRepository.findAll()
                .map(reservationMapper::toDomain);
    }

    @Override
    public Flux<Reservation> findByRoomId(Long roomId) {
        return reservationR2dbcRepository.findByRoomId(roomId)
                .map(reservationMapper::toDomain);
    }

    @Override
    public Flux<Reservation> findByUserId(Long userId) {
        return reservationR2dbcRepository.findByUserId(userId)
                .map(reservationMapper::toDomain);
    }

    @Override
    public Flux<Reservation> findActiveReservationsByRoomId(Long roomId) {
        return reservationR2dbcRepository.findActiveReservationsByRoomId(roomId)
                .map(reservationMapper::toDomain);
    }

    @Override
    public Flux<Reservation> findByStatus(Reservation.ReservationStatus status) {
        return reservationR2dbcRepository.findByStatus(status.name())
                .map(reservationMapper::toDomain);
    }

    @Override
    public Flux<Reservation> findOverlappingReservations(Long roomId, LocalDateTime checkIn, LocalDateTime checkOut) {
        return reservationR2dbcRepository.findOverlappingReservations(roomId, checkIn, checkOut)
                .map(reservationMapper::toDomain);
    }

    @Override
    public Mono<Reservation> update(Reservation reservation) {
        return Mono.just(reservation)
                .map(reservationMapper::toEntity)
                .flatMap(reservationR2dbcRepository::save)
                .map(reservationMapper::toDomain);
    }

    @Override
    public Mono<Void> deleteById(Long id) {
        return reservationR2dbcRepository.deleteById(id);
    }

    @Override
    public Mono<Boolean> existsById(Long id) {
        return reservationR2dbcRepository.existsById(id);
    }
}
