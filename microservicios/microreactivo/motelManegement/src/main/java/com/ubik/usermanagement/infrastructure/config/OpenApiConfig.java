package com.ubik.usermanagement.infrastructure.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * Configuración de OpenAPI/Swagger para el microservicio de gestión de moteles
 */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI motelManagementOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Motel Management API")
                        .description("API reactiva para gestión de moteles, habitaciones, reservaciones y servicios")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Ubik Team")
                                .email("support@ubik.com")
                                .url("https://ubik.com"))
                        .license(new License()
                                .name("Apache 2.0")
                                .url("https://www.apache.org/licenses/LICENSE-2.0.html")))
                .servers(List.of(
                        new Server().url("http://localhost:8084").description("Development Server"),
                        new Server().url("http://localhost:8080/api").description("Gateway Server")));
    }
}
