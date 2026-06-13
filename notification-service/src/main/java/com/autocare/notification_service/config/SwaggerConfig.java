package com.autocare.notification_service.config;

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
                        .title("AutoCare — Notification Service")
                        .version("1.0")
                        .description("API RESTful para el envío de alertas automatizadas, correos de confirmación y mensajería del estado de reparaciones."));
    }
}