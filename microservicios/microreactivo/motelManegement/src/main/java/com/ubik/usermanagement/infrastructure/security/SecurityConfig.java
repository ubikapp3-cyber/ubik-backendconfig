package com.ubik.usermanagement.infrastructure.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsWebFilter;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import java.util.Arrays;
import java.util.List;

/**
 * Configuración de seguridad para la aplicación reactiva
 * Incluye CORS, headers de seguridad y protecciones básicas
 * 
 * IMPORTANTE: Esta configuración es básica. Para producción se requiere:
 * - Spring Security con autenticación JWT
 * - Verificación de ownership de sesiones
 * - CSRF tokens para operaciones de modificación
 * - Rate limiting
 */
@Configuration
public class SecurityConfig {

    /**
     * Configura CORS para permitir requests del frontend
     * NOTA: Ajustar allowedOrigins para producción con el dominio real
     */
    @Bean
    public CorsWebFilter corsWebFilter() {
        CorsConfiguration corsConfig = new CorsConfiguration();
        
        // TODO: Cambiar en producción al dominio real del frontend
        corsConfig.setAllowedOrigins(List.of(
                "http://localhost:3000",  // React dev
                "http://localhost:4200"   // Angular dev
        ));
        
        corsConfig.setAllowedMethods(Arrays.asList(
                HttpMethod.GET.name(),
                HttpMethod.POST.name(),
                HttpMethod.PUT.name(),
                HttpMethod.PATCH.name(),
                HttpMethod.DELETE.name(),
                HttpMethod.OPTIONS.name()
        ));
        
        corsConfig.setAllowedHeaders(List.of("*"));
        corsConfig.setAllowCredentials(true);
        corsConfig.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", corsConfig);

        return new CorsWebFilter(source);
    }

    /**
     * Añade headers de seguridad a todas las respuestas
     * Incluye protección contra XSS, clickjacking, y otros ataques comunes
     */
    @Bean
    public WebFilter securityHeadersFilter() {
        return (ServerWebExchange exchange, WebFilterChain chain) -> {
            exchange.getResponse().getHeaders().add("X-Content-Type-Options", "nosniff");
            exchange.getResponse().getHeaders().add("X-Frame-Options", "DENY");
            exchange.getResponse().getHeaders().add("X-XSS-Protection", "1; mode=block");
            exchange.getResponse().getHeaders().add("Referrer-Policy", "strict-origin-when-cross-origin");
            
            // Content Security Policy para prevenir XSS
            exchange.getResponse().getHeaders().add("Content-Security-Policy", 
                    "default-src 'self'; script-src 'self'; style-src 'self' 'unsafe-inline'; img-src 'self' data:;");
            
            // Strict-Transport-Security solo en producción con HTTPS
            // exchange.getResponse().getHeaders().add("Strict-Transport-Security", "max-age=31536000; includeSubDomains");
            
            return chain.filter(exchange);
        };
    }

    /**
     * TODO: Implementar rate limiting para prevenir abuso
     * Ejemplo: Limitar a 10 mensajes por minuto por usuario
     * 
     * @Bean
     * public WebFilter rateLimitFilter() {
     *     // Implementar rate limiting con Redis o similar
     * }
     */

    /**
     * TODO: Implementar validación de CSRF tokens
     * Para operaciones que modifican estado (POST, PUT, DELETE)
     * 
     * @Bean
     * public WebFilter csrfFilter() {
     *     // Validar CSRF tokens en requests de modificación
     * }
     */
}
