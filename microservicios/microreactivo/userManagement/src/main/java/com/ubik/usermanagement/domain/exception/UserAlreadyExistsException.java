package com.ubik.usermanagement.domain.exception;

/**
 * Exception thrown when attempting to register a user that already exists
 */
public class UserAlreadyExistsException extends RuntimeException {
    
    public UserAlreadyExistsException(String message) {
        super(message);
    }
    
    public UserAlreadyExistsException(String value, String fieldName) {
        super(String.format("%s already exists: %s", fieldName, value));
    }
}
