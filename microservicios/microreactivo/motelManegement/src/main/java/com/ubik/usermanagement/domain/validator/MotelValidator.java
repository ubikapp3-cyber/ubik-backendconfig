package com.ubik.usermanagement.domain.validator;

import com.ubik.usermanagement.domain.model.Motel;
import reactor.core.publisher.Mono;

/**
 * Validator for Motel domain objects
 * Follows Single Responsibility Principle - only validates motel data
 */
public class MotelValidator {
    
    private static final int MAX_IMAGES = 10;
    
    /**
     * Validates all required fields for a motel
     */
    public Mono<Void> validate(Motel motel) {
        return validateName(motel.name())
                .then(validateAddress(motel.address()))
                .then(validateCity(motel.city()))
                .then(validateImageUrls(motel.imageUrls()));
    }
    
    /**
     * Validates that motel name is not null or empty
     */
    public Mono<Void> validateName(String name) {
        if (name == null || name.trim().isEmpty()) {
            return Mono.error(new IllegalArgumentException("El nombre del motel es requerido"));
        }
        return Mono.empty();
    }
    
    /**
     * Validates that address is not null or empty
     */
    public Mono<Void> validateAddress(String address) {
        if (address == null || address.trim().isEmpty()) {
            return Mono.error(new IllegalArgumentException("La dirección del motel es requerida"));
        }
        return Mono.empty();
    }
    
    /**
     * Validates that city is not null or empty
     */
    public Mono<Void> validateCity(String city) {
        if (city == null || city.trim().isEmpty()) {
            return Mono.error(new IllegalArgumentException("La ciudad del motel es requerida"));
        }
        return Mono.empty();
    }
    
    /**
     * Validates that image URLs list doesn't exceed maximum
     */
    public Mono<Void> validateImageUrls(java.util.List<String> imageUrls) {
        if (imageUrls != null && imageUrls.size() > MAX_IMAGES) {
            return Mono.error(new IllegalArgumentException(
                String.format("No se pueden agregar más de %d imágenes", MAX_IMAGES)));
        }
        return Mono.empty();
    }
}
