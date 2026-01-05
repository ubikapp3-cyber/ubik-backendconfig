package com.ubik.usermanagement.infrastructure.config;

import com.ubik.usermanagement.domain.factory.UserFactory;
import com.ubik.usermanagement.domain.validator.UserInputValidator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration for domain layer beans
 */
@Configuration
public class DomainConfig {
    
    @Bean
    public UserInputValidator userInputValidator() {
        return new UserInputValidator();
    }
    
    @Bean
    public UserFactory userFactory() {
        return new UserFactory();
    }
}
