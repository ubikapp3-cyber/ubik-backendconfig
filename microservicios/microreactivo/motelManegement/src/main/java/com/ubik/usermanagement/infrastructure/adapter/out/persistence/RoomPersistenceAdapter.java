package com.ubik.usermanagement.infrastructure.adapter.out.persistence;

import com.ubik.usermanagement.domain.model.Room;
import com.ubik.usermanagement.domain.port.out.RoomRepositoryPort;
import com.ubik.usermanagement.infrastructure.adapter.out.persistence.entity.RoomImageEntity;
import com.ubik.usermanagement.infrastructure.adapter.out.persistence.mapper.RoomMapper;
import com.ubik.usermanagement.infrastructure.adapter.out.persistence.repository.RoomImageR2dbcRepository;
import com.ubik.usermanagement.infrastructure.adapter.out.persistence.repository.RoomR2dbcRepository;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;

/**
 * Adaptador de persistencia para Room con soporte de imágenes
 */
@Component
public class RoomPersistenceAdapter implements RoomRepositoryPort {

    private final RoomR2dbcRepository roomR2dbcRepository;
    private final RoomImageR2dbcRepository roomImageRepository;
    private final RoomMapper roomMapper;

    public RoomPersistenceAdapter(
            RoomR2dbcRepository roomR2dbcRepository,
            RoomImageR2dbcRepository roomImageRepository,
            RoomMapper roomMapper) {
        this.roomR2dbcRepository = roomR2dbcRepository;
        this.roomImageRepository = roomImageRepository;
        this.roomMapper = roomMapper;
    }

    @Override
    public Mono<Room> save(Room room) {
        return Mono.just(room)
                .map(roomMapper::toEntity)
                .flatMap(roomR2dbcRepository::save)
                .flatMap(savedEntity -> saveImages(savedEntity.id(), room.imageUrls())
                        .then(Mono.just(savedEntity)))
                .flatMap(this::loadRoomWithImages);
    }

    @Override
    public Mono<Room> findById(Long id) {
        return roomR2dbcRepository.findById(id)
                .flatMap(this::loadRoomWithImages);
    }

    @Override
    public Flux<Room> findAll() {
        return roomR2dbcRepository.findAll()
                .flatMap(this::loadRoomWithImages);
    }

    @Override
    public Flux<Room> findByMotelId(Long motelId) {
        return roomR2dbcRepository.findByMotelId(motelId)
                .flatMap(this::loadRoomWithImages);
    }

    @Override
    public Flux<Room> findAvailableByMotelId(Long motelId) {
        return roomR2dbcRepository.findByMotelIdAndIsAvailable(motelId, true)
                .flatMap(this::loadRoomWithImages);
    }

    @Override
    public Mono<Room> update(Room room) {
        return Mono.just(room)
                .map(roomMapper::toEntity)
                .flatMap(roomR2dbcRepository::save)
                .flatMap(savedEntity ->
                        roomImageRepository.deleteByRoomId(savedEntity.id())
                                .then(saveImages(savedEntity.id(), room.imageUrls()))
                                .then(Mono.just(savedEntity))
                )
                .flatMap(this::loadRoomWithImages);
    }

    @Override
    public Mono<Void> deleteById(Long id) {
        return roomImageRepository.deleteByRoomId(id)
                .then(roomR2dbcRepository.deleteById(id));
    }

    @Override
    public Mono<Boolean> existsById(Long id) {
        return roomR2dbcRepository.existsById(id);
    }

    /**
     * Carga una habitación con sus imágenes
     */
    private Mono<Room> loadRoomWithImages(com.ubik.usermanagement.infrastructure.adapter.out.persistence.entity.RoomEntity entity) {
        return roomImageRepository.findByRoomIdOrderByDisplayOrder(entity.id())
                .map(RoomImageEntity::imageUrl)
                .collectList()
                .map(imageUrls -> roomMapper.toDomain(entity, imageUrls));
    }

    /**
     * Guarda las imágenes de una habitación
     */
    private Mono<Void> saveImages(Long roomId, java.util.List<String> imageUrls) {
        if (imageUrls == null || imageUrls.isEmpty()) {
            return Mono.empty();
        }

        LocalDateTime now = LocalDateTime.now();
        return Flux.fromIterable(imageUrls)
                .index()
                .map(tuple -> new RoomImageEntity(
                        null,
                        roomId,
                        tuple.getT2(),
                        (int) (tuple.getT1() + 1),
                        now
                ))
                .flatMap(roomImageRepository::save)
                .then();
    }
}