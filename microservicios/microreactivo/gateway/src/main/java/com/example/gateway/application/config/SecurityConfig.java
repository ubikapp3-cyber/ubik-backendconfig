package com.example.gateway.application.config;

import com.example.gateway.application.filter.JwtAuthenticationFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.SecurityWebFiltersOrder;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;

@Configuration
@EnableWebFluxSecurity
public class SecurityConfig {

    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http,
                                                         JwtAuthenticationFilter jwtFilter) {

        return http
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .authorizeExchange(exchanges -> exchanges
                        // ---- ENDPOINTS PUBLICOS ----
                        .pathMatchers("/api/auth/login").permitAll()
                        .pathMatchers("/api/auth/register").permitAll()
                        .pathMatchers("/api/auth/reset-password-request").permitAll()
                        .pathMatchers("/api/auth/reset-password").permitAll()

                        // ---- SWAGGER/OPENAPI ----
                        .pathMatchers("/swagger-ui.html", "/swagger-ui/**", "/v3/api-docs/**", "/webjars/**").permitAll()

                        // ---- ENDPOINTS PROTEGIDOS ----
                        .pathMatchers(HttpMethod.GET, "/api/user").authenticated()
                        .pathMatchers(HttpMethod.PUT, "/api/user").authenticated()

                        // (Opcional) Endpoints admin:
                        //.pathMatchers("/api/admin/**").hasRole("ADMIN")

                        // Todo lo demás requiere autenticación
                        .anyExchange().authenticated()
                )
                .addFilterAt(jwtFilter, SecurityWebFiltersOrder.AUTHENTICATION)
                .build();
    }
}

