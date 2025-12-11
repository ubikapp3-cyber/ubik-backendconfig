package com.example.gateway.domain.port.out;

import java.util.Map;

public interface JwtValidatorPort {

    /**
     * Valida un token JWT y devuelve los claims si es válido.
     * @param token El token JWT a validar
     * @return Map con los claims del token
     * @throws IllegalArgumentException si el token es inválido o expirado
     */
    Map<String, Object> validateToken(String token);
}
