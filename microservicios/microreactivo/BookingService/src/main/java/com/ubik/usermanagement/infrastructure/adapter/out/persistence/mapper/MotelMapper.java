package com.ubik.usermanagement.infrastructure.adapter.out.persistence.mapper;

import com.ubik.usermanagement.domain.model.Motel;
import com.ubik.usermanagement.infrastructure.adapter.out.persistence.entity.MotelEntity;
import org.springframework.stereotype.Component;

/**
 * Mapper para convertir entre el modelo de dominio Motel y la entidad de persistencia MotelEntity
 */
@Component
public class MotelMapper {

    /**
     * Convierte de entidad de persistencia a modelo de dominio
     */
    public Motel toDomain(MotelEntity entity) {
        if (entity == null) {
            return null;
        }
        return new Motel(
                entity.id(),
                entity.name(),
                entity.address(),
                entity.phoneNumber(),
                entity.description(),
                entity.city(),
                entity.propertyId(),
                entity.dateCreated()
        );
    }

    /**
     * Convierte de modelo de dominio a entidad de persistencia
     */
    public MotelEntity toEntity(Motel motel) {
        if (motel == null) {
            return null;
        }
        return new MotelEntity(
                motel.id(),
                motel.name(),
                motel.address(),
                motel.phoneNumber(),
                motel.description(),
                motel.city(),
                motel.propertyId(),
                motel.dateCreated()
        );
    }
}