package com.ubik.usermanagement.domain.service;

import com.ubik.usermanagement.domain.model.ChatMessage;
import com.ubik.usermanagement.domain.model.ChatSession;
import com.ubik.usermanagement.domain.model.Reservation;
import com.ubik.usermanagement.domain.model.Room;
import com.ubik.usermanagement.domain.model.Motel;
import com.ubik.usermanagement.domain.port.in.ChatbotUseCasePort;
import com.ubik.usermanagement.domain.port.out.ChatbotRepositoryPort;
import com.ubik.usermanagement.domain.port.out.ReservationRepositoryPort;
import com.ubik.usermanagement.domain.port.out.RoomRepositoryPort;
import com.ubik.usermanagement.domain.port.out.MotelRepositoryPort;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.stream.Collectors;

/**
 * Servicio de dominio del chatbot
 * Procesa mensajes del usuario y genera respuestas inteligentes
 * Incluye seguridad para proteger información confidencial
 */
@Service
public class ChatbotService implements ChatbotUseCasePort {

    private final ChatbotRepositoryPort chatbotRepositoryPort;
    private final ReservationRepositoryPort reservationRepositoryPort;
    private final RoomRepositoryPort roomRepositoryPort;
    private final MotelRepositoryPort motelRepositoryPort;

    public ChatbotService(
            ChatbotRepositoryPort chatbotRepositoryPort,
            ReservationRepositoryPort reservationRepositoryPort,
            RoomRepositoryPort roomRepositoryPort,
            MotelRepositoryPort motelRepositoryPort
    ) {
        this.chatbotRepositoryPort = chatbotRepositoryPort;
        this.reservationRepositoryPort = reservationRepositoryPort;
        this.roomRepositoryPort = roomRepositoryPort;
        this.motelRepositoryPort = motelRepositoryPort;
    }

    @Override
    public Mono<ChatMessage> processMessage(Long sessionId, String message, Long userId, String userRole) {
        // Crear el mensaje del usuario
        ChatMessage userMessage = ChatMessage.createUserMessage(sessionId, message, userId);
        
        // Guardar el mensaje del usuario y obtener la sesión
        return chatbotRepositoryPort.saveMessage(userMessage)
                .then(chatbotRepositoryPort.findSessionById(sessionId))
                .flatMap(session -> {
                    // Actualizar última actividad de la sesión
                    ChatSession updatedSession = session.updateActivity();
                    return chatbotRepositoryPort.updateSession(updatedSession)
                            .then(processUserIntent(message.toLowerCase(Locale.ROOT), userId, session));
                })
                .flatMap(response -> {
                    ChatMessage responseMessage = userMessage.withBotResponse(response);
                    return chatbotRepositoryPort.saveMessage(responseMessage);
                });
    }

    @Override
    public Mono<ChatSession> createSession(Long userId, String userRole) {
        ChatSession newSession = ChatSession.createNew(userId, userRole);
        return chatbotRepositoryPort.saveSession(newSession);
    }

    @Override
    public Mono<ChatSession> getSession(Long sessionId) {
        return chatbotRepositoryPort.findSessionById(sessionId)
                .switchIfEmpty(Mono.error(new RuntimeException("Sesión no encontrada: " + sessionId)));
    }

    @Override
    public Flux<ChatMessage> getSessionHistory(Long sessionId) {
        return chatbotRepositoryPort.findMessagesBySessionId(sessionId);
    }

    @Override
    public Mono<ChatSession> closeSession(Long sessionId) {
        return chatbotRepositoryPort.findSessionById(sessionId)
                .flatMap(session -> {
                    ChatSession closedSession = session.close();
                    return chatbotRepositoryPort.updateSession(closedSession);
                });
    }

    @Override
    public Mono<Void> deleteSession(Long sessionId) {
        return chatbotRepositoryPort.deleteMessagesBySessionId(sessionId)
                .then(chatbotRepositoryPort.deleteSession(sessionId));
    }

    /**
     * Procesa la intención del usuario y genera una respuesta apropiada
     */
    private Mono<String> processUserIntent(String message, Long userId, ChatSession session) {
        // Clasificación de intenciones
        if (containsKeywords(message, "hola", "buenos días", "buenas tardes", "buenas noches", "hi", "hello")) {
            return Mono.just(generateGreeting(session));
        }
        
        if (containsKeywords(message, "ayuda", "help", "qué puedes hacer", "comandos")) {
            return Mono.just(generateHelpMessage(session));
        }
        
        if (containsKeywords(message, "reserva", "reservación", "booking", "mis reservas")) {
            return handleReservationQuery(userId, session);
        }
        
        if (containsKeywords(message, "habitacion", "habitación", "room", "cuarto", "disponible")) {
            return handleRoomQuery(message, session);
        }
        
        if (containsKeywords(message, "motel", "hotel", "establecimiento", "ubicación")) {
            return handleMotelQuery(message);
        }
        
        // Comandos de administrador
        if (session.isAdmin()) {
            if (containsKeywords(message, "crear habitacion", "nueva habitacion", "agregar habitacion")) {
                return Mono.just(generateRoomCreationHelp());
            }
            
            if (containsKeywords(message, "gestionar", "administrar", "panel")) {
                return Mono.just(generateAdminHelp());
            }
        }
        
        // Respuesta por defecto
        return Mono.just("Lo siento, no entendí tu consulta. Escribe 'ayuda' para ver las opciones disponibles.");
    }

    /**
     * Verifica si el mensaje contiene alguna de las palabras clave
     */
    private boolean containsKeywords(String message, String... keywords) {
        for (String keyword : keywords) {
            if (message.contains(keyword.toLowerCase())) {
                return true;
            }
        }
        return false;
    }

    /**
     * Genera un mensaje de saludo personalizado
     */
    private String generateGreeting(ChatSession session) {
        String greeting = "¡Hola! Soy el asistente virtual de gestión de moteles. ";
        if (session.isAdmin()) {
            greeting += "Veo que eres administrador. Puedo ayudarte a gestionar habitaciones, ver reservaciones y más. ";
        } else {
            greeting += "Puedo ayudarte con información sobre reservaciones, habitaciones disponibles y nuestros moteles. ";
        }
        greeting += "¿En qué puedo asistirte hoy?";
        return greeting;
    }

    /**
     * Genera un mensaje de ayuda basado en el rol del usuario
     */
    private String generateHelpMessage(ChatSession session) {
        StringBuilder help = new StringBuilder("Puedo ayudarte con lo siguiente:\n\n");
        
        help.append("📋 Comandos disponibles:\n");
        help.append("• 'mis reservas' - Ver tus reservaciones\n");
        help.append("• 'habitaciones disponibles' - Ver habitaciones disponibles\n");
        help.append("• 'información del motel' - Conocer nuestros establecimientos\n");
        
        if (session.isAdmin()) {
            help.append("\n🔧 Comandos de administrador:\n");
            help.append("• 'crear habitación' - Guía para crear una nueva habitación\n");
            help.append("• 'panel de administración' - Información sobre gestión del motel\n");
            help.append("• 'todas las reservas' - Ver todas las reservaciones del sistema\n");
        }
        
        help.append("\n¿Qué te gustaría hacer?");
        return help.toString();
    }

    /**
     * Maneja consultas sobre reservaciones
     */
    private Mono<String> handleReservationQuery(Long userId, ChatSession session) {
        if (session.isAdmin()) {
            // Administradores pueden ver todas las reservas (limitadas por seguridad)
            return reservationRepositoryPort.findAll()
                    .take(20)
                    .collectList()
                    .map(reservations -> {
                        if (reservations.isEmpty()) {
                            return "No hay reservaciones en el sistema actualmente.";
                        }
                        return formatReservationsAdmin(reservations);
                    });
        } else {
            // Usuarios normales solo ven sus propias reservas
            return reservationRepositoryPort.findByUserId(userId)
                    .collectList()
                    .map(reservations -> {
                        if (reservations.isEmpty()) {
                            return "No tienes reservaciones registradas.";
                        }
                        return formatReservationsUser(reservations);
                    });
        }
    }

    /**
     * Formatea las reservaciones para usuarios administradores
     */
    private String formatReservationsAdmin(java.util.List<Reservation> reservations) {
        StringBuilder response = new StringBuilder("📊 Reservaciones recientes (últimas 20):\n\n");
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        
        for (Reservation reservation : reservations) {
            response.append(String.format("🔹 ID: %d | Estado: %s\n", 
                    reservation.id(), reservation.status()));
            response.append(String.format("   Check-in: %s\n", 
                    reservation.checkInDate().format(formatter)));
            response.append(String.format("   Check-out: %s\n", 
                    reservation.checkOutDate().format(formatter)));
            response.append(String.format("   Precio: $%.2f\n\n", reservation.totalPrice()));
        }
        
        return response.toString();
    }

    /**
     * Formatea las reservaciones para usuarios normales (sin información sensible)
     */
    private String formatReservationsUser(java.util.List<Reservation> reservations) {
        StringBuilder response = new StringBuilder("🎯 Tus reservaciones:\n\n");
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        
        for (Reservation reservation : reservations) {
            response.append(String.format("🔹 Reservación #%d\n", reservation.id()));
            response.append(String.format("   Estado: %s\n", getStatusInSpanish(reservation.status())));
            response.append(String.format("   Check-in: %s\n", 
                    reservation.checkInDate().format(formatter)));
            response.append(String.format("   Check-out: %s\n", 
                    reservation.checkOutDate().format(formatter)));
            response.append(String.format("   Total: $%.2f\n", reservation.totalPrice()));
            
            if (reservation.specialRequests() != null && !reservation.specialRequests().isEmpty()) {
                response.append(String.format("   Observaciones: %s\n", reservation.specialRequests()));
            }
            response.append("\n");
        }
        
        return response.toString();
    }

    /**
     * Maneja consultas sobre habitaciones
     */
    private Mono<String> handleRoomQuery(String message, ChatSession session) {
        // Si pide habitaciones disponibles
        return roomRepositoryPort.findAll()
                .filter(Room::isAvailable)
                .take(10)
                .collectList()
                .map(rooms -> {
                    if (rooms.isEmpty()) {
                        return "Lo siento, no hay habitaciones disponibles en este momento.";
                    }
                    return formatRooms(rooms, session);
                });
    }

    /**
     * Formatea información de habitaciones
     */
    private String formatRooms(java.util.List<Room> rooms, ChatSession session) {
        StringBuilder response = new StringBuilder("🏨 Habitaciones disponibles:\n\n");
        
        for (Room room : rooms) {
            response.append(String.format("🔸 Habitación #%s\n", room.number()));
            response.append(String.format("   Tipo: %s\n", room.roomType()));
            response.append(String.format("   Precio: $%.2f por noche\n", room.price()));
            
            if (session.isAdmin()) {
                response.append(String.format("   ID Motel: %d\n", room.motelId()));
            }
            
            if (room.description() != null && !room.description().isEmpty()) {
                response.append(String.format("   Descripción: %s\n", room.description()));
            }
            response.append("\n");
        }
        
        return response.toString();
    }

    /**
     * Maneja consultas sobre moteles
     */
    private Mono<String> handleMotelQuery(String message) {
        return motelRepositoryPort.findAll()
                .take(10)
                .collectList()
                .map(motels -> {
                    if (motels.isEmpty()) {
                        return "No hay información de moteles disponible en este momento.";
                    }
                    return formatMotels(motels);
                });
    }

    /**
     * Formatea información de moteles (información pública)
     */
    private String formatMotels(java.util.List<Motel> motels) {
        StringBuilder response = new StringBuilder("🏢 Nuestros establecimientos:\n\n");
        
        for (Motel motel : motels) {
            response.append(String.format("🔹 %s\n", motel.name()));
            response.append(String.format("   📍 %s, %s\n", motel.address(), motel.city()));
            
            if (motel.phoneNumber() != null && !motel.phoneNumber().isEmpty()) {
                response.append(String.format("   📞 %s\n", motel.phoneNumber()));
            }
            
            if (motel.description() != null && !motel.description().isEmpty()) {
                response.append(String.format("   ℹ️  %s\n", motel.description()));
            }
            response.append("\n");
        }
        
        return response.toString();
    }

    /**
     * Genera ayuda para crear habitaciones (solo administradores)
     */
    private String generateRoomCreationHelp() {
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
     * Genera información de ayuda para administradores
     */
    private String generateAdminHelp() {
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
     * Traduce el estado de reservación a español
     */
    private String getStatusInSpanish(Reservation.ReservationStatus status) {
        return switch (status) {
            case PENDING -> "Pendiente";
            case CONFIRMED -> "Confirmada";
            case CHECKED_IN -> "Check-in realizado";
            case CHECKED_OUT -> "Check-out realizado";
            case CANCELLED -> "Cancelada";
        };
    }
}
