package com.ubik.usermanagement.infrastructure.adapter.in.web.controller;

import com.ubik.usermanagement.domain.port.in.MotelUseCasePort;
import com.ubik.usermanagement.infrastructure.adapter.in.web.dto.CreateMotelRequest;
import com.ubik.usermanagement.infrastructure.adapter.in.web.dto.MotelResponse;
import com.ubik.usermanagement.infrastructure.adapter.in.web.dto.UpdateMotelRequest;
import com.ubik.usermanagement.infrastructure.adapter.in.web.mapper.MotelDtoMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
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
@Tag(name = "Motels", description = "API para gestión de moteles")
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
    @Operation(summary = "Crear un nuevo motel", description = "Crea un nuevo motel con la información proporcionada")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Motel creado exitosamente"),
            @ApiResponse(responseCode = "400", description = "Datos de entrada inválidos")
    })
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
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
    @Operation(summary = "Obtener motel por ID", description = "Obtiene la información detallada de un motel específico")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Motel encontrado"),
            @ApiResponse(responseCode = "404", description = "Motel no encontrado")
    })
    @GetMapping("/{id}")
    public Mono<MotelResponse> getMotelById(
            @Parameter(description = "ID del motel", required = true) @PathVariable Long id) {
        return motelUseCasePort.getMotelById(id)
                .map(motelDtoMapper::toResponse);
    }

    /**
     * Obtiene todos los moteles
     * GET /api/motels
     */
    @Operation(summary = "Listar todos los moteles", description = "Obtiene un listado de todos los moteles registrados")
    @ApiResponse(responseCode = "200", description = "Listado de moteles obtenido exitosamente")
    @GetMapping
    public Flux<MotelResponse> getAllMotels() {
        return motelUseCasePort.getAllMotels()
                .map(motelDtoMapper::toResponse);
    }

    /**
     * Obtiene moteles por ciudad
     * GET /api/motels/city/{city}
     */
    @Operation(summary = "Buscar moteles por ciudad", description = "Obtiene todos los moteles ubicados en una ciudad específica")
    @ApiResponse(responseCode = "200", description = "Moteles encontrados")
    @GetMapping("/city/{city}")
    public Flux<MotelResponse> getMotelsByCity(
            @Parameter(description = "Nombre de la ciudad", required = true) @PathVariable String city) {
        return motelUseCasePort.getMotelsByCity(city)
                .map(motelDtoMapper::toResponse);
    }

    /**
     * Actualiza un motel existente
     * PUT /api/motels/{id}
     */
    @Operation(summary = "Actualizar un motel", description = "Actualiza la información de un motel existente")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Motel actualizado exitosamente"),
            @ApiResponse(responseCode = "404", description = "Motel no encontrado"),
            @ApiResponse(responseCode = "400", description = "Datos de entrada inválidos")
    })
    @PutMapping("/{id}")
    public Mono<MotelResponse> updateMotel(
            @Parameter(description = "ID del motel", required = true) @PathVariable Long id,
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
    @Operation(summary = "Eliminar un motel", description = "Elimina un motel del sistema")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Motel eliminado exitosamente"),
            @ApiResponse(responseCode = "404", description = "Motel no encontrado")
    })
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public Mono<Void> deleteMotel(
            @Parameter(description = "ID del motel", required = true) @PathVariable Long id) {
        return motelUseCasePort.deleteMotel(id);
    }
}