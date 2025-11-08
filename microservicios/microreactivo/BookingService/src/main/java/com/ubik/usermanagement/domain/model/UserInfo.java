package com.ubik.reservation.domain.model;

/**
 * DTO con información del usuario del microservicio User Management
 */
public record UserInfo(
        Long id,
        String username,
        String email,
        String phoneNumber,
        String role,
        boolean anonymous
) {}
