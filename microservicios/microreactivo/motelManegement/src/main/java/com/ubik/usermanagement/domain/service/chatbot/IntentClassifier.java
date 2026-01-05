package com.ubik.usermanagement.domain.service.chatbot;

import com.ubik.usermanagement.domain.model.ChatSession;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

import java.util.Arrays;

/**
 * Componente responsable de clasificar la intención del usuario basado en palabras clave
 * Aplica Single Responsibility Principle (SOLID)
 */
@Component
public class IntentClassifier {

    private static final String[] GREETING_KEYWORDS = {
            "hola", "buenos días", "buenas tardes", "buenas noches", "hi", "hello"
    };

    private static final String[] HELP_KEYWORDS = {
            "ayuda", "help", "qué puedes hacer", "comandos"
    };

    private static final String[] RESERVATION_KEYWORDS = {
            "reserva", "reservación", "booking", "mis reservas"
    };

    private static final String[] ROOM_KEYWORDS = {
            "habitacion", "habitación", "room", "cuarto", "disponible"
    };

    private static final String[] MOTEL_KEYWORDS = {
            "motel", "hotel", "establecimiento", "ubicación"
    };

    private static final String[] ADMIN_CREATE_ROOM_KEYWORDS = {
            "crear habitacion", "nueva habitacion", "agregar habitacion"
    };

    private static final String[] ADMIN_PANEL_KEYWORDS = {
            "gestionar", "administrar", "panel"
    };

    /**
     * Clasifica la intención del usuario basada en el mensaje
     */
    @NonNull
    public Intent classifyIntent(@NonNull String message, @NonNull ChatSession session) {
        String normalizedMessage = message.toLowerCase();

        if (containsAnyKeyword(normalizedMessage, GREETING_KEYWORDS)) {
            return Intent.GREETING;
        }

        if (containsAnyKeyword(normalizedMessage, HELP_KEYWORDS)) {
            return Intent.HELP;
        }

        if (containsAnyKeyword(normalizedMessage, RESERVATION_KEYWORDS)) {
            return Intent.RESERVATION_QUERY;
        }

        if (containsAnyKeyword(normalizedMessage, ROOM_KEYWORDS)) {
            return Intent.ROOM_QUERY;
        }

        if (containsAnyKeyword(normalizedMessage, MOTEL_KEYWORDS)) {
            return Intent.MOTEL_QUERY;
        }

        // Intenciones solo para administradores
        if (session.isAdmin()) {
            if (containsAnyKeyword(normalizedMessage, ADMIN_CREATE_ROOM_KEYWORDS)) {
                return Intent.ADMIN_CREATE_ROOM;
            }

            if (containsAnyKeyword(normalizedMessage, ADMIN_PANEL_KEYWORDS)) {
                return Intent.ADMIN_PANEL;
            }
        }

        return Intent.UNKNOWN;
    }

    /**
     * Verifica si el mensaje contiene alguna de las palabras clave
     */
    private boolean containsAnyKeyword(String message, String[] keywords) {
        return Arrays.stream(keywords)
                .anyMatch(message::contains);
    }

    /**
     * Enumeración de intenciones posibles del usuario
     */
    public enum Intent {
        GREETING,
        HELP,
        RESERVATION_QUERY,
        ROOM_QUERY,
        MOTEL_QUERY,
        ADMIN_CREATE_ROOM,
        ADMIN_PANEL,
        UNKNOWN
    }
}
