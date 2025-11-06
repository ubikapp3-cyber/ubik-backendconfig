package com.ubik.usermanagement.domain.service;

import com.ubik.usermanagement.domain.model.Reservation;
import com.ubik.usermanagement.domain.model.RoomAvailability;
import com.ubik.usermanagement.domain.port.in.ReservationUseCasePort;
import com.ubik.usermanagement.domain.port.out.ReservationRepositoryPort;
import com.ubik.usermanagement.domain.port.out.RoomRepositoryPort;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;

@Service
public class ReservationService implements ReservationUseCasePort {

    private final ReservationRepositoryPort reservationRepository;
    private final RoomRepositoryPort roomRepository;

    public ReservationService(ReservationRepositoryPort reservationRepository, 
                            RoomRepositoryPort roomRepository) {
        this.reservationRepository = reservationRepository;
        this.roomRepository = roomRepository;
    }

    @Override
    public Mono<RoomAvailability> checkRoomAvailability(Long roomId, LocalDateTime checkIn, 
                                                       LocalDateTime checkOut) {
        return validateDates(checkIn, checkOut)
            .then(roomRepository.existsById(roomId))
            .flatMap(exists -> {
                if (!exists) {
                    return Mono.error(new RuntimeException("Habitación no encontrada"));
                }
                return reservationRepository.hasConflictingReservations(roomId, checkIn, checkOut);
            })
            .map(hasConflict -> hasConflict 
                ? RoomAvailability.unavailable(roomId, checkIn, checkOut, "reserved")
                : RoomAvailability.available(roomId, checkIn, checkOut)
            );
    }

    @Override
    public Flux<Long> findAvailableRooms(Long motelId, LocalDateTime checkIn, LocalDateTime checkOut) {
        return validateDates(checkIn, checkOut)
            .thenMany(roomRepository.findByMotelId(motelId))
            .flatMap(room -> 
                checkRoomAvailability(room.id(), checkIn, checkOut)
                    .filter(RoomAvailability::isAvailable)
                    .map(availability -> room.id())
            );
    }

    @Override
    public Mono<Reservation> createReservation(Reservation reservation) {
        return validateReservation(reservation)
            .then(checkRoomAvailability(reservation.roomId(), 
                                       reservation.checkIn(), 
                                       reservation.checkOut()))
            .flatMap(availability -> {
                if (!availability.isAvailable()) {
                    return Mono.error(new RuntimeException(
                        "Habitación no disponible en las fechas seleccionadas"));
                }
                return reservationRepository.save(reservation);
            });
    }

    @Override
    public Mono<Reservation> confirmReservation(Long id) {
        return reservationRepository.findById(id)
            .switchIfEmpty(Mono.error(new RuntimeException("Reserva no encontrada")))
            .flatMap(reservation -> {
                if (reservation.status() != Reservation.ReservationStatus.PENDING) {
                    return Mono.error(new RuntimeException(
                        "Solo se pueden confirmar reservas pendientes"));
                }
                return reservationRepository.update(reservation.confirm());
            });
    }

    @Override
    public Mono<Reservation> checkIn(Long id) {
        return reservationRepository.findById(id)
            .switchIfEmpty(Mono.error(new RuntimeException("Reserva no encontrada")))
            .flatMap(reservation -> {
                if (reservation.status() != Reservation.ReservationStatus.CONFIRMED) {
                    return Mono.error(new RuntimeException(
                        "Solo se puede hacer check-in de reservas confirmadas"));
                }
                if (reservation.checkIn().isAfter(LocalDateTime.now().plusHours(2))) {
                    return Mono.error(new RuntimeException(
                        "Check-in solo disponible 2 horas antes de la hora reservada"));
                }
                return reservationRepository.update(reservation.checkIn());
            });
    }

    @Override
    public Mono<Reservation> checkOut(Long id) {
        return reservationRepository.findById(id)
            .switchIfEmpty(Mono.error(new RuntimeException("Reserva no encontrada")))
            .flatMap(reservation -> {
                if (reservation.status() != Reservation.ReservationStatus.CHECKED_IN) {
                    return Mono.error(new RuntimeException(
                        "Solo se puede hacer check-out si ya se hizo check-in"));
                }
                return reservationRepository.update(reservation.checkOut());
            });
    }

    @Override
    public Mono<Reservation> cancelReservation(Long id) {
        return reservationRepository.findById(id)
            .switchIfEmpty(Mono.error(new RuntimeException("Reserva no encontrada")))
            .flatMap(reservation -> {
                if (!reservation.canBeCancelled()) {
                    return Mono.error(new RuntimeException(
                        "Esta reserva no puede ser cancelada"));
                }
                return reservationRepository.update(reservation.cancel());
            });
    }

    @Override
    public Mono<Void> lockRoom(Long roomId, String sessionId, int minutesToLock) {
        return reservationRepository.lockRoom(roomId, sessionId, minutesToLock);
    }

    @Override
    public Mono<Void> unlockRoom(Long roomId, String sessionId) {
        return reservationRepository.unlockRoom(roomId, sessionId);
    }

    // Métodos auxiliares
    private Mono<Void> validateDates(LocalDateTime checkIn, LocalDateTime checkOut) {
        if (checkIn.isAfter(checkOut)) {
            return Mono.error(new IllegalArgumentException(
                "La fecha de entrada debe ser anterior a la de salida"));
        }
        if (checkIn.isBefore(LocalDateTime.now())) {
            return Mono.error(new IllegalArgumentException(
                "La fecha de entrada no puede ser en el pasado"));
        }
        return Mono.empty();
    }

    private Mono<Void> validateReservation(Reservation reservation) {
        if (reservation.customerName() == null || reservation.customerName().trim().isEmpty()) {
            return Mono.error(new IllegalArgumentException("El nombre del cliente es requerido"));
        }
        if (reservation.totalPrice() == null || reservation.totalPrice() <= 0) {
            return Mono.error(new IllegalArgumentException("El precio debe ser mayor que cero"));
        }
        return validateDates(reservation.checkIn(), reservation.checkOut());
    }

    @Override
    public Mono<Reservation> getReservationById(Long id) {
        return reservationRepository.findById(id)
            .switchIfEmpty(Mono.error(new RuntimeException("Reserva no encontrada")));
    }

    @Override
    public Flux<Reservation> getReservationsByRoomId(Long roomId) {
        return reservationRepository.findByRoomId(roomId);
    }

    @Override
    public Flux<Reservation> getReservationsByCustomerEmail(String email) {
        return reservationRepository.findByCustomerEmail(email);
    }

    @Override
    public Flux<Reservation> getActiveReservations(Long motelId) {
        return reservationRepository.findActiveReservationsByMotelId(motelId);
    }
}