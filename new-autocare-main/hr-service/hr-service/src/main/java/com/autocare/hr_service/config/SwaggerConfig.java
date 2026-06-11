package com.autocare.hr_service.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("AutoCare — Módulo de Recursos Humanos")
                        .version("1.0")
                        .description("API para la gestión del personal, registro de mecánicos, asignaciones y disponibilidad dentro del taller."));
    }
}