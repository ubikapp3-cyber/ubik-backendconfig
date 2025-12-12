package com.ubik.usermanagement.infrastructure.adapter.in.web.controller;

import com.ubik.usermanagement.domain.port.in.RoomUseCasePort;
import com.ubik.usermanagement.infrastructure.adapter.in.web.dto.CreateRoomRequest;
import com.ubik.usermanagement.infrastructure.adapter.in.web.dto.RoomResponse;
import com.ubik.usermanagement.infrastructure.adapter.in.web.dto.UpdateRoomRequest;
import com.ubik.usermanagement.infrastructure.adapter.in.web.mapper.RoomDtoMapper;
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
 * Controlador REST reactivo para operaciones CRUD de Room
 * Adaptador primario en arquitectura hexagonal
 */
@RestController
@RequestMapping("/api/rooms")
@Tag(name = "Rooms", description = "Endpoints para gestión de habitaciones")
@SecurityRequirement(name = "bearerAuth")
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
    @GetMapping("/{id}")
    public Mono<RoomResponse> getRoomById(@PathVariable Long id) {
        return roomUseCasePort.getRoomById(id)
                .map(roomDtoMapper::toResponse);
    }

    /**
     * Obtiene todas las habitaciones
     * GET /api/rooms
     */
    @GetMapping
    public Flux<RoomResponse> getAllRooms() {
        return roomUseCasePort.getAllRooms()
                .map(roomDtoMapper::toResponse);
    }

    /**
     * Obtiene habitaciones por ID de motel
     * GET /api/rooms/motel/{motelId}
     */
    @GetMapping("/motel/{motelId}")
    public Flux<RoomResponse> getRoomsByMotelId(@PathVariable Long motelId) {
        return roomUseCasePort.getRoomsByMotelId(motelId)
                .map(roomDtoMapper::toResponse);
    }

    /**
     * Obtiene habitaciones disponibles por ID de motel
     * GET /api/rooms/motel/{motelId}/available
     */
    @GetMapping("/motel/{motelId}/available")
    public Flux<RoomResponse> getAvailableRoomsByMotelId(@PathVariable Long motelId) {
        return roomUseCasePort.getAvailableRoomsByMotelId(motelId)
                .map(roomDtoMapper::toResponse);
    }

    /**
     * Actualiza una habitación existente
     * PUT /api/rooms/{id}
     */
    @PutMapping("/{id}")
    public Mono<RoomResponse> updateRoom(
            @PathVariable Long id,
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
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public Mono<Void> deleteRoom(@PathVariable Long id) {
        return roomUseCasePort.deleteRoom(id);
    }
}