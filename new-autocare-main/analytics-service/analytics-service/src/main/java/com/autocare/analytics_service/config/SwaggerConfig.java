package com.autocare.analytics_service.config;

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
                        .title("AutoCare — Módulo de Analítica y Reportes")
                        .version("1.0")
                        .description("API para la generación de reportes mensuales y análisis de datos del desempeño del taller."));
    }
}