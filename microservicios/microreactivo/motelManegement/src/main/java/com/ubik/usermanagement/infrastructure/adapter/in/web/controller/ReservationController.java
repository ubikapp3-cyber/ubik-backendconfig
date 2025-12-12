package com.ubik.usermanagement.infrastructure.adapter.in.web.controller;

import com.ubik.usermanagement.domain.model.Reservation;
import com.ubik.usermanagement.domain.port.in.ReservationUseCasePort;
import com.ubik.usermanagement.infrastructure.adapter.in.web.dto.CreateReservationRequest;
import com.ubik.usermanagement.infrastructure.adapter.in.web.dto.ReservationResponse;
import com.ubik.usermanagement.infrastructure.adapter.in.web.dto.UpdateReservationRequest;
import com.ubik.usermanagement.infrastructure.adapter.in.web.mapper.ReservationDtoMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;

/**
 * Controlador REST reactivo para operaciones CRUD de Reservation
 * Adaptador primario en arquitectura hexagonal
 */
@RestController
@RequestMapping("/api/reservations")
@Tag(name = "Reservations", description = "API para gestión de reservaciones")
public class ReservationController {

    private final ReservationUseCasePort reservationUseCasePort;
    private final ReservationDtoMapper reservationDtoMapper;

    public ReservationController(
            ReservationUseCasePort reservationUseCasePort,
            ReservationDtoMapper reservationDtoMapper
    ) {
        this.reservationUseCasePort = reservationUseCasePort;
        this.reservationDtoMapper = reservationDtoMapper;
    }

    /**
     * Crea una nueva reserva
     * POST /api/reservations
     */
    @Operation(summary = "Crear una nueva reserva", description = "Crea una nueva reserva de habitación")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Reserva creada exitosamente"),
            @ApiResponse(responseCode = "400", description = "Datos de entrada inválidos")
    })
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<ReservationResponse> createReservation(@Valid @RequestBody CreateReservationRequest request) {
        return Mono.just(request)
                .map(reservationDtoMapper::toDomain)
                .flatMap(reservationUseCasePort::createReservation)
                .map(reservationDtoMapper::toResponse);
    }

    /**
     * Obtiene una reserva por ID
     * GET /api/reservations/{id}
     */
    @Operation(summary = "Obtener reserva por ID", description = "Obtiene la información detallada de una reserva específica")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Reserva encontrada"),
            @ApiResponse(responseCode = "404", description = "Reserva no encontrada")
    })
    @GetMapping("/{id}")
    public Mono<ReservationResponse> getReservationById(
            @Parameter(description = "ID de la reserva", required = true) @PathVariable Long id) {
        return reservationUseCasePort.getReservationById(id)
                .map(reservationDtoMapper::toResponse);
    }

    /**
     * Obtiene todas las reservas
     * GET /api/reservations
     */
    @Operation(summary = "Listar todas las reservas", description = "Obtiene un listado de todas las reservas registradas")
    @ApiResponse(responseCode = "200", description = "Listado de reservas obtenido exitosamente")
    @GetMapping
    public Flux<ReservationResponse> getAllReservations() {
        return reservationUseCasePort.getAllReservations()
                .map(reservationDtoMapper::toResponse);
    }

    /**
     * Obtiene reservas por ID de habitación
     * GET /api/reservations/room/{roomId}
     */
    @GetMapping("/room/{roomId}")
    public Flux<ReservationResponse> getReservationsByRoomId(@PathVariable Long roomId) {
        return reservationUseCasePort.getReservationsByRoomId(roomId)
                .map(reservationDtoMapper::toResponse);
    }

    /**
     * Obtiene reservas por ID de usuario
     * GET /api/reservations/user/{userId}
     */
    @GetMapping("/user/{userId}")
    public Flux<ReservationResponse> getReservationsByUserId(@PathVariable Long userId) {
        return reservationUseCasePort.getReservationsByUserId(userId)
                .map(reservationDtoMapper::toResponse);
    }

    /**
     * Obtiene reservas activas por ID de habitación
     * GET /api/reservations/room/{roomId}/active
     */
    @GetMapping("/room/{roomId}/active")
    public Flux<ReservationResponse> getActiveReservationsByRoomId(@PathVariable Long roomId) {
        return reservationUseCasePort.getActiveReservationsByRoomId(roomId)
                .map(reservationDtoMapper::toResponse);
    }

    /**
     * Obtiene reservas por estado
     * GET /api/reservations/status/{status}
     */
    @GetMapping("/status/{status}")
    public Flux<ReservationResponse> getReservationsByStatus(@PathVariable String status) {
        try {
            Reservation.ReservationStatus reservationStatus = Reservation.ReservationStatus.valueOf(status.toUpperCase());
            return reservationUseCasePort.getReservationsByStatus(reservationStatus)
                    .map(reservationDtoMapper::toResponse);
        } catch (IllegalArgumentException e) {
            return Flux.error(new IllegalArgumentException("Estado inválido: " + status));
        }
    }

    /**
     * Verifica disponibilidad de una habitación
     * GET /api/reservations/room/{roomId}/available
     */
    @GetMapping("/room/{roomId}/available")
    public Mono<Boolean> checkRoomAvailability(
            @PathVariable Long roomId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime checkIn,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime checkOut
    ) {
        return reservationUseCasePort.isRoomAvailable(roomId, checkIn, checkOut);
    }

    /**
     * Actualiza una reserva existente
     * PUT /api/reservations/{id}
     */
    @PutMapping("/{id}")
    public Mono<ReservationResponse> updateReservation(
            @PathVariable Long id,
            @Valid @RequestBody UpdateReservationRequest request
    ) {
        return Mono.just(request)
                .map(reservationDtoMapper::toDomain)
                .flatMap(reservation -> reservationUseCasePort.updateReservation(id, reservation))
                .map(reservationDtoMapper::toResponse);
    }

    /**
     * Confirma una reserva
     * PATCH /api/reservations/{id}/confirm
     */
    @Operation(summary = "Confirmar reserva", description = "Cambia el estado de una reserva a confirmada")
    @ApiResponse(responseCode = "200", description = "Reserva confirmada exitosamente")
    @PatchMapping("/{id}/confirm")
    public Mono<ReservationResponse> confirmReservation(
            @Parameter(description = "ID de la reserva", required = true) @PathVariable Long id) {
        return reservationUseCasePort.confirmReservation(id)
                .map(reservationDtoMapper::toResponse);
    }

    /**
     * Cancela una reserva
     * PATCH /api/reservations/{id}/cancel
     */
    @PatchMapping("/{id}/cancel")
    public Mono<ReservationResponse> cancelReservation(@PathVariable Long id) {
        return reservationUseCasePort.cancelReservation(id)
                .map(reservationDtoMapper::toResponse);
    }

    /**
     * Realiza check-in
     * PATCH /api/reservations/{id}/checkin
     */
    @PatchMapping("/{id}/checkin")
    public Mono<ReservationResponse> checkIn(@PathVariable Long id) {
        return reservationUseCasePort.checkIn(id)
                .map(reservationDtoMapper::toResponse);
    }

    /**
     * Realiza check-out
     * PATCH /api/reservations/{id}/checkout
     */
    @PatchMapping("/{id}/checkout")
    public Mono<ReservationResponse> checkOut(@PathVariable Long id) {
        return reservationUseCasePort.checkOut(id)
                .map(reservationDtoMapper::toResponse);
    }

    /**
     * Elimina una reserva (solo canceladas)
     * DELETE /api/reservations/{id}
     */
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public Mono<Void> deleteReservation(@PathVariable Long id) {
        return reservationUseCasePort.deleteReservation(id);
    }
}