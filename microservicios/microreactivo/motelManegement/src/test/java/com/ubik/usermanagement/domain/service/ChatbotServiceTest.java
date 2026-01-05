package com.ubik.usermanagement.domain.service;

import com.ubik.usermanagement.domain.model.ChatMessage;
import com.ubik.usermanagement.domain.model.ChatSession;
import com.ubik.usermanagement.domain.model.Reservation;
import com.ubik.usermanagement.domain.model.Room;
import com.ubik.usermanagement.domain.model.Motel;
import com.ubik.usermanagement.domain.port.out.ChatbotRepositoryPort;
import com.ubik.usermanagement.domain.port.out.ReservationRepositoryPort;
import com.ubik.usermanagement.domain.port.out.RoomRepositoryPort;
import com.ubik.usermanagement.domain.port.out.MotelRepositoryPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

/**
 * Tests unitarios para ChatbotService
 */
@ExtendWith(MockitoExtension.class)
class ChatbotServiceTest {

    @Mock
    private ChatbotRepositoryPort chatbotRepositoryPort;

    @Mock
    private ReservationRepositoryPort reservationRepositoryPort;

    @Mock
    private RoomRepositoryPort roomRepositoryPort;

    @Mock
    private MotelRepositoryPort motelRepositoryPort;

    private ChatbotService chatbotService;

    @BeforeEach
    void setUp() {
        chatbotService = new ChatbotService(
                chatbotRepositoryPort,
                reservationRepositoryPort,
                roomRepositoryPort,
                motelRepositoryPort
        );
    }

    @Test
    void shouldCreateSessionSuccessfully() {
        // Given
        Long userId = 1L;
        String userRole = "USER";
        ChatSession expectedSession = new ChatSession(
                1L, userId, userRole, LocalDateTime.now(), 
                LocalDateTime.now(), ChatSession.SessionStatus.ACTIVE, Map.of()
        );

        when(chatbotRepositoryPort.saveSession(any(ChatSession.class)))
                .thenReturn(Mono.just(expectedSession));

        // When & Then
        StepVerifier.create(chatbotService.createSession(userId, userRole))
                .expectNextMatches(session -> 
                        session.userId().equals(userId) && 
                        session.userRole().equals(userRole) &&
                        session.status() == ChatSession.SessionStatus.ACTIVE
                )
                .verifyComplete();
    }

    @Test
    void shouldProcessGreetingMessage() {
        // Given
        Long sessionId = 1L;
        Long userId = 1L;
        String userRole = "USER";
        String message = "hola";
        
        ChatSession session = new ChatSession(
                sessionId, userId, userRole, LocalDateTime.now(),
                LocalDateTime.now(), ChatSession.SessionStatus.ACTIVE, Map.of()
        );

        ChatMessage savedMessage = new ChatMessage(
                1L, sessionId, message, "¡Hola! Soy el asistente virtual...",
                ChatMessage.MessageType.BOT_RESPONSE, LocalDateTime.now(), userId
        );

        when(chatbotRepositoryPort.saveMessage(any(ChatMessage.class)))
                .thenReturn(Mono.just(savedMessage));
        when(chatbotRepositoryPort.findSessionById(sessionId))
                .thenReturn(Mono.just(session));
        when(chatbotRepositoryPort.updateSession(any(ChatSession.class)))
                .thenReturn(Mono.just(session));

        // When & Then
        StepVerifier.create(chatbotService.processMessage(sessionId, message, userId, userRole))
                .expectNextMatches(chatMessage -> 
                        chatMessage.response() != null &&
                        chatMessage.response().contains("Hola") &&
                        chatMessage.messageType() == ChatMessage.MessageType.BOT_RESPONSE
                )
                .verifyComplete();
    }

    @Test
    void shouldProcessReservationQueryForUser() {
        // Given
        Long sessionId = 1L;
        Long userId = 1L;
        String userRole = "USER";
        String message = "mis reservas";
        
        ChatSession session = new ChatSession(
                sessionId, userId, userRole, LocalDateTime.now(),
                LocalDateTime.now(), ChatSession.SessionStatus.ACTIVE, Map.of()
        );

        Reservation reservation = new Reservation(
                1L, 1L, userId, LocalDateTime.now().plusDays(1),
                LocalDateTime.now().plusDays(2), Reservation.ReservationStatus.CONFIRMED,
                100.0, "Sin observaciones", LocalDateTime.now(), LocalDateTime.now()
        );

        ChatMessage savedMessage = new ChatMessage(
                1L, sessionId, message, "🎯 Tus reservaciones...",
                ChatMessage.MessageType.BOT_RESPONSE, LocalDateTime.now(), userId
        );

        when(chatbotRepositoryPort.saveMessage(any(ChatMessage.class)))
                .thenReturn(Mono.just(savedMessage));
        when(chatbotRepositoryPort.findSessionById(sessionId))
                .thenReturn(Mono.just(session));
        when(chatbotRepositoryPort.updateSession(any(ChatSession.class)))
                .thenReturn(Mono.just(session));
        when(reservationRepositoryPort.findByUserId(userId))
                .thenReturn(Flux.just(reservation));

        // When & Then
        StepVerifier.create(chatbotService.processMessage(sessionId, message, userId, userRole))
                .expectNextMatches(chatMessage -> 
                        chatMessage.response() != null &&
                        chatMessage.response().contains("reservaciones") &&
                        chatMessage.messageType() == ChatMessage.MessageType.BOT_RESPONSE
                )
                .verifyComplete();
    }

    @Test
    void shouldProcessRoomQuerySuccessfully() {
        // Given
        Long sessionId = 1L;
        Long userId = 1L;
        String userRole = "USER";
        String message = "habitaciones disponibles";
        
        ChatSession session = new ChatSession(
                sessionId, userId, userRole, LocalDateTime.now(),
                LocalDateTime.now(), ChatSession.SessionStatus.ACTIVE, Map.of()
        );

        Room room = new Room(1L, 1L, "101", "Suite", 150.0, 
                "Habitación de lujo", true, List.of());

        ChatMessage savedMessage = new ChatMessage(
                1L, sessionId, message, "🏨 Habitaciones disponibles...",
                ChatMessage.MessageType.BOT_RESPONSE, LocalDateTime.now(), userId
        );

        when(chatbotRepositoryPort.saveMessage(any(ChatMessage.class)))
                .thenReturn(Mono.just(savedMessage));
        when(chatbotRepositoryPort.findSessionById(sessionId))
                .thenReturn(Mono.just(session));
        when(chatbotRepositoryPort.updateSession(any(ChatSession.class)))
                .thenReturn(Mono.just(session));
        when(roomRepositoryPort.findAll())
                .thenReturn(Flux.just(room));

        // When & Then
        StepVerifier.create(chatbotService.processMessage(sessionId, message, userId, userRole))
                .expectNextMatches(chatMessage -> 
                        chatMessage.response() != null &&
                        chatMessage.response().contains("disponibles") &&
                        chatMessage.messageType() == ChatMessage.MessageType.BOT_RESPONSE
                )
                .verifyComplete();
    }

    @Test
    void shouldProvideAdminHelpForAdminUser() {
        // Given
        Long sessionId = 1L;
        Long userId = 1L;
        String userRole = "ADMIN";
        String message = "ayuda";
        
        ChatSession session = new ChatSession(
                sessionId, userId, userRole, LocalDateTime.now(),
                LocalDateTime.now(), ChatSession.SessionStatus.ACTIVE, Map.of()
        );

        ChatMessage savedMessage = new ChatMessage(
                1L, sessionId, message, "Puedo ayudarte con lo siguiente:\n\n📋 Comandos disponibles:\n• 'mis reservas' - Ver tus reservaciones\n• 'habitaciones disponibles' - Ver habitaciones disponibles\n• 'información del motel' - Conocer nuestros establecimientos\n\n🔧 Comandos de administrador:\n• 'crear habitación' - Guía para crear una nueva habitación\n• 'panel de administración' - Información sobre gestión del motel\n• 'todas las reservas' - Ver todas las reservaciones del sistema\n\n¿Qué te gustaría hacer?",
                ChatMessage.MessageType.BOT_RESPONSE, LocalDateTime.now(), userId
        );

        when(chatbotRepositoryPort.saveMessage(any(ChatMessage.class)))
                .thenReturn(Mono.just(savedMessage));
        when(chatbotRepositoryPort.findSessionById(sessionId))
                .thenReturn(Mono.just(session));
        when(chatbotRepositoryPort.updateSession(any(ChatSession.class)))
                .thenReturn(Mono.just(session));

        // When & Then
        StepVerifier.create(chatbotService.processMessage(sessionId, message, userId, userRole))
                .expectNextMatches(chatMessage -> 
                        chatMessage.response() != null &&
                        chatMessage.response().contains("Comandos de administrador") &&
                        chatMessage.messageType() == ChatMessage.MessageType.BOT_RESPONSE
                )
                .verifyComplete();
    }

    @Test
    void shouldCloseSessionSuccessfully() {
        // Given
        Long sessionId = 1L;
        ChatSession activeSession = new ChatSession(
                sessionId, 1L, "USER", LocalDateTime.now(),
                LocalDateTime.now(), ChatSession.SessionStatus.ACTIVE, Map.of()
        );

        ChatSession closedSession = activeSession.close();

        when(chatbotRepositoryPort.findSessionById(sessionId))
                .thenReturn(Mono.just(activeSession));
        when(chatbotRepositoryPort.updateSession(any(ChatSession.class)))
                .thenReturn(Mono.just(closedSession));

        // When & Then
        StepVerifier.create(chatbotService.closeSession(sessionId))
                .expectNextMatches(session -> 
                        session.status() == ChatSession.SessionStatus.CLOSED
                )
                .verifyComplete();
    }

    @Test
    void shouldDeleteSessionSuccessfully() {
        // Given
        Long sessionId = 1L;

        when(chatbotRepositoryPort.deleteMessagesBySessionId(sessionId))
                .thenReturn(Mono.empty());
        when(chatbotRepositoryPort.deleteSession(sessionId))
                .thenReturn(Mono.empty());

        // When & Then
        StepVerifier.create(chatbotService.deleteSession(sessionId))
                .verifyComplete();
    }

    @Test
    void shouldGetSessionHistorySuccessfully() {
        // Given
        Long sessionId = 1L;
        ChatMessage message1 = new ChatMessage(
                1L, sessionId, "hola", "¡Hola!",
                ChatMessage.MessageType.BOT_RESPONSE, LocalDateTime.now(), 1L
        );
        ChatMessage message2 = new ChatMessage(
                2L, sessionId, "ayuda", "Puedo ayudarte...",
                ChatMessage.MessageType.BOT_RESPONSE, LocalDateTime.now(), 1L
        );

        when(chatbotRepositoryPort.findMessagesBySessionId(sessionId))
                .thenReturn(Flux.just(message1, message2));

        // When & Then
        StepVerifier.create(chatbotService.getSessionHistory(sessionId))
                .expectNextCount(2)
                .verifyComplete();
    }
}
