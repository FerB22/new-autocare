package com.autocare.garage_service.client;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class LoyaltyClient {

    private final WebClient.Builder webClientBuilder;

    public void inicializarPerfilLealtad(Long clienteId) {
        log.info("Enviando señal para inicializar cuenta de lealtad al cliente: {}", clienteId);
        
        Map<String, Object> requestBody = Map.of("clienteId", clienteId);

        try {
            webClientBuilder.build().post()
                    .uri("http://loyalty-service/api/lealtad/cliente")
                    .body(Mono.just(requestBody), Map.class)
                    .retrieve()
                    .toBodilessEntity()
                    .block(); // Mantenemos síncrono para asegurar el flujo en esta etapa
            
            log.info("Perfil de lealtad creado exitosamente en el módulo externo.");
        } catch (Exception e) {
            // Tolerancia a fallos contida: No rompemos la transacción del Garaje
            log.warn("No se pudo conectar con loyalty-service. El cliente fue registrado, pero su cuenta de lealtad deberá sincronizarse luego. Motivo: {}", e.getMessage());
        }
    }
}