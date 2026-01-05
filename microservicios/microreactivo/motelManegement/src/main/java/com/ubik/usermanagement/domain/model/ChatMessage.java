package com.ubik.usermanagement.domain.model;

import java.time.LocalDateTime;

/**
 * Modelo de dominio para ChatMessage
 * Representa un mensaje en una conversación del chatbot
 */
public record ChatMessage(
        Long id,
        Long sessionId,
        String message,
        String response,
        MessageType messageType,
        LocalDateTime timestamp,
        Long userId
) {
    /**
     * Tipos de mensaje del chatbot
     */
    public enum MessageType {
        USER_QUERY,           // Consulta del usuario
        BOT_RESPONSE,         // Respuesta del bot
        SYSTEM_INFO,          // Información del sistema
        ERROR_MESSAGE         // Mensaje de error
    }

    /**
     * Constructor para crear un nuevo mensaje de usuario
     */
    public static ChatMessage createUserMessage(
            Long sessionId,
            String message,
            Long userId
    ) {
        return new ChatMessage(
                null,
                sessionId,
                message,
                null,
                MessageType.USER_QUERY,
                LocalDateTime.now(),
                userId
        );
    }

    /**
     * Constructor para crear una respuesta del bot
     */
    public ChatMessage withBotResponse(String response) {
        return new ChatMessage(
                this.id,
                this.sessionId,
                this.message,
                response,
                MessageType.BOT_RESPONSE,
                this.timestamp,
                this.userId
        );
    }
}
