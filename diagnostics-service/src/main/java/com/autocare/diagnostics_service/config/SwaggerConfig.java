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
                        .title("AutoCare — Diagnostics Service")
                        .version("1.0")
                        .description("API RESTful para el registro de códigos de falla OBD-II e informes técnicos de inspección vehicular inicial."));
    }
}