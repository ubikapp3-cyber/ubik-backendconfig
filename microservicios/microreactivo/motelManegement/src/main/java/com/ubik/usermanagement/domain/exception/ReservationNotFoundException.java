package com.ubik.usermanagement.domain.exception;

/**
 * Exception thrown when a reservation is not found
 */
public class ReservationNotFoundException extends RuntimeException {
    
    public ReservationNotFoundException(Long id) {
        super(String.format("Reserva no encontrada con ID: %d", id));
    }
    
    public ReservationNotFoundException(String message) {
        super(message);
    }
}
