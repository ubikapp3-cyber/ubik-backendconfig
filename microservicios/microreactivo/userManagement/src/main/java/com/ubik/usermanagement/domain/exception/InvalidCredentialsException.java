package com.ubik.usermanagement.domain.exception;

/**
 * Exception thrown when login credentials are invalid
 */
public class InvalidCredentialsException extends RuntimeException {
    
    public InvalidCredentialsException() {
        super("Invalid username or password");
    }
    
    public InvalidCredentialsException(String message) {
        super(message);
    }
}
