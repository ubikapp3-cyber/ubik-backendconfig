package com.ubik.usermanagement.domain.exception;

/**
 * Exception thrown when a motel is not found
 */
public class MotelNotFoundException extends RuntimeException {
    
    public MotelNotFoundException(Long id) {
        super(String.format("Motel no encontrado con ID: %d", id));
    }
    
    public MotelNotFoundException(String message) {
        super(message);
    }
}
