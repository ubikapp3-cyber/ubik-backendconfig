package com.ubik.usermanagement.infrastructure.adapter.in.web.controller;

import com.ubik.usermanagement.domain.port.in.RoomUseCasePort;
import com.ubik.usermanagement.infrastructure.adapter.in.web.dto.CreateRoomRequest;
import com.ubik.usermanagement.infrastructure.adapter.in.web.dto.RoomResponse;
import com.ubik.usermanagement.infrastructure.adapter.in.web.dto.UpdateRoomRequest;
import com.ubik.usermanagement.infrastructure.adapter.in.web.mapper.RoomDtoMapper;
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
 * Controlador REST reactivo para operaciones CRUD de Room
 * Adaptador primario en arquitectura hexagonal
 */
@RestController
@RequestMapping("/api/rooms")
@Tag(name = "Rooms", description = "API para gestión de habitaciones")
public class RoomController {

    private final RoomUseCasePort roomUseCasePort;
    private final RoomDtoMapper roomDtoMapper;

    public RoomController(RoomUseCasePort roomUseCasePort, RoomDtoMapper roomDtoMapper) {
        this.roomUseCasePort = roomUseCasePort;
        this.roomDtoMapper = roomDtoMapper;
    }

    /**
     * Crea una nueva habitación
     * POST /api/rooms
     */
    @Operation(summary = "Crear una nueva habitación", description = "Crea una nueva habitación en un motel")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Habitación creada exitosamente"),
            @ApiResponse(responseCode = "400", description = "Datos de entrada inválidos")
    })
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<RoomResponse> createRoom(@Valid @RequestBody CreateRoomRequest request) {
        return Mono.just(request)
                .map(roomDtoMapper::toDomain)
                .flatMap(roomUseCasePort::createRoom)
                .map(roomDtoMapper::toResponse);
    }

    /**
     * Obtiene una habitación por ID
     * GET /api/rooms/{id}
     */
    @Operation(summary = "Obtener habitación por ID", description = "Obtiene la información detallada de una habitación específica")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Habitación encontrada"),
            @ApiResponse(responseCode = "404", description = "Habitación no encontrada")
    })
    @GetMapping("/{id}")
    public Mono<RoomResponse> getRoomById(
            @Parameter(description = "ID de la habitación", required = true) @PathVariable Long id) {
        return roomUseCasePort.getRoomById(id)
                .map(roomDtoMapper::toResponse);
    }

    /**
     * Obtiene todas las habitaciones
     * GET /api/rooms
     */
    @Operation(summary = "Listar todas las habitaciones", description = "Obtiene un listado de todas las habitaciones registradas")
    @ApiResponse(responseCode = "200", description = "Listado de habitaciones obtenido exitosamente")
    @GetMapping
    public Flux<RoomResponse> getAllRooms() {
        return roomUseCasePort.getAllRooms()
                .map(roomDtoMapper::toResponse);
    }

    /**
     * Obtiene habitaciones por ID de motel
     * GET /api/rooms/motel/{motelId}
     */
    @Operation(summary = "Buscar habitaciones por motel", description = "Obtiene todas las habitaciones de un motel específico")
    @ApiResponse(responseCode = "200", description = "Habitaciones encontradas")
    @GetMapping("/motel/{motelId}")
    public Flux<RoomResponse> getRoomsByMotelId(
            @Parameter(description = "ID del motel", required = true) @PathVariable Long motelId) {
        return roomUseCasePort.getRoomsByMotelId(motelId)
                .map(roomDtoMapper::toResponse);
    }

    /**
     * Obtiene habitaciones disponibles por ID de motel
     * GET /api/rooms/motel/{motelId}/available
     */
    @Operation(summary = "Buscar habitaciones disponibles", description = "Obtiene todas las habitaciones disponibles de un motel específico")
    @ApiResponse(responseCode = "200", description = "Habitaciones disponibles encontradas")
    @GetMapping("/motel/{motelId}/available")
    public Flux<RoomResponse> getAvailableRoomsByMotelId(
            @Parameter(description = "ID del motel", required = true) @PathVariable Long motelId) {
        return roomUseCasePort.getAvailableRoomsByMotelId(motelId)
                .map(roomDtoMapper::toResponse);
    }

    /**
     * Actualiza una habitación existente
     * PUT /api/rooms/{id}
     */
    @Operation(summary = "Actualizar una habitación", description = "Actualiza la información de una habitación existente")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Habitación actualizada exitosamente"),
            @ApiResponse(responseCode = "404", description = "Habitación no encontrada"),
            @ApiResponse(responseCode = "400", description = "Datos de entrada inválidos")
    })
    @PutMapping("/{id}")
    public Mono<RoomResponse> updateRoom(
            @Parameter(description = "ID de la habitación", required = true) @PathVariable Long id,
            @Valid @RequestBody UpdateRoomRequest request) {
        return Mono.just(request)
                .map(roomDtoMapper::toDomain)
                .flatMap(room -> roomUseCasePort.updateRoom(id, room))
                .map(roomDtoMapper::toResponse);
    }

    /**
     * Elimina una habitación
     * DELETE /api/rooms/{id}
     */
    @Operation(summary = "Eliminar una habitación", description = "Elimina una habitación del sistema")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Habitación eliminada exitosamente"),
            @ApiResponse(responseCode = "404", description = "Habitación no encontrada")
    })
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public Mono<Void> deleteRoom(
            @Parameter(description = "ID de la habitación", required = true) @PathVariable Long id) {
        return roomUseCasePort.deleteRoom(id);
    }
}