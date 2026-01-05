package com.ubik.usermanagement.domain.port.in;

import com.ubik.usermanagement.domain.model.ChatMessage;
import com.ubik.usermanagement.domain.model.ChatSession;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Puerto de entrada para casos de uso del chatbot
 * Define las operaciones que el chatbot puede realizar
 */
public interface ChatbotUseCasePort {

    /**
     * Procesa un mensaje del usuario y genera una respuesta
     * 
     * @param sessionId ID de la sesión del chat
     * @param message Mensaje del usuario
     * @param userId ID del usuario que envía el mensaje
     * @param userRole Rol del usuario (USER, ADMIN, etc.)
     * @return Respuesta del chatbot
     */
    Mono<ChatMessage> processMessage(Long sessionId, String message, Long userId, String userRole);

    /**
     * Crea una nueva sesión de chat
     * 
     * @param userId ID del usuario
     * @param userRole Rol del usuario
     * @return Nueva sesión de chat
     */
    Mono<ChatSession> createSession(Long userId, String userRole);

    /**
     * Obtiene una sesión de chat por ID
     * 
     * @param sessionId ID de la sesión
     * @return Sesión de chat
     */
    Mono<ChatSession> getSession(Long sessionId);

    /**
     * Obtiene el historial de mensajes de una sesión
     * 
     * @param sessionId ID de la sesión
     * @return Historial de mensajes
     */
    Flux<ChatMessage> getSessionHistory(Long sessionId);

    /**
     * Cierra una sesión de chat
     * 
     * @param sessionId ID de la sesión
     * @return Sesión cerrada
     */
    Mono<ChatSession> closeSession(Long sessionId);

    /**
     * Elimina una sesión y su historial
     * 
     * @param sessionId ID de la sesión
     * @return Vacío
     */
    Mono<Void> deleteSession(Long sessionId);
}
