package com.ubik.usermanagement.domain.validator;

import com.ubik.usermanagement.domain.constants.ReservationErrorMessages;
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
            return Mono.error(new IllegalArgumentException(ReservationErrorMessages.ROOM_ID_REQUIRED));
        }
        return Mono.empty();
    }
    
    /**
     * Validates that user ID is not null
     */
    public Mono<Void> validateUserId(Long userId) {
        if (userId == null) {
            return Mono.error(new IllegalArgumentException(ReservationErrorMessages.USER_ID_REQUIRED));
        }
        return Mono.empty();
    }
    
    /**
     * Validates that check-in date is not null
     */
    public Mono<Void> validateCheckInDate(LocalDateTime checkInDate) {
        if (checkInDate == null) {
            return Mono.error(new IllegalArgumentException(ReservationErrorMessages.CHECK_IN_DATE_REQUIRED));
        }
        return Mono.empty();
    }
    
    /**
     * Validates that check-out date is not null
     */
    public Mono<Void> validateCheckOutDate(LocalDateTime checkOutDate) {
        if (checkOutDate == null) {
            return Mono.error(new IllegalArgumentException(ReservationErrorMessages.CHECK_OUT_DATE_REQUIRED));
        }
        return Mono.empty();
    }
    
    /**
     * Validates that check-in is before check-out
     */
    public Mono<Void> validateDateOrder(LocalDateTime checkInDate, LocalDateTime checkOutDate) {
        if (!checkInDate.isBefore(checkOutDate)) {
            return Mono.error(new IllegalArgumentException(ReservationErrorMessages.CHECK_IN_BEFORE_CHECK_OUT));
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
                return Mono.error(new IllegalArgumentException(ReservationErrorMessages.CHECK_IN_NOT_IN_PAST));
            }
        }
        return Mono.empty();
    }
    
    /**
     * Validates that total price is positive
     */
    public Mono<Void> validateTotalPrice(Double totalPrice) {
        if (totalPrice == null || totalPrice <= 0) {
            return Mono.error(new IllegalArgumentException(ReservationErrorMessages.TOTAL_PRICE_POSITIVE));
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
                    String.format(ReservationErrorMessages.MAX_DURATION_EXCEEDED_FORMAT, MAX_RESERVATION_DAYS)));
        }
        return Mono.empty();
    }
}
