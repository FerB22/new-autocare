package com.autocare.workshop_service.client;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.Map;

@Component
@RequiredArgsConstructor
public class InventoryClient {

    private final WebClient.Builder webClientBuilder;

    public void descontarRepuesto(Long repuestoId, Integer cantidad) {
        Map<String, Object> requestBody = Map.of(
                "repuestoId", repuestoId,
                "cantidadUtilizada", cantidad
        );

        webClientBuilder.build().post()
                .uri("http://inventory-service/api/inventario/reducir-stock")
                .body(Mono.just(requestBody), Map.class)
                .retrieve()
                .bodyToMono(String.class)
                .block(); // Usamos block() para mantenerlo síncrono en esta etapa
    }
}