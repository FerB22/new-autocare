package com.autocare.booking_service.config;

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
                        .title("AutoCare — Módulo de Agendamiento (Booking Service)")
                        .version("1.0")
                        .description("API RESTful para la gestión del calendario de citas, " +
                                     "validación de ventanas horarias y transiciones de estado de reservas."));
    }
}