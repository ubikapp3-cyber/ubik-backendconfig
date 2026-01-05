package com.ubik.usermanagement.domain.port.out;

import com.ubik.usermanagement.domain.model.ChatMessage;
import com.ubik.usermanagement.domain.model.ChatSession;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Puerto de salida para el repositorio de chatbot
 * Define las operaciones de persistencia para el chatbot
 */
public interface ChatbotRepositoryPort {

    /**
     * Guarda un mensaje de chat
     * 
     * @param chatMessage Mensaje a guardar
     * @return Mensaje guardado con ID
     */
    Mono<ChatMessage> saveMessage(ChatMessage chatMessage);

    /**
     * Guarda una sesión de chat
     * 
     * @param chatSession Sesión a guardar
     * @return Sesión guardada con ID
     */
    Mono<ChatSession> saveSession(ChatSession chatSession);

    /**
     * Actualiza una sesión de chat
     * 
     * @param chatSession Sesión a actualizar
     * @return Sesión actualizada
     */
    Mono<ChatSession> updateSession(ChatSession chatSession);

    /**
     * Busca una sesión por ID
     * 
     * @param sessionId ID de la sesión
     * @return Sesión encontrada
     */
    Mono<ChatSession> findSessionById(Long sessionId);

    /**
     * Busca mensajes de una sesión
     * 
     * @param sessionId ID de la sesión
     * @return Lista de mensajes
     */
    Flux<ChatMessage> findMessagesBySessionId(Long sessionId);

    /**
     * Elimina una sesión
     * 
     * @param sessionId ID de la sesión
     * @return Vacío
     */
    Mono<Void> deleteSession(Long sessionId);

    /**
     * Elimina mensajes de una sesión
     * 
     * @param sessionId ID de la sesión
     * @return Vacío
     */
    Mono<Void> deleteMessagesBySessionId(Long sessionId);

    /**
     * Busca sesiones activas de un usuario
     * 
     * @param userId ID del usuario
     * @return Lista de sesiones activas
     */
    Flux<ChatSession> findActiveSessionsByUserId(Long userId);
}
