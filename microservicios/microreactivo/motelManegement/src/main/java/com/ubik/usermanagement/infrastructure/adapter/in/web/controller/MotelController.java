package com.ubik.usermanagement.infrastructure.adapter.in.web.controller;

import com.ubik.usermanagement.domain.port.in.MotelUseCasePort;
import com.ubik.usermanagement.infrastructure.adapter.in.web.dto.CreateMotelRequest;
import com.ubik.usermanagement.infrastructure.adapter.in.web.dto.MotelResponse;
import com.ubik.usermanagement.infrastructure.adapter.in.web.dto.UpdateMotelRequest;
import com.ubik.usermanagement.infrastructure.adapter.in.web.mapper.MotelDtoMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
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
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Controlador REST reactivo para operaciones CRUD de Motel
 * Adaptador primario en arquitectura hexagonal
 */
@RestController
@RequestMapping("/api/motels")
@Tag(name = "Motels", description = "Endpoints para gestión de moteles")
public class MotelController {

    private final MotelUseCasePort motelUseCasePort;
    private final MotelDtoMapper motelDtoMapper;

    public MotelController(MotelUseCasePort motelUseCasePort, MotelDtoMapper motelDtoMapper) {
        this.motelUseCasePort = motelUseCasePort;
        this.motelDtoMapper = motelDtoMapper;
    }

    /**
     * Crea un nuevo motel
     * POST /api/motels
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(
            summary = "Crear nuevo motel",
            description = "Crea un nuevo motel con información completa incluyendo nombre, dirección, ciudad, descripción e imágenes.",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Datos del motel a crear",
                    required = true,
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = CreateMotelRequest.class),
                            examples = @ExampleObject(
                                    name = "Motel Completo",
                                    value = """
                                            {
                                              "name": "Motel Paradise",
                                              "address": "Av. Principal 123",
                                              "phoneNumber": "+593-987654321",
                                              "description": "Motel de lujo con todas las comodidades",
                                              "city": "Quito",
                                              "propertyId": 1,
                                              "imageUrls": [
                                                "https://example.com/images/motel1.jpg",
                                                "https://example.com/images/motel2.jpg"
                                              ]
                                            }
                                            """
                            )
                    )
            )
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "Motel creado exitosamente",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = MotelResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Datos inválidos - Validación fallida"
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Error interno del servidor"
            )
    })
    public Mono<MotelResponse> createMotel(@Valid @RequestBody CreateMotelRequest request) {
        return Mono.just(request)
                .map(motelDtoMapper::toDomain)
                .flatMap(motelUseCasePort::createMotel)
                .map(motelDtoMapper::toResponse);
    }

    /**
     * Obtiene un motel por ID
     * GET /api/motels/{id}
     */
    @GetMapping("/{id}")
    @Operation(
            summary = "Obtener motel por ID",
            description = "Retorna la información completa de un motel específico incluyendo sus imágenes.",
            parameters = @Parameter(
                    name = "id",
                    description = "ID único del motel",
                    required = true,
                    example = "1"
            )
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Motel encontrado",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = MotelResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Motel no encontrado"
            )
    })
    public Mono<MotelResponse> getMotelById(@PathVariable Long id) {
        return motelUseCasePort.getMotelById(id)
                .map(motelDtoMapper::toResponse);
    }

    /**
     * Obtiene todos los moteles
     * GET /api/motels
     */
    @GetMapping
    @Operation(
            summary = "Listar todos los moteles",
            description = "Retorna una lista reactiva de todos los moteles registrados en el sistema."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Lista de moteles obtenida exitosamente",
                    content = @Content(
                            mediaType = "application/json",
                            array = @ArraySchema(schema = @Schema(implementation = MotelResponse.class))
                    )
            )
    })
    public Flux<MotelResponse> getAllMotels() {
        return motelUseCasePort.getAllMotels()
                .map(motelDtoMapper::toResponse);
    }

    /**
     * Obtiene moteles por ciudad
     * GET /api/motels/city/{city}
     */
    @GetMapping("/city/{city}")
    @Operation(
            summary = "Buscar moteles por ciudad",
            description = "Retorna todos los moteles ubicados en una ciudad específica.",
            parameters = @Parameter(
                    name = "city",
                    description = "Nombre de la ciudad",
                    required = true,
                    example = "Quito"
            )
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Moteles encontrados en la ciudad",
                    content = @Content(
                            mediaType = "application/json",
                            array = @ArraySchema(schema = @Schema(implementation = MotelResponse.class))
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "No se encontraron moteles en la ciudad especificada"
            )
    })
    public Flux<MotelResponse> getMotelsByCity(@PathVariable String city) {
        return motelUseCasePort.getMotelsByCity(city)
                .map(motelDtoMapper::toResponse);
    }

    /**
     * Actualiza un motel existente
     * PUT /api/motels/{id}
     */
    @PutMapping("/{id}")
    @Operation(
            summary = "Actualizar motel",
            description = "Actualiza la información de un motel existente. Todos los campos son opcionales.",
            parameters = @Parameter(
                    name = "id",
                    description = "ID del motel a actualizar",
                    required = true,
                    example = "1"
            ),
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Datos actualizados del motel",
                    required = true,
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = UpdateMotelRequest.class),
                            examples = @ExampleObject(
                                    name = "Actualizar Motel",
                                    value = """
                                            {
                                              "name": "Motel Paradise Updated",
                                              "description": "Descripción actualizada con nuevas amenidades",
                                              "phoneNumber": "+593-999888777"
                                            }
                                            """
                            )
                    )
            )
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Motel actualizado exitosamente",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = MotelResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Motel no encontrado"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Datos inválidos"
            )
    })
    public Mono<MotelResponse> updateMotel(
            @PathVariable Long id,
            @Valid @RequestBody UpdateMotelRequest request) {
        return Mono.just(request)
                .map(motelDtoMapper::toDomain)
                .flatMap(motel -> motelUseCasePort.updateMotel(id, motel))
                .map(motelDtoMapper::toResponse);
    }

    /**
     * Elimina un motel
     * DELETE /api/motels/{id}
     */
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(
            summary = "Eliminar motel",
            description = "Elimina un motel del sistema. Esta operación es irreversible y también eliminará todas las habitaciones asociadas.",
            parameters = @Parameter(
                    name = "id",
                    description = "ID del motel a eliminar",
                    required = true,
                    example = "1"
            )
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "204",
                    description = "Motel eliminado exitosamente"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Motel no encontrado"
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "No se puede eliminar - Existen reservas activas asociadas"
            )
    })
    public Mono<Void> deleteMotel(@PathVariable Long id) {
        return motelUseCasePort.deleteMotel(id);
    }
}