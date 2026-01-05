package com.ubik.usermanagement.domain.exception;

/**
 * Exception thrown when a user is not found
 */
public class UserNotFoundException extends RuntimeException {
    
    public UserNotFoundException(String message) {
        super(message);
    }
    
    public UserNotFoundException(String username, String fieldName) {
        super(String.format("User not found with %s: %s", fieldName, username));
    }
}
