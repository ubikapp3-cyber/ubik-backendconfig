package com.ubik.usermanagement.infrastructure.adapter.in.web.controller;

import com.ubik.usermanagement.domain.port.in.ChatbotUseCasePort;
import com.ubik.usermanagement.infrastructure.adapter.in.web.dto.ChatMessageRequest;
import com.ubik.usermanagement.infrastructure.adapter.in.web.dto.ChatMessageResponse;
import com.ubik.usermanagement.infrastructure.adapter.in.web.dto.CreateChatSessionRequest;
import com.ubik.usermanagement.infrastructure.adapter.in.web.dto.ChatSessionResponse;
import com.ubik.usermanagement.infrastructure.adapter.in.web.mapper.ChatbotDtoMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Controlador REST reactivo para el chatbot
 * Permite a los usuarios interactuar con el asistente virtual
 * y a los administradores gestionar el motel mediante conversación
 */
@RestController
@RequestMapping("/api/chatbot")
@Tag(name = "Chatbot", description = "API del asistente virtual para consultas y gestión del motel")
public class ChatbotController {

    private final ChatbotUseCasePort chatbotUseCasePort;
    private final ChatbotDtoMapper chatbotDtoMapper;

    public ChatbotController(
            ChatbotUseCasePort chatbotUseCasePort,
            ChatbotDtoMapper chatbotDtoMapper
    ) {
        this.chatbotUseCasePort = chatbotUseCasePort;
        this.chatbotDtoMapper = chatbotDtoMapper;
    }

    /**
     * Crea una nueva sesión de chat
     * POST /api/chatbot/sessions
     */
    @Operation(
            summary = "Crear sesión de chat",
            description = "Crea una nueva sesión de conversación con el chatbot. " +
                    "Cada usuario debe crear una sesión antes de enviar mensajes."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Sesión creada exitosamente"),
            @ApiResponse(responseCode = "400", description = "Datos de entrada inválidos")
    })
    @PostMapping("/sessions")
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<ChatSessionResponse> createSession(
            @Valid @RequestBody CreateChatSessionRequest request
    ) {
        return chatbotUseCasePort.createSession(request.userId(), request.userRole())
                .map(chatbotDtoMapper::toSessionResponse);
    }

    /**
     * Obtiene información de una sesión
     * GET /api/chatbot/sessions/{sessionId}
     */
    @Operation(
            summary = "Obtener sesión de chat",
            description = "Obtiene la información de una sesión de chat existente"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Sesión encontrada"),
            @ApiResponse(responseCode = "404", description = "Sesión no encontrada")
    })
    @GetMapping("/sessions/{sessionId}")
    public Mono<ChatSessionResponse> getSession(
            @Parameter(description = "ID de la sesión", required = true)
            @PathVariable Long sessionId
    ) {
        return chatbotUseCasePort.getSession(sessionId)
                .map(chatbotDtoMapper::toSessionResponse);
    }

    /**
     * Envía un mensaje al chatbot
     * POST /api/chatbot/message
     */
    @Operation(
            summary = "Enviar mensaje al chatbot",
            description = "Envía un mensaje al chatbot y recibe una respuesta inteligente. " +
                    "El chatbot puede proporcionar información sobre reservaciones, habitaciones y moteles. " +
                    "Los administradores tienen acceso a funciones adicionales de gestión."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Mensaje procesado exitosamente"),
            @ApiResponse(responseCode = "400", description = "Datos de entrada inválidos"),
            @ApiResponse(responseCode = "404", description = "Sesión no encontrada")
    })
    @PostMapping("/message")
    public Mono<ChatMessageResponse> sendMessage(
            @Valid @RequestBody ChatMessageRequest request
    ) {
        return chatbotUseCasePort.processMessage(
                        request.sessionId(),
                        request.message(),
                        request.userId(),
                        request.userRole()
                )
                .map(chatbotDtoMapper::toMessageResponse);
    }

    /**
     * Obtiene el historial de mensajes de una sesión
     * GET /api/chatbot/sessions/{sessionId}/history
     */
    @Operation(
            summary = "Obtener historial de chat",
            description = "Obtiene todos los mensajes de una sesión de chat ordenados cronológicamente"
    )
    @ApiResponse(responseCode = "200", description = "Historial obtenido exitosamente")
    @GetMapping("/sessions/{sessionId}/history")
    public Flux<ChatMessageResponse> getSessionHistory(
            @Parameter(description = "ID de la sesión", required = true)
            @PathVariable Long sessionId,
            @RequestParam(defaultValue = "100")
            @Parameter(description = "Límite de mensajes a retornar")
            int limit
    ) {
        if (limit <= 0 || limit > 500) {
            limit = 100;
        }
        return chatbotUseCasePort.getSessionHistory(sessionId)
                .take(limit)
                .map(chatbotDtoMapper::toMessageResponse);
    }

    /**
     * Cierra una sesión de chat
     * PATCH /api/chatbot/sessions/{sessionId}/close
     */
    @Operation(
            summary = "Cerrar sesión de chat",
            description = "Marca una sesión como cerrada. El historial se conserva pero no se pueden enviar más mensajes."
    )
    @ApiResponse(responseCode = "200", description = "Sesión cerrada exitosamente")
    @PatchMapping("/sessions/{sessionId}/close")
    public Mono<ChatSessionResponse> closeSession(
            @Parameter(description = "ID de la sesión", required = true)
            @PathVariable Long sessionId
    ) {
        return chatbotUseCasePort.closeSession(sessionId)
                .map(chatbotDtoMapper::toSessionResponse);
    }

    /**
     * Elimina una sesión y su historial
     * DELETE /api/chatbot/sessions/{sessionId}
     */
    @Operation(
            summary = "Eliminar sesión de chat",
            description = "Elimina permanentemente una sesión y todo su historial de mensajes"
    )
    @ApiResponse(responseCode = "204", description = "Sesión eliminada exitosamente")
    @DeleteMapping("/sessions/{sessionId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public Mono<Void> deleteSession(
            @Parameter(description = "ID de la sesión", required = true)
            @PathVariable Long sessionId
    ) {
        return chatbotUseCasePort.deleteSession(sessionId);
    }
}
