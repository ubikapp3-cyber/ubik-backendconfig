package com.ubik.usermanagement.infrastructure.adapter.out.persistence.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;

/**
 * Entidad JPA para ChatSession
 */
@Table("chat_sessions")
public class ChatSessionEntity {

    @Id
    private Long id;

    @Column("user_id")
    private Long userId;

    @Column("user_role")
    private String userRole;

    @Column("started_at")
    private LocalDateTime startedAt;

    @Column("last_activity_at")
    private LocalDateTime lastActivityAt;

    @Column("status")
    private String status;

    @Column("context")
    private String context; // JSON serializado como String

    // Constructor vacío requerido por R2DBC
    public ChatSessionEntity() {
    }

    public ChatSessionEntity(Long id, Long userId, String userRole, LocalDateTime startedAt,
                             LocalDateTime lastActivityAt, String status, String context) {
        this.id = id;
        this.userId = userId;
        this.userRole = userRole;
        this.startedAt = startedAt;
        this.lastActivityAt = lastActivityAt;
        this.status = status;
        this.context = context;
    }

    // Getters y Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getUserRole() {
        return userRole;
    }

    public void setUserRole(String userRole) {
        this.userRole = userRole;
    }

    public LocalDateTime getStartedAt() {
        return startedAt;
    }

    public void setStartedAt(LocalDateTime startedAt) {
        this.startedAt = startedAt;
    }

    public LocalDateTime getLastActivityAt() {
        return lastActivityAt;
    }

    public void setLastActivityAt(LocalDateTime lastActivityAt) {
        this.lastActivityAt = lastActivityAt;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getContext() {
        return context;
    }

    public void setContext(String context) {
        this.context = context;
    }
}
