package com.ubik.usermanagement.domain.service;

import com.ubik.usermanagement.domain.exception.MotelNotFoundException;
import com.ubik.usermanagement.domain.model.Motel;
import com.ubik.usermanagement.domain.port.in.MotelUseCasePort;
import com.ubik.usermanagement.domain.port.out.MotelRepositoryPort;
import com.ubik.usermanagement.domain.validator.MotelValidator;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Servicio de dominio que implementa los casos de uso de Motel
 * Refactored to follow SOLID principles with extracted validation
 */
@Service
public class MotelService implements MotelUseCasePort {

    private final MotelRepositoryPort motelRepositoryPort;
    private final MotelValidator motelValidator;

    public MotelService(MotelRepositoryPort motelRepositoryPort, MotelValidator motelValidator) {
        this.motelRepositoryPort = motelRepositoryPort;
        this.motelValidator = motelValidator;
    }

    @Override
    public Mono<Motel> createMotel(Motel motel) {
        return motelValidator.validate(motel)
                .then(motelRepositoryPort.save(motel));
    }

    @Override
    public Mono<Motel> getMotelById(Long id) {
        return motelRepositoryPort.findById(id)
                .switchIfEmpty(Mono.error(new MotelNotFoundException(id)));
    }

    @Override
    public Flux<Motel> getAllMotels() {
        return motelRepositoryPort.findAll();
    }

    @Override
    public Flux<Motel> getMotelsByCity(String city) {
        return motelValidator.validateCity(city)
                .thenMany(motelRepositoryPort.findByCity(city));
    }

    @Override
    public Mono<Motel> updateMotel(Long id, Motel motel) {
        return findMotelById(id)
                .flatMap(existingMotel -> validateAndUpdateMotel(id, motel, existingMotel));
    }

    @Override
    public Mono<Void> deleteMotel(Long id) {
        return motelRepositoryPort.existsById(id)
                .flatMap(exists -> {
                    if (!exists) {
                        return Mono.error(new MotelNotFoundException(id));
                    }
                    return motelRepositoryPort.deleteById(id);
                });
    }

    /**
     * Finds motel by ID or throws exception
     */
    private Mono<Motel> findMotelById(Long id) {
        return motelRepositoryPort.findById(id)
                .switchIfEmpty(Mono.error(new MotelNotFoundException(id)));
    }

    /**
     * Validates and updates motel with preserved fields from existing motel
     */
    private Mono<Motel> validateAndUpdateMotel(Long id, Motel motel, Motel existingMotel) {
        Motel updatedMotel = new Motel(
                id,
                motel.name(),
                motel.address(),
                motel.phoneNumber(),
                motel.description(),
                motel.city(),
                existingMotel.propertyId(),
                existingMotel.dateCreated(),
                motel.imageUrls()
        );
        
        return motelValidator.validate(updatedMotel)
                .then(motelRepositoryPort.update(updatedMotel));
    }
}