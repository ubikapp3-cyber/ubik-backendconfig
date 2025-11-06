package com.ubik.usermanagement.domain.model;

import java.time.LocalDateTime;

public record Reservation(
    Long id,
    Long roomId,
    String customerName,
    String customerEmail,
    String customerPhone,
    LocalDateTime checkIn,
    LocalDateTime checkOut,
    Double totalPrice,
    ReservationStatus status,
    String paymentMethod,
    String specialRequests,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {
    public enum ReservationStatus {
        PENDING,      // Reserva pendiente de confirmación
        CONFIRMED,    // Confirmada y pagada
        CHECKED_IN,   // Cliente ya hizo check-in
        CHECKED_OUT,  // Cliente hizo check-out
        CANCELLED     // Reserva cancelada
    }

    // Constructor para crear nueva reserva
    public static Reservation createNew(
        Long roomId,
        String customerName,
        String customerEmail,
        String customerPhone,
        LocalDateTime checkIn,
        LocalDateTime checkOut,
        Double totalPrice,
        String paymentMethod,
        String specialRequests
    ) {
        return new Reservation(
            null, roomId, customerName, customerEmail, customerPhone,
            checkIn, checkOut, totalPrice, ReservationStatus.PENDING,
            paymentMethod, specialRequests, LocalDateTime.now(), LocalDateTime.now()
        );
    }

    // Métodos de transición de estados
    public Reservation confirm() {
        return new Reservation(id, roomId, customerName, customerEmail, customerPhone,
            checkIn, checkOut, totalPrice, ReservationStatus.CONFIRMED,
            paymentMethod, specialRequests, createdAt, LocalDateTime.now());
    }

    public Reservation checkIn() {
        return new Reservation(id, roomId, customerName, customerEmail, customerPhone,
            checkIn, checkOut, totalPrice, ReservationStatus.CHECKED_IN,
            paymentMethod, specialRequests, createdAt, LocalDateTime.now());
    }

    public Reservation checkOut() {
        return new Reservation(id, roomId, customerName, customerEmail, customerPhone,
            checkIn, checkOut, totalPrice, ReservationStatus.CHECKED_OUT,
            paymentMethod, specialRequests, createdAt, LocalDateTime.now());
    }

    public Reservation cancel() {
        return new Reservation(id, roomId, customerName, customerEmail, customerPhone,
            checkIn, checkOut, totalPrice, ReservationStatus.CANCELLED,
            paymentMethod, specialRequests, createdAt, LocalDateTime.now());
    }

    // Validaciones de negocio
    public boolean isActive() {
        return status == ReservationStatus.CONFIRMED || status == ReservationStatus.CHECKED_IN;
    }

    public boolean canBeCancelled() {
        return status == ReservationStatus.PENDING || status == ReservationStatus.CONFIRMED;
    }
}