package com.ubik.notification.domain.service;

import com.ubik.notification.domain.model.Notification;
import com.ubik.notification.domain.port.out.NotificationRepositoryPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;

/**
 * Tests unitarios para NotificationService
 */
@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

    @Mock
    private NotificationRepositoryPort repositoryPort;

    private NotificationService notificationService;

    @BeforeEach
    void setUp() {
        notificationService = new NotificationService(repositoryPort);
    }

    @Test
    void createNotification_shouldCreateSuccessfully() {
        // Given
        Notification notification = Notification.createNew(
                "Test Title",
                "Test Message",
                "TEST",
                "user123",
                "USER",
                null
        );

        Notification savedNotification = new Notification(
                1L,
                notification.title(),
                notification.message(),
                notification.type(),
                notification.recipient(),
                notification.recipientType(),
                notification.status(),
                notification.createdAt(),
                null,
                null,
                null
        );

        when(repositoryPort.save(any(Notification.class))).thenReturn(Mono.just(savedNotification));

        // When & Then
        StepVerifier.create(notificationService.createNotification(notification))
                .expectNext(savedNotification)
                .verifyComplete();
    }

    @Test
    void createNotification_withEmptyTitle_shouldFail() {
        // Given
        Notification notification = Notification.createNew(
                "",
                "Test Message",
                "TEST",
                "user123",
                "USER",
                null
        );

        // When & Then
        StepVerifier.create(notificationService.createNotification(notification))
                .expectErrorMatches(throwable ->
                        throwable instanceof IllegalArgumentException &&
                                throwable.getMessage().contains("título"))
                .verify();
    }

    @Test
    void getNotificationById_shouldReturnNotification() {
        // Given
        Long notificationId = 1L;
        Notification notification = new Notification(
                notificationId,
                "Test Title",
                "Test Message",
                "TEST",
                "user123",
                "USER",
                Notification.NotificationStatus.PENDING,
                null,
                null,
                null,
                null
        );

        when(repositoryPort.findById(notificationId)).thenReturn(Mono.just(notification));

        // When & Then
        StepVerifier.create(notificationService.getNotificationById(notificationId))
                .expectNext(notification)
                .verifyComplete();
    }

    @Test
    void getNotificationById_notFound_shouldFail() {
        // Given
        Long notificationId = 999L;
        when(repositoryPort.findById(notificationId)).thenReturn(Mono.empty());

        // When & Then
        StepVerifier.create(notificationService.getNotificationById(notificationId))
                .expectErrorMatches(throwable ->
                        throwable instanceof RuntimeException &&
                                throwable.getMessage().contains("no encontrada"))
                .verify();
    }

    @Test
    void sendNotification_shouldUpdateStatus() {
        // Given
        Long notificationId = 1L;
        Notification pendingNotification = new Notification(
                notificationId,
                "Test Title",
                "Test Message",
                "TEST",
                "user123",
                "USER",
                Notification.NotificationStatus.PENDING,
                null,
                null,
                null,
                null
        );

        Notification sentNotification = pendingNotification.markAsSent();

        when(repositoryPort.findById(notificationId)).thenReturn(Mono.just(pendingNotification));
        when(repositoryPort.update(any(Notification.class))).thenReturn(Mono.just(sentNotification));

        // When & Then
        StepVerifier.create(notificationService.sendNotification(notificationId))
                .expectNextMatches(notification ->
                        notification.status() == Notification.NotificationStatus.SENT &&
                                notification.sentAt() != null)
                .verifyComplete();
    }

    @Test
    void getNotificationsByRecipient_shouldReturnNotifications() {
        // Given
        String recipient = "user123";
        Notification notification1 = new Notification(1L, "Title 1", "Message 1", "TEST", recipient, "USER",
                Notification.NotificationStatus.SENT, null, null, null, null);
        Notification notification2 = new Notification(2L, "Title 2", "Message 2", "TEST", recipient, "USER",
                Notification.NotificationStatus.PENDING, null, null, null, null);

        when(repositoryPort.findByRecipient(recipient)).thenReturn(Flux.just(notification1, notification2));

        // When & Then
        StepVerifier.create(notificationService.getNotificationsByRecipient(recipient))
                .expectNext(notification1)
                .expectNext(notification2)
                .verifyComplete();
    }
}
