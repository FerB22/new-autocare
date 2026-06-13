package com.autocare.garage_service.config;

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
                        .title("AutoCare — Garage Service")
                        .version("1.0")
                        .description("API RESTful para la gestión del registro de clientes, perfiles de usuario y hojas de vida de los vehículos asociados."));
    }
}