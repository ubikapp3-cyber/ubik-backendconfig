package com.ubik.usermanagement.infrastructure.adapter.out.persistence.mapper;

import com.ubik.usermanagement.domain.model.Reservation;
import com.ubik.usermanagement.infrastructure.adapter.out.persistence.entity.ReservationEntity;
import org.springframework.stereotype.Component;

/**
 * Mapper para convertir entre el modelo de dominio Reservation y la entidad de persistencia ReservationEntity
 */
@Component
public class ReservationMapper {

    /**
     * Convierte de entidad de persistencia a modelo de dominio
     */
    public Reservation toDomain(ReservationEntity entity) {
        if (entity == null) {
            return null;
        }
        return new Reservation(
                entity.id(),
                entity.roomId(),
                entity.userId(),
                entity.checkInDate(),
                entity.checkOutDate(),
                Reservation.ReservationStatus.valueOf(entity.status()),
                entity.totalPrice(),
                entity.specialRequests(),
                entity.createdAt(),
                entity.updatedAt()
        );
    }

    /**
     * Convierte de modelo de dominio a entidad de persistencia
     */
    public ReservationEntity toEntity(Reservation reservation) {
        if (reservation == null) {
            return null;
        }
        return new ReservationEntity(
                reservation.id(),
                reservation.roomId(),
                reservation.userId(),
                reservation.checkInDate(),
                reservation.checkOutDate(),
                reservation.status() != null ? reservation.status().name() : null,
                reservation.totalPrice(),
                reservation.specialRequests(),
                reservation.createdAt(),
                reservation.updatedAt()
        );
    }
}
