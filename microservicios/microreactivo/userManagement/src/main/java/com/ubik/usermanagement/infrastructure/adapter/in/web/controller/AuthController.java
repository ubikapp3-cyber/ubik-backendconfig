package com.ubik.usermanagement.infrastructure.adapter.in.web.controller;

import com.ubik.usermanagement.application.port.in.UserUseCase;
import com.ubik.usermanagement.infrastructure.adapter.in.web.dto.LoginRequest;
import com.ubik.usermanagement.infrastructure.adapter.in.web.dto.RegisterRequest;
import com.ubik.usermanagement.infrastructure.adapter.in.web.dto.ResetPasswordRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Authentication", description = "Endpoints para registro, login y gestión de contraseñas")
public class AuthController {

    private final UserUseCase userUseCase;

    public AuthController(UserUseCase userUseCase) {
        this.userUseCase = userUseCase;
    }

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(
            summary = "Registrar nuevo usuario",
            description = "Crea un nuevo usuario en el sistema con las credenciales proporcionadas. " +
                    "El usuario puede ser anónimo o tener un rol específico (1=Cliente, 2=Admin, 3=Owner).",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Datos del usuario a registrar",
                    required = true,
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = RegisterRequest.class),
                            examples = @ExampleObject(
                                    name = "Usuario Cliente",
                                    value = """
                                            {
                                              "username": "john_doe",
                                              "password": "SecureP@ss123",
                                              "email": "john@example.com",
                                              "anonymous": false,
                                              "roleId": 1
                                            }
                                            """
                            )
                    )
            )
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "Usuario registrado exitosamente",
                    content = @Content(
                            mediaType = "text/plain",
                            examples = @ExampleObject(value = "Usuario registrado exitosamente")
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Datos inválidos o usuario ya existe",
                    content = @Content(
                            mediaType = "text/plain",
                            examples = @ExampleObject(value = "Error: El username ya está en uso")
                    )
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Error interno del servidor"
            )
    })
    public Mono<String> register(@Valid @RequestBody RegisterRequest request) {
        return userUseCase.register(request);
    }

    @PostMapping("/login")
    @Operation(
            summary = "Autenticar usuario",
            description = "Autentica al usuario con username y password, devolviendo un JWT token válido por 24 horas.",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Credenciales de acceso",
                    required = true,
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = LoginRequest.class),
                            examples = @ExampleObject(
                                    name = "Login Example",
                                    value = """
                                            {
                                              "username": "john_doe",
                                              "password": "SecureP@ss123"
                                            }
                                            """
                            )
                    )
            )
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Login exitoso, retorna JWT token",
                    content = @Content(
                            mediaType = "text/plain",
                            examples = @ExampleObject(
                                    value = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJqb2huX2RvZSIsInJvbGUiOiJDTElFTlQifQ..."
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Credenciales inválidas",
                    content = @Content(
                            mediaType = "text/plain",
                            examples = @ExampleObject(value = "Error: Credenciales inválidas")
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Usuario no autorizado"
            )
    })
    public Mono<String> login(@Valid @RequestBody LoginRequest request) {
        return userUseCase.login(request);
    }

    @PostMapping("/reset-password-request")
    @Operation(
            summary = "Solicitar reseteo de contraseña",
            description = "Envía una solicitud para resetear la contraseña del usuario asociado al email proporcionado. " +
                    "Se genera un token de reseteo que es enviado por email.",
            parameters = @Parameter(
                    name = "email",
                    description = "Email del usuario que solicita el reseteo",
                    required = true,
                    example = "john@example.com"
            )
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Solicitud procesada exitosamente",
                    content = @Content(
                            mediaType = "text/plain",
                            examples = @ExampleObject(
                                    value = "Se ha enviado un email con instrucciones para resetear la contraseña"
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Email no encontrado",
                    content = @Content(
                            mediaType = "text/plain",
                            examples = @ExampleObject(value = "Error: Email no registrado")
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Email inválido"
            )
    })
    public Mono<String> requestPasswordReset(@RequestParam String email) {
        return userUseCase.requestPasswordReset(email);
    }

    @PostMapping("/reset-password")
    @Operation(
            summary = "Resetear contraseña",
            description = "Completa el proceso de reseteo de contraseña utilizando el token recibido por email.",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Token y nueva contraseña",
                    required = true,
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ResetPasswordRequest.class),
                            examples = @ExampleObject(
                                    name = "Reset Password",
                                    value = """
                                            {
                                              "token": "abc123xyz789",
                                              "newPassword": "NewSecureP@ss456"
                                            }
                                            """
                            )
                    )
            )
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Contraseña actualizada exitosamente",
                    content = @Content(
                            mediaType = "text/plain",
                            examples = @ExampleObject(value = "Contraseña actualizada exitosamente")
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Token inválido o expirado",
                    content = @Content(
                            mediaType = "text/plain",
                            examples = @ExampleObject(value = "Error: Token inválido o expirado")
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Usuario no encontrado"
            )
    })
    public Mono<String> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        return userUseCase.resetPassword(request);
    }

    @GetMapping("/admin/test")
    @Operation(
            summary = "Endpoint de prueba para Admin",
            description = "Endpoint protegido que solo permite acceso a usuarios con rol ADMIN. " +
                    "Útil para verificar la correcta autenticación y autorización.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Acceso concedido",
                    content = @Content(
                            mediaType = "text/plain",
                            examples = @ExampleObject(value = "Admin access granted")
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "No autenticado - Token JWT faltante o inválido"
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "No autorizado - Usuario sin rol ADMIN"
            )
    })
    public Mono<String> adminTest() {
        return Mono.just("Admin access granted");
    }

    @GetMapping("/user/test")
    @Operation(
            summary = "Endpoint de prueba para usuarios autenticados",
            description = "Endpoint protegido que permite acceso a cualquier usuario autenticado (CLIENT u OWNER). " +
                    "Útil para verificar la correcta autenticación.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Acceso concedido",
                    content = @Content(
                            mediaType = "text/plain",
                            examples = @ExampleObject(value = "User or Client access granted")
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "No autenticado - Token JWT faltante o inválido"
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "No autorizado - Usuario sin permisos suficientes"
            )
    })
    public Mono<String> userTest() {
        return Mono.just("User or Client access granted");
    }

    @ExceptionHandler(RuntimeException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Mono<String> handleInvalidCredentials(RuntimeException ex) {
        return Mono.just("Error"+ex.getMessage());
    }
}
