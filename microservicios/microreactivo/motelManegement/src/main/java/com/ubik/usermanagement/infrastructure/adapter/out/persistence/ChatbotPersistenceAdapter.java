package com.ubik.usermanagement.infrastructure.adapter.out.persistence;

import com.ubik.usermanagement.domain.model.ChatMessage;
import com.ubik.usermanagement.domain.model.ChatSession;
import com.ubik.usermanagement.domain.port.out.ChatbotRepositoryPort;
import com.ubik.usermanagement.infrastructure.adapter.out.persistence.mapper.ChatbotMapper;
import com.ubik.usermanagement.infrastructure.adapter.out.persistence.repository.ChatMessageR2dbcRepository;
import com.ubik.usermanagement.infrastructure.adapter.out.persistence.repository.ChatSessionR2dbcRepository;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Adaptador de persistencia para el chatbot
 * Implementa las operaciones de acceso a datos
 */
@Component
public class ChatbotPersistenceAdapter implements ChatbotRepositoryPort {

    private final ChatMessageR2dbcRepository chatMessageRepository;
    private final ChatSessionR2dbcRepository chatSessionRepository;
    private final ChatbotMapper chatbotMapper;

    public ChatbotPersistenceAdapter(
            ChatMessageR2dbcRepository chatMessageRepository,
            ChatSessionR2dbcRepository chatSessionRepository,
            ChatbotMapper chatbotMapper
    ) {
        this.chatMessageRepository = chatMessageRepository;
        this.chatSessionRepository = chatSessionRepository;
        this.chatbotMapper = chatbotMapper;
    }

    @Override
    public Mono<ChatMessage> saveMessage(ChatMessage chatMessage) {
        return Mono.just(chatMessage)
                .map(chatbotMapper::toEntity)
                .flatMap(chatMessageRepository::save)
                .map(chatbotMapper::toDomain);
    }

    @Override
    public Mono<ChatSession> saveSession(ChatSession chatSession) {
        return Mono.just(chatSession)
                .map(chatbotMapper::toEntity)
                .flatMap(chatSessionRepository::save)
                .map(chatbotMapper::toDomain);
    }

    @Override
    public Mono<ChatSession> updateSession(ChatSession chatSession) {
        return Mono.just(chatSession)
                .map(chatbotMapper::toEntity)
                .flatMap(chatSessionRepository::save)
                .map(chatbotMapper::toDomain);
    }

    @Override
    public Mono<ChatSession> findSessionById(Long sessionId) {
        return chatSessionRepository.findById(sessionId)
                .map(chatbotMapper::toDomain);
    }

    @Override
    public Flux<ChatMessage> findMessagesBySessionId(Long sessionId) {
        return chatMessageRepository.findBySessionIdOrderByTimestamp(sessionId)
                .map(chatbotMapper::toDomain);
    }

    @Override
    public Mono<Void> deleteSession(Long sessionId) {
        return chatSessionRepository.deleteById(sessionId);
    }

    @Override
    public Mono<Void> deleteMessagesBySessionId(Long sessionId) {
        return chatMessageRepository.deleteBySessionId(sessionId);
    }

    @Override
    public Flux<ChatSession> findActiveSessionsByUserId(Long userId) {
        return chatSessionRepository.findActiveSessionsByUserId(userId)
                .map(chatbotMapper::toDomain);
    }
}
