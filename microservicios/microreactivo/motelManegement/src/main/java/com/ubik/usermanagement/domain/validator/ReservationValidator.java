package com.ubik.usermanagement.domain.validator;

import com.ubik.usermanagement.domain.model.Reservation;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;

/**
 * Validator for Reservation domain objects
 * Follows Single Responsibility Principle - only validates reservation data
 */
public class ReservationValidator {
    
    private static final int MAX_RESERVATION_DAYS = 30;
    private static final int CHECK_IN_GRACE_PERIOD_HOURS = 1;
    
    /**
     * Validates all required fields for a reservation
     */
    public Mono<Void> validate(Reservation reservation) {
        return validateRoomId(reservation.roomId())
                .then(validateUserId(reservation.userId()))
                .then(validateCheckInDate(reservation.checkInDate()))
                .then(validateCheckOutDate(reservation.checkOutDate()))
                .then(validateDateOrder(reservation.checkInDate(), reservation.checkOutDate()))
                .then(validateCheckInNotInPast(reservation.checkInDate(), reservation.status()))
                .then(validateTotalPrice(reservation.totalPrice()))
                .then(validateDuration(reservation.checkInDate(), reservation.checkOutDate()));
    }
    
    /**
     * Validates that room ID is not null
     */
    public Mono<Void> validateRoomId(Long roomId) {
        if (roomId == null) {
            return Mono.error(new IllegalArgumentException("El ID de la habitación es requerido"));
        }
        return Mono.empty();
    }
    
    /**
     * Validates that user ID is not null
     */
    public Mono<Void> validateUserId(Long userId) {
        if (userId == null) {
            return Mono.error(new IllegalArgumentException("El ID del usuario es requerido"));
        }
        return Mono.empty();
    }
    
    /**
     * Validates that check-in date is not null
     */
    public Mono<Void> validateCheckInDate(LocalDateTime checkInDate) {
        if (checkInDate == null) {
            return Mono.error(new IllegalArgumentException("La fecha de check-in es requerida"));
        }
        return Mono.empty();
    }
    
    /**
     * Validates that check-out date is not null
     */
    public Mono<Void> validateCheckOutDate(LocalDateTime checkOutDate) {
        if (checkOutDate == null) {
            return Mono.error(new IllegalArgumentException("La fecha de check-out es requerida"));
        }
        return Mono.empty();
    }
    
    /**
     * Validates that check-in is before check-out
     */
    public Mono<Void> validateDateOrder(LocalDateTime checkInDate, LocalDateTime checkOutDate) {
        if (!checkInDate.isBefore(checkOutDate)) {
            return Mono.error(new IllegalArgumentException(
                    "La fecha de check-in debe ser anterior a la fecha de check-out"));
        }
        return Mono.empty();
    }
    
    /**
     * Validates that check-in date is not in the past (for new reservations only)
     */
    public Mono<Void> validateCheckInNotInPast(LocalDateTime checkInDate, Reservation.ReservationStatus status) {
        // Only validate future dates for new reservations
        if (status == null || status == Reservation.ReservationStatus.PENDING) {
            LocalDateTime now = LocalDateTime.now();
            if (checkInDate.isBefore(now.minusHours(CHECK_IN_GRACE_PERIOD_HOURS))) {
                return Mono.error(new IllegalArgumentException(
                        "La fecha de check-in no puede ser en el pasado"));
            }
        }
        return Mono.empty();
    }
    
    /**
     * Validates that total price is positive
     */
    public Mono<Void> validateTotalPrice(Double totalPrice) {
        if (totalPrice == null || totalPrice <= 0) {
            return Mono.error(new IllegalArgumentException("El precio total debe ser mayor que cero"));
        }
        return Mono.empty();
    }
    
    /**
     * Validates that reservation duration doesn't exceed maximum
     */
    public Mono<Void> validateDuration(LocalDateTime checkInDate, LocalDateTime checkOutDate) {
        long daysBetween = java.time.temporal.ChronoUnit.DAYS.between(
                checkInDate.toLocalDate(), 
                checkOutDate.toLocalDate());
        
        if (daysBetween > MAX_RESERVATION_DAYS) {
            return Mono.error(new IllegalArgumentException(
                    String.format("La duración máxima de la reserva es de %d días", MAX_RESERVATION_DAYS)));
        }
        return Mono.empty();
    }
}
