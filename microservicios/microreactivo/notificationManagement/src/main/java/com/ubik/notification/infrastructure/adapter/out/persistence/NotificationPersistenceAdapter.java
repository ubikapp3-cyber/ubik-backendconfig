package com.ubik.notification.infrastructure.adapter.out.persistence;

import com.ubik.notification.domain.model.Notification;
import com.ubik.notification.domain.port.out.NotificationRepositoryPort;
import com.ubik.notification.infrastructure.adapter.out.persistence.entity.NotificationEntity;
import com.ubik.notification.infrastructure.adapter.out.persistence.mapper.NotificationEntityMapper;
import com.ubik.notification.infrastructure.adapter.out.persistence.repository.NotificationR2dbcRepository;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Adaptador de persistencia que implementa el puerto de salida
 * Conecta el dominio con la infraestructura de persistencia R2DBC
 */
@Component
public class NotificationPersistenceAdapter implements NotificationRepositoryPort {

    private final NotificationR2dbcRepository repository;
    private final NotificationEntityMapper mapper;

    public NotificationPersistenceAdapter(
            NotificationR2dbcRepository repository,
            NotificationEntityMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Override
    public Mono<Notification> save(Notification notification) {
        NotificationEntity entity = mapper.toEntity(notification);
        return repository.save(entity)
                .map(mapper::toDomain);
    }

    @Override
    public Mono<Notification> findById(Long id) {
        return repository.findById(id)
                .map(mapper::toDomain);
    }

    @Override
    public Flux<Notification> findAll() {
        return repository.findAll()
                .map(mapper::toDomain);
    }

    @Override
    public Flux<Notification> findByRecipient(String recipient) {
        return repository.findByRecipient(recipient)
                .map(mapper::toDomain);
    }

    @Override
    public Flux<Notification> findByType(String type) {
        return repository.findByType(type)
                .map(mapper::toDomain);
    }

    @Override
    public Flux<Notification> findByStatus(Notification.NotificationStatus status) {
        return repository.findByStatus(status.name())
                .map(mapper::toDomain);
    }

    @Override
    public Flux<Notification> findByRecipientAndStatus(String recipient, Notification.NotificationStatus status) {
        return repository.findByRecipientAndStatus(recipient, status.name())
                .map(mapper::toDomain);
    }

    @Override
    public Mono<Notification> update(Notification notification) {
        NotificationEntity entity = mapper.toEntity(notification);
        return repository.save(entity)
                .map(mapper::toDomain);
    }

    @Override
    public Mono<Void> deleteById(Long id) {
        return repository.deleteById(id);
    }

    @Override
    public Mono<Boolean> existsById(Long id) {
        return repository.existsById(id);
    }

    @Override
    public Mono<Long> countUnreadByRecipient(String recipient) {
        return repository.countUnreadByRecipient(recipient);
    }
}
