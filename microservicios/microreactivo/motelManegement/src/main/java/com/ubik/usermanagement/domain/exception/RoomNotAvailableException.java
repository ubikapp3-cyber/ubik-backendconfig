package com.ubik.usermanagement.domain.exception;

/**
 * Exception thrown when a room is not available for the requested dates
 */
public class RoomNotAvailableException extends RuntimeException {
    
    public RoomNotAvailableException() {
        super("La habitación no está disponible para las fechas seleccionadas");
    }
    
    public RoomNotAvailableException(String message) {
        super(message);
    }
}
