package com.ubik.usermanagement.infrastructure.adapter.out.persistence.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;

/**
 * Entidad JPA para ChatMessage
 */
@Table("chat_messages")
public class ChatMessageEntity {

    @Id
    private Long id;

    @Column("session_id")
    private Long sessionId;

    @Column("message")
    private String message;

    @Column("response")
    private String response;

    @Column("message_type")
    private String messageType;

    @Column("timestamp")
    private LocalDateTime timestamp;

    @Column("user_id")
    private Long userId;

    // Constructor vacío requerido por R2DBC
    public ChatMessageEntity() {
    }

    public ChatMessageEntity(Long id, Long sessionId, String message, String response,
                             String messageType, LocalDateTime timestamp, Long userId) {
        this.id = id;
        this.sessionId = sessionId;
        this.message = message;
        this.response = response;
        this.messageType = messageType;
        this.timestamp = timestamp;
        this.userId = userId;
    }

    // Getters y Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getSessionId() {
        return sessionId;
    }

    public void setSessionId(Long sessionId) {
        this.sessionId = sessionId;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getResponse() {
        return response;
    }

    public void setResponse(String response) {
        this.response = response;
    }

    public String getMessageType() {
        return messageType;
    }

    public void setMessageType(String messageType) {
        this.messageType = messageType;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }
}
