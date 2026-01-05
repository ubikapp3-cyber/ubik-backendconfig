package com.ubik.usermanagement.domain.service.chatbot;

import com.ubik.usermanagement.domain.model.ChatSession;
import com.ubik.usermanagement.domain.model.Motel;
import com.ubik.usermanagement.domain.model.Reservation;
import com.ubik.usermanagement.domain.model.Room;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

/**
 * Componente responsable de formatear respuestas del chatbot
 * Aplica Single Responsibility Principle (SOLID)
 */
@Component
public class ResponseFormatter {

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private static final int MAX_ITEMS_TO_DISPLAY = 10;

    /**
     * Genera mensaje de saludo personalizado según el rol del usuario
     */
    @NonNull
    public String formatGreeting(@NonNull ChatSession session) {
        StringBuilder greeting = new StringBuilder("¡Hola! Soy el asistente virtual de gestión de moteles. ");
        
        if (session.isAdmin()) {
            greeting.append("Veo que eres administrador. Puedo ayudarte a gestionar habitaciones, ver reservaciones y más. ");
        } else {
            greeting.append("Puedo ayudarte con información sobre reservaciones, habitaciones disponibles y nuestros moteles. ");
        }
        
        greeting.append("¿En qué puedo asistirte hoy?");
        return greeting.toString();
    }

    /**
     * Genera mensaje de ayuda basado en el rol del usuario
     */
    @NonNull
    public String formatHelpMessage(@NonNull ChatSession session) {
        StringBuilder help = new StringBuilder("Puedo ayudarte con lo siguiente:\n\n");
        
        help.append("📋 Comandos disponibles:\n");
        appendUserCommands(help);
        
        if (session.isAdmin()) {
            help.append("\n🔧 Comandos de administrador:\n");
            appendAdminCommands(help);
        }
        
        help.append("\n¿Qué te gustaría hacer?");
        return help.toString();
    }

    /**
     * Formatea lista de reservaciones para administradores
     */
    @NonNull
    public String formatAdminReservations(@NonNull List<Reservation> reservations) {
        if (reservations.isEmpty()) {
            return "No hay reservaciones en el sistema actualmente.";
        }

        StringBuilder response = new StringBuilder("📊 Reservaciones recientes (últimas 20):\n\n");
        
        reservations.forEach(reservation -> 
            appendAdminReservationDetails(response, reservation)
        );
        
        return response.toString();
    }

    /**
     * Formatea lista de reservaciones para usuarios normales
     */
    @NonNull
    public String formatUserReservations(@NonNull List<Reservation> reservations) {
        if (reservations.isEmpty()) {
            return "No tienes reservaciones registradas.";
        }

        StringBuilder response = new StringBuilder("🎯 Tus reservaciones:\n\n");
        
        reservations.forEach(reservation -> 
            appendUserReservationDetails(response, reservation)
        );
        
        return response.toString();
    }

    /**
     * Formatea lista de habitaciones disponibles
     */
    @NonNull
    public String formatRooms(@NonNull List<Room> rooms, @NonNull ChatSession session) {
        if (rooms.isEmpty()) {
            return "Lo siento, no hay habitaciones disponibles en este momento.";
        }

        StringBuilder response = new StringBuilder("🏨 Habitaciones disponibles:\n\n");
        
        rooms.stream()
                .limit(MAX_ITEMS_TO_DISPLAY)
                .forEach(room -> appendRoomDetails(response, room, session));
        
        return response.toString();
    }

    /**
     * Formatea lista de moteles
     */
    @NonNull
    public String formatMotels(@NonNull List<Motel> motels) {
        if (motels.isEmpty()) {
            return "No hay información de moteles disponible en este momento.";
        }

        StringBuilder response = new StringBuilder("🏢 Nuestros establecimientos:\n\n");
        
        motels.stream()
                .limit(MAX_ITEMS_TO_DISPLAY)
                .forEach(motel -> appendMotelDetails(response, motel));
        
        return response.toString();
    }

    /**
     * Genera guía para crear habitaciones (solo administradores)
     */
    @NonNull
    public String formatRoomCreationHelp() {
        return """
                📝 Guía para crear una habitación:
                
                Para crear una nueva habitación, necesitas utilizar la API REST directamente:
                
                POST /api/rooms
                {
                  "motelId": [ID del motel],
                  "number": "[Número de habitación]",
                  "roomType": "[Tipo: suite, standard, deluxe]",
                  "price": [Precio por noche],
                  "description": "[Descripción]",
                  "imageUrls": []
                }
                
                También puedes usar el panel de administración web para una experiencia más amigable.
                """;
    }

    /**
     * Genera información del panel de administración
     */
    @NonNull
    public String formatAdminPanelHelp() {
        return """
                🔧 Panel de Administración
                
                Como administrador, puedes:
                
                ✅ Ver todas las reservaciones del sistema
                ✅ Consultar habitaciones y su disponibilidad
                ✅ Ver información de todos los moteles
                ✅ Crear y gestionar habitaciones (vía API)
                
                Endpoints útiles:
                • GET /api/reservations - Todas las reservaciones
                • GET /api/rooms - Todas las habitaciones
                • POST /api/rooms - Crear habitación
                • PUT /api/rooms/{id} - Actualizar habitación
                • GET /api/motels - Todos los moteles
                
                ¿En qué más puedo ayudarte?
                """;
    }

    /**
     * Genera mensaje por defecto para intenciones desconocidas
     */
    @NonNull
    public String formatUnknownIntentMessage() {
        return "Lo siento, no entendí tu consulta. Escribe 'ayuda' para ver las opciones disponibles.";
    }

    /**
     * Traduce el estado de reservación a español
     */
    @NonNull
    public String translateReservationStatus(@NonNull Reservation.ReservationStatus status) {
        return switch (status) {
            case PENDING -> "Pendiente";
            case CONFIRMED -> "Confirmada";
            case CHECKED_IN -> "Check-in realizado";
            case CHECKED_OUT -> "Check-out realizado";
            case CANCELLED -> "Cancelada";
        };
    }

    // Métodos privados de ayuda para construcción de respuestas

    private void appendUserCommands(StringBuilder help) {
        help.append("• 'mis reservas' - Ver tus reservaciones\n");
        help.append("• 'habitaciones disponibles' - Ver habitaciones disponibles\n");
        help.append("• 'información del motel' - Conocer nuestros establecimientos\n");
    }

    private void appendAdminCommands(StringBuilder help) {
        help.append("• 'crear habitación' - Guía para crear una nueva habitación\n");
        help.append("• 'panel de administración' - Información sobre gestión del motel\n");
        help.append("• 'todas las reservas' - Ver todas las reservaciones del sistema\n");
    }

    private void appendAdminReservationDetails(StringBuilder response, Reservation reservation) {
        response.append(String.format("🔹 ID: %d | Estado: %s\n", 
                reservation.id(), reservation.status()));
        response.append(String.format("   Check-in: %s\n", 
                formatDateTime(reservation.checkInDate())));
        response.append(String.format("   Check-out: %s\n", 
                formatDateTime(reservation.checkOutDate())));
        response.append(String.format("   Precio: $%.2f\n\n", reservation.totalPrice()));
    }

    private void appendUserReservationDetails(StringBuilder response, Reservation reservation) {
        response.append(String.format("🔹 Reservación #%d\n", reservation.id()));
        response.append(String.format("   Estado: %s\n", 
                translateReservationStatus(reservation.status())));
        response.append(String.format("   Check-in: %s\n", 
                formatDateTime(reservation.checkInDate())));
        response.append(String.format("   Check-out: %s\n", 
                formatDateTime(reservation.checkOutDate())));
        response.append(String.format("   Total: $%.2f\n", reservation.totalPrice()));
        
        appendSpecialRequests(response, reservation.specialRequests());
        response.append("\n");
    }

    private void appendRoomDetails(StringBuilder response, Room room, ChatSession session) {
        response.append(String.format("🔸 Habitación #%s\n", room.number()));
        response.append(String.format("   Tipo: %s\n", room.roomType()));
        response.append(String.format("   Precio: $%.2f por noche\n", room.price()));
        
        if (session.isAdmin()) {
            response.append(String.format("   ID Motel: %d\n", room.motelId()));
        }
        
        appendOptionalDescription(response, room.description());
        response.append("\n");
    }

    private void appendMotelDetails(StringBuilder response, Motel motel) {
        response.append(String.format("🔹 %s\n", motel.name()));
        response.append(String.format("   📍 %s, %s\n", motel.address(), motel.city()));
        
        appendOptionalPhoneNumber(response, motel.phoneNumber());
        appendOptionalDescription(response, motel.description());
        response.append("\n");
    }

    private void appendSpecialRequests(StringBuilder response, String specialRequests) {
        Optional.ofNullable(specialRequests)
                .filter(s -> !s.isEmpty())
                .ifPresent(s -> response.append(String.format("   Observaciones: %s\n", s)));
    }

    private void appendOptionalPhoneNumber(StringBuilder response, String phoneNumber) {
        Optional.ofNullable(phoneNumber)
                .filter(p -> !p.isEmpty())
                .ifPresent(p -> response.append(String.format("   📞 %s\n", p)));
    }

    private void appendOptionalDescription(StringBuilder response, String description) {
        Optional.ofNullable(description)
                .filter(d -> !d.isEmpty())
                .ifPresent(d -> response.append(String.format("   ℹ️  %s\n", d)));
    }

    private String formatDateTime(LocalDateTime dateTime) {
        return dateTime.format(DATE_TIME_FORMATTER);
    }
}
