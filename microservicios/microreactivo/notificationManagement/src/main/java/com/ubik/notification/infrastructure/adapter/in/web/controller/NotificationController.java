package com.ubik.notification.infrastructure.adapter.in.web.controller;

import com.ubik.notification.domain.model.Notification;
import com.ubik.notification.domain.port.in.NotificationUseCasePort;
import com.ubik.notification.infrastructure.adapter.in.web.dto.CreateNotificationRequest;
import com.ubik.notification.infrastructure.adapter.in.web.dto.NotificationResponse;
import com.ubik.notification.infrastructure.adapter.in.web.dto.UpdateNotificationRequest;
import com.ubik.notification.infrastructure.adapter.in.web.mapper.NotificationDtoMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Controlador REST para operaciones de notificaciones
 * Adaptador de entrada en la arquitectura hexagonal
 */
@RestController
@RequestMapping("/api/notifications")
@Tag(name = "Notifications", description = "API para gestión de notificaciones del sistema")
public class NotificationController {

    private final NotificationUseCasePort notificationUseCasePort;
    private final NotificationDtoMapper mapper;

    public NotificationController(
            NotificationUseCasePort notificationUseCasePort,
            NotificationDtoMapper mapper) {
        this.notificationUseCasePort = notificationUseCasePort;
        this.mapper = mapper;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Crear una nueva notificación", description = "Crea una nueva notificación en el sistema")
    public Mono<NotificationResponse> createNotification(
            @Valid @RequestBody CreateNotificationRequest request) {
        Notification notification = mapper.toDomain(request);
        return notificationUseCasePort.createNotification(notification)
                .map(mapper::toResponse);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener notificación por ID", description = "Obtiene los detalles de una notificación específica")
    public Mono<NotificationResponse> getNotificationById(
            @Parameter(description = "ID de la notificación") @PathVariable Long id) {
        return notificationUseCasePort.getNotificationById(id)
                .map(mapper::toResponse);
    }

    @GetMapping
    @Operation(summary = "Obtener todas las notificaciones", description = "Lista todas las notificaciones del sistema")
    public Flux<NotificationResponse> getAllNotifications(
            @RequestParam(defaultValue = "100") @Parameter(description = "Límite de resultados") int limit) {
        if (limit <= 0 || limit > 1000) {
            limit = 100;
        }
        return notificationUseCasePort.getAllNotifications()
                .take(limit)
                .map(mapper::toResponse);
    }

    @GetMapping("/recipient/{recipient}")
    @Operation(summary = "Obtener notificaciones por destinatario", 
               description = "Lista todas las notificaciones de un destinatario específico")
    public Flux<NotificationResponse> getNotificationsByRecipient(
            @Parameter(description = "Identificador del destinatario") @PathVariable String recipient) {
        return notificationUseCasePort.getNotificationsByRecipient(recipient)
                .map(mapper::toResponse);
    }

    @GetMapping("/type/{type}")
    @Operation(summary = "Obtener notificaciones por tipo", 
               description = "Lista todas las notificaciones de un tipo específico")
    public Flux<NotificationResponse> getNotificationsByType(
            @Parameter(description = "Tipo de notificación") @PathVariable String type) {
        return notificationUseCasePort.getNotificationsByType(type)
                .map(mapper::toResponse);
    }

    @GetMapping("/status/{status}")
    @Operation(summary = "Obtener notificaciones por estado", 
               description = "Lista todas las notificaciones en un estado específico")
    public Flux<NotificationResponse> getNotificationsByStatus(
            @Parameter(description = "Estado de la notificación") @PathVariable String status) {
        try {
            Notification.NotificationStatus notificationStatus = Notification.NotificationStatus.valueOf(status.toUpperCase());
            return notificationUseCasePort.getNotificationsByStatus(notificationStatus)
                    .map(mapper::toResponse);
        } catch (IllegalArgumentException e) {
            return Flux.error(new IllegalArgumentException("Invalid status: " + status + ". Valid values are: PENDING, SENT, READ, CANCELLED"));
        }
    }

    @PostMapping("/{id}/send")
    @Operation(summary = "Enviar notificación", 
               description = "Marca una notificación como enviada")
    public Mono<NotificationResponse> sendNotification(
            @Parameter(description = "ID de la notificación") @PathVariable Long id) {
        return notificationUseCasePort.sendNotification(id)
                .map(mapper::toResponse);
    }

    @PostMapping("/{id}/read")
    @Operation(summary = "Marcar notificación como leída", 
               description = "Marca una notificación como leída por el destinatario")
    public Mono<NotificationResponse> markNotificationAsRead(
            @Parameter(description = "ID de la notificación") @PathVariable Long id) {
        return notificationUseCasePort.markNotificationAsRead(id)
                .map(mapper::toResponse);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar notificación", 
               description = "Actualiza los datos de una notificación existente (solo si está en estado PENDING)")
    public Mono<NotificationResponse> updateNotification(
            @Parameter(description = "ID de la notificación") @PathVariable Long id,
            @Valid @RequestBody UpdateNotificationRequest request) {
        // Primero obtenemos la notificación existente, luego la actualizamos
        return notificationUseCasePort.getNotificationById(id)
                .map(existing -> mapper.toDomainForUpdate(request, existing))
                .flatMap(updated -> notificationUseCasePort.updateNotification(id, updated))
                .map(mapper::toResponse);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Eliminar notificación", 
               description = "Elimina una notificación del sistema")
    public Mono<Void> deleteNotification(
            @Parameter(description = "ID de la notificación") @PathVariable Long id) {
        return notificationUseCasePort.deleteNotification(id);
    }

    @GetMapping("/recipient/{recipient}/unread-count")
    @Operation(summary = "Contar notificaciones no leídas", 
               description = "Obtiene el conteo de notificaciones no leídas de un destinatario")
    public Mono<Long> countUnreadNotifications(
            @Parameter(description = "Identificador del destinatario") @PathVariable String recipient) {
        return notificationUseCasePort.countUnreadNotificationsByRecipient(recipient);
    }
}
