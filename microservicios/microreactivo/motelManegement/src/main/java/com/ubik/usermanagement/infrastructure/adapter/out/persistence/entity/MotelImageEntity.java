package com.ubik.usermanagement.infrastructure.adapter.out.persistence.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;

/**
 * Entidad de persistencia para MotelImage
 * Representa la tabla 'motel_image' en la base de datos
 */
@Table("motel_image")
public record MotelImageEntity(
        @Id Long id,
        @Column("motel_id") Long motelId,
        @Column("image_url") String imageUrl,
        @Column("display_order") Integer displayOrder,
        @Column("created_at") LocalDateTime createdAt
) {
}
