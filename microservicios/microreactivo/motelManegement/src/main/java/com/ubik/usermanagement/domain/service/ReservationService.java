package com.ubik.usermanagement.domain.service;

import com.ubik.usermanagement.domain.exception.InvalidReservationStateException;
import com.ubik.usermanagement.domain.exception.ReservationNotFoundException;
import com.ubik.usermanagement.domain.exception.RoomNotAvailableException;
import com.ubik.usermanagement.domain.exception.RoomNotFoundException;
import com.ubik.usermanagement.domain.model.Reservation;
import com.ubik.usermanagement.domain.port.in.ReservationUseCasePort;
import com.ubik.usermanagement.domain.port.out.ReservationRepositoryPort;
import com.ubik.usermanagement.domain.port.out.RoomRepositoryPort;
import com.ubik.usermanagement.domain.validator.ReservationValidator;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;

/**
 * Servicio de dominio que implementa los casos de uso de Reservation
 * Refactored to follow SOLID principles with extracted validation and early returns
 */
@Service
public class ReservationService implements ReservationUseCasePort {

    private final ReservationRepositoryPort reservationRepositoryPort;
    private final RoomRepositoryPort roomRepositoryPort;
    private final ReservationValidator reservationValidator;

    public ReservationService(
            ReservationRepositoryPort reservationRepositoryPort,
            RoomRepositoryPort roomRepositoryPort,
            ReservationValidator reservationValidator
    ) {
        this.reservationRepositoryPort = reservationRepositoryPort;
        this.roomRepositoryPort = roomRepositoryPort;
        this.reservationValidator = reservationValidator;
    }

    @Override
    public Mono<Reservation> createReservation(Reservation reservation) {
        return reservationValidator.validate(reservation)
                .then(checkRoomExists(reservation.roomId()))
                .then(checkRoomAvailability(reservation))
                .then(reservationRepositoryPort.save(reservation));
    }

    @Override
    public Mono<Reservation> getReservationById(Long id) {
        return findReservationById(id);
    }

    @Override
    public Flux<Reservation> getAllReservations() {
        return reservationRepositoryPort.findAll();
    }

    @Override
    public Flux<Reservation> getReservationsByRoomId(Long roomId) {
        return reservationRepositoryPort.findByRoomId(roomId);
    }

    @Override
    public Flux<Reservation> getReservationsByUserId(Long userId) {
        return reservationRepositoryPort.findByUserId(userId);
    }

    @Override
    public Flux<Reservation> getActiveReservationsByRoomId(Long roomId) {
        return reservationRepositoryPort.findActiveReservationsByRoomId(roomId);
    }

    @Override
    public Flux<Reservation> getReservationsByStatus(Reservation.ReservationStatus status) {
        return reservationRepositoryPort.findByStatus(status);
    }

    @Override
    public Mono<Boolean> isRoomAvailable(Long roomId, LocalDateTime checkIn, LocalDateTime checkOut) {
        return reservationRepositoryPort.findOverlappingReservations(roomId, checkIn, checkOut)
                .filter(Reservation::isActive)
                .hasElements()
                .map(hasOverlapping -> !hasOverlapping);
    }

    @Override
    public Mono<Reservation> updateReservation(Long id, Reservation reservation) {
        return findReservationById(id)
                .flatMap(existingReservation -> validateAndUpdateReservation(id, reservation, existingReservation));
    }

    @Override
    public Mono<Reservation> confirmReservation(Long id) {
        return findReservationById(id)
                .flatMap(this::performConfirmation);
    }

    @Override
    public Mono<Reservation> cancelReservation(Long id) {
        return findReservationById(id)
                .flatMap(this::performCancellation);
    }

    @Override
    public Mono<Reservation> checkIn(Long id) {
        return findReservationById(id)
                .flatMap(this::performCheckIn);
    }

    @Override
    public Mono<Reservation> checkOut(Long id) {
        return findReservationById(id)
                .flatMap(this::performCheckOut);
    }

    @Override
    public Mono<Void> deleteReservation(Long id) {
        return findReservationById(id)
                .flatMap(this::performDeletion);
    }

    /**
     * Finds reservation by ID or throws exception
     */
    private Mono<Reservation> findReservationById(Long id) {
        return reservationRepositoryPort.findById(id)
                .switchIfEmpty(Mono.error(new ReservationNotFoundException(id)));
    }

    /**
     * Checks if room exists or throws exception
     */
    private Mono<Void> checkRoomExists(Long roomId) {
        return roomRepositoryPort.existsById(roomId)
                .flatMap(exists -> {
                    if (!exists) {
                        return Mono.error(new RoomNotFoundException(roomId));
                    }
                    return Mono.empty();
                });
    }

    /**
     * Checks if room is available for the reservation dates
     */
    private Mono<Void> checkRoomAvailability(Reservation reservation) {
        return reservationRepositoryPort.findOverlappingReservations(
                reservation.roomId(), 
                reservation.checkInDate(), 
                reservation.checkOutDate())
                .filter(Reservation::isActive)
                .hasElements()
                .flatMap(hasOverlapping -> {
                    if (hasOverlapping) {
                        return Mono.error(new RoomNotAvailableException());
                    }
                    return Mono.empty();
                });
    }

    /**
     * Validates and updates reservation, checking availability if dates changed
     */
    private Mono<Reservation> validateAndUpdateReservation(Long id, Reservation reservation, Reservation existingReservation) {
        if (!existingReservation.canBeCancelled()) {
            return Mono.error(new InvalidReservationStateException(
                    "modificar", existingReservation.status().toString()));
        }

        boolean datesChanged = haveDatesChanged(existingReservation, reservation);
        Reservation updatedReservation = buildUpdatedReservation(id, reservation, existingReservation);

        return reservationValidator.validate(updatedReservation)
                .then(datesChanged ? checkAvailabilityForUpdate(id, updatedReservation) : Mono.empty())
                .then(reservationRepositoryPort.update(updatedReservation));
    }

    /**
     * Checks if check-in or check-out dates have changed
     */
    private boolean haveDatesChanged(Reservation existing, Reservation updated) {
        return !existing.checkInDate().equals(updated.checkInDate()) ||
               !existing.checkOutDate().equals(updated.checkOutDate());
    }

    /**
     * Builds updated reservation with preserved fields
     */
    private Reservation buildUpdatedReservation(Long id, Reservation newData, Reservation existing) {
        return new Reservation(
                id,
                existing.roomId(),
                existing.userId(),
                newData.checkInDate(),
                newData.checkOutDate(),
                existing.status(),
                newData.totalPrice(),
                newData.specialRequests(),
                existing.createdAt(),
                LocalDateTime.now()
        );
    }

    /**
     * Checks availability for updated reservation dates
     */
    private Mono<Void> checkAvailabilityForUpdate(Long reservationId, Reservation reservation) {
        return reservationRepositoryPort.findOverlappingReservations(
                reservation.roomId(),
                reservation.checkInDate(),
                reservation.checkOutDate())
                .filter(r -> r.isActive() && !r.id().equals(reservationId))
                .hasElements()
                .flatMap(hasOverlapping -> {
                    if (hasOverlapping) {
                        return Mono.error(new RoomNotAvailableException(
                                "La habitación no está disponible para las nuevas fechas"));
                    }
                    return Mono.empty();
                });
    }

    /**
     * Performs confirmation state transition
     */
    private Mono<Reservation> performConfirmation(Reservation reservation) {
        if (!reservation.canBeConfirmed()) {
            return Mono.error(new InvalidReservationStateException(
                    "confirmar", reservation.status().toString()));
        }
        return reservationRepositoryPort.update(
                reservation.withStatus(Reservation.ReservationStatus.CONFIRMED));
    }

    /**
     * Performs cancellation state transition
     */
    private Mono<Reservation> performCancellation(Reservation reservation) {
        if (!reservation.canBeCancelled()) {
            return Mono.error(new InvalidReservationStateException(
                    "cancelar", reservation.status().toString()));
        }
        return reservationRepositoryPort.update(
                reservation.withStatus(Reservation.ReservationStatus.CANCELLED));
    }

    /**
     * Performs check-in state transition
     */
    private Mono<Reservation> performCheckIn(Reservation reservation) {
        if (!reservation.canCheckIn()) {
            return Mono.error(new InvalidReservationStateException(
                    "check-in", reservation.status().toString()));
        }
        return reservationRepositoryPort.update(
                reservation.withStatus(Reservation.ReservationStatus.CHECKED_IN));
    }

    /**
     * Performs check-out state transition
     */
    private Mono<Reservation> performCheckOut(Reservation reservation) {
        if (!reservation.canCheckOut()) {
            return Mono.error(new InvalidReservationStateException(
                    "check-out", reservation.status().toString()));
        }
        return reservationRepositoryPort.update(
                reservation.withStatus(Reservation.ReservationStatus.CHECKED_OUT));
    }

    /**
     * Performs deletion, only allowed for cancelled reservations
     */
    private Mono<Void> performDeletion(Reservation reservation) {
        if (reservation.status() != Reservation.ReservationStatus.CANCELLED) {
            return Mono.error(new IllegalArgumentException(
                    "Solo se pueden eliminar reservas canceladas"));
        }
        return reservationRepositoryPort.deleteById(reservation.id());
    }
}