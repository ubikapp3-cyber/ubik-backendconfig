package com.ubik.usermanagement.infrastructure.adapter.out.persistence.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;

/**
 * Entidad de persistencia para RoomImage
 * Representa la tabla 'room_image' en la base de datos
 */
@Table("room_image")
public record RoomImageEntity(
        @Id Long id,
        @Column("room_id") Long roomId,
        @Column("image_url") String imageUrl,
        @Column("display_order") Integer displayOrder,
        @Column("created_at") LocalDateTime createdAt
) {
}
