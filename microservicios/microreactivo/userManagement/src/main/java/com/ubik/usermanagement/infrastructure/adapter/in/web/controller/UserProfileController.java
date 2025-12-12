package com.ubik.usermanagement.infrastructure.adapter.in.web.controller;

import com.ubik.usermanagement.application.port.in.UserProfileUseCase;
import com.ubik.usermanagement.infrastructure.adapter.in.web.dto.UpdateUserRequest;
import com.ubik.usermanagement.infrastructure.adapter.in.web.dto.UserProfileResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/user")
@Tag(name = "User Profile", description = "Endpoints para gestión del perfil de usuario")
@SecurityRequirement(name = "bearerAuth")
public class UserProfileController {

    private final UserProfileUseCase userProfileUseCase;

    public UserProfileController(UserProfileUseCase userProfileUseCase) {
        this.userProfileUseCase = userProfileUseCase;
    }

    @GetMapping
    @Operation(
            summary = "Obtener perfil de usuario",
            description = "Retorna la información del perfil del usuario autenticado. " +
                    "El username se extrae del header X-User-Username que es agregado por el Gateway.",
            parameters = @Parameter(
                    name = "X-User-Username",
                    description = "Username del usuario autenticado (agregado por Gateway)",
                    required = true,
                    example = "john_doe"
            )
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Perfil obtenido exitosamente",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = UserProfileResponse.class),
                            examples = @ExampleObject(
                                    name = "User Profile",
                                    value = """
                                            {
                                              "id": 1,
                                              "username": "john_doe",
                                              "email": "john@example.com",
                                              "roleId": 1,
                                              "roleName": "CLIENT",
                                              "anonymous": false,
                                              "createdAt": "2024-01-15T10:30:00"
                                            }
                                            """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Usuario no encontrado"
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "No autenticado - Token JWT faltante o inválido"
            )
    })
    public Mono<ResponseEntity<UserProfileResponse>> getProfile(ServerWebExchange exchange) {
        String username = exchange.getRequest().getHeaders().getFirst("X-User-Username");

        return userProfileUseCase.getUserProfile(username)
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @PutMapping
    @Operation(
            summary = "Actualizar perfil de usuario",
            description = "Actualiza la información del perfil del usuario autenticado. " +
                    "Permite modificar email y contraseña. El username se extrae del header X-User-Username.",
            parameters = @Parameter(
                    name = "X-User-Username",
                    description = "Username del usuario autenticado (agregado por Gateway)",
                    required = true,
                    example = "john_doe"
            ),
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Datos a actualizar del perfil",
                    required = true,
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = UpdateUserRequest.class),
                            examples = @ExampleObject(
                                    name = "Update Profile",
                                    value = """
                                            {
                                              "email": "newemail@example.com",
                                              "password": "NewSecureP@ss789"
                                            }
                                            """
                            )
                    )
            )
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Perfil actualizado exitosamente",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = UserProfileResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Usuario no encontrado"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Datos de actualización inválidos"
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "No autenticado - Token JWT faltante o inválido"
            )
    })
    public Mono<ResponseEntity<UserProfileResponse>> updateProfile(
            @RequestBody UpdateUserRequest request,
            ServerWebExchange exchange) {

        String username = exchange.getRequest().getHeaders().getFirst("X-User-Username");

        return userProfileUseCase.updateUserProfile(username, request)
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.status(HttpStatus.NOT_FOUND).build());
    }
}
