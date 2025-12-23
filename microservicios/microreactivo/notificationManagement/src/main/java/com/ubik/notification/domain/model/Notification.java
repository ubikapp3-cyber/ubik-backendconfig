package com.ubik.notification.domain.model;

import java.time.LocalDateTime;

/**
 * Modelo de dominio para Notification
 * Representa la entidad de negocio independiente de la infraestructura
 */
public record Notification(
        Long id,
        String title,
        String message,
        String type,
        String recipient,
        String recipientType,
        NotificationStatus status,
        LocalDateTime createdAt,
        LocalDateTime sentAt,
        LocalDateTime readAt,
        String metadata
) {
    /**
     * Estado de la notificación
     */
    public enum NotificationStatus {
        PENDING,    // Pendiente de envío
        SENT,       // Enviada
        READ,       // Leída por el destinatario
        FAILED,     // Falló el envío
        CANCELLED   // Cancelada
    }

    /**
     * Constructor para creación de nuevas notificaciones (sin ID)
     */
    public static Notification createNew(
            String title,
            String message,
            String type,
            String recipient,
            String recipientType,
            String metadata
    ) {
        return new Notification(
                null,
                title,
                message,
                type,
                recipient,
                recipientType,
                NotificationStatus.PENDING,
                LocalDateTime.now(),
                null,
                null,
                metadata
        );
    }

    /**
     * Marca la notificación como enviada
     */
    public Notification markAsSent() {
        return new Notification(
                this.id,
                this.title,
                this.message,
                this.type,
                this.recipient,
                this.recipientType,
                NotificationStatus.SENT,
                this.createdAt,
                LocalDateTime.now(),
                this.readAt,
                this.metadata
        );
    }

    /**
     * Marca la notificación como leída
     */
    public Notification markAsRead() {
        return new Notification(
                this.id,
                this.title,
                this.message,
                this.type,
                this.recipient,
                this.recipientType,
                NotificationStatus.READ,
                this.createdAt,
                this.sentAt,
                LocalDateTime.now(),
                this.metadata
        );
    }

    /**
     * Marca la notificación como fallida
     */
    public Notification markAsFailed() {
        return new Notification(
                this.id,
                this.title,
                this.message,
                this.type,
                this.recipient,
                this.recipientType,
                NotificationStatus.FAILED,
                this.createdAt,
                this.sentAt,
                this.readAt,
                this.metadata
        );
    }

    /**
     * Actualiza la información de la notificación
     */
    public Notification withUpdatedInfo(
            String title,
            String message,
            String type
    ) {
        return new Notification(
                this.id,
                title,
                message,
                type,
                this.recipient,
                this.recipientType,
                this.status,
                this.createdAt,
                this.sentAt,
                this.readAt,
                this.metadata
        );
    }
}
