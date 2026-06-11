package com.autocare.inventory_service.config;

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
                        .title("AutoCare — Módulo de Inventario")
                        .version("1.0")
                        .description("API dedicada a la gestión del catálogo de repuestos, control de stock y reducciones de inventario."));
    }
}