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
                        .title("AutoCare — Módulo de Notificaciones")
                        .version("1.0")
                        .description("API encargada del envío centralizado de alertas, mensajes y notificaciones de estado a los clientes."));
    }
}