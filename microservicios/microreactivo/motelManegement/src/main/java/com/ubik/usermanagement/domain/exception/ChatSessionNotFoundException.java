package com.ubik.usermanagement.domain.exception;

/**
 * Excepción lanzada cuando no se encuentra una sesión de chat
 */
public class ChatSessionNotFoundException extends RuntimeException {
    
    private final Long sessionId;

    public ChatSessionNotFoundException(Long sessionId) {
        super("Sesión de chat no encontrada con ID: " + sessionId);
        this.sessionId = sessionId;
    }

    public Long getSessionId() {
        return sessionId;
    }
}
