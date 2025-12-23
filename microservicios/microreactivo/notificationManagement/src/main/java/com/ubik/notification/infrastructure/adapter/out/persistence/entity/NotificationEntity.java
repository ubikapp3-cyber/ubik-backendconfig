package com.ubik.notification.infrastructure.adapter.out.persistence.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;

/**
 * Entidad JPA para la tabla de notificaciones
 * Representa la estructura de persistencia
 */
@Table("notifications")
public record NotificationEntity(
        @Id
        Long id,

        @Column("title")
        String title,

        @Column("message")
        String message,

        @Column("type")
        String type,

        @Column("recipient")
        String recipient,

        @Column("recipient_type")
        String recipientType,

        @Column("status")
        String status,

        @Column("created_at")
        LocalDateTime createdAt,

        @Column("sent_at")
        LocalDateTime sentAt,

        @Column("read_at")
        LocalDateTime readAt,

        @Column("metadata")
        String metadata
) {
}
