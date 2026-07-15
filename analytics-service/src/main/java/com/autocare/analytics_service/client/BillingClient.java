package com.autocare.analytics_service.client;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Slf4j
@Component
@RequiredArgsConstructor
public class BillingClient {


    public BigDecimal obtenerIngresosDelMes(Integer mes, Integer anio) {
        log.info("Consultando ingresos al billing-service para {}/{}", mes, anio);
        try {
            // Simulamos la llamada
            return new BigDecimal("15400.50"); // Valor de prueba
        } catch (Exception e) {
            log.error("Fallo al comunicarse con billing-service: {}", e.getMessage());
            return BigDecimal.ZERO;
        }
    }
}