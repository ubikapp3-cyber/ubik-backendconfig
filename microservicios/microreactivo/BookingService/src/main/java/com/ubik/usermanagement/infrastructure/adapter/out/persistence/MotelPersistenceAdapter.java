package com.ubik.usermanagement.infrastructure.adapter.out.persistence;

import com.ubik.usermanagement.domain.model.Motel;
import com.ubik.usermanagement.domain.port.out.MotelRepositoryPort;
import com.ubik.usermanagement.infrastructure.adapter.out.persistence.mapper.MotelMapper;
import com.ubik.usermanagement.infrastructure.adapter.out.persistence.repository.MotelR2dbcRepository;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Adaptador de persistencia para Motel
 * Implementa el puerto de salida utilizando R2DBC
 * Parte de la arquitectura hexagonal - Adaptador secundario
 */
@Component
public class MotelPersistenceAdapter implements MotelRepositoryPort {

    private final MotelR2dbcRepository motelR2dbcRepository;
    private final MotelMapper motelMapper;

    public MotelPersistenceAdapter(MotelR2dbcRepository motelR2dbcRepository, MotelMapper motelMapper) {
        this.motelR2dbcRepository = motelR2dbcRepository;
        this.motelMapper = motelMapper;
    }

    @Override
    public Mono<Motel> save(Motel motel) {
        return Mono.just(motel)
                .map(motelMapper::toEntity)
                .flatMap(motelR2dbcRepository::save)
                .map(motelMapper::toDomain);
    }

    @Override
    public Mono<Motel> findById(Long id) {
        return motelR2dbcRepository.findById(id)
                .map(motelMapper::toDomain);
    }

    @Override
    public Flux<Motel> findAll() {
        return motelR2dbcRepository.findAll()
                .map(motelMapper::toDomain);
    }

    @Override
    public Flux<Motel> findByCity(String city) {
        return motelR2dbcRepository.findByCity(city)
                .map(motelMapper::toDomain);
    }

    @Override
    public Mono<Motel> update(Motel motel) {
        return Mono.just(motel)
                .map(motelMapper::toEntity)
                .flatMap(motelR2dbcRepository::save)
                .map(motelMapper::toDomain);
    }

    @Override
    public Mono<Void> deleteById(Long id) {
        return motelR2dbcRepository.deleteById(id);
    }

    @Override
    public Mono<Boolean> existsById(Long id) {
        return motelR2dbcRepository.existsById(id);
    }
}