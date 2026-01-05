package com.ubik.usermanagement.infrastructure.adapter.in.web.mapper;

import com.ubik.usermanagement.domain.model.ChatMessage;
import com.ubik.usermanagement.domain.model.ChatSession;
import com.ubik.usermanagement.infrastructure.adapter.in.web.dto.ChatMessageResponse;
import com.ubik.usermanagement.infrastructure.adapter.in.web.dto.ChatSessionResponse;
import org.springframework.stereotype.Component;

/**
 * Mapper para convertir entre modelos de dominio y DTOs del chatbot
 */
@Component
public class ChatbotDtoMapper {

    /**
     * Convierte un ChatMessage de dominio a ChatMessageResponse DTO
     */
    public ChatMessageResponse toMessageResponse(ChatMessage chatMessage) {
        return new ChatMessageResponse(
                chatMessage.id(),
                chatMessage.sessionId(),
                chatMessage.message(),
                chatMessage.response(),
                chatMessage.messageType().name(),
                chatMessage.timestamp()
        );
    }

    /**
     * Convierte un ChatSession de dominio a ChatSessionResponse DTO
     */
    public ChatSessionResponse toSessionResponse(ChatSession chatSession) {
        return new ChatSessionResponse(
                chatSession.id(),
                chatSession.userId(),
                chatSession.userRole(),
                chatSession.startedAt(),
                chatSession.lastActivityAt(),
                chatSession.status().name(),
                chatSession.context()
        );
    }
}
