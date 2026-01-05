package com.ubik.usermanagement.domain.exception;

/**
 * Exception thrown when a reset token is invalid or expired
 */
public class InvalidTokenException extends RuntimeException {
    
    public InvalidTokenException() {
        super("Invalid or expired token");
    }
    
    public InvalidTokenException(String message) {
        super(message);
    }
}
