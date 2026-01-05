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
        // Validate input
        if (request.username() == null || request.username().trim().isEmpty()) {
            return Mono.error(new IllegalArgumentException("Username is required"));
        }
        if (request.email() == null || request.email().trim().isEmpty()) {
            return Mono.error(new IllegalArgumentException("Email is required"));
        }
        if (request.password() == null || request.password().length() < 6) {
            return Mono.error(new IllegalArgumentException("Password must be at least 6 characters"));
        }

        return userRepository.findByUsername(request.username())
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
                );
    }

    @Override
    public Mono<String> login(LoginRequest request) {
        if (request.username() == null || request.username().trim().isEmpty()) {
            return Mono.error(new IllegalArgumentException("Username is required"));
        }
        if (request.password() == null || request.password().isEmpty()) {
            return Mono.error(new IllegalArgumentException("Password is required"));
        }
        
        return userRepository.findByUsername(request.username())
                .filter(user -> passwordEncoder.matches(request.password(), user.password()))
                .switchIfEmpty(Mono.error(new RuntimeException("Invalid credentials")))
                .map(user -> jwtAdapter.generateToken(
                        user.username(),
                        user.roleId()      // Integer para JWT
                ));
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
        if (request.token() == null || request.token().trim().isEmpty()) {
            return Mono.error(new IllegalArgumentException("Reset token is required"));
        }
        if (request.newPassword() == null || request.newPassword().length() < 6) {
            return Mono.error(new IllegalArgumentException("New password must be at least 6 characters"));
        }
        
        return userRepository.findByResetToken(request.token())
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
                .map(user -> "Password reset successfully");
    }
}

