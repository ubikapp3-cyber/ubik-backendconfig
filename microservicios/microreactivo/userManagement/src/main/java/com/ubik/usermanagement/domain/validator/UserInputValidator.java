package com.ubik.usermanagement.domain.validator;

import reactor.core.publisher.Mono;

/**
 * Validator for user input data
 * Follows Single Responsibility Principle - only validates input data
 */
public class UserInputValidator {
    
    private static final int MIN_PASSWORD_LENGTH = 6;
    // RFC 5322 compliant email regex (simplified but more robust)
    private static final String EMAIL_REGEX = "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,}$";
    
    /**
     * Validates that username is not null or empty
     */
    public Mono<Void> validateUsername(String username) {
        if (username == null || username.trim().isEmpty()) {
            return Mono.error(new IllegalArgumentException("Username is required"));
        }
        return Mono.empty();
    }
    
    /**
     * Validates that email is not null or empty and has valid format
     */
    public Mono<Void> validateEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            return Mono.error(new IllegalArgumentException("Email is required"));
        }
        if (!email.matches(EMAIL_REGEX)) {
            return Mono.error(new IllegalArgumentException("Email format is invalid"));
        }
        return Mono.empty();
    }
    
    /**
     * Validates password strength
     */
    public Mono<Void> validatePassword(String password) {
        if (password == null || password.trim().isEmpty()) {
            return Mono.error(new IllegalArgumentException("Password is required"));
        }
        if (password.length() < MIN_PASSWORD_LENGTH) {
            return Mono.error(new IllegalArgumentException(
                String.format("Password must be at least %d characters", MIN_PASSWORD_LENGTH)));
        }
        return Mono.empty();
    }
    
    /**
     * Validates that token is not null or empty
     */
    public Mono<Void> validateToken(String token) {
        if (token == null || token.trim().isEmpty()) {
            return Mono.error(new IllegalArgumentException("Reset token is required"));
        }
        return Mono.empty();
    }
}
