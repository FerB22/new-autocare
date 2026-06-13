package com.autocare.workshop_service.config;

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
                        .title("AutoCare — Workshop Service")
                        .version("1.0")
                        .description("API RESTful para el seguimiento en tiempo real de órdenes de trabajo en el taller mecánico, asignación de bahías y uso de insumos."));
    }
}