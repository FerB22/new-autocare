package com.autocare.procurement_service.config;

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
                        .title("AutoCare — Procurement Service")
                        .version("1.0")
                        .description("API RESTful para la gestión operativa de órdenes de compra y control de abastecimiento con proveedores externos."));
    }
}