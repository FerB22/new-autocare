package com.autocare.booking_service.client;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

@Slf4j
@Component
@RequiredArgsConstructor
public class GarageClient {

    private final WebClient.Builder webClientBuilder;

    public boolean existeCliente(Long clienteId) {
        log.info("Verificando existencia del cliente {} en garage-service", clienteId);
        try {
            webClientBuilder.build().get()
                    .uri("http://garage-service/api/garage/clientes/{id}", clienteId)
                    .retrieve()
                    .toBodilessEntity()
                    .block();
            return true;
        } catch (WebClientResponseException.NotFound e) {
            log.warn("El cliente {} no existe en garage-service", clienteId);
            return false;
        } catch (Exception e) {
            log.error("Error al comunicarse con garage-service para verificar cliente {}: {}", clienteId, e.getMessage());
            throw e;
        }
    }

    public boolean existeVehiculo(Long vehiculoId) {
        log.info("Verificando existencia del vehículo {} en garage-service", vehiculoId);
        try {
            webClientBuilder.build().get()
                    .uri("http://garage-service/api/garage/vehiculos/{id}", vehiculoId)
                    .retrieve()
                    .toBodilessEntity()
                    .block();
            return true;
        } catch (WebClientResponseException.NotFound e) {
            log.warn("El vehículo {} no existe en garage-service", vehiculoId);
            return false;
        } catch (Exception e) {
            log.error("Error al comunicarse con garage-service para verificar vehículo {}: {}", vehiculoId, e.getMessage());
            throw e;
        }
    }
}
