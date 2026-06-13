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
                        .title("AutoCare — Analytics Service")
                        .version("1.0")
                        .description("API RESTful para la generación de reportes mensuales, métricas operativas y análisis predictivo del rendimiento del taller."));
    }
}