package com.ubik.reservation.domain.model;

import java.time.LocalDateTime;

/**
 * Modelo de dominio para Reservación
 */
public record Reservation(
        Long id,
        Long roomId,
        Long motelId,
        
        // Información del usuario que reserva
        String username,        // Del JWT / User Management
        String customerName,
        String customerEmail,
        String customerPhone,
        String customerDocument,
        
        // Fechas
        LocalDateTime checkIn,
        LocalDateTime checkOut,
        
        // Información financiera
        Double totalPrice,
        String paymentMethod,
        PaymentStatus paymentStatus,
        
        // Estado de la reserva
        ReservationStatus status,
        
        // Información adicional
        String specialRequests,
        String cancellationReason,
        
        // Auditoría
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public enum ReservationStatus {
        PENDING,      // Pendiente de confirmación
        CONFIRMED,    // Confirmada
        CHECKED_IN,   // Cliente en el motel
        CHECKED_OUT,  // Finalizada
        CANCELLED     // Cancelada
    }

    public enum PaymentStatus {
        PENDING,      // Pendiente de pago
        PAID,         // Pagada
        REFUNDED,     // Reembolsada
        FAILED        // Pago fallido
    }

    /**
     * Constructor para crear nueva reserva desde un usuario autenticado
     */
    public static Reservation createNewForUser(
            String username,
            Long roomId,
            Long motelId,
            String customerName,
            String customerEmail,
            String customerPhone,
            String customerDocument,
            LocalDateTime checkIn,
            LocalDateTime checkOut,
            Double totalPrice,
            String paymentMethod,
            String specialRequests
    ) {
        return new Reservation(
                null, roomId, motelId, username,
                customerName, customerEmail, customerPhone, customerDocument,
                checkIn, checkOut, totalPrice, paymentMethod,
                PaymentStatus.PENDING, ReservationStatus.PENDING,
                specialRequests, null,
                LocalDateTime.now(), LocalDateTime.now()
        );
    }

    // Métodos de transición de estados
    public Reservation confirm() {
        if (this.status != ReservationStatus.PENDING) {
            throw new IllegalStateException("Solo se pueden confirmar reservas pendientes");
        }
        return new Reservation(
                id, roomId, motelId, username, customerName, customerEmail,
                customerPhone, customerDocument, checkIn, checkOut, totalPrice,
                paymentMethod, PaymentStatus.PAID, ReservationStatus.CONFIRMED,
                specialRequests, cancellationReason, createdAt, LocalDateTime.now()
        );
    }

    public Reservation checkIn() {
        if (this.status != ReservationStatus.CONFIRMED) {
            throw new IllegalStateException("Solo se puede hacer check-in de reservas confirmadas");
        }
        return new Reservation(
                id, roomId, motelId, username, customerName, customerEmail,
                customerPhone, customerDocument, checkIn, checkOut, totalPrice,
                paymentMethod, paymentStatus, ReservationStatus.CHECKED_IN,
                specialRequests, cancellationReason, createdAt, LocalDateTime.now()
        );
    }

    public Reservation checkOut() {
        if (this.status != ReservationStatus.CHECKED_IN) {
            throw new IllegalStateException("Solo se puede hacer check-out si está checked-in");
        }
        return new Reservation(
                id, roomId, motelId, username, customerName, customerEmail,
                customerPhone, customerDocument, checkIn, checkOut, totalPrice,
                paymentMethod, paymentStatus, ReservationStatus.CHECKED_OUT,
                specialRequests, cancellationReason, createdAt, LocalDateTime.now()
        );
    }

    public Reservation cancel(String reason) {
        if (!canBeCancelled()) {
            throw new IllegalStateException("Esta reserva no puede ser cancelada");
        }
        return new Reservation(
                id, roomId, motelId, username, customerName, customerEmail,
                customerPhone, customerDocument, checkIn, checkOut, totalPrice,
                paymentMethod, paymentStatus, ReservationStatus.CANCELLED,
                specialRequests, reason, createdAt, LocalDateTime.now()
        );
    }

    public boolean isActive() {
        return status == ReservationStatus.CONFIRMED || 
               status == ReservationStatus.CHECKED_IN;
    }

    public boolean canBeCancelled() {
        return status == ReservationStatus.PENDING || 
               status == ReservationStatus.CONFIRMED;
    }

    public boolean belongsToUser(String usernameToCheck) {
        return this.username != null && this.username.equals(usernameToCheck);
    }
}