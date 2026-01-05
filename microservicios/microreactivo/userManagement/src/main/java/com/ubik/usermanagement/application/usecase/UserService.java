package com.ubik.usermanagement.application.usecase;

import com.ubik.usermanagement.application.port.in.UserUseCase;
import com.ubik.usermanagement.application.port.out.UserRepositoryPort;
import com.ubik.usermanagement.domain.model.User;
import com.ubik.usermanagement.infrastructure.adapter.in.web.dto.LoginRequest;
import com.ubik.usermanagement.infrastructure.adapter.in.web.dto.RegisterRequest;
import com.ubik.usermanagement.infrastructure.adapter.in.web.dto.ResetPasswordRequest;
import com.ubik.usermanagement.infrastructure.adapter.out.jwt.JwtAdapter;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class UserService implements UserUseCase {

    private static final int MIN_PASSWORD_LENGTH = 6;

    private final UserRepositoryPort userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtAdapter jwtAdapter;

    public UserService(UserRepositoryPort userRepository, PasswordEncoder passwordEncoder, JwtAdapter jwtAdapter) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtAdapter = jwtAdapter;
    }

    @Override
    public Mono<String> register(RegisterRequest request) {
        return validateRegistrationInput(request)
                .then(userRepository.findByUsername(request.username())
                        .flatMap(existing -> Mono.<String>error(new RuntimeException("Username already exists")))
                        .switchIfEmpty(
                                userRepository.findByEmail(request.email())
                                        .flatMap(existing -> Mono.<String>error(new RuntimeException("Email already exists")))
                                        .switchIfEmpty(Mono.defer(() -> {

                                            User user = new User(
                                                    null,
                                                    request.username(),
                                                    passwordEncoder.encode(request.password()),
                                                    request.email(),
                                                    null,                    // phoneNumber
                                                    null,                    // createdAt
                                                    request.anonymous(),
                                                    request.roleId(),        // ahora es Integer
                                                    null,                    // resetToken
                                                    null                     // resetTokenExpiry
                                            );

                                            return userRepository.save(user)
                                                    .map(saved -> jwtAdapter.generateToken(
                                                            saved.username(),
                                                            saved.roleId()       // Integer, se envía al JWT
                                                    ));
                                        }))
                        )
                );
    }

    @Override
    public Mono<String> login(LoginRequest request) {
        return validateLoginInput(request)
                .then(userRepository.findByUsername(request.username())
                        .filter(user -> passwordEncoder.matches(request.password(), user.password()))
                        .switchIfEmpty(Mono.error(new RuntimeException("Invalid credentials")))
                        .map(user -> jwtAdapter.generateToken(
                                user.username(),
                                user.roleId()      // Integer para JWT
                        ))
                );
    }

    @Override
    public Mono<String> requestPasswordReset(String email) {
        if (email == null || email.trim().isEmpty()) {
            return Mono.error(new IllegalArgumentException("Email is required"));
        }

        String resetToken = UUID.randomUUID().toString();

        return userRepository.findByEmail(email)
                .switchIfEmpty(Mono.error(new RuntimeException("Email not found")))
                .flatMap(user -> {
                    // Check if there's already a valid reset token
                    if (user.resetToken() != null && user.resetTokenExpiry() != null 
                            && user.resetTokenExpiry().isAfter(LocalDateTime.now())) {
                        return Mono.error(new IllegalStateException(
                                "A password reset request is already active. Please wait before requesting another."));
                    }
                    
                    return userRepository.save(new User(
                            user.id(),
                            user.username(),
                            user.password(),
                            user.email(),
                            user.phoneNumber(),
                            user.createdAt(),
                            user.anonymous(),
                            user.roleId(),
                            resetToken,
                            LocalDateTime.now().plusHours(1)
                    ));
                })
                .map(user -> resetToken);
    }

    @Override
    public Mono<String> resetPassword(ResetPasswordRequest request) {
        return validatePasswordResetInput(request)
                .then(userRepository.findByResetToken(request.token())
                        .filter(user -> user.resetTokenExpiry() != null && user.resetTokenExpiry().isAfter(LocalDateTime.now()))
                        .switchIfEmpty(Mono.error(new RuntimeException("Invalid or expired token")))
                        .flatMap(user -> userRepository.save(new User(
                                user.id(),
                                user.username(),
                                passwordEncoder.encode(request.newPassword()),
                                user.email(),
                                user.phoneNumber(),
                                user.createdAt(),
                                user.anonymous(),
                                user.roleId(),          // Integer
                                null,
                                null
                        )))
                        .map(user -> "Password reset successfully")
                );
    }

    /**
     * Validates registration input
     */
    private Mono<Void> validateRegistrationInput(RegisterRequest request) {
        if (request.username() == null || request.username().trim().isEmpty()) {
            return Mono.error(new IllegalArgumentException("Username is required"));
        }
        if (request.email() == null || request.email().trim().isEmpty()) {
            return Mono.error(new IllegalArgumentException("Email is required"));
        }
        return validatePassword(request.password());
    }

    /**
     * Validates login input
     */
    private Mono<Void> validateLoginInput(LoginRequest request) {
        if (request.username() == null || request.username().trim().isEmpty()) {
            return Mono.error(new IllegalArgumentException("Username is required"));
        }
        if (request.password() == null || request.password().trim().isEmpty()) {
            return Mono.error(new IllegalArgumentException("Password is required"));
        }
        return Mono.empty();
    }

    /**
     * Validates password reset input
     */
    private Mono<Void> validatePasswordResetInput(ResetPasswordRequest request) {
        if (request.token() == null || request.token().trim().isEmpty()) {
            return Mono.error(new IllegalArgumentException("Reset token is required"));
        }
        return validatePassword(request.newPassword());
    }

    /**
     * Validates password strength
     */
    private Mono<Void> validatePassword(String password) {
        if (password == null || password.trim().isEmpty()) {
            return Mono.error(new IllegalArgumentException("Password is required"));
        }
        if (password.length() < MIN_PASSWORD_LENGTH) {
            return Mono.error(new IllegalArgumentException("Password must be at least " + MIN_PASSWORD_LENGTH + " characters"));
        }
        return Mono.empty();
    }
}

