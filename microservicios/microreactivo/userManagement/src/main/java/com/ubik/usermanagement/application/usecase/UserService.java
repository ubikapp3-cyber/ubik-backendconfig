package com.ubik.usermanagement.application.usecase;

import com.ubik.usermanagement.application.port.in.UserUseCase;
import com.ubik.usermanagement.application.port.out.UserRepositoryPort;
import com.ubik.usermanagement.domain.exception.ActiveResetTokenException;
import com.ubik.usermanagement.domain.exception.InvalidCredentialsException;
import com.ubik.usermanagement.domain.exception.InvalidTokenException;
import com.ubik.usermanagement.domain.exception.UserAlreadyExistsException;
import com.ubik.usermanagement.domain.exception.UserNotFoundException;
import com.ubik.usermanagement.domain.factory.UserFactory;
import com.ubik.usermanagement.domain.model.User;
import com.ubik.usermanagement.domain.validator.UserInputValidator;
import com.ubik.usermanagement.infrastructure.adapter.in.web.dto.LoginRequest;
import com.ubik.usermanagement.infrastructure.adapter.in.web.dto.RegisterRequest;
import com.ubik.usermanagement.infrastructure.adapter.in.web.dto.ResetPasswordRequest;
import com.ubik.usermanagement.infrastructure.adapter.out.jwt.JwtAdapter;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * User service implementing authentication and user management use cases
 * Refactored to follow SOLID principles and use early returns
 */
@Service
public class UserService implements UserUseCase {

    private static final int RESET_TOKEN_EXPIRY_HOURS = 1;

    private final UserRepositoryPort userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtAdapter jwtAdapter;
    private final UserInputValidator inputValidator;
    private final UserFactory userFactory;

    public UserService(
            UserRepositoryPort userRepository, 
            PasswordEncoder passwordEncoder, 
            JwtAdapter jwtAdapter,
            UserInputValidator inputValidator,
            UserFactory userFactory) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtAdapter = jwtAdapter;
        this.inputValidator = inputValidator;
        this.userFactory = userFactory;
    }

    @Override
    public Mono<String> register(RegisterRequest request) {
        return validateRegistrationInput(request)
                .then(checkUsernameAvailability(request.username()))
                .then(checkEmailAvailability(request.email()))
                .then(Mono.defer(() -> createAndSaveUser(request)));
    }

    @Override
    public Mono<String> login(LoginRequest request) {
        return validateLoginInput(request)
                .then(authenticateUser(request));
    }

    @Override
    public Mono<String> requestPasswordReset(String email) {
        return inputValidator.validateEmail(email)
                .then(findUserByEmail(email))
                .flatMap(this::checkAndSetResetToken)
                .map(user -> user.resetToken());
    }

    @Override
    public Mono<String> resetPassword(ResetPasswordRequest request) {
        return validatePasswordResetInput(request)
                .then(findAndValidateResetToken(request.token()))
                .flatMap(user -> updateUserPassword(user, request.newPassword()))
                .map(user -> "Password reset successfully");
    }

    /**
     * Validates registration input data
     */
    private Mono<Void> validateRegistrationInput(RegisterRequest request) {
        return inputValidator.validateUsername(request.username())
                .then(inputValidator.validateEmail(request.email()))
                .then(inputValidator.validatePassword(request.password()));
    }

    /**
     * Validates login input data
     */
    private Mono<Void> validateLoginInput(LoginRequest request) {
        return inputValidator.validateUsername(request.username())
                .then(inputValidator.validatePassword(request.password()));
    }

    /**
     * Validates password reset input data
     */
    private Mono<Void> validatePasswordResetInput(ResetPasswordRequest request) {
        return inputValidator.validateToken(request.token())
                .then(inputValidator.validatePassword(request.newPassword()));
    }

    /**
     * Checks if username is available (not already taken)
     */
    private Mono<Void> checkUsernameAvailability(String username) {
        return userRepository.findByUsername(username)
                .flatMap(existingUser -> 
                    Mono.<Void>error(new UserAlreadyExistsException(username, "Username")))
                .then();
    }

    /**
     * Checks if email is available (not already registered)
     */
    private Mono<Void> checkEmailAvailability(String email) {
        return userRepository.findByEmail(email)
                .flatMap(existingUser -> 
                    Mono.<Void>error(new UserAlreadyExistsException(email, "Email")))
                .then();
    }

    /**
     * Creates new user and generates JWT token
     */
    private Mono<String> createAndSaveUser(RegisterRequest request) {
        String encodedPassword = passwordEncoder.encode(request.password());
        User newUser = userFactory.createFromRegistration(request, encodedPassword);
        
        return userRepository.save(newUser)
                .map(savedUser -> generateJwtToken(savedUser));
    }

    /**
     * Authenticates user and generates JWT token
     */
    private Mono<String> authenticateUser(LoginRequest request) {
        return userRepository.findByUsername(request.username())
                .filter(user -> passwordEncoder.matches(request.password(), user.password()))
                .switchIfEmpty(Mono.error(new InvalidCredentialsException()))
                .map(this::generateJwtToken);
    }

    /**
     * Finds user by email or throws exception
     */
    private Mono<User> findUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .switchIfEmpty(Mono.error(new UserNotFoundException(email, "email")));
    }

    /**
     * Checks for active reset token and sets new one if allowed
     */
    private Mono<User> checkAndSetResetToken(User user) {
        if (hasActiveResetToken(user)) {
            return Mono.error(new ActiveResetTokenException());
        }
        
        String resetToken = UUID.randomUUID().toString();
        LocalDateTime tokenExpiry = LocalDateTime.now().plusHours(RESET_TOKEN_EXPIRY_HOURS);
        User updatedUser = userFactory.createWithResetToken(user, resetToken, tokenExpiry);
        
        return userRepository.save(updatedUser);
    }

    /**
     * Checks if user has an active (non-expired) reset token
     */
    private boolean hasActiveResetToken(User user) {
        return user.resetToken() != null 
                && user.resetTokenExpiry() != null 
                && user.resetTokenExpiry().isAfter(LocalDateTime.now());
    }

    /**
     * Finds user by reset token and validates it's not expired
     */
    private Mono<User> findAndValidateResetToken(String token) {
        return userRepository.findByResetToken(token)
                .filter(user -> isTokenValid(user))
                .switchIfEmpty(Mono.error(new InvalidTokenException()));
    }

    /**
     * Checks if reset token is valid (exists and not expired)
     */
    private boolean isTokenValid(User user) {
        return user.resetTokenExpiry() != null 
                && user.resetTokenExpiry().isAfter(LocalDateTime.now());
    }

    /**
     * Updates user password and clears reset token
     */
    private Mono<User> updateUserPassword(User user, String newPassword) {
        String encodedPassword = passwordEncoder.encode(newPassword);
        User updatedUser = userFactory.createWithNewPassword(user, encodedPassword);
        return userRepository.save(updatedUser);
    }

    /**
     * Generates JWT token for authenticated user
     */
    private String generateJwtToken(User user) {
        return jwtAdapter.generateToken(user.username(), user.roleId());
    }
}

