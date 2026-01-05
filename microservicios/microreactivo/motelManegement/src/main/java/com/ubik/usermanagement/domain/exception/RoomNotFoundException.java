package com.ubik.usermanagement.domain.exception;

/**
 * Exception thrown when a room is not found
 */
public class RoomNotFoundException extends RuntimeException {
    
    public RoomNotFoundException(Long id) {
        super(String.format("Habitación no encontrada con ID: %d", id));
    }
    
    public RoomNotFoundException(String message) {
        super(message);
    }
}
