package com.ubik.reservation.infrastructure.adapter.in.web.controller;

import com.ubik.reservation.domain.port.in.ReservationUseCasePort;
import com.ubik.reservation.infrastructure.adapter.in.web.dto.*;
import com.ubik.reservation.infrastructure.adapter.in.web.mapper.ReservationDtoMapper;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/reservations")
public class ReservationController {

    private final ReservationUseCasePort reservationUseCase;
    private final ReservationDtoMapper mapper;

    public ReservationController(
            ReservationUseCasePort reservationUseCase,
            ReservationDtoMapper mapper
    ) {
        this.reservationUseCase = reservationUseCase;
        this.mapper = mapper;
    }

    /**
     * Verificar disponibilidad (público)
     */
    @GetMapping("/availability/room/{roomId}")
    public Mono<RoomAvailabilityResponse> checkAvailability(
            @PathVariable Long roomId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime checkIn,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime checkOut
    ) {
        return reservationUseCase.checkRoomAvailability(roomId, checkIn, checkOut)
                .map(mapper::toAvailabilityResponse);
    }

    /**
     * Buscar habitaciones disponibles (público)
     */
    @GetMapping("/availability/motel/{motelId}")
    public Flux<Long> findAvailableRooms(
            @PathVariable Long motelId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime checkIn,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime checkOut
    ) {
        return reservationUseCase.findAvailableRooms(motelId, checkIn, checkOut);
    }

    /**
     * Crear reserva (requiere autenticación)
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<ReservationResponse> createReservation(
            @Valid @RequestBody CreateReservationRequest request
    ) {
        return ReactiveSecurityContextHolder.getContext()
                .map(context -> context.getAuthentication().getName())
                .flatMap(username -> {
                    var reservation = mapper.toDomain(request, username);
                    return reservationUseCase.createReservation(reservation);
                })
                .map(mapper::toResponse);
    }

    /**
     * Obtener reserva por ID
     * Solo el dueño o admin pueden ver
     */
    @GetMapping("/{id}")
    public Mono<ReservationResponse> getReservation(@PathVariable Long id) {
        return ReactiveSecurityContextHolder.getContext()
                .map(context -> context.getAuthentication())
                .flatMap(auth -> {
                    String username = auth.getName();
                    boolean isAdmin = auth.getAuthorities().stream()
                            .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
                    
                    return reservationUseCase.getReservationById(id)
                            .flatMap(reservation -> {
                                if (isAdmin || reservation.belongsToUser(username)) {
                                    return Mono.just(mapper.toResponse(reservation));
                                }
                                return Mono.error(new RuntimeException(
                                    "No tienes permiso para ver esta reserva"));
                            });
                });
    }

    /**
     * Mis reservas (del usuario autenticado)
     */
    @GetMapping("/my-reservations")
    public Flux<ReservationResponse> getMyReservations() {
        return ReactiveSecurityContextHolder.getContext()
                .map(context -> context.getAuthentication().getName())
                .flatMapMany(reservationUseCase::getReservationsByUsername)
                .map(mapper::toResponse);
    }

    /**
     * Reservas activas de un motel (solo admin)
     */
    @GetMapping("/admin/motel/{motelId}/active")
    public Flux<ReservationResponse> getActiveReservations(@PathVariable Long motelId) {
        return reservationUseCase.getActiveReservations(motelId)
                .map(mapper::toResponse);
    }

    /**
     * Todas las reservas (solo admin)
     */
    @GetMapping("/admin/all")
    public Flux<ReservationResponse> getAllReservations() {
        return reservationUseCase.getAllReservations()
                .map(mapper::toResponse);
    }

    /**
     * Confirmar reserva
     */
    @PutMapping("/{id}/confirm")
    public Mono<ReservationResponse> confirmReservation(@PathVariable Long id) {
        return reservationUseCase.confirmReservation(id)
                .map(mapper::toResponse);
    }

    /**
     * Check-in
     */
    @PutMapping("/{id}/check-in")
    public Mono<ReservationResponse> checkIn(@PathVariable Long id) {
        return reservationUseCase.checkIn(id)
                .map(mapper::toResponse);
    }

    /**
     * Check-out
     */
    @PutMapping("/{id}/check-out")
    public Mono<ReservationResponse> checkOut(@PathVariable Long id) {
        return reservationUseCase.checkOut(id)
                .map(mapper::toResponse);
    }

    /**
     * Cancelar reserva
     */
    @PutMapping("/{id}/cancel")
    public Mono<ReservationResponse> cancelReservation(
            @PathVariable Long id,
            @RequestParam(required = false) String reason
    ) {
        return ReactiveSecurityContextHolder.getContext()
                .map(context -> context.getAuthentication())
                .flatMap(auth -> {
                    String username = auth.getName();
                    boolean isAdmin = auth.getAuthorities().stream()
                            .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
                    
                    return reservationUseCase.cancelReservation(id, username, isAdmin, reason);
                })
                .map(mapper::toResponse);
    }
}