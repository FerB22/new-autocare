package com.autocare.diagnostics_service.config;

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
                        .title("AutoCare — Módulo de Diagnóstico")
                        .version("1.0")
                        .description("API encargada de registrar, evaluar y consultar los reportes de diagnóstico técnico de los vehículos."));
    }
}