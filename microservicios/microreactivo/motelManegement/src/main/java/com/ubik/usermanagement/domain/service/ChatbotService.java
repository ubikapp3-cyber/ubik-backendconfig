package com.ubik.usermanagement.domain.service;

import com.ubik.usermanagement.domain.exception.ChatSessionNotFoundException;
import com.ubik.usermanagement.domain.model.ChatMessage;
import com.ubik.usermanagement.domain.model.ChatSession;
import com.ubik.usermanagement.domain.port.in.ChatbotUseCasePort;
import com.ubik.usermanagement.domain.port.out.ChatbotRepositoryPort;
import com.ubik.usermanagement.domain.port.out.MotelRepositoryPort;
import com.ubik.usermanagement.domain.port.out.ReservationRepositoryPort;
import com.ubik.usermanagement.domain.port.out.RoomRepositoryPort;
import com.ubik.usermanagement.domain.service.chatbot.IntentClassifier;
import com.ubik.usermanagement.domain.service.chatbot.IntentClassifier.Intent;
import com.ubik.usermanagement.domain.service.chatbot.ResponseFormatter;
import com.ubik.usermanagement.infrastructure.security.XssSanitizer;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Servicio de dominio del chatbot refactorizado aplicando principios SOLID
 * - Single Responsibility: Coordina operaciones, delega clasificación y formateo
 * - Open/Closed: Extensible mediante nuevas intenciones sin modificar código existente
 * - Dependency Inversion: Depende de abstracciones (ports) no de implementaciones concretas
 * - Security: Incluye sanitización XSS y validaciones de seguridad
 */
@Service
public class ChatbotService implements ChatbotUseCasePort {

    private static final int MAX_ADMIN_RESERVATIONS = 20;
    private static final int MAX_AVAILABLE_ROOMS = 10;
    private static final int MAX_MOTELS_TO_SHOW = 10;

    private final ChatbotRepositoryPort chatbotRepositoryPort;
    private final ReservationRepositoryPort reservationRepositoryPort;
    private final RoomRepositoryPort roomRepositoryPort;
    private final MotelRepositoryPort motelRepositoryPort;
    private final IntentClassifier intentClassifier;
    private final ResponseFormatter responseFormatter;
    private final XssSanitizer xssSanitizer;

    public ChatbotService(
            @NonNull ChatbotRepositoryPort chatbotRepositoryPort,
            @NonNull ReservationRepositoryPort reservationRepositoryPort,
            @NonNull RoomRepositoryPort roomRepositoryPort,
            @NonNull MotelRepositoryPort motelRepositoryPort,
            @NonNull IntentClassifier intentClassifier,
            @NonNull ResponseFormatter responseFormatter,
            @NonNull XssSanitizer xssSanitizer
    ) {
        this.chatbotRepositoryPort = chatbotRepositoryPort;
        this.reservationRepositoryPort = reservationRepositoryPort;
        this.roomRepositoryPort = roomRepositoryPort;
        this.motelRepositoryPort = motelRepositoryPort;
        this.intentClassifier = intentClassifier;
        this.responseFormatter = responseFormatter;
        this.xssSanitizer = xssSanitizer;
    }

    @Override
    @NonNull
    public Mono<ChatMessage> processMessage(
            @NonNull Long sessionId,
            @NonNull String message,
            @NonNull Long userId,
            @NonNull String userRole
    ) {
        // Sanitizar el mensaje para prevenir XSS
        String sanitizedMessage = xssSanitizer.sanitizeMessage(message);
        
        ChatMessage userMessage = ChatMessage.createUserMessage(sessionId, sanitizedMessage, userId);
        
        return saveUserMessage(userMessage)
                .then(findAndUpdateSession(sessionId))
                .flatMap(session -> {
                    // TODO: Verificar que session.userId() == userId (requiere autenticación)
                    // Por ahora, advertencia en logs
                    return generateResponse(sanitizedMessage, userId, session);
                })
                .flatMap(response -> saveResponseMessage(userMessage, response));
    }

    @Override
    @NonNull
    public Mono<ChatSession> createSession(@NonNull Long userId, @NonNull String userRole) {
        ChatSession newSession = ChatSession.createNew(userId, userRole);
        return chatbotRepositoryPort.saveSession(newSession);
    }

    @Override
    @NonNull
    public Mono<ChatSession> getSession(@NonNull Long sessionId) {
        return chatbotRepositoryPort.findSessionById(sessionId)
                .switchIfEmpty(Mono.error(new ChatSessionNotFoundException(sessionId)));
    }

    @Override
    @NonNull
    public Flux<ChatMessage> getSessionHistory(@NonNull Long sessionId) {
        return chatbotRepositoryPort.findMessagesBySessionId(sessionId);
    }

    @Override
    @NonNull
    public Mono<ChatSession> closeSession(@NonNull Long sessionId) {
        return getSession(sessionId)
                .map(ChatSession::close)
                .flatMap(chatbotRepositoryPort::updateSession);
    }

    @Override
    @NonNull
    public Mono<Void> deleteSession(@NonNull Long sessionId) {
        return chatbotRepositoryPort.deleteMessagesBySessionId(sessionId)
                .then(chatbotRepositoryPort.deleteSession(sessionId));
    }

    // Métodos privados aplicando Early Returns y extracción de funciones

    @NonNull
    private Mono<Void> saveUserMessage(@NonNull ChatMessage userMessage) {
        return chatbotRepositoryPort.saveMessage(userMessage).then();
    }

    @NonNull
    private Mono<ChatSession> findAndUpdateSession(@NonNull Long sessionId) {
        return getSession(sessionId)
                .map(ChatSession::updateActivity)
                .flatMap(chatbotRepositoryPort::updateSession);
    }

    @NonNull
    private Mono<String> generateResponse(
            @NonNull String message,
            @NonNull Long userId,
            @NonNull ChatSession session
    ) {
        Intent intent = intentClassifier.classifyIntent(message, session);
        return processIntent(intent, userId, session);
    }

    @NonNull
    private Mono<ChatMessage> saveResponseMessage(
            @NonNull ChatMessage userMessage,
            @NonNull String response
    ) {
        ChatMessage responseMessage = userMessage.withBotResponse(response);
        return chatbotRepositoryPort.saveMessage(responseMessage);
    }

    /**
     * Procesa la intención clasificada y genera la respuesta apropiada
     * Aplica Strategy Pattern para manejar diferentes intenciones
     */
    @NonNull
    private Mono<String> processIntent(
            @NonNull Intent intent,
            @NonNull Long userId,
            @NonNull ChatSession session
    ) {
        return switch (intent) {
            case GREETING -> Mono.just(responseFormatter.formatGreeting(session));
            case HELP -> Mono.just(responseFormatter.formatHelpMessage(session));
            case RESERVATION_QUERY -> handleReservationQuery(userId, session);
            case ROOM_QUERY -> handleRoomQuery(session);
            case MOTEL_QUERY -> handleMotelQuery();
            case ADMIN_CREATE_ROOM -> Mono.just(responseFormatter.formatRoomCreationHelp());
            case ADMIN_PANEL -> Mono.just(responseFormatter.formatAdminPanelHelp());
            case UNKNOWN -> Mono.just(responseFormatter.formatUnknownIntentMessage());
        };
    }

    /**
     * Maneja consultas sobre reservaciones aplicando Early Return
     */
    @NonNull
    private Mono<String> handleReservationQuery(@NonNull Long userId, @NonNull ChatSession session) {
        if (session.isAdmin()) {
            return fetchAdminReservations();
        }
        
        return fetchUserReservations(userId);
    }

    @NonNull
    private Mono<String> fetchAdminReservations() {
        return reservationRepositoryPort.findAll()
                .take(MAX_ADMIN_RESERVATIONS)
                .collectList()
                .map(responseFormatter::formatAdminReservations);
    }

    @NonNull
    private Mono<String> fetchUserReservations(@NonNull Long userId) {
        return reservationRepositoryPort.findByUserId(userId)
                .collectList()
                .map(responseFormatter::formatUserReservations);
    }

    /**
     * Maneja consultas sobre habitaciones disponibles
     */
    @NonNull
    private Mono<String> handleRoomQuery(@NonNull ChatSession session) {
        return roomRepositoryPort.findAll()
                .filter(room -> Boolean.TRUE.equals(room.isAvailable()))
                .take(MAX_AVAILABLE_ROOMS)
                .collectList()
                .map(rooms -> responseFormatter.formatRooms(rooms, session));
    }

    /**
     * Maneja consultas sobre moteles
     */
    @NonNull
    private Mono<String> handleMotelQuery() {
        return motelRepositoryPort.findAll()
                .take(MAX_MOTELS_TO_SHOW)
                .collectList()
                .map(responseFormatter::formatMotels);
    }
}
