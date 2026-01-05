package com.ubik.usermanagement.infrastructure.config;

import com.ubik.usermanagement.domain.validator.MotelValidator;
import com.ubik.usermanagement.domain.validator.ReservationValidator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration for domain layer beans in Motel Management service
 */
@Configuration
public class DomainConfig {
    
    @Bean
    public MotelValidator motelValidator() {
        return new MotelValidator();
    }
    
    @Bean
    public ReservationValidator reservationValidator() {
        return new ReservationValidator();
    }
}
