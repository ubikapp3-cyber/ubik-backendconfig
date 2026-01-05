package com.ubik.usermanagement.domain.exception;

/**
 * Exception thrown when attempting to request a password reset while an active token exists
 */
public class ActiveResetTokenException extends RuntimeException {
    
    public ActiveResetTokenException() {
        super("A password reset request is already active. Please wait before requesting another.");
    }
    
    public ActiveResetTokenException(String message) {
        super(message);
    }
}
