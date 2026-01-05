package com.ubik.usermanagement.domain.exception;

/**
 * Exception thrown when a reservation state transition is invalid
 */
public class InvalidReservationStateException extends RuntimeException {
    
    public InvalidReservationStateException(String operation, String currentState) {
        super(String.format("No se puede realizar la operación '%s' en el estado actual: %s", 
                operation, currentState));
    }
    
    public InvalidReservationStateException(String message) {
        super(message);
    }
}
