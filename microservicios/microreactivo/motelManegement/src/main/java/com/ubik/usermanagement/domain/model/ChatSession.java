package com.ubik.usermanagement.domain.model;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Modelo de dominio para ChatSession
 * Representa una sesión de conversación con el chatbot
 */
public record ChatSession(
        Long id,
        Long userId,
        String userRole,
        LocalDateTime startedAt,
        LocalDateTime lastActivityAt,
        SessionStatus status,
        Map<String, String> context
) {
    /**
     * Estados de una sesión de chat
     */
    public enum SessionStatus {
        ACTIVE,      // Sesión activa
        INACTIVE,    // Sesión inactiva
        EXPIRED,     // Sesión expirada
        CLOSED       // Sesión cerrada
    }

    /**
     * Constructor para crear una nueva sesión
     */
    public static ChatSession createNew(Long userId, String userRole) {
        LocalDateTime now = LocalDateTime.now();
        return new ChatSession(
                null,
                userId,
                userRole,
                now,
                now,
                SessionStatus.ACTIVE,
                Map.of()
        );
    }

    /**
     * Actualiza la última actividad
     */
    public ChatSession updateActivity() {
        return new ChatSession(
                this.id,
                this.userId,
                this.userRole,
                this.startedAt,
                LocalDateTime.now(),
                this.status,
                this.context
        );
    }

    /**
     * Actualiza el contexto de la sesión
     */
    public ChatSession withContext(Map<String, String> newContext) {
        return new ChatSession(
                this.id,
                this.userId,
                this.userRole,
                this.startedAt,
                LocalDateTime.now(),
                this.status,
                newContext
        );
    }

    /**
     * Cierra la sesión
     */
    public ChatSession close() {
        return new ChatSession(
                this.id,
                this.userId,
                this.userRole,
                this.startedAt,
                LocalDateTime.now(),
                SessionStatus.CLOSED,
                this.context
        );
    }

    /**
     * Verifica si el usuario es administrador
     */
    public boolean isAdmin() {
        return "ADMIN".equalsIgnoreCase(this.userRole) || 
               "ROLE_ADMIN".equalsIgnoreCase(this.userRole);
    }

    /**
     * Verifica si la sesión está activa
     */
    public boolean isActive() {
        return this.status == SessionStatus.ACTIVE;
    }
}
