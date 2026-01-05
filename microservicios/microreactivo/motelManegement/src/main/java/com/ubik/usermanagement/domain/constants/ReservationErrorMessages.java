package com.ubik.usermanagement.domain.constants;

/**
 * Constants for error messages in the reservation domain
 * Externalized for easier internationalization and maintenance
 */
public final class ReservationErrorMessages {
    
    // Validation error messages
    public static final String ROOM_ID_REQUIRED = "El ID de la habitación es requerido";
    public static final String USER_ID_REQUIRED = "El ID del usuario es requerido";
    public static final String CHECK_IN_DATE_REQUIRED = "La fecha de check-in es requerida";
    public static final String CHECK_OUT_DATE_REQUIRED = "La fecha de check-out es requerida";
    public static final String CHECK_IN_BEFORE_CHECK_OUT = "La fecha de check-in debe ser anterior a la fecha de check-out";
    public static final String CHECK_IN_NOT_IN_PAST = "La fecha de check-in no puede ser en el pasado";
    public static final String TOTAL_PRICE_POSITIVE = "El precio total debe ser mayor que cero";
    
    // State transition error messages
    public static final String CANNOT_MODIFY_RESERVATION = "La reserva no puede ser modificada en su estado actual";
    public static final String CANNOT_CONFIRM_RESERVATION = "La reserva no puede ser confirmada en su estado actual";
    public static final String CANNOT_CANCEL_RESERVATION = "La reserva no puede ser cancelada en su estado actual";
    public static final String CANNOT_CHECK_IN = "No se puede hacer check-in en el estado actual";
    public static final String CANNOT_CHECK_OUT = "No se puede hacer check-out en el estado actual";
    public static final String ONLY_CANCELLED_CAN_BE_DELETED = "Solo se pueden eliminar reservas canceladas";
    
    // Availability error messages
    public static final String ROOM_NOT_AVAILABLE = "La habitación no está disponible para las fechas seleccionadas";
    public static final String ROOM_NOT_AVAILABLE_NEW_DATES = "La habitación no está disponible para las nuevas fechas";
    
    // Duration format message
    public static final String MAX_DURATION_EXCEEDED_FORMAT = "La duración máxima de la reserva es de %d días";
    
    private ReservationErrorMessages() {
        // Private constructor to prevent instantiation
    }
}
