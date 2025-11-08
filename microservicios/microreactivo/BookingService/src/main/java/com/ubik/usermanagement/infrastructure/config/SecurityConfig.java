package com.ubik.reservation.infrastructure.config;

import com.ubik.reservation.infrastructure.adapter.in.web.security.JwtAuthenticationFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.SecurityWebFiltersOrder;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;

@Configuration
@EnableWebFluxSecurity
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }

    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
        return http
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .authorizeExchange(exchanges -> exchanges
                        // Endpoints públicos para verificar disponibilidad
                        .pathMatchers("/api/reservations/availability/**").permitAll()
                        .pathMatchers("/actuator/**").permitAll()
                        
                        // Admin puede ver todas las reservas
                        .pathMatchers("/api/reservations/admin/**").hasRole("ADMIN")
                        
                        // Usuarios autenticados pueden hacer reservas
                        .pathMatchers("/api/reservations/**").hasAnyRole("USER", "CLIENT", "ADMIN")
                        
                        .anyExchange().authenticated()
                )
                .addFilterAt(
                    jwtAuthenticationFilter.authenticationFilter(),
                    SecurityWebFiltersOrder.AUTHENTICATION
                )
                .build();
    }
}