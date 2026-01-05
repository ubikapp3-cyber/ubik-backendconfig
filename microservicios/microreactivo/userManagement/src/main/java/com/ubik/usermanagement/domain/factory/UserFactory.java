package com.ubik.usermanagement.domain.factory;

import com.ubik.usermanagement.domain.model.User;
import com.ubik.usermanagement.infrastructure.adapter.in.web.dto.RegisterRequest;

import java.time.LocalDateTime;

/**
 * Factory for creating User domain objects
 * Follows Single Responsibility Principle - only responsible for User creation
 */
public class UserFactory {
    
    /**
     * Creates a new User from registration request with encoded password
     * 
     * @param request the registration request containing user data
     * @param encodedPassword the password already encoded
     * @return a new User domain object
     */
    public User createFromRegistration(RegisterRequest request, String encodedPassword) {
        return new User(
            null,                           // id - will be set by database
            request.username(),
            encodedPassword,
            request.email(),
            null,                           // phoneNumber
            null,                           // createdAt - will be set by database
            request.anonymous(),
            request.roleId(),
            null,                           // resetToken
            null                            // resetTokenExpiry
        );
    }
    
    /**
     * Creates a User with password reset token
     * 
     * @param existingUser the existing user
     * @param resetToken the password reset token
     * @param tokenExpiry when the token expires
     * @return updated User with reset token
     */
    public User createWithResetToken(User existingUser, String resetToken, LocalDateTime tokenExpiry) {
        return new User(
            existingUser.id(),
            existingUser.username(),
            existingUser.password(),
            existingUser.email(),
            existingUser.phoneNumber(),
            existingUser.createdAt(),
            existingUser.anonymous(),
            existingUser.roleId(),
            resetToken,
            tokenExpiry
        );
    }
    
    /**
     * Creates a User with new password (clearing reset token)
     * 
     * @param existingUser the existing user
     * @param newEncodedPassword the new encoded password
     * @return updated User with new password and cleared reset token
     */
    public User createWithNewPassword(User existingUser, String newEncodedPassword) {
        return new User(
            existingUser.id(),
            existingUser.username(),
            newEncodedPassword,
            existingUser.email(),
            existingUser.phoneNumber(),
            existingUser.createdAt(),
            existingUser.anonymous(),
            existingUser.roleId(),
            null,                           // clear reset token
            null                            // clear token expiry
        );
    }
}
