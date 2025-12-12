package com.example.gateway.application.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * Configuración de OpenAPI/Swagger para el API Gateway
 */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI apiGatewayOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("API Gateway")
                        .description("API Gateway reactivo para enrutamiento y autenticación de microservicios")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Ubik Team")
                                .email("support@ubik.com")
                                .url("https://ubik.com"))
                        .license(new License()
                                .name("Apache 2.0")
                                .url("https://www.apache.org/licenses/LICENSE-2.0.html")))
                .servers(List.of(
                        new Server().url("http://localhost:8080").description("Gateway Server")))
                .addSecurityItem(new SecurityRequirement().addList("bearerAuth"))
                .components(new io.swagger.v3.oas.models.Components()
                        .addSecuritySchemes("bearerAuth",
                                new SecurityScheme()
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")
                                        .description("JWT token obtenido del endpoint /api/auth/login")));
    }
}
