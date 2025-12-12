package com.ubik.usermanagement.infrastructure.adapter.in.web.controller;

import com.ubik.usermanagement.application.port.in.UserUseCase;
import com.ubik.usermanagement.infrastructure.adapter.in.web.dto.LoginRequest;
import com.ubik.usermanagement.infrastructure.adapter.in.web.dto.RegisterRequest;
import com.ubik.usermanagement.infrastructure.adapter.in.web.dto.ResetPasswordRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Authentication", description = "API para autenticación y gestión de usuarios")
public class AuthController {

    private final UserUseCase userUseCase;

    public AuthController(UserUseCase userUseCase) {
        this.userUseCase = userUseCase;
    }

    @Operation(summary = "Registrar nuevo usuario", description = "Registra un nuevo usuario en el sistema")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Usuario registrado exitosamente"),
            @ApiResponse(responseCode = "400", description = "Datos de entrada inválidos")
    })
    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<String> register(@Valid @RequestBody RegisterRequest request) {
        return userUseCase.register(request);
    }

    @Operation(summary = "Iniciar sesión", description = "Autentica un usuario y retorna un token JWT")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Autenticación exitosa, retorna JWT token"),
            @ApiResponse(responseCode = "401", description = "Credenciales inválidas")
    })
    @PostMapping("/login")
    public Mono<String> login(@Valid @RequestBody LoginRequest request) {
        return userUseCase.login(request);
    }

    @Operation(summary = "Solicitar restablecimiento de contraseña", description = "Envía una solicitud para restablecer la contraseña de un usuario")
    @ApiResponse(responseCode = "200", description = "Solicitud de restablecimiento procesada")
    @PostMapping("/reset-password-request")
    public Mono<String> requestPasswordReset(
            @Parameter(description = "Email del usuario", required = true) @RequestParam String email) {
        return userUseCase.requestPasswordReset(email);
    }

    @Operation(summary = "Restablecer contraseña", description = "Restablece la contraseña de un usuario con un token válido")
    @ApiResponse(responseCode = "200", description = "Contraseña restablecida exitosamente")
    @PostMapping("/reset-password")
    public Mono<String> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        return userUseCase.resetPassword(request);
    }

    @GetMapping("/admin/test")
    public Mono<String> adminTest() {
        return Mono.just("Admin access granted");
    }

    @GetMapping("/user/test")
    public Mono<String> userTest() {
        return Mono.just("User or Client access granted");
    }

    @ExceptionHandler(RuntimeException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Mono<String> handleInvalidCredentials(RuntimeException ex) {
        return Mono.just("Error"+ex.getMessage());
    }
}
