package com.ubik.usermanagement.infrastructure.adapter.out.persistence.mapper;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ubik.usermanagement.domain.model.ChatMessage;
import com.ubik.usermanagement.domain.model.ChatSession;
import com.ubik.usermanagement.infrastructure.adapter.out.persistence.entity.ChatMessageEntity;
import com.ubik.usermanagement.infrastructure.adapter.out.persistence.entity.ChatSessionEntity;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * Mapper entre entidades de persistencia y modelos de dominio del chatbot
 */
@Component
public class ChatbotMapper {

    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Convierte ChatMessage de dominio a entidad
     */
    public ChatMessageEntity toEntity(ChatMessage chatMessage) {
        return new ChatMessageEntity(
                chatMessage.id(),
                chatMessage.sessionId(),
                chatMessage.message(),
                chatMessage.response(),
                chatMessage.messageType().name(),
                chatMessage.timestamp(),
                chatMessage.userId()
        );
    }

    /**
     * Convierte entidad a ChatMessage de dominio
     */
    public ChatMessage toDomain(ChatMessageEntity entity) {
        return new ChatMessage(
                entity.getId(),
                entity.getSessionId(),
                entity.getMessage(),
                entity.getResponse(),
                ChatMessage.MessageType.valueOf(entity.getMessageType()),
                entity.getTimestamp(),
                entity.getUserId()
        );
    }

    /**
     * Convierte ChatSession de dominio a entidad
     */
    public ChatSessionEntity toEntity(ChatSession chatSession) {
        String contextJson = serializeContext(chatSession.context());
        return new ChatSessionEntity(
                chatSession.id(),
                chatSession.userId(),
                chatSession.userRole(),
                chatSession.startedAt(),
                chatSession.lastActivityAt(),
                chatSession.status().name(),
                contextJson
        );
    }

    /**
     * Convierte entidad a ChatSession de dominio
     */
    public ChatSession toDomain(ChatSessionEntity entity) {
        Map<String, String> context = deserializeContext(entity.getContext());
        return new ChatSession(
                entity.getId(),
                entity.getUserId(),
                entity.getUserRole(),
                entity.getStartedAt(),
                entity.getLastActivityAt(),
                ChatSession.SessionStatus.valueOf(entity.getStatus()),
                context
        );
    }

    /**
     * Serializa el contexto a JSON
     */
    private String serializeContext(Map<String, String> context) {
        try {
            return objectMapper.writeValueAsString(context);
        } catch (JsonProcessingException e) {
            return "{}";
        }
    }

    /**
     * Deserializa el contexto desde JSON
     */
    private Map<String, String> deserializeContext(String contextJson) {
        if (contextJson == null || contextJson.isEmpty()) {
            return new HashMap<>();
        }
        try {
            return objectMapper.readValue(contextJson, new TypeReference<Map<String, String>>() {});
        } catch (JsonProcessingException e) {
            return new HashMap<>();
        }
    }
}
